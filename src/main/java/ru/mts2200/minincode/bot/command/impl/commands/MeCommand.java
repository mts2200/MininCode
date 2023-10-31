package ru.mts2200.minincode.bot.command.impl.commands;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
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
@Slf4j(topic = "MeCommand")
public final class MeCommand extends Command {
	public MeCommand() {
		super("me", "Отображает информацию о вашем профиле Telegram.");
	}

	@Override
	public boolean canUseCommand(User user) {
		return true;
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		String info = "Информация о вашем профиле Telegram:" + '\n' +
				"Имя: `" + user.getFirstName() + '`' +
				'\n' +
				"Фамилия: `" + user.getLastName() + '`' +
				'\n' +
				"Username: `" + user.getUserName() + '`' +
				'\n' +
				"Индитенфикатор: `" + user.getId() + '`' +
				'\n' +
				"Язык: `" + user.getLanguageCode() + '`' +
				'\n' +
				"Premium: `" + (user.getIsPremium() != null ? "Да" : "Нет") + '`';

		var message = SendMessage.builder().chatId(chat.getId()).text(info).parseMode(ParseMode.MARKDOWN);
		try {
			bot.executeAsync(message.build());
		} catch (TelegramApiException exception) {
			log.error(Errors.TELEGRAM_API, exception);
			throw new RuntimeException(exception);
		}
	}
}
