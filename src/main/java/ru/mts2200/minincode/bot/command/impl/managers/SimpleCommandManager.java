package ru.mts2200.minincode.bot.command.impl.managers;

import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mts2200.minincode.bot.command.Command;
import ru.mts2200.minincode.bot.command.CommandManager;

import java.util.Map;

/**
 * @author mts2200
 */
@RequiredArgsConstructor
public class SimpleCommandManager extends CommandManager {
	private static final Splitter SPACE_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();
	private static final String NO_PERMISSIONS = "Данная команда вам недоступна!";

	private final AbsSender bot;
	private final Map<String, Command> commands = Maps.newConcurrentMap();

	@Override
	public void register(Command command) {
		commands.put('/' + command.getCommandName(), command);
	}

	@Override
	public void execute(Message message, MessageEntity entity) {
		if (entity.getType().equals("bot_command")) {
			var command = commands.get(entity.getText());
			if (command == null) return;

			if (command.canUseCommand(message.getFrom())) {
				var textWithoutCommand = message.getText().substring(entity.getOffset() + entity.getLength());
				command.execute(bot, message.getFrom(), message.getChat(), SPACE_SPLITTER.splitToList(textWithoutCommand));
			} else {
				try {
					bot.executeAsync(SendMessage.builder().text(NO_PERMISSIONS).chatId(message.getChatId()).build());
				} catch (TelegramApiException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	@Override
	public SetMyCommands.SetMyCommandsBuilder commandsMessage() {
		var builder = SetMyCommands.builder();

		for (Command command : commands.values()) {
			builder.command(new BotCommand(command.getCommandName(), command.getDescription()));
		}

		return builder;
	}
}
