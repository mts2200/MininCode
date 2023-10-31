package ru.mts2200.minincode.bot.sql.manager;

import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mts2200.minincode.bot.sql.profile.DatabaseProfile;

import java.util.Map;

/**
 * @author mts2200
 */
@Slf4j(topic = "SQLManager")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DatabaseManager {
	public static final String MAIN_DB = "main";
	private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
	public static final DatabaseManager INSTANCE = new DatabaseManager();

	private final Map<String, HikariDataSource> connections = Maps.newConcurrentMap();

	/**
	 * Создаёт подключение к базе данных.
	 *
	 * @param name    уникальный ключ базы данных
	 * @param profile профиль настройки подключения
	 */
	public void createConnection(String name, DatabaseProfile profile) {
		HikariConfig config = new HikariConfig();
		config.setPoolName(profile.getPoolName());
		config.setUsername(profile.getUsername());
		config.setPassword(profile.getPassword());
		config.setJdbcUrl(profile.getJdbcUrl());
		config.setDriverClassName(DRIVER);

		for (Map.Entry<String, String> setting : profile.getSettings().entrySet()) {
			config.addDataSourceProperty(setting.getKey(), setting.getKey());
		}

		connections.put(name, new HikariDataSource(config));
	}

	/**
	 * @param name уникальный ключ базы данных
	 * @return источник данных
	 */
	public HikariDataSource connection(String name) {
		return connections.get(name);
	}

	/**
	 * Останавливает все подключения к базам данных из {@link DatabaseManager#connections}
	 */
	public void shutdown() {
		connections.values().forEach(HikariDataSource::close);
	}
}
