package ru.mts2200.minincode.bot.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mts2200.minincode.launch.Starter;

/**
 * @author mts2200
 */
@Slf4j(topic = "ThreadExceptionHandler")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LogExceptionHandler implements Thread.UncaughtExceptionHandler {
	public static final LogExceptionHandler INSTANCE = new LogExceptionHandler();

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error(e.getLocalizedMessage(), e);
		Starter.getSession().stop();
	}
}
