package it.drwolf.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

	private DateUtils() {
	}

	public static LocalDate convertFromString(String dateAsString, String datePattern) {
		LocalDate localDate = null;
		if (dateAsString != null && !dateAsString.trim().isEmpty()) {
			final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);
			localDate = LocalDate.parse(dateAsString, dateTimeFormatter);
		}
		return localDate;
	}

	public static LocalDateTime convertFromString(String dateAsString, String datePattern, LocalTime localTime) {
		LocalDateTime localDate = null;
		if (dateAsString != null && !dateAsString.trim().isEmpty()) {
			final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(datePattern);
			localDate = LocalDate.parse(dateAsString, dateTimeFormatter).atTime(localTime);
		}
		return localDate;
	}

	public static Date convertToDate(LocalDateTime dateToConvert) {
		return Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDateTime convertToLocalDateTime(Date dateToConvert) {
		return dateToConvert.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

}
