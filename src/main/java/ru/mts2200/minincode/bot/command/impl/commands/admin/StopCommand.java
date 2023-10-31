package ru.mts2200.minincode.bot.command.impl.commands.admin;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.mts2200.minincode.bot.command.Command;
import ru.mts2200.minincode.launch.Starter;
import ru.mts2200.minincode.launch.config.GlobalConfig;

import java.util.List;

/**
 * @author mts2200
 */
@Slf4j(topic = "Stopper")
public final class StopCommand extends Command {
	private static final boolean ENABLE = true;

	public StopCommand() {
		super("stop", "Останавливает работу бота (Admin)");
	}

	@Override
	public boolean canUseCommand(User user) {
		return ENABLE && GlobalConfig.settings.getAdmins().contains(user.getId());
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		log.info("Инициализировано выключение... Username: {}. UserID: {}. ChatId: {}", user.getUserName(), user.getId(), chat.getId());
		Starter.getSession().stop();
	}
}
