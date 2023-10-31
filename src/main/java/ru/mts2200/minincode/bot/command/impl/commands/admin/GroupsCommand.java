package ru.mts2200.minincode.bot.command.impl.commands.admin;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j(topic = "GroupsCommand")
public final class GroupsCommand extends Command {
	private static final String LIST = "%d. %s";

	public GroupsCommand() {
		super("groups", "Список всех групп (Admin)");
	}

	@Override
	public boolean canUseCommand(User user) {
		return GlobalConfig.settings.getAdmins().contains(user.getId());
	}

	@Override
	public void execute(AbsSender bot, User user, Chat chat, List<String> arguments) {
		AsyncIO.GLOBAL.getExecutor().submit(() -> {
			var groups = TableManager.selectAllGroups();

			StringBuilder text = new StringBuilder("Полный список всех групп:");

			for (var group : groups) {
				text.append('\n');
				text.append(LIST.formatted(group.getGroupId(), group.getGroupName()));
			}

			try {
				bot.executeAsync(SendMessage.builder().chatId(chat.getId()).text(text.toString()).build());
			} catch (TelegramApiException e) {
				log.error(Errors.TELEGRAM_API, e);
				throw new RuntimeException(e);
			}
		});
	}
}
