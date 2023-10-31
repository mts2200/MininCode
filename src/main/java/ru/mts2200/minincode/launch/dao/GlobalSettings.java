package ru.mts2200.minincode.launch.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import ru.mts2200.minincode.bot.sql.profile.DatabaseProfile;

import java.util.Set;

/**
 * @author mts2200
 */
@Data
@Builder
@Jacksonized
public class GlobalSettings {
	@Builder.Default()
	@JsonPropertyDescription("Токен текущего бота Telegram.")
	private final String token = "";

	@Builder.Default
	@JsonPropertyDescription("Username текущего бота Telegram.")
	private final String username = "";

	@Builder.Default
	@JsonPropertyDescription("Максимальное количество потоков, используемых для выполнения асинхронных методов.")
	private final int maxThreads = 1;

	@Builder.Default
	@JsonPropertyDescription("Настройки подключения к базе данных MySQL.")
	private final DatabaseProfile sql = DatabaseProfile.DEFAULT;

	@Builder.Default
	@JsonPropertyDescription("Список ID Администраторов ItCube.")
	private final Set<Long> admins = Sets.newHashSet();

	@JsonIgnore
	@Builder.Default
	private boolean isInitialized = false;
}
