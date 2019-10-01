package org.motechproject.umurinzi.service;

import org.motechproject.umurinzi.dto.UnscheduledVisitDto;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.umurinzi.web.domain.Records;

import java.io.IOException;

public interface UnscheduledVisitService {

    Records<UnscheduledVisitDto> getUnscheduledVisitsRecords(GridSettings settings) throws IOException;

    UnscheduledVisitDto addOrUpdate(UnscheduledVisitDto unscheduledVisitDto,
                                    Boolean ignoreLimitation);
}
