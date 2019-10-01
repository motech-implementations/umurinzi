package org.motechproject.umurinzi.util;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;
import org.motechproject.umurinzi.exception.UmurinziExportException;
import org.motechproject.umurinzi.template.XlsBasicTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExcelTableWriter implements TableWriter {

    private static final float HEIGHT_IN_POINTS = 40F;

    private XlsBasicTemplate xlsTemplate;

    private Map<String, Integer> columnIndexMap;

    private int currentRowIndex;

    public ExcelTableWriter(XlsBasicTemplate template) {
        xlsTemplate = template;
        currentRowIndex = 0;
    }

    @Override
    public void writeHeader(String[] titles) throws IOException {
        columnIndexMap = new HashMap<>();
        Sheet sheet = xlsTemplate.getSheet();
        Row headerRow = sheet.createRow(xlsTemplate.getIndexOfHeaderRow());
        headerRow.setHeightInPoints(HEIGHT_IN_POINTS);
        Cell headerCell;
        for (int i = 0; i < titles.length; i++) {
            headerCell = headerRow.createCell(i);
            headerCell.setCellValue(titles[i]);
            headerCell.setCellStyle(xlsTemplate.getCellStyleForHeader());
            columnIndexMap.put(titles[i], i);
        }
    }

    @Override
    public void writeRow(Map<String, String> map, String[] strings) throws IOException {
        Sheet sheet = xlsTemplate.getSheet();
        Row row = sheet.createRow(xlsTemplate.getIndexOfFirstDataRow() + currentRowIndex);
        Cell dataCell;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            Integer columnIndex = columnIndexMap.get(entry.getKey());
            if (columnIndex != null) {
                dataCell = row.createCell(columnIndex);
                dataCell.setCellValue(entry.getValue());
                dataCell.setCellStyle(xlsTemplate.getCellStyleForCell());
            } else {
                throw new UmurinziExportException("No such column: " + entry.getKey());
            }
        }
        currentRowIndex++;
    }

    @Override
    public void close() {
        Sheet sheet = xlsTemplate.getSheet();
        for (int i = 0; i < columnIndexMap.size(); i++) {
            sheet.autoSizeColumn(i);
        }
        xlsTemplate.close();
    }

}
