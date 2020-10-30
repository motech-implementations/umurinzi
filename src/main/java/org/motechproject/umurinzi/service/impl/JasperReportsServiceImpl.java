package org.motechproject.umurinzi.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.Exporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.motechproject.config.core.domain.SQLDBConfig;
import org.motechproject.config.core.service.CoreConfigurationService;
import org.motechproject.umurinzi.domain.Config;
import org.motechproject.umurinzi.dto.EmailReportConfigDto;
import org.motechproject.umurinzi.helper.EmailHelper;
import org.motechproject.umurinzi.mapper.EmailConfigMapper;
import org.motechproject.umurinzi.scheduler.UmurinziScheduler;
import org.motechproject.umurinzi.service.ConfigService;
import org.motechproject.umurinzi.service.JasperReportsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("jasperReportsService")
public class JasperReportsServiceImpl implements JasperReportsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(JasperReportsServiceImpl.class);

  private static final DateTimeFormatter START_DATE_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm Z");

  private static final String REPORT_PATH = "/reports/VaccinationSummaryReport.jasper";
  private static final String LOGO_PATH = "/reports/ebodac_logo.jpg";

  private static final String LOGO_PARAM = "logo";
  private static final String GENERATION_DATE_PARAM = "generationDate";
  private static final String START_DATE_PARAM = "startDate";
  private static final String END_DATE_PARAM = "endDate";
  private static final String CHART_START_DATE = "chartStartDate";

  private static final EmailConfigMapper MAPPER = EmailConfigMapper.INSTANCE;

  @Autowired
  private CoreConfigurationService coreConfigurationService;

  @Autowired
  private UmurinziScheduler umurinziScheduler;

  @Autowired
  @Qualifier("configService")
  private ConfigService configService;

  @Autowired
  private EmailHelper emailHelper;

  @Override
  public EmailReportConfigDto getEmailReportConfig() {
    Config config = configService.getConfig();

    return MAPPER.toDto(config);
  }

  @Override
  public EmailReportConfigDto saveEmailReportConfig(EmailReportConfigDto emailReportConfigDto) {
    Config config = configService.getConfig();

    if (!emailReportConfigDto.getEnableEmailReportJob() || emailReportConfigDto.getEmailSchedulePeriod() == null
        || StringUtils.isBlank(emailReportConfigDto.getEmailReportStartDate())) {
      umurinziScheduler.unscheduleEmailReportJob();
    } else if (!emailReportConfigDto.getEmailSchedulePeriod().equals(config.getEmailSchedulePeriod())
        || !emailReportConfigDto.getEmailReportStartDate().equals(config.getEmailReportStartDate())
        || !config.getEnableEmailReportJob()) {
      Period period = emailReportConfigDto.getEmailSchedulePeriod().getPeriod();
      DateTime startDate = START_DATE_FORMATTER.parseDateTime(emailReportConfigDto.getEmailReportStartDate());

      if (startDate.isBeforeNow()) {
        startDate = startDate.plus(period);
      }

      umurinziScheduler.rescheduleEmailReportJob(startDate, period);
    }

    MAPPER.updateFromDto(emailReportConfigDto, config);
    configService.updateConfig(config);

    return MAPPER.toDto(config);
  }

  @Override
  public void sendVaccinationSummaryReport() {
    Config config = configService.getConfig();

    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      generateVaccinationSummaryReport(out);
      DataSource source = new ByteArrayDataSource(out.toByteArray(), "application/pdf");

      List<String> recipients = new ArrayList<>();

      for (String recipient : config.getEmailRecipients().split(",")) {
        recipients.add(recipient.trim());
      }

      DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyyMMddHHmmss");
      String fileName = "VaccinationSummaryReport_" + DateTime.now().toString(dateTimeFormatter) + ".pdf";

      emailHelper.sendNewMessage(config.getEmailReportHost(), config.getEmailReportAddress(),
          config.getEmailReportPassword(), config.getEmailReportPort(), config.getEmailSubject(),
          recipients, config.getEmailBody(), source, fileName);
    } catch (IllegalArgumentException e) {
      LOGGER.error("Could not send email report, because of wrong report data: {}", e.getMessage(), e);
    } catch (Exception e) {
      LOGGER.error("Could not send email report, because exception occurred when generating report", e);
    }
  }

  @Override
  public void fetchVaccinationSummaryReport(HttpServletResponse response)
      throws SQLException, JRException, IOException {
    response.setContentType("application/pdf");
    response.setHeader(
        "Content-Disposition",
        "attachment; filename=VaccinationSummaryReport.pdf");
    generateVaccinationSummaryReport(response.getOutputStream());
  }

  private void generateVaccinationSummaryReport(OutputStream outputStream)
      throws SQLException, JRException {
    Map<String, Object> params = new HashMap<>();
    params.put(LOGO_PARAM, getClass().getResourceAsStream(LOGO_PATH));

    LocalDate date = new LocalDate();
    LocalDate endDate = date.withDayOfWeek(DateTimeConstants.MONDAY).minusWeeks(1);
    LocalDate startDate = endDate.minusWeeks(1);
    LocalDate chartStartDate = date.withDayOfMonth(1).minusMonths(2);

    params.put(GENERATION_DATE_PARAM, date);
    params.put(START_DATE_PARAM, startDate);
    params.put(END_DATE_PARAM, endDate);
    params.put(CHART_START_DATE, chartStartDate);

    generateReport(REPORT_PATH, params, outputStream);
  }

  private void generateReport(String reportName, Map<String, Object> params, OutputStream outputStream)
      throws SQLException, JRException {
    SQLDBConfig sqlConfig = coreConfigurationService.loadBootstrapConfig().getSqlConfig();

    try (Connection connection = DriverManager.getConnection(sqlConfig.getUrl(), sqlConfig.getUsername(), sqlConfig.getPassword())) {
      JasperReport jasperReport = (JasperReport) JRLoader.loadObject(getClass().getResource(reportName));
      JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, connection);

      Exporter exporter = new JRPdfExporter();

      exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
      exporter.setExporterInput(new SimpleExporterInput(jasperPrint));

      exporter.exportReport();
    }
  }
}
