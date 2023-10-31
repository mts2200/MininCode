package ru.mts2200.minincode.bot.sql.model;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import ru.mts2200.minincode.bot.util.Errors;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author mts2200
 */
@Getter
@Builder
@Jacksonized
@ToString
@Slf4j(topic = "Group")
public final class Group {
	private final int groupId;
	private final @NonNull String groupName;

	public static GroupBuilder readFromSQL(ResultSet resultSet) {
		var builder = builder();

		try {
			builder.groupId = resultSet.getInt("group_id");
			builder.groupName = resultSet.getString("group_name");
		} catch (SQLException exception) {
			log.error(Errors.MODEL_READING, exception);
		}

		return builder;
	}
}
