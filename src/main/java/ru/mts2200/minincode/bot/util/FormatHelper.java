package ru.mts2200.minincode.bot.util;

import java.util.regex.Pattern;

/**
 * @author mts2200
 */
public final class FormatHelper {
	/**
	 * Паттерн для ФИО.
	 */
	public static final Pattern NAME_PATTERN = Pattern.compile(".+ .+ .+");

	/**
	 * Паттерн числа.
	 */
	public static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

	/**
	 * Паттерн для названия группы.
	 */
	public static final Pattern IT_CUBE_GROUP_PATTERN = Pattern.compile(".+-[0-9]+");
}
