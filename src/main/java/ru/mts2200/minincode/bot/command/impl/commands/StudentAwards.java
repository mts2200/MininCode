package ru.mts2200.minincode.bot.command.impl.commands;

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

import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * @author mts2200
 */
@Slf4j(topic = "StudentAwards")
public final class StudentAwards extends Command {
	private static final String REQUIRED_ARGUMENT = "Для использования этой команды необходимо указать приватный ключ доступа.";
	private static final String WRONG_FORMAT = "Введён неверный ключ. Пример: `00000000-0000-0000-0000-000000000000`";
	private static final String NOT_FOUND = "Студент с заданным ключом не найден.";
	private static final String AWARDS = "Награды:";
	private static final String AWARDS_EMPTY = "Список наград пуст.";
	private static final int UUID_LENGTH = 36;

	public StudentAwards() {
		super("awards", "Список наград студента по уникальному ключу.");
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
						var awards = TableManager.selectAwardsByStudent(student);

						if (!awards.isEmpty()) {
							try {
								bot.execute(message.text(AWARDS).build());

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
							message.text(AWARDS_EMPTY);
						}
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
