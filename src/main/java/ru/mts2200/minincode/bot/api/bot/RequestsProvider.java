package ru.mts2200.minincode.bot.api.bot;

import ru.mts2200.minincode.bot.request.RequestManager;

/**
 * @author mts2200
 */
public interface RequestsProvider {
	RequestManager getRequestManager();
}
