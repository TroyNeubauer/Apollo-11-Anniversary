package com.troy.apollo11anni;

import static com.troy.apollo11anni.Constants.*;

import java.io.*;
import java.util.*;

import javax.swing.table.*;

import org.apache.logging.log4j.*;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.*;
import org.joda.time.format.*;

public class Apollo11 extends AbstractTableModel {

	private static final Logger logger = LogManager.getLogger(Apollo11.class);

	private List<MissionEvent> events = new ArrayList<MissionEvent>();
	DefaultTableColumnModel model = new DefaultTableColumnModel();
	private DateTime launchTime;
	private int lastEvent = -1;

	public Apollo11() {
		readEvents();
		if (LAST_LAUNCH_TIME_FILE.exists()) {
			try (BufferedReader reader = new BufferedReader(new FileReader(LAST_LAUNCH_TIME_FILE))) {
				String time = reader.readLine();
				try {
					setLaunchTime(ISODateTimeFormat.basicDateTime().parseDateTime(time));
					logger.info("Successfully read and parsed launchtime file");
				} catch (Exception e) {
					logger.warn("LaunchTime file is not a parseable ISO datetime!");
					logger.catching(e);
				}

			} catch (FileNotFoundException e) {
				logger.fatal("java.io.File said the file existed but BufferedReader says otherwise!");
				logger.catching(e);
				System.exit(0);
			} catch (IOException e) {
				logger.warn("Error reading time file:");
				logger.catching(e);
			}
		}
		if (this.launchTime == null) {// Pick the anniversary date
			setLaunchTimeToAnni();
		}

		model.addColumn(setName(new TableColumn(0, 500), "Name"));
		model.addColumn(setName(new TableColumn(1, 125), "Time Until"));
		model.addColumn(setName(new TableColumn(2, 60), "Real Time"));
	}

	private TableColumn setName(TableColumn col, String string) {
		col.setHeaderValue(string);
		return col;
	}

	public void setLaunchTimeToAnni() {
		DateTime now = DateTime.now();
		int year = (int) (Math.ceil((now.getYear() - 4) / 5.0) * 5.0) + 4;// Round to nearest 5 year anniversary
		setLaunchTime(new DateTime(year, APOLLO_11_LAUNCH_TIME.getMonthOfYear(), APOLLO_11_LAUNCH_TIME.getDayOfMonth(), APOLLO_11_LAUNCH_TIME.getHourOfDay(), APOLLO_11_LAUNCH_TIME.getMinuteOfHour(),
				APOLLO_11_LAUNCH_TIME.getSecondOfMinute(), APOLLO_11_LAUNCH_TIME.getZone()).withZone(DateTimeZone.getDefault()));
	}

	private void readEvents() {
		try {
			Workbook workbook = WorkbookFactory.create(new File(MISSION_FILE));
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> it = sheet.rowIterator();
			Row header = it.next();

			while (it.hasNext()) {
				Row row = it.next();
				Cell nameCell = row.getCell(0);
				Cell gmtDateCell = row.getCell(2);
				Cell gmtTimeCell = row.getCell(1);
				Cell commentsCell = row.getCell(3);
				if ((nameCell == null || nameCell.getCellType() == CellType.BLANK) && (gmtDateCell == null || gmtDateCell.getCellType() == CellType.BLANK)
						&& (gmtTimeCell == null || gmtTimeCell.getCellType() == CellType.BLANK) && (commentsCell == null || commentsCell.getCellType() == CellType.BLANK)) {
					if (row.getRowNum() < 100)
						logger.warn("Stopping Mission File read at Row: " + (row.getRowNum() + 1));
					else
						logger.info("Stopping Mission File read at Row: " + (row.getRowNum() + 1));
					break;
				}

				if (nameCell == null)
					throw new RuntimeException("Missing name Cell! Row " + (row.getRowNum() + 1) + " Col 1");
				if (nameCell.getCellType() != CellType.STRING)
					throw new RuntimeException("Event Name Cell is of invalid type! Row " + (row.getRowNum() + 1) + " Col 1: " + nameCell.getCellType());

				if (gmtTimeCell == null)
					throw new RuntimeException("Missing GMT Time Cell! Row " + (row.getRowNum() + 1) + " Col 1");
				if (gmtTimeCell.getCellType() != CellType.NUMERIC)
					throw new RuntimeException("GMT Time Cell is of invalid type! Row " + (row.getRowNum() + 1) + " Col 2: " + gmtTimeCell.getCellType());

				if (gmtDateCell == null)
					throw new RuntimeException("Missing GMT Date Cell! Row " + (row.getRowNum() + 1) + " Col 1");
				if (gmtDateCell.getCellType() != CellType.NUMERIC)
					throw new RuntimeException("GMT Date Cell is of invalid type! Row " + (row.getRowNum() + 1) + " Col 3: " + gmtDateCell.getCellType());

				String comments = "";
				if (commentsCell != null) {
					if (commentsCell.getCellType() != CellType.STRING)
						throw new RuntimeException("Comments Cell is of invalid type! Row " + (row.getRowNum() + 1) + " Col 4: " + commentsCell.getCellType());
					comments = commentsCell.getStringCellValue();
				}

				events.add(new MissionEvent(nameCell.getStringCellValue(), gmtDateCell.getNumericCellValue(), gmtTimeCell.getNumericCellValue(), comments));

			}

			workbook.close();
		} catch (EncryptedDocumentException | IOException e) {
			logger.fatal("Unable to read mission times file!");
			logger.catching(e);
			System.exit(0);
		}
	}

	public String getCountdownString(DateTime time) {
		DateTime now = DateTime.now();
		Duration duration = new Duration(now, time);
		Period period = duration.toPeriodFrom(now, PeriodType
				.forFields(new DurationFieldType[] { DurationFieldType.years(), DurationFieldType.days(), DurationFieldType.hours(), DurationFieldType.minutes(), DurationFieldType.seconds() }));

		if (now.isAfter(time)) {// The event already happened
			PeriodFormatter formatter = new PeriodFormatterBuilder().printZeroRarelyLast().appendYears().appendSuffix(" year", " years").appendSeparator(", ").appendDays()
					.appendSuffix(" day", " days").appendSeparator(", ").appendHours().appendSuffix(" hours", " hours").appendSeparator(", ").appendMinutes().appendSuffix(" minute", " minutes")
					.toFormatter();
			return formatter.print(period.negated()) + " ago";
		} else if (time.isBefore(now.plusDays(1))) {// The event happens within the next day
			return "T-" + to2Digits(period.getHours()) + ":" + to2Digits(period.getMinutes()) + ":" + to2Digits(period.getSeconds());
		} else {
			PeriodFormatter formatter = new PeriodFormatterBuilder().printZeroRarelyLast().appendYears().appendSuffix(" year", " years").appendSeparator(", ").appendDays()
					.appendSuffix(" day", " days").appendSeparator(", ").appendHours().appendSuffix(" hours", " hours").toFormatter();

			return "In " + formatter.print(period);
		}
	}

	public int getEventsComplete() {
		int count = 0;
		DateTime now = DateTime.now();
		for (MissionEvent event : events) {
			if (now.isAfter(event.getTime(launchTime)))
				count++;
			else
				break;
		}
		return count;
	}

	private String to2Digits(long value) {
		if (value < 10)
			return "0" + value;
		else
			return Long.toString(value);
	}

	@Override
	public int getRowCount() {
		return events.size();
	}

	@Override
	public int getColumnCount() {
		return getColumnModel().getColumnCount();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		MissionEvent event = events.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return event.getName();
		case 1:
			return getCountdownString(event.getTime(launchTime));
		case 2:
			return event.getRealTime().toString(STANDARD_FORMATTER);
		default:
			throw new RuntimeException("Col out of range! " + columnIndex);
		}
	}

	public TableColumnModel getColumnModel() {
		return model;
	}

	public DateTime getLaunchTime() {
		return launchTime;
	}

	public List<MissionEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}

	public void setLaunchTime(DateTime launchTime) {
		this.launchTime = launchTime;
		logger.info("Setting launch time to " + launchTime.toString(DateTimeFormat.fullDateTime()));
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(LAST_LAUNCH_TIME_FILE))) {
			writer.write(ISODateTimeFormat.basicDateTime().print(launchTime));
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			logger.warn("Error saving launch time file!");
			logger.catching(e);
		}
	}

	public void update() {

	}

}
