package com.personal.secondhand.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;

public class ExcelUtil {


    public static XSSFWorkbook make2007Excel(String sheetName, String[] title, List<String[]> dataList) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(4000);
        Row firstRow = sheet.createRow(0);
        for (int i = 0; i < title.length; i++) {
            Cell cell = firstRow.createCell(i);
            cell.setCellStyle(useDefaultCellStyle(workbook));
            cell.setCellValue(title[i]);
        }
        for (int i = 0; i < dataList.size(); i++) {
            String[] data = dataList.get(i);
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < data.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(useDefaultCellStyle(workbook));
                cell.setCellValue(data[j]);
            }
        }
        return workbook;
    }

    private static CellStyle useDefaultCellStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
//        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.CENTER_SELECTION);
        return cellStyle;
    }


    private ExcelUtil() {
    }

}
