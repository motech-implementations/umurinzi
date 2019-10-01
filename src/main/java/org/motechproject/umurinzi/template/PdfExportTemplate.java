package org.motechproject.umurinzi.template;

import com.itextpdf.text.Rectangle;

import java.io.OutputStream;

public class PdfExportTemplate extends PdfBasicTemplate {

    private static final String TEMPLATE_PATH = "/templates/export_template.pdf";
    private static final Rectangle FIRST_PAGE_RECTANGLE = new Rectangle(20, 36, 580, 680);

    public PdfExportTemplate(OutputStream outputStream) {
        super(TEMPLATE_PATH, FIRST_PAGE_RECTANGLE, outputStream);
    }

}
