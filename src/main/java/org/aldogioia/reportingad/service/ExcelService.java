package org.aldogioia.reportingad.service;

import org.aldogioia.reportingad.model.enumerator.MessageCode;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelService {

    public static MessageCode writeExcel(
            String campaignName,
            List<String> infoLines,
            String headerLine,
            List<String> dataLines
    ) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy"));
        String fileName = "report_" + String.join(" ", campaignName) + "_" + date + ".xlsx";
        String outputFilePath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName;

        try (
                Workbook workbook = new XSSFWorkbook();
                FileOutputStream fos = new FileOutputStream(outputFilePath)
        ) {
            Sheet sheet = workbook.createSheet("Report");
            int rowIndex = 0;

            // Informazioni generali
            rowIndex = getRowIndex(infoLines, sheet, rowIndex);

            rowIndex++;

            // Intestazione
            String[] headers = headerLine.split(";");
            Row headerRow = sheet.createRow(rowIndex++);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Dati
            getRowIndex(dataLines, sheet, rowIndex);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(fos);
            return MessageCode.OK;
        } catch (IOException e) {
            System.err.println("Errore nella scrittura Excel: " + e.getMessage());
            return MessageCode.ERROR;
        }
    }

    private static int getRowIndex(List<String> infoLines, Sheet sheet, int rowIndex) {
        for (String info : infoLines) {
            String[] values = info.split(";");
            Row row = sheet.createRow(rowIndex++);
            for (int i = 0; i < values.length; i++) {
                row.createCell(i).setCellValue(values[i]);
            }
        }
        return rowIndex;
    }
}
