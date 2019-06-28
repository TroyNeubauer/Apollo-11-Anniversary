package com.troy.apollo11anni;

import java.io.File;

import org.joda.time.*;
import org.joda.time.format.DateTimeFormatter;

public class Constants {
	public static final String MISSION_FILE = "Mission Times.xlsx";
	public static final LocalDate EXCEL_DAY_ZERO = new LocalDate(1900, 1, 1).minusDays(2);

	public static final DateTimeFormatter STANDARD_FORMATTER = new org.joda.time.format.DateTimeFormatterBuilder().appendMonthOfYear(1).appendLiteral('/').appendDayOfMonth(1).appendLiteral('/')
			.appendYear(1, 5).appendLiteral(' ').appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).appendLiteral(':').appendSecondOfMinute(2).toFormatter();

	public static final DateTime APOLLO_11_LAUNCH_TIME = new DateTime(1969, 7, 16, 9, 32, 0, DateTimeZone.forOffsetHours(-4));// EDT
	
	public static final File LAST_LAUNCH_TIME_FILE = new File("./LaunchTime.txt");
}
