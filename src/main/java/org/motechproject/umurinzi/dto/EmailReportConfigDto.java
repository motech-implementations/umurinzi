package org.motechproject.umurinzi.dto;

import lombok.Getter;
import lombok.Setter;
import org.motechproject.umurinzi.domain.enums.EmailSchedulePeriod;

public class EmailReportConfigDto {

  @Getter
  @Setter
  private Boolean enableEmailReportJob = false;

  @Getter
  @Setter
  private String emailReportStartDate;

  @Getter
  @Setter
  private EmailSchedulePeriod emailSchedulePeriod;

  @Getter
  @Setter
  private String emailRecipients;

  @Getter
  @Setter
  private String emailSubject;

  @Getter
  @Setter
  private String emailBody;
}
