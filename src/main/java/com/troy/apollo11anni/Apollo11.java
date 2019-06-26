package com.troy.apollo11anni;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class Apollo11 {

	private static final Logger logger = LogManager.getLogger(Apollo11.class);

	private List<MissionEvent> events = new ArrayList<MissionEvent>();

	public Apollo11() {
		readEvents();
	}

	public void start() {
		while (true) {

		}
	}

	private void readEvents() {
		try {
			Workbook workbook = WorkbookFactory.create(Constants.MISSION_FILE);
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> it = sheet.rowIterator();
			Row header = it.next();
			
			while (it.hasNext()) {
				Row row = it.next();
				Cell nameCell = row.getCell(0);
				Cell gmtDateCell = row.getCell(2);
				Cell gmtTimeCell = row.getCell(1);
				Cell commentsCell = row.getCell(3);
				if(        (nameCell == null || nameCell.getCellType() == CellType.BLANK) 
						&& (gmtDateCell == null || gmtDateCell.getCellType() == CellType.BLANK) 
						&& (gmtTimeCell  == null || gmtTimeCell.getCellType() == CellType.BLANK) 
						&& (commentsCell == null || commentsCell.getCellType() == CellType.BLANK)) {
					if(row.getRowNum() < 100)
						logger.warn("Stopping Mission File read at Row: " + (row.getRowNum() + 1));
					else
						logger.info("Stopping Mission File read at Row: " + (row.getRowNum() + 1));
					break;
				}

				if (nameCell == null) throw new RuntimeException("Missing name Cell! Row " + (row.getRowNum() + 1) + " Col 1");
				if (nameCell.getCellType() != CellType.STRING) throw new RuntimeException("Event Name Cell is of invalid type! Row " + (row.getRowNum() + 1) + " Col 1: " + nameCell.getCellType());

				if (gmtTimeCell == null) throw new RuntimeException("Missing GMT Time Cell! Row " + (row.getRowNum() + 1) + " Col 1");
				if (gmtTimeCell.getCellType() != CellType.NUMERIC) throw new RuntimeException("GMT Time Cell is of invalid type! Row " + (row.getRowNum() + 1) + " Col 2: " + gmtTimeCell.getCellType());
				
				if (gmtDateCell == null) throw new RuntimeException("Missing GMT Date Cell! Row " + (row.getRowNum() + 1) + " Col 1");
				if (gmtDateCell.getCellType() != CellType.NUMERIC) throw new RuntimeException("GMT Date Cell is of invalid type! Row " + (row.getRowNum() + 1) + " Col 3: " + gmtDateCell.getCellType());
				
				String comments = "";
				if(commentsCell != null) {
					if(commentsCell.getCellType() != CellType.STRING) throw new RuntimeException("Comments Cell is of invalid type! Row " + (row.getRowNum() + 1) + " Col 4: " + commentsCell.getCellType());
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
}
