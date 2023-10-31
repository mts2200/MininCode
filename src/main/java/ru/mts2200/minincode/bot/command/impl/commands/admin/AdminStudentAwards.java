package ru.mts2200.minincode.bot.command.impl.commands.admin;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mts2200.minincode.bot.async.AsyncIO;
import ru.mts2200.minincode.bot.command.Command;
import ru.mts2200.minincode.bot.sql.manager.TableManager;
import ru.mts2200.minincode.bot.util.Errors;
import ru.mts2200.minincode.launch.config.GlobalConfig;

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * @author mts2200
 */
@Slf4j(topic = "AdminStudentAwards")
public class AdminStudentAwards extends Command {
	private static final String NO_LAST_NAME = "Необходимо указать фамилию ученика! Пример `/a_awards Иванов`";
	private static final String NOT_FOUND = "Учеников с заданной фамилией не найдено!";
	private static final String NO_AWARDS = "Список наград пуст.";
	private static final String STUDENT_INFO = "ID: `%d` ФИО: `%s %s %s` Группа: `%s`";

	public AdminStudentAwards() {
		super("a_awards", "Список наград ученика по его фамилии. (Admin)");
	}

	@Override
	public boolean canUseCommand(User user) {
		return GlobalConfig.settings.getAdmins().contains(user.getId());
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		AsyncIO.GLOBAL.getExecutor().submit(() -> {
			var message = SendMessage.builder().chatId(chat.getId());

			if (!arguments.isEmpty()) {
				var lastName = arguments.get(0);

				if (!Strings.isNullOrEmpty(lastName)) {
					var students = TableManager.selectStudentsByLastName(lastName);

					if (!students.isEmpty()) {
						for (var student : students) {
							var awards = TableManager.selectAwardsByStudent(student);

							if (!awards.isEmpty()) {
								try {
									bot.execute(message.parseMode(ParseMode.MARKDOWN).text(STUDENT_INFO.formatted(student.getStudentId(), student.getLastName(), student.getFirstName(), student.getSurname(), student.getGroup().getGroupName())).build());

									for (var award : awards) {
										var awardDocument = new InputFile(new ByteArrayInputStream(award.getDocument().getDocumentBinary()), award.getDocument().getDocumentName());
										var awardMessage = SendDocument.builder().chatId(chat.getId()).document(awardDocument);
										bot.execute(awardMessage.build());
									}
								} catch (TelegramApiException exception) {
									log.error(Errors.TELEGRAM_API, exception);
									throw new RuntimeException(exception);
								}
								return;
							} else {
								message.text(NO_AWARDS);
							}
						}
					} else {
						message.text(NOT_FOUND);
					}
				} else {
					message.text(NO_LAST_NAME).parseMode(ParseMode.MARKDOWN);
				}
			} else {
				message.text(NO_LAST_NAME).parseMode(ParseMode.MARKDOWN);
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
