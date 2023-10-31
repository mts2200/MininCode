package ru.mts2200.minincode.launch.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import ru.mts2200.minincode.bot.async.AsyncIO;
import ru.mts2200.minincode.bot.util.Errors;
import ru.mts2200.minincode.launch.dao.GlobalSettings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author mts2200
 */
@UtilityClass
@Slf4j(topic = "Config")
public class GlobalConfig {
	public static final ObjectMapper CONFIG_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private static final File configFile = new File("minincode_settings.json");

	public static volatile GlobalSettings settings = GlobalSettings.builder().build();

	public static void load() {
		if (!configFile.exists()) {
			save();
		} else {
			try {
				var json = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
				settings = CONFIG_MAPPER.readValue(json, GlobalSettings.class);
			} catch (IOException exception) {
				log.error(Errors.CONFIG_LOAD, exception);
			}
		}
	}

	public static void save() {
		try {
			var json = CONFIG_MAPPER.writeValueAsString(settings);
			FileUtils.write(configFile, json, StandardCharsets.UTF_8);
		} catch (IOException exception) {
			log.error(Errors.CONFIG_SAVE, exception);
		}
	}

	public static void asyncSave() {
		AsyncIO.GLOBAL.getExecutor().submit(GlobalConfig::save);
	}
}
