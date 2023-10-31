package ru.mts2200.minincode.bot.sql.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import ru.mts2200.minincode.bot.util.Compressor;
import ru.mts2200.minincode.bot.util.Errors;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.zip.DataFormatException;

/**
 * @author mts2200
 */
@Getter
@Builder
@Jacksonized
@ToString
@Slf4j(topic = "Document")
public final class Document {
	private final int documentId;
	private final String documentName;
	private final byte[] documentBinary;

	public static DocumentBuilder readFromSQL(ResultSet resultSet) {
		var builder = builder();

		try {
			builder.documentId = resultSet.getInt("document_id");
			builder.documentName = resultSet.getString("document_name");
			builder.documentBinary = Compressor.decompress(resultSet.getBytes("document_binary"));
		} catch (SQLException | DataFormatException exception) {
			log.error(Errors.MODEL_READING, exception);
		}

		return builder;
	}
}
