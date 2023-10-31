package ru.mts2200.minincode.bot.request;

import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * @author mts2200
 */
@Getter
@Setter
public abstract class Request {
	private long lastTouch = -1;

	/**
	 * Выполняется при добавлении этого запроса в очередь.
	 *
	 * @param user идентификатор пользователя Telegram
	 */
	public abstract void onPendingNewRequest(AbsSender bot, User user, Chat chat);

	/**
	 * Обрабатывает пакет обновления от User. Метод выполняется каждое обновление до тех пор, пока {@link Request#isDone()} возвращает false.
	 *
	 * @param update пакет обновления.
	 */
	public abstract void process(AbsSender bot, Update update);

	/**
	 * Проверяет заполненность данного запроса.
	 *
	 * @return возвращает true, если запрос заполнен и его нужно освободить.
	 */
	public abstract boolean isDone();

	/**
	 * Завершает выполнение текущего запроса. Выполняется после возвращения истины {@link Request#isDone()}
	 */
	public abstract void complete(AbsSender bot, User user, Chat chat);

	/**
	 * Используется для проверки «свежести» данного запроса.
	 *
	 * @return максимальное время ожидания следующего Update
	 */
	public abstract long getTimeoutInMills();
}
