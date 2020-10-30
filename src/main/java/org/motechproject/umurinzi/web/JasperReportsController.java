package org.motechproject.umurinzi.web;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JRException;
import org.motechproject.umurinzi.constants.UmurinziConstants;
import org.motechproject.umurinzi.dto.EmailReportConfigDto;
import org.motechproject.umurinzi.service.JasperReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@PreAuthorize(UmurinziConstants.HAS_REPORTS_TAB_ROLE)
public class JasperReportsController {

  @Autowired
  private JasperReportsService jasperReportsService;

  @RequestMapping(value = "/emailReportConfig", method = RequestMethod.GET)
  @ResponseBody
  public EmailReportConfigDto getEmailReportConfig() {
    return jasperReportsService.getEmailReportConfig();
  }

  @RequestMapping(value = "/emailReportConfig", method = RequestMethod.POST)
  @ResponseBody
  public EmailReportConfigDto saveEmailReportConfig(@RequestBody EmailReportConfigDto emailReportConfigDto) {
    return jasperReportsService.saveEmailReportConfig(emailReportConfigDto);
  }

  @RequestMapping(value = "/fetchVaccinationSummaryReport", method = RequestMethod.GET)
  public void fetchVaccinationSummaryReport(HttpServletResponse response)
      throws SQLException, JRException, IOException {
    jasperReportsService.fetchVaccinationSummaryReport(response);
  }
}
