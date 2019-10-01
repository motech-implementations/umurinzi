package org.motechproject.umurinzi.repository;

import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;
import org.motechproject.mds.util.Constants;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.domain.enums.VisitType;

import java.util.List;
import java.util.Set;

public interface VisitDataService extends MotechDataService<Visit> {

    @Lookup(name = "Find By exact Participant Id")
    List<Visit> findBySubjectId(
            @LookupField(name = "subject.subjectId") String subjectId);

    @Lookup
    List<Visit> findByVisitTypeAndActualDateLessOrEqual(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date", customOperator = Constants.Operators.LT_EQ) LocalDate date);

    /**
     * UI Lookups
     */

    @Lookup
    List<Visit> findByParticipantId(@LookupField(name = "subject.subjectId",
            customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId);

    @Lookup
    List<Visit> findByType(@LookupField(name = "type") VisitType type);

    @Lookup
    List<Visit> findByVisitActualDate(
            @LookupField(name = "date") Range<LocalDate> date);

    @Lookup
    List<Visit> findByVisitActualDateRange(
            @LookupField(name = "date") Range<LocalDate> date);

    @Lookup
    List<Visit> findByVisitPlannedDateRange(
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitPlannedDate(
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitTypeAndActualDateRange(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date") Range<LocalDate> date);

    @Lookup
    List<Visit> findByVisitTypeAndActualDate(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date") Range<LocalDate> date);


    @Lookup
    List<Visit> findByPlannedVisitDateRange(
        @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByPlannedVisitDate(
        @LookupField(name = "dateProjected") LocalDate plannedDate);

    @Lookup
    List<Visit> findByPlannedVisitDateRangeAndType(
        @LookupField(name = "type") VisitType type,
        @LookupField(name = "dateProjected") Range<LocalDate> date);

    @Lookup
    List<Visit> findByPlannedVisitDateAndType(
        @LookupField(name = "type") VisitType type,
        @LookupField(name = "dateProjected") LocalDate date);

    /**
     * Reschedule Screen Lookups
     */

    @Lookup
    List<Visit> findByVisitTypeSetAndPlannedDate(
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected",
                    customOperator = Constants.Operators.NEQ) LocalDate plannedDate);

    @Lookup
    List<Visit> findByVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> date);

    @Lookup
    List<Visit> findByParticipantIdAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitTypeAndPlannedDate(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitTypeAndPlannedDateRange(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitActualDateAndVisitTypeSetAndPlannedDateRange(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "type") Set<VisitType> typeSet,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitPlannedDateRangeAndVisitTypeSet(
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate,
            @LookupField(name = "type") Set<VisitType> typeSet);

    @Lookup
    List<Visit> findByVisitTypeAndActualDateRangeAndPlannedDateRange(
            @LookupField(name = "type") VisitType type,
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "dateProjected") Range<LocalDate> plannedDate);

    @Lookup
    List<Visit> findByVisitActualDateAndVisitTypeSet(
            @LookupField(name = "date") Range<LocalDate> date,
            @LookupField(name = "type") Set<VisitType> typeSet);

    @Lookup
    List<Visit> findByParticipantIdAndVisitTypeSet(
            @LookupField(name = "subject.subjectId",
                    customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
            @LookupField(name = "type") Set<VisitType> typeSet);

    /**
     * Followups Missed Clinic Visits Report Lookups
     */

    @Lookup
    List<Visit> findByPlannedDateLessAndActualDateEqAndSubjectPhoneNumberEq(
        @LookupField(name = "subject.phoneNumber", customOperator = Constants.Operators.EQ) String phoneNumber,
        @LookupField(name = "dateProjected", customOperator = Constants.Operators.LT) LocalDate dateProjected,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup
    List<Visit> findByPlannedVisitDateEq(
        @LookupField(name = "subject.phoneNumber", customOperator = Constants.Operators.EQ) String phoneNumber,
        @LookupField(name = "dateProjected") LocalDate dateProjected,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup
    List<Visit> findByPlannedVisitDateAndTypeEq(
        @LookupField(name = "subject.phoneNumber", customOperator = Constants.Operators.EQ) String phoneNumber,
        @LookupField(name = "dateProjected") LocalDate dateProjected,
        @LookupField(name = "type") VisitType visitType,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup
    List<Visit> findByPlannedVisitDateRangeEq(
        @LookupField(name = "subject.phoneNumber", customOperator = Constants.Operators.EQ) String phoneNumber,
        @LookupField(name = "dateProjected") Range<LocalDate> dateRange,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup
    List<Visit> findByPlannedVisitDateRangeAndTypeEq(
        @LookupField(name = "subject.phoneNumber", customOperator = Constants.Operators.EQ) String phoneNumber,
        @LookupField(name = "dateProjected") Range<LocalDate> dateProjectedRange,
        @LookupField(name = "type") VisitType visitType,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    /**
     * M&E Missed Clinic Visits Report Lookups
     */

    @Lookup
    List<Visit> findByPlannedVisitDateLessAndActualVisitDate(
        @LookupField(name = "dateProjected", customOperator = Constants.Operators.LT) LocalDate dateProjected,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup(name = "Find By Participant Id And Planned Visit Date And Actual Visit Date")
    List<Visit> findBySubjectIdAndPlannedVisitDateAndActualVisitDate(
        @LookupField(name = "subject.subjectId", customOperator = Constants.Operators.MATCHES_CASE_INSENSITIVE) String subjectId,
        @LookupField(name = "dateProjected", customOperator = Constants.Operators.LT) LocalDate dateProjected,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup
    List<Visit> findByPlannedVisitDateAndActualVisitDate(
        @LookupField(name = "dateProjected") LocalDate dateProjected,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup
    List<Visit> findByPlannedVisitDateAndTypeAndActualVisitDate(
        @LookupField(name = "dateProjected") LocalDate dateProjected,
        @LookupField(name = "type") VisitType visitType,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup
    List<Visit> findByPlannedVisitDateRangeAndActualVisitDate(
        @LookupField(name = "dateProjected") Range<LocalDate> dateRange,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

    @Lookup
    List<Visit> findByPlannedVisitDateRangeAndTypeAndActualVisitDate(
        @LookupField(name = "dateProjected") Range<LocalDate> dateProjectedRange,
        @LookupField(name = "type") VisitType visitType,
        @LookupField(name = "date", customOperator = Constants.Operators.EQ) LocalDate date);

}
