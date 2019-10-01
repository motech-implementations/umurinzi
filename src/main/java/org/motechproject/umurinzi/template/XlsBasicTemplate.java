package org.motechproject.umurinzi.template;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.motechproject.umurinzi.exception.UmurinziExportException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public abstract class XlsBasicTemplate {

    private static final short FONT_HEIGHT_IN_POINTS = 11;
    private final Workbook workbook;
    private final Sheet sheet;
    private final int indexOfHeaderRow;
    private final OutputStream outputStream;
    private Map<String, CellStyle> styles;

    public XlsBasicTemplate(String templatePath, int indexOfHeaderRow, OutputStream outputStream) {
        this.indexOfHeaderRow = indexOfHeaderRow;
        this.outputStream = outputStream;

        try {
            workbook = new HSSFWorkbook(getClass().getResourceAsStream(templatePath));
        } catch (IOException e) {
            throw new UmurinziExportException(e.getMessage(), e);
        }
        setStyleMap();
        sheet = workbook.getSheetAt(0);

        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
    }

    public int getIndexOfHeaderRow() {
        return indexOfHeaderRow;
    }

    public int getIndexOfFirstDataRow() {
        return indexOfHeaderRow + 1;
    }

    public CellStyle getCellStyleForHeader() {
        return styles.get("header");
    }

    public CellStyle getCellStyleForCell() {
        return styles.get("cell");
    }

    public Sheet getSheet() {
        return sheet;
    }

    public void close() {
        try {
            workbook.write(outputStream);
        } catch (IOException e) {
            throw new UmurinziExportException(e.getMessage(), e);
        }
    }

    protected void setAdditionalCellValue(String cellAddress, String cellValue) {
        CellReference cr = new CellReference(cellAddress);
        Row row = sheet.getRow(cr.getRow());
        Cell cell = row.getCell(cr.getCol());
        cell.setCellValue(cellValue);
        sheet.autoSizeColumn(cr.getCol());
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
