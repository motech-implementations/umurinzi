package org.motechproject.umurinzi.repository;

import java.util.List;
import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.umurinzi.domain.SubjectEnrollments;
import org.motechproject.umurinzi.domain.enums.EnrollmentStatus;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;

public interface SubjectEnrollmentsDataService extends MotechDataService<SubjectEnrollments> {

    @Lookup(name = "Find unique By Participant Id")
    SubjectEnrollments findBySubjectId(@LookupField(name = "subject.subjectId") String subjectId);

    @Lookup(name = "Find By Participant Id")
    List<SubjectEnrollments> findByMatchesCaseInsensitiveSubjectId(
        @LookupField(name = "subject.subjectId",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);

    @Lookup(name = "Find By Participant Name")
    List<SubjectEnrollments> findBySubjectName(@LookupField(name = "subject.name",
        customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String name);

    @Lookup
    List<SubjectEnrollments> findByDateOfUnenrollment(
        @LookupField(name = "dateOfUnenrollment") Range<LocalDate> dateOfUnenrollment);

    @Lookup
    List<SubjectEnrollments> findByStatus(@LookupField(name = "status") EnrollmentStatus status);

    @Lookup(name = "Find By Participant Id And Status")
    List<SubjectEnrollments> findBySubjectIdAndStatus(@LookupField(name = "subject.subjectId",
        customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
        @LookupField(name = "status") EnrollmentStatus status);

    @Lookup
    List<SubjectEnrollments> findByDateOfUnenrollmentAndStatus(
        @LookupField(name = "dateOfUnenrollment") Range<LocalDate> dateOfUnenrollment,
        @LookupField(name = "status") EnrollmentStatus status);
}
