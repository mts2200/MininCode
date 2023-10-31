package ru.mts2200.minincode.bot.request.impl.requests;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mts2200.minincode.bot.async.AsyncIO;
import ru.mts2200.minincode.bot.request.Request;
import ru.mts2200.minincode.bot.sql.manager.TableManager;
import ru.mts2200.minincode.bot.sql.model.Group;
import ru.mts2200.minincode.bot.sql.model.Student;
import ru.mts2200.minincode.bot.util.Errors;
import ru.mts2200.minincode.bot.util.FormatHelper;

import java.util.concurrent.TimeUnit;

/**
 * @author mts2200
 */
@NoArgsConstructor
@Slf4j(topic = "Registration")
public final class RegistrationRequest extends Request {
	private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(10);

	private static final String START = "Это диалог регистрации. Для регистрации необходимо указать ваше ФИО, возраст и группу. После регистрации вы получите уникальный ключ, который позволит вам использовать остальной функционал данного бота. Никому не передавайте и не распространяйте ключ! Сгенерировать новый ключ может только Администратор IT-Cube.";

	private static final String NEED_FULL_NAME = "Необходимо указать ваше ФИО в формате `Фамилия Имя Отчество`. Пример: `Иванов Иван Иванович`.";
	private static final String WRONG_FULL_NAME_FORMAT = "Вы указали ФИО в неверном формате, попробуйте снова. Пример: `Иванов Иван Иванович`.";

	private static final String NEED_AGE = "Необходимо указать ваш возраст, в качестве ответа просто отправьте цифру. Пример: `14`.";
	private static final String WRONG_AGE_FORMAT = "Вы указали возраст в неверном формате, попробуйте снова. Пример: `14`.";

	private static final String NEED_GROUP = "Необходимо указать вашу группу. Аббревиатуру Вы можете уточнить у преподавателя. Пример: `АЛ-1`, `ПП-1`, `М-2`.";
	private static final String WRONG_GROUP_FORMAT = "Вы указали группу в неверном формате, попробуйте снова. Пример: `АЛ-1`, `ПП-1`, `М-2`.";
	private static final String GROUP_NOT_FOUND = "Указанная группа не найдена в базе данных! Перепроверьте правильность и соблюдайте регистр.";

	private static final String REGISTRATION_SUCSESS = "Вы успешно зарегистрировались! Для дальнейшего использования вам потребуется уникальный ключ: `%s`. Держите его в секрете, в случае утери, восстановить сможет только Администратор ItCube!";
	private static final String REGISTRATION_FAULT = "Произошла ошибка при регистрации! Обратитесь к Администрации!";

	private static final Splitter SPACE_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();

	private String firstName = null;
	private String lastName = null;
	private String surname = null;
	private Group.GroupBuilder group = null;
	private int age = -1;

	@Override
	public void onPendingNewRequest(AbsSender bot, User user, Chat chat) {
		var start = SendMessage.builder()
				.text(START)
				.chatId(chat.getId())
				.parseMode(ParseMode.MARKDOWN)
				.build();

		var needFullName = SendMessage.builder()
				.text(NEED_FULL_NAME)
				.chatId(chat.getId())
				.parseMode(ParseMode.MARKDOWN)
				.build();

		try {
			bot.executeAsync(start);
			bot.executeAsync(needFullName);
		} catch (TelegramApiException exception) {
			log.error(Errors.TELEGRAM_API, exception);
			throw new RuntimeException(exception);
		}
	}

	@Override
	public void process(AbsSender bot, Update update) {
		var message = update.getMessage();

		if (message.hasText()) {
			var text = message.getText().trim();

			try {
				var sendMessage = SendMessage.builder().chatId(message.getChatId()).parseMode(ParseMode.MARKDOWN);

				if (!isDoneFullName()) {
					if (FormatHelper.NAME_PATTERN.matcher(text).matches()) {
						var name = SPACE_SPLITTER.splitToList(text);
						lastName = name.get(0);
						firstName = name.get(1);
						surname = name.get(2);

						bot.executeAsync(sendMessage.text(NEED_AGE).build());
					} else {
						bot.executeAsync(sendMessage.text(WRONG_FULL_NAME_FORMAT).build());
					}
				} else if (!isDoneAge()) {
					if (FormatHelper.NUMBER_PATTERN.matcher(text).matches()) {
						age = Integer.parseInt(text);

						bot.executeAsync(sendMessage.text(NEED_GROUP).build());
					} else {
						bot.executeAsync(sendMessage.text(WRONG_AGE_FORMAT).build());
					}
				} else if (!isDoneGroup()) {
					if (FormatHelper.IT_CUBE_GROUP_PATTERN.matcher(text).matches()) {
						var itCubeGroup = TableManager.selectGroupByName(text);

						if (itCubeGroup.isPresent()) {
							group = Group.builder().groupName(itCubeGroup.get().getGroupName()).groupId(itCubeGroup.get().getGroupId());
						} else {
							bot.executeAsync(sendMessage.text(GROUP_NOT_FOUND).build());
						}
					} else {
						bot.executeAsync(sendMessage.text(WRONG_GROUP_FORMAT).build());
					}
				}
			} catch (TelegramApiException exception) {
				log.error(Errors.TELEGRAM_API, exception);
				throw new RuntimeException(exception);
			}
		}
	}

	@Override
	public void complete(AbsSender bot, User user, Chat chat) {
		AsyncIO.GLOBAL.getExecutor().submit(() -> {
			var student = Student.builder().firstName(firstName).lastName(lastName).surname(surname).age(age).group(group.build()).build();
			var token = TableManager.insertStudent(student);

			try {
				var message = SendMessage.builder().chatId(chat.getId());

				if (!Strings.isNullOrEmpty(token)) {
					bot.executeAsync(message.parseMode(ParseMode.MARKDOWN).text(String.format(REGISTRATION_SUCSESS, token)).build());
				} else {
					bot.executeAsync(message.text(REGISTRATION_FAULT).build());
				}
			} catch (TelegramApiException exception) {
				log.error(Errors.TELEGRAM_API, exception);
				throw new RuntimeException(exception);
			}
		});
	}

	@Override
	public boolean isDone() {
		return isDoneAge() && isDoneGroup() && isDoneFullName();
	}

	private boolean isDoneFullName() {
		return !Strings.isNullOrEmpty(firstName) && !Strings.isNullOrEmpty(lastName) && !Strings.isNullOrEmpty(surname);
	}

	private boolean isDoneGroup() {
		return group != null;
	}

	private boolean isDoneAge() {
		return age > 0;
	}

	@Override
	public long getTimeoutInMills() {
		return TIMEOUT;
	}
}
