package ru.mts2200.minincode.bot.command;

import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

/**
 * @author mts2200
 * @see ru.mts2200.minincode.bot.command.impl.managers.SimpleCommandManager
 */
public abstract class CommandManager {
	/**
	 * Регистрация новой команды.
	 *
	 * @param command команда.
	 */
	public abstract void register(Command command);

	/**
	 * Исполнение команды.
	 *
	 * @param message сообщение
	 * @param entity  сущность с типом «bot_command»
	 */
	public abstract void execute(Message message, MessageEntity entity);

	/**
	 * Подготовка сообщения с зарегистрированными командами.
	 *
	 * @return билдер с зарегистрированными командами данного менеджера
	 */
	public abstract SetMyCommands.SetMyCommandsBuilder commandsMessage();
}
