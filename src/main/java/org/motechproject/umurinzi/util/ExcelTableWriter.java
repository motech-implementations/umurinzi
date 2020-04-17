package org.motechproject.umurinzi.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;
import org.motechproject.umurinzi.exception.UmurinziExportException;

public class ExcelTableWriter implements TableWriter {

    private static final float HEIGHT_IN_POINTS = 40F;
    private static final short FONT_HEIGHT_IN_POINTS = 11;
    private static final String SHEET_NAME = "Report";

    private final Workbook workbook;
    private final Sheet sheet;

    private final OutputStream outputStream;
    private Map<String, CellStyle> styles;

    private Map<String, Integer> columnIndexMap;

    private int currentRowIndex;

    public ExcelTableWriter(OutputStream outputStream) {
        this.outputStream = outputStream;

        workbook = new HSSFWorkbook();
        sheet = workbook.createSheet(SHEET_NAME);

        setStyleMap();

        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);

        currentRowIndex = 0;
    }

    @Override
    public void writeHeader(String[] titles) throws IOException {
        columnIndexMap = new HashMap<>();

        Row headerRow = sheet.createRow(currentRowIndex);
        headerRow.setHeightInPoints(HEIGHT_IN_POINTS);
        Cell headerCell;

        for (int i = 0; i < titles.length; i++) {
            headerCell = headerRow.createCell(i);
            headerCell.setCellValue(titles[i]);
            headerCell.setCellStyle(getCellStyleForHeader());
            columnIndexMap.put(titles[i], i);
        }

        currentRowIndex++;
    }

    @Override
    public void writeRow(Map<String, String> map, String[] strings) throws IOException {
        Row row = sheet.createRow(currentRowIndex);
        Cell dataCell;

        for (Map.Entry<String, String> entry : map.entrySet()) {
            Integer columnIndex = columnIndexMap.get(entry.getKey());
            if (columnIndex != null) {
                dataCell = row.createCell(columnIndex);
                dataCell.setCellValue(entry.getValue());
                dataCell.setCellStyle(getCellStyleForCell());
            } else {
                throw new UmurinziExportException("No such column: " + entry.getKey());
            }
        }

        currentRowIndex++;
    }

    @Override
    public void close() {
        for (int i = 0; i < columnIndexMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        try {
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new UmurinziExportException(e.getMessage(), e);
        }
    }

    private CellStyle getCellStyleForHeader() {
        return styles.get("header");
    }

    private CellStyle getCellStyleForCell() {
        return styles.get("cell");
    }

    private void setStyleMap() {
        styles = new HashMap<>();
        CellStyle style;
        Font monthFont = workbook.createFont();
        monthFont.setFontHeightInPoints(FONT_HEIGHT_IN_POINTS);
        monthFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        style = workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setFont(monthFont);
        style.setWrapText(true);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styles.put("header", style);

        style = workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setWrapText(true);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(IndexedColors.BLACK.getIndex());
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        styles.put("cell", style);
    }
}
