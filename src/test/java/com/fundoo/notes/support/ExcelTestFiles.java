package com.fundoo.notes.support;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Builds small .xlsx files in memory for the Excel import tests.
 */
public final class ExcelTestFiles {

    private ExcelTestFiles() {
    }

    public static byte[] workbook(String[]... dataRows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Notes");
            String[] header = {"Title", "Description", "Owner Email", "Pinned", "Archived"};
            write(sheet.createRow(0), header);
            for (int i = 0; i < dataRows.length; i++) {
                write(sheet.createRow(i + 1), dataRows[i]);
            }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    private static void write(Row row, String[] values) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                row.createCell(i).setCellValue(values[i]);
            }
        }
    }
}
