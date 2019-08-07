package com.personal.secondhand.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.List;
import java.util.Objects;

public class ExcelUtil {


    public static XSSFWorkbook make2007Excel(XSSFWorkbook workbook, String sheetName, String[] title, List<String[]> dataList) throws Exception {
        if (Objects.isNull(workbook)) {
            workbook = new XSSFWorkbook();
        }
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        XSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.setDefaultColumnWidth(8000);
        Row firstRow = sheet.createRow(0);
        for (int i = 0; i < title.length; i++) {
            Cell cell = firstRow.createCell(i);
            cell.setCellStyle(cellStyle);
            cell.setCellValue(title[i]);
        }
        for (int i = 0; i < dataList.size(); i++) {
            String[] data = dataList.get(i);
            Row row = sheet.createRow(i + 1);
            for (int j = 0; j < data.length; j++) {
                Cell cell = row.createCell(j);
                cell.setCellStyle(cellStyle);
                cell.setCellValue(data[j]);
            }
        }
        return workbook;
    }


    private ExcelUtil() {
    }

}
