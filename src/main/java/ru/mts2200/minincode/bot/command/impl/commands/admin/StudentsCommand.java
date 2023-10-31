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
import ru.mts2200.minincode.bot.util.Errors;
import ru.mts2200.minincode.launch.config.GlobalConfig;

import java.util.List;

/**
 * @author mts2200
 */
@Slf4j(topic = "StudentsCommand")
public final class StudentsCommand extends Command {
	private static final String STUDENTS_LIST = "Полный список студентов:";
	private static final String STUDENT_INFO = "%d. `%s %s %s`. `%d лет`. Группа: `%s`.";
	private static final String STUDENTS_EMPTY = "Список студентов пуст.";

	public StudentsCommand() {
		super("students", "Полный список пользователей (Admin)");
	}

	@Override
	public boolean canUseCommand(User user) {
		return GlobalConfig.settings.getAdmins().contains(user.getId());
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		AsyncIO.GLOBAL.getExecutor().submit(() -> {
			var students = TableManager.selectAllStudents();

			var message = SendMessage.builder().chatId(chat.getId());

			if (!students.isEmpty()) {
				StringBuilder text = new StringBuilder(STUDENTS_LIST);

				for (var student : students) {
					text.append('\n');
					text.append(STUDENT_INFO.formatted(student.getStudentId(), student.getLastName(), student.getFirstName(), student.getSurname(), student.getAge(), student.getGroup().getGroupName()));
				}

				message.text(text.toString()).parseMode(ParseMode.MARKDOWN);
			} else {
				message.text(STUDENTS_EMPTY);
			}

			try {
				bot.executeAsync(message.build());
			} catch (TelegramApiException e) {
				log.error(Errors.TELEGRAM_API, e);
				throw new RuntimeException(e);
			}
		});
	}
}
