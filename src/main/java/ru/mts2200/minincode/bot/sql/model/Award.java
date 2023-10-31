package ru.mts2200.minincode.bot.sql.model;

import lombok.Builder;
import lombok.Getter;
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
@Slf4j(topic = "Award")
public final class Award {
	private final int awardId;
	private final Student student;
	private final Document document;

	public static AwardBuilder readFromSQL(ResultSet resultSet) {
		var builder = builder();

		try {
			builder.awardId = resultSet.getInt("award_id");
			builder.student = Student.readFromSQL(resultSet).build();
			builder.document = Document.readFromSQL(resultSet).build();
		} catch (SQLException exception) {
			log.error(Errors.MODEL_READING, exception);
		}

		return builder;
	}
}
