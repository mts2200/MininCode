package ru.mts2200.minincode.bot.request.impl.managers;

import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.mts2200.minincode.bot.async.AsyncIO;
import ru.mts2200.minincode.bot.request.Request;
import ru.mts2200.minincode.bot.request.RequestManager;

import java.util.Map;

/**
 * @author mts2200
 */
@RequiredArgsConstructor
public class SimpleRequestManager extends RequestManager {
	private final AbsSender bot;
	private final Map<Long, Request> requests = Maps.newConcurrentMap();

	@Override
	public boolean isHaveRequest(User user) {
		return requests.get(user.getId()) != null;
	}

	@Override
	public boolean process(Update update) {
		if (requests.isEmpty()) return false;

		if (update.hasMessage()) {
			var message = update.getMessage();
			var user = message.getFrom();

			var request = requests.get(user.getId());

			if (request != null) {
				request.process(bot, update);
				if (request.isDone()) {
					request.complete(bot, user, message.getChat());
					requests.remove(user.getId());
				}
				request.setLastTouch(System.currentTimeMillis());
			}
		}

		removeOldEntries();
		return true;
	}

	@Override
	public void request(User user, Chat chat, Request request) {
		request.onPendingNewRequest(bot, user, chat);
		requests.put(user.getId(), request);
	}

	/**
	 * Удаляет все старые запросы из ожидания.
	 */
	private void removeOldEntries() {
		AsyncIO.GLOBAL.getExecutor().submit(() -> {
			var iterator = requests.entrySet().iterator();

			while (iterator.hasNext()) {
				var request = iterator.next().getValue();
				var timeDifference = System.currentTimeMillis() - request.getLastTouch();

				if (timeDifference > request.getTimeoutInMills()) {
					iterator.remove();
				}
			}
		});
	}
}
