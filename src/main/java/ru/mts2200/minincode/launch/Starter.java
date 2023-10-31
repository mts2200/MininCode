package ru.mts2200.minincode.launch;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import ru.mts2200.minincode.bot.MininCodeBot;
import ru.mts2200.minincode.bot.util.Errors;
import ru.mts2200.minincode.launch.config.GlobalConfig;
import ru.mts2200.minincode.launch.dao.GlobalSettings;

/**
 * Входная точка запуска {@link ru.mts2200.minincode.bot.MininCodeBot}
 *
 * @author mts2200
 */
@Slf4j(topic = "Starter")
public final class Starter {
	@Getter
	private static BotSession session;

	/**
	 * Точка смерти.
	 *
	 * @param args аргументы программы.
	 */
	public static void main(String[] args) {
		GlobalConfig.load();

		GlobalSettings settings = GlobalConfig.settings;

		Preconditions.checkArgument(!Strings.isNullOrEmpty(settings.getToken()), Errors.NO_TOKEN);
		Preconditions.checkArgument(!Strings.isNullOrEmpty(settings.getUsername()), Errors.NO_USERNAME);

		TelegramBotsApi api = createApiOrNull();
		if (api == null) {
			throw new IllegalArgumentException(Errors.TELEGRAM_API);
		}

		try {
			session = api.registerBot(configureBot(settings));
		} catch (TelegramApiException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Создаёт инстанс {@link MininCodeBot}
	 *
	 * @param settings настройки
	 * @return бот
	 */
	private static MininCodeBot configureBot(GlobalSettings settings) {
		var options = new DefaultBotOptions();
		options.setMaxThreads(settings.getMaxThreads());
		return new MininCodeBot(options, settings.getToken(), settings.getUsername());
	}

	/**
	 * @return API ботов Telegram.
	 */
	private static TelegramBotsApi createApiOrNull() {
		try {
			return new TelegramBotsApi(DefaultBotSession.class);
		} catch (TelegramApiException exception) {
			log.error(Errors.TELEGRAM_API, exception);
			return null;
		}
	}
}