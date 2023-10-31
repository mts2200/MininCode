package ru.mts2200.minincode.bot.command.impl.commands.admin;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mts2200.minincode.bot.async.AsyncIO;
import ru.mts2200.minincode.bot.command.Command;
import ru.mts2200.minincode.bot.sql.manager.TableManager;
import ru.mts2200.minincode.bot.sql.model.Award;
import ru.mts2200.minincode.bot.util.Errors;
import ru.mts2200.minincode.bot.util.FormatHelper;
import ru.mts2200.minincode.launch.config.GlobalConfig;

import java.util.List;

/**
 * @author mts2200
 */
@Slf4j(topic = "AwardLink")
public final class AwardLink extends Command {
	private static final String NO_ARGUMENTS = "Необходимо указать идентификатор студента и документа! Пример использования: `/award_link 1 1`.";
	private static final String STUDENT_NOT_FOUND = "Студент с идентификатором %d не найден!";
	private static final String DOCUMENT_NOT_FOUND = "Документ с идентификатором %d не найден!";
	private static final String WRONG_FORMAT = "Неверный формат данных! Аргумент должен являться числом! Пример: `/award_link 1 1`.";
	private static final String LINK_SUCCESS = "Документ `%s` успешно закреплён за учеником `%s %s %s`!";

	public AwardLink() {
		super("award_link", "Закрепить документ за определённым студентом.");
	}

	@Override
	public boolean canUseCommand(User user) {
		return GlobalConfig.settings.getAdmins().contains(user.getId());
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		AsyncIO.GLOBAL.getExecutor().submit(() -> {
			var message = SendMessage.builder().parseMode(ParseMode.MARKDOWN).chatId(chat.getId());

			if (arguments.size() >= 2) {
				var studentStringId = arguments.get(0);
				var documentStringId = arguments.get(1);

				if (FormatHelper.NUMBER_PATTERN.matcher(studentStringId).matches() && FormatHelper.NUMBER_PATTERN.matcher(documentStringId).matches()) {
					var optionalStudent = TableManager.selectStudent(Integer.parseInt(studentStringId));

					if (optionalStudent.isPresent()) {
						var optionalDocument = TableManager.selectDocument(Integer.parseInt(documentStringId));

						if (optionalDocument.isPresent()) {
							var student = optionalStudent.get();
							var document = optionalDocument.get();

							TableManager.insertAward(Award.builder().student(student).document(document).build());
							message.text(LINK_SUCCESS.formatted(document.getDocumentName(), student.getLastName(), student.getFirstName(), student.getSurname()));
						} else {
							message.text(DOCUMENT_NOT_FOUND);
						}
					} else {
						message.text(STUDENT_NOT_FOUND);
					}
				} else {
					message.text(WRONG_FORMAT);
				}
			} else {
				message.text(NO_ARGUMENTS);
			}

			try {
				bot.execute(message.build());
			} catch (TelegramApiException exception) {
				log.error(Errors.TELEGRAM_API, exception);
				throw new RuntimeException(exception);
			}
		});
	}
}
