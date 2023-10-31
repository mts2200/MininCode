package ru.mts2200.minincode.bot.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.mts2200.minincode.bot.sql.manager.TableManager;

import java.io.IOException;

/**
 * @author mts2200
 */
@UtilityClass
@Slf4j(topic = "DocumentHandler")
public final class DocumentHandler {
	private static final String SUCCESS_LOAD = "Файл под названием `%s` успешно загружен на сервер! Его уникальный идентификатор: `%d`";
	private static final String LOAD_FAILURE = "Произошла ошибка при загрузке файла `%s`!";
	private static final long MAX_FILE_SIZE = 10_485_760; // 10 Mb
	private static final String MAX_FILE_SIZE_ERROR = "Невозможно загрузить файл размером более `" + FileUtils.byteCountToDisplaySize(MAX_FILE_SIZE) + "`!";

	public static void processDocument(DefaultAbsSender bot, Chat chat, Document document) {
		try {
			var sendMessage = SendMessage.builder().chatId(chat.getId());

			if (document.getFileSize() <= MAX_FILE_SIZE) {
				int fileId;
				try (var downloadedFileStream = bot.downloadFileAsStream(bot.execute(GetFile.builder().fileId(document.getFileId()).build()))) {
					fileId = TableManager.insertDocument(ru.mts2200.minincode.bot.sql.model.Document.builder().documentName(document.getFileName()).documentBinary(IOUtils.toByteArray(downloadedFileStream)).build());
				}

				if (fileId != -1) {
					sendMessage.text(SUCCESS_LOAD.formatted(document.getFileName(), fileId));
				} else {
					sendMessage.text(LOAD_FAILURE);
				}
			} else {
				sendMessage.text(MAX_FILE_SIZE_ERROR);
			}

			bot.executeAsync(sendMessage.parseMode(ParseMode.MARKDOWN).build());
		} catch (TelegramApiException exception) {
			log.error(Errors.TELEGRAM_API, exception);
			throw new RuntimeException(exception);
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
