package org.motechproject.umurinzi.service;

import org.motechproject.umurinzi.domain.VisitScheduleOffset;
import org.motechproject.umurinzi.domain.enums.VisitType;

import java.util.List;
import java.util.Map;


public interface VisitScheduleOffsetService {

    VisitScheduleOffset findByVisitType(VisitType visitType);

    List<VisitScheduleOffset> getAll();

    Map<VisitType, VisitScheduleOffset> getAllAsMap();
}
