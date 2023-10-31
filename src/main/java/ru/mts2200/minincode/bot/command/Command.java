package ru.mts2200.minincode.bot.command;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.List;

/**
 * @author mts2200
 */
@Getter
@RequiredArgsConstructor
public abstract class Command {
	/**
	 * Название команды.
	 */
	private final String commandName;

	/**
	 * Описание команды.
	 */
	private final String description;

	/**
	 * Определяет, может ли этот пользователь использовать эту команду.
	 *
	 * @param user пользователь Telegram.
	 * @return истина, если пользовать может использовать команду.
	 */
	public abstract boolean canUseCommand(User user);

	/**
	 * Исполнение команды.
	 *
	 * @param bot       бот.
	 * @param user      пользователь Telegram.
	 * @param chat      чат с пользователем Telegram.
	 * @param arguments аргументы.
	 */
	public abstract void execute(AbsSender bot, User user, Chat chat, List<String> arguments);
}
