package org.motechproject.umurinzi.repository;

import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.umurinzi.domain.UnscheduledVisit;

import java.util.List;

public interface UnscheduledVisitDataService extends MotechDataService<UnscheduledVisit> {

    @Lookup
    List<UnscheduledVisit> findByDate(@LookupField(name = "date") Range<LocalDate> dateRange);

    @Lookup
    List<UnscheduledVisit> findByParticipantId(@LookupField(name = "subject.subjectId",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);

    @Lookup
    List<UnscheduledVisit> findByParticipantIdAndDate(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "subject.subjectId", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);
}
