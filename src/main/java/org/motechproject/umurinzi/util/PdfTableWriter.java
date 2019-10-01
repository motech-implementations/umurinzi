package org.motechproject.umurinzi.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfStamper;
import org.apache.commons.lang.StringUtils;
import org.motechproject.mds.ex.csv.DataExportException;
import org.motechproject.mds.service.impl.csv.writer.TableWriter;
import org.motechproject.umurinzi.template.PdfBasicTemplate;

import java.io.IOException;
import java.util.Map;

public class PdfTableWriter implements TableWriter {

    private static final float WIDTH_PERCENTAGE = 100.0F;

    private static final float CELL_PADDING_BOTTOM = 5.0F;
    protected PdfPTable dataTable; //NO CHECKSTYLE VisibilityModifierCheck
    private PdfBasicTemplate template;

    public PdfTableWriter(PdfBasicTemplate template) {
        this.template = template;
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
            Paragraph paragraph = new Paragraph(value, PdfBasicTemplate.TABLE_FONT);
            PdfPCell cell = new PdfPCell(paragraph);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingBottom(CELL_PADDING_BOTTOM);
            this.dataTable.addCell(cell);
        }
    }

    @Override
    public void writeHeader(String[] headers) throws IOException {
        this.dataTable = new PdfPTable(headers.length);
        this.dataTable.setWidthPercentage(WIDTH_PERCENTAGE);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Paragraph(header, PdfBasicTemplate.HEADER_FONT));
            cell.setBackgroundColor(BaseColor.GRAY);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingBottom(CELL_PADDING_BOTTOM);
            this.dataTable.addCell(cell);
        }
    }

    @Override
    public void close() {
        try {
            ColumnText column = new ColumnText(template.getPdfStamper().getOverContent(1));
            column.setSimpleColumn(template.getFirstPageRectangle());
            column.addElement(dataTable);
            int pageCount = 1;
            int status = column.go();
            while (ColumnText.hasMoreText(status)) {
                status = triggerNewPage(column, ++pageCount);
            }
            template.close();
        } catch (DocumentException ex) {
            throw new DataExportException("Unable to add a table to the PDF file", ex);
        }
    }

    private int triggerNewPage(ColumnText column, int pageCount) throws DocumentException {
        PdfStamper stamper = template.getPdfStamper();
        stamper.insertPage(pageCount, template.getTemplatePageSize());
        PdfContentByte canvas = stamper.getOverContent(pageCount);
        column.setCanvas(canvas);
        column.setSimpleColumn(template.getNextPageRectangle());
        return column.go();
    }
}
