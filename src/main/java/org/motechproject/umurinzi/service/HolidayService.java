package org.motechproject.umurinzi.service;

import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.Holiday;

public interface HolidayService {

  Holiday findByDate(LocalDate date);

  Holiday create(Holiday holiday);

  Holiday update(Holiday holiday);

  void dataChanged(Holiday holiday);
}
