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
@Slf4j(topic = "Student")
public final class Student {
	private final int studentId;
	private final @NonNull String firstName;
	private final @NonNull String lastName;
	private final @NonNull String surname;
	private final int age;
	private final Group group;
	private final String accessToken;

	public static StudentBuilder readFromSQL(ResultSet resultSet) {
		var builder = builder();

		try {
			builder.studentId = resultSet.getInt("student_id");
			builder.firstName = resultSet.getString("first_name");
			builder.lastName = resultSet.getString("last_name");
			builder.surname = resultSet.getString("surname");
			builder.age = resultSet.getInt("age");
			builder.group = Group.readFromSQL(resultSet).build();
			builder.accessToken = resultSet.getString("access_token");
		} catch (SQLException exception) {
			log.error(Errors.MODEL_READING, exception);
		}

		return builder;
	}
}
