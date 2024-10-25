package org.example.printer.impl;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.model.Event;
import org.example.model.League;
import org.example.model.Market;
import org.example.printer.ResultPrinter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelResultPrinter implements ResultPrinter {

    private final String fileName;

    public ExcelResultPrinter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void printLeagues(List<League> leagues) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Leagues");

            int rowNum = 0;
            for (League league : leagues) {
                putDataToNewRow(league.getCaption(), sheet, rowNum++, 0);

                for (Event event : league.getEvents()) {
                    putDataToNewRow(event.getCaption(), sheet, rowNum++, 1);

                    for (Market market : event.getMarkets()) {
                        putDataToNewRow(market.getName(), sheet, rowNum++, 2);

                        for (String runner : market.getRunners()) {
                            putDataToNewRow(runner, sheet, rowNum++, 3);
                        }
                    }
                }
            }

            try (FileOutputStream out = new FileOutputStream(fileName)) {
                workbook.write(out);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void putDataToNewRow(String data, XSSFSheet sheet, int rowNum, int colNum) {
        XSSFRow row = sheet.createRow(rowNum);
        XSSFCell cell = row.createCell(colNum);
        cell.setCellValue(data);
    }
}
