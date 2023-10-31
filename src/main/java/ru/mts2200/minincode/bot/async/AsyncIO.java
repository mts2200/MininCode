package ru.mts2200.minincode.bot.async;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mts2200.minincode.bot.util.LogExceptionHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author mts2200
 */
@Getter
@Slf4j(topic = "MininIO")
@NoArgsConstructor()
public final class AsyncIO {
	public static final AsyncIO GLOBAL = new AsyncIO();

	private final ExecutorService executor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setUncaughtExceptionHandler(LogExceptionHandler.INSTANCE).setNameFormat("MininIO #%d").setDaemon(true).build());

	/**
	 * Завершает задачи {@link AsyncIO#executor}
	 */
	public void shutdown() {
		try {
			executor.shutdown();

			if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
				log.error("Задачи AsyncIO не были завершены!");
			}
		} catch (InterruptedException exception) {
			// NO-OP
		}
	}
}
