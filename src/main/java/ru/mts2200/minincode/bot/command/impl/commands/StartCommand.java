package ru.mts2200.minincode.bot.command.impl.commands;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mts2200.minincode.bot.command.Command;
import ru.mts2200.minincode.bot.util.Errors;

import java.util.List;

/**
 * @author mts2200
 */
@Slf4j(topic = "StartCommand")
public class StartCommand extends Command {
	private static final String HELLO = "Приветствую, @%s! Данный бот создан для хранения всех сертификатов, дипломов и грамот обучающихся ЦЦОД «IT-Куб», чтобы все ваши достижения были легко доступны в любой необходимый вам момент времени.";

	public StartCommand() {
		super("start", "Hello World!");
	}

	@Override
	public boolean canUseCommand(User user) {
		return true;
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		var message = SendMessage.builder().chatId(chat.getId()).text(HELLO.formatted(user.getUserName()));

		try {
			bot.executeAsync(message.build());
		} catch (TelegramApiException exception) {
			log.error(Errors.TELEGRAM_API, exception);
			throw new RuntimeException(exception);
		}
	}
}
