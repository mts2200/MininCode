package ru.mts2200.minincode.bot.command.impl.commands;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.mts2200.minincode.bot.api.bot.RequestsProvider;
import ru.mts2200.minincode.bot.command.Command;
import ru.mts2200.minincode.bot.request.impl.requests.RegistrationRequest;

import java.util.List;

/**
 * @author mts2200
 */
public final class RegisterCommand extends Command {
	public RegisterCommand() {
		super("register", "Регистрация в системе ItCube");
	}

	@Override
	public boolean canUseCommand(User user) {
		return true;
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		if (bot instanceof RequestsProvider provider) {
			provider.getRequestManager().request(user, chat, new RegistrationRequest());
		}
	}
}
