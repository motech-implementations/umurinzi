package org.motechproject.umurinzi.service;

import org.motechproject.umurinzi.dto.VisitRescheduleDto;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.umurinzi.web.domain.Records;

import java.io.IOException;

public interface VisitRescheduleService {

    Records<VisitRescheduleDto> getVisitsRecords(GridSettings settings) throws IOException;

    VisitRescheduleDto saveVisitReschedule(VisitRescheduleDto visitRescheduleDto,
                                           Boolean ignoreLimitation);
}
