package org.motechproject.umurinzi.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.motechproject.mds.ex.csv.DataExportException;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;

public class PdfTableWriter implements TableWriter {

    private static final float WIDTH_PERCENTAGE = 100.0F;
    private static final float CELL_PADDING_BOTTOM = 5.0F;

    private static final int WRITE_TABLE_EVERY_ROWS = 100;

    private static final Font TABLE_FONT = new Font(Font.FontFamily.HELVETICA, 8);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD);

    private int currentRowIndex;

    private final Document document;

    protected PdfPTable dataTable; //NO CHECKSTYLE VisibilityModifierCheck

    public PdfTableWriter(OutputStream outputStream) {
        currentRowIndex = 0;

        try {
            document = new Document();
            PdfWriter.getInstance(document, outputStream);
        } catch (DocumentException e) {
            throw new DataExportException("Error occurred when creating Pdf", e);
        }

        document.open();
    }

    @Override
    public void writeRow(Map<String, String> row, String[] headers) throws IOException {
        if (this.dataTable == null) {
            this.writeHeader(headers);
        }

        for (String header : headers) {
            String value = row.get(header);
            if (StringUtils.isBlank(value)) {
                value = "\n";
            }
            Paragraph paragraph = new Paragraph(value, TABLE_FONT);
            PdfPCell cell = new PdfPCell(paragraph);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingBottom(CELL_PADDING_BOTTOM);
            this.dataTable.addCell(cell);
        }

        try {
            if (currentRowIndex % WRITE_TABLE_EVERY_ROWS == 0) {
                document.add(dataTable);
            }
        } catch (DocumentException e) {
            throw new IOException(e);
        }

        currentRowIndex++;
    }

    @Override
    public void writeHeader(String[] headers) throws IOException {
        dataTable = new PdfPTable(headers.length);
        dataTable.setWidthPercentage(WIDTH_PERCENTAGE);

        dataTable.setSplitLate(true);
        dataTable.setSplitRows(false);

        dataTable.setComplete(false);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, HEADER_FONT));
            cell.setBackgroundColor(BaseColor.GRAY);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingBottom(CELL_PADDING_BOTTOM);
            dataTable.addCell(cell);
        }
    }

    @Override
    public void close() {
        try {
            dataTable.setComplete(true);
            document.add(dataTable);
            document.close();
        } catch (DocumentException ex) {
            throw new DataExportException("Unable to add a table to the PDF file", ex);
        }
    }
}
