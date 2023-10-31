package ru.mts2200.minincode.bot;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mts2200.minincode.bot.api.bot.RequestsProvider;
import ru.mts2200.minincode.bot.async.AsyncIO;
import ru.mts2200.minincode.bot.command.CommandManager;
import ru.mts2200.minincode.bot.command.impl.commands.*;
import ru.mts2200.minincode.bot.command.impl.commands.admin.*;
import ru.mts2200.minincode.bot.command.impl.managers.SimpleCommandManager;
import ru.mts2200.minincode.bot.request.RequestManager;
import ru.mts2200.minincode.bot.request.impl.managers.SimpleRequestManager;
import ru.mts2200.minincode.bot.sql.manager.DatabaseManager;
import ru.mts2200.minincode.bot.sql.manager.TableManager;
import ru.mts2200.minincode.bot.util.DocumentHandler;
import ru.mts2200.minincode.launch.config.GlobalConfig;

/**
 * @author mts2200
 */
@Slf4j(topic = "MininCodeBot")
public final class MininCodeBot extends TelegramLongPollingBot implements RequestsProvider {
	private final String username;
	private final RequestManager requestManager;
	private final CommandManager commandManager;

	public MininCodeBot(DefaultBotOptions options, String token, String username) {
		super(options, token);
		this.username = username;
		this.requestManager = new SimpleRequestManager(this);
		this.commandManager = new SimpleCommandManager(this);
	}

	@Override
	public RequestManager getRequestManager() {
		return requestManager;
	}

	@Override
	public void onRegister() {
		super.onRegister();
		registerDatabase();
		registerCommands();
		log.info("Бот @{} успешно инициализирован!", getBotUsername());
	}

	private void registerDatabase() {
		DatabaseManager.INSTANCE.createConnection(DatabaseManager.MAIN_DB, GlobalConfig.settings.getSql());
		TableManager.createTables();
	}

	private void registerCommands() {
		commandManager.register(new RegisterCommand());
		commandManager.register(new StudentsCommand());
		commandManager.register(new StopCommand());
		commandManager.register(new MeCommand());
		commandManager.register(new StudentCommand());
		commandManager.register(new CreateGroupCommand());
		commandManager.register(new GroupsCommand());
		commandManager.register(new StudentAwards());
		commandManager.register(new AwardLink());
		commandManager.register(new AdminStudentAwards());
		commandManager.register(new StartCommand());

		try {
			execute(commandManager.commandsMessage().build());
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onUpdateReceived(Update update) {
		var message = update.getMessage();

		if (message.hasEntities()) {
			for (var entity : message.getEntities()) {
				if ("bot_command".equals(entity.getType())) {
					commandManager.execute(message, entity);
				}
			}
		} else if (!requestManager.process(update)) {
			if (message.hasDocument() && GlobalConfig.settings.getAdmins().contains(message.getFrom().getId())) {
				AsyncIO.GLOBAL.getExecutor().submit(() -> DocumentHandler.processDocument(this, message.getChat(), message.getDocument()));
			}
		}
	}

	@Override
	public String getBotUsername() {
		return username;
	}

	@Override
	public void onClosing() {
		GlobalConfig.asyncSave();
		AsyncIO.GLOBAL.getExecutor().submit(DatabaseManager.INSTANCE::shutdown);
		AsyncIO.GLOBAL.shutdown();
		super.onClosing();
	}
}
