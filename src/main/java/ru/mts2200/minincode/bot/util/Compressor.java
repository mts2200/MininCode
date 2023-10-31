package ru.mts2200.minincode.bot.util;

import lombok.experimental.UtilityClass;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * @author mts2200
 */
@UtilityClass
public final class Compressor {
	private static final int BUFFER_SIZE = 1024;

	/**
	 * Сжимает массив с помощью GZIP
	 *
	 * @param bytes массив байтов
	 * @return сжатый массив байтов
	 */
	public static byte[] compress(byte[] bytes) {
		Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
		deflater.setInput(bytes);
		deflater.finish();

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		byte[] readBuffer = new byte[BUFFER_SIZE];

		while (!deflater.finished()) {
			int readCount = deflater.deflate(readBuffer);
			if (readCount > 0) {
				bao.write(readBuffer, 0, readCount);
			}
		}

		deflater.end();
		return bao.toByteArray();
	}

	/**
	 * Распаковывает массив с помощью GZIP
	 *
	 * @param bytes массив байтов
	 * @return распакованный массив байтов
	 * @throws DataFormatException если формат сжатых данных недействителен
	 */
	public static byte[] decompress(byte[] bytes) throws DataFormatException {
		Inflater inflater = new Inflater(true);
		inflater.setInput(bytes);

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		byte[] readBuffer = new byte[BUFFER_SIZE];

		while (!inflater.finished()) {
			int readCount = inflater.inflate(readBuffer);
			if (readCount > 0) {
				bao.write(readBuffer, 0, readCount);
			}
		}

		inflater.end();
		return bao.toByteArray();
	}
}
