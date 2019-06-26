package com.troy.apollo11anni;

import java.io.File;

import org.joda.time.LocalDate;

public class Constants {
	public static final File MISSION_FILE = new File("./Mission Times.xlsx");
	public static final LocalDate EXCEL_DAY_ZERO = new LocalDate(1900, 1, 1).minusDays(2);
}
