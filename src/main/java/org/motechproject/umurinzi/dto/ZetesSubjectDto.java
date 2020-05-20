package org.motechproject.umurinzi.dto;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;

public class ZetesSubjectDto {

  @Getter
  @Setter
  private String subjectId;

  @Getter
  @Setter
  private String phoneNumber;

  @Getter
  @Setter
  private String vaccinationSite;

  @Getter
  @Setter
  private Date primeVaccinationDate;

  @Getter
  @Setter
  private Date boostVaccinationDate;
}
