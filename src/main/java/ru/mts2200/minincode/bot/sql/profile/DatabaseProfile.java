package ru.mts2200.minincode.bot.sql.profile;

import com.google.common.collect.Maps;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * @author mts2200
 */
@Getter
@Builder
@Jacksonized
@ToString
public final class DatabaseProfile {
	public static final DatabaseProfile DEFAULT = DatabaseProfile.builder()
			.poolName("HikariPool")
			.jdbcUrl("jdbc:mysql://localhost:3306/minincode?autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=UTC")
			.username("root")
			.password("")
			.settings(Maps.newHashMap())
			.build();

	private final @NonNull String poolName;
	private final @NonNull String jdbcUrl;
	private final @NonNull String username;
	private final @NonNull String password;
	private final @NonNull Map<String, String> settings;
}
