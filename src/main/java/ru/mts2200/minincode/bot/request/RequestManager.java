package ru.mts2200.minincode.bot.request;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import ru.mts2200.minincode.bot.request.impl.managers.SimpleRequestManager;

/**
 * @author mts2200
 * @see SimpleRequestManager
 */
public abstract class RequestManager {
	/**
	 * Проверяет наличие какого-либо запроса у пользователя.
	 *
	 * @param user пользователь Telegram.
	 * @return истина, если запрос имеется.
	 */
	public abstract boolean isHaveRequest(User user);

	/**
	 * Обрабатывает обновление.
	 *
	 * @param update обновление.
	 * @return истина, если менеджер обработал это обновление.
	 */
	public abstract boolean process(Update update);

	/**
	 * Создаёт запрос для пользователя.
	 *
	 * @param user    пользователь Telegram.
	 * @param chat    чат с пользователем Telegram.
	 * @param request запрос.
	 */
	public abstract void request(User user, Chat chat, Request request);
}
