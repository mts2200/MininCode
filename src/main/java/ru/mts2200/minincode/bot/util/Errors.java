package ru.mts2200.minincode.bot.util;

import lombok.experimental.UtilityClass;

/**
 * @author mts2200
 */
@UtilityClass
public final class Errors {
	public static final String NO_TOKEN = "Необходимо указать Telegram Token для MininBot!";
	public static final String NO_USERNAME = "Необходимо указать Telegram Username для MininBot!";

	// Telegram
	public static final String TELEGRAM_API = "Ошибка взаимодействия с TelegramAPI!";

	// SQL
	public static final String CONNECTION_CREATION = "Произошла ошибка при создании нового подключения!";
	public static final String STATEMENT_EXECUTE = "Произошла ошибка при исполнении запроса к SQL!";
	public static final String MODEL_READING = "Произошла ошибка при чтении значений из SQL!";

	// Config
	public static final String CONFIG_LOAD = "Произошла ошибка при загрузке конфигурации!";
	public static final String CONFIG_SAVE = "Произошла ошибка при сохранении конфигурации!";
}
