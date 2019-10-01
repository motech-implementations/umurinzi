package org.motechproject.umurinzi.service;

import org.joda.time.LocalDate;

public interface ReportService {

    void generateIvrAndSmsStatisticReports();

    void generateIvrAndSmsStatisticReportsFromDate(LocalDate startDate);
}
