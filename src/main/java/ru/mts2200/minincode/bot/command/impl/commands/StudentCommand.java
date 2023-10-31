package ru.mts2200.minincode.bot.command.impl.commands;

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

import java.util.List;

/**
 * @author mts2200
 */
@Slf4j(topic = "StudentCommand")
public final class StudentCommand extends Command {
	private static final String REQUIRED_ARGUMENT = "Для использования этой команды необходимо указать приватный ключ доступа.";
	private static final String WRONG_FORMAT = "Введён неверный ключ. Пример: `00000000-0000-0000-0000-000000000000`";
	private static final String NOT_FOUND = "Студент с заданным ключом не найден.";
	private static final String INFO = "ФИО: `%s %s %s`.\nВозраст: `%d лет`.\nГруппа: `%s`.\nИдентификатор: `%d`";

	private static final int UUID_LENGTH = 36;

	public StudentCommand() {
		super("student", "Отображает информацию о студенте по специальному ключу.");
	}

	@Override
	public boolean canUseCommand(User user) {
		return true;
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		AsyncIO.GLOBAL.getExecutor().submit(() -> {
			var message = SendMessage.builder().chatId(chat.getId());

			if (!arguments.isEmpty()) {
				var uuid = arguments.get(0);
				if (uuid.length() == UUID_LENGTH) {
					var optionalStudent = TableManager.selectStudentByToken(uuid);

					if (optionalStudent.isPresent()) {
						var student = optionalStudent.get();
						message.text(INFO.formatted(student.getLastName(), student.getFirstName(), student.getSurname(), student.getAge(), student.getGroup().getGroupName(), student.getStudentId())).parseMode(ParseMode.MARKDOWN);
					} else {
						message.text(NOT_FOUND);
					}
				} else {
					message.text(WRONG_FORMAT).parseMode(ParseMode.MARKDOWN);
				}
			} else {
				message.text(REQUIRED_ARGUMENT);
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
