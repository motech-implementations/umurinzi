package org.motechproject.umurinzi.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.joda.time.LocalDate;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.domain.VisitScheduleOffset;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.repository.VisitDataService;
import org.motechproject.umurinzi.service.ConfigService;
import org.motechproject.umurinzi.service.UmurinziEnrollmentService;
import org.motechproject.umurinzi.service.VisitScheduleOffsetService;
import org.motechproject.umurinzi.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link org.motechproject.umurinzi.service.VisitService} interface. Uses
 * {@link org.motechproject.umurinzi.repository.VisitDataService} in order to retrieve and persist records.
 */
@Service("visitService")
public class VisitServiceImpl implements VisitService {

    private static final Logger LOGGER = LoggerFactory.getLogger(VisitServiceImpl.class);

    @Autowired
    private VisitDataService visitDataService;

    @Autowired
    private VisitScheduleOffsetService visitScheduleOffsetService;

    @Autowired
    private UmurinziEnrollmentService umurinziEnrollmentService;

    @Autowired
    private ConfigService configService;

    @Override
    public Visit create(Visit visit) {
        return visitDataService.create(visit);
    }

    @Override
    public Visit update(Visit visit) {
        return visitDataService.update(visit);
    }

    @Override
    public void delete(Visit visit) {
        visitDataService.delete(visit);
    }

    @Override
    public void createVisitsForSubject(Subject subject) {
        for (VisitType visitType : VisitType.values()) {
            if ((subject.getSubStudy() != null && subject.getSubStudy()) || !subStudyVisit(visitType)) {
                subject.addVisit(visitDataService.create(new Visit(subject, visitType)));
            }
        }

        calculateVisitsPlannedDates(subject);
    }

    @Override
    public void calculateVisitsPlannedDates(Subject subject) {
        LocalDate primeVacDate = subject.getPrimerVaccinationDate();
        LocalDate boostVacDate = subject.getBoosterVaccinationDate();

        if (primeVacDate != null) {
            Map<VisitType, VisitScheduleOffset> offsetMap = visitScheduleOffsetService.getAllAsMap();

            if (boostVacDate == null) {
                VisitScheduleOffset offset = offsetMap.get(VisitType.BOOST_VACCINATION_DAY);

                boostVacDate = primeVacDate.plusDays(offset.getTimeOffset());
            }

            for (Visit visit : subject.getVisits()) {
                if (VisitType.PRIME_VACCINATION_DAY.equals(visit.getType())) {
                    visit.setDate(primeVacDate);
                } else if (VisitType.BOOST_VACCINATION_DAY.equals(visit.getType())
                    && subject.getBoosterVaccinationDate() != null) {
                    visit.setDate(boostVacDate);

                    umurinziEnrollmentService.completeCampaign(visit);
                } else if (visit.getDate() == null) {
                    VisitScheduleOffset offset = offsetMap.get(visit.getType());

                    if (boostRelatedVisit(visit.getType())) {
                        visit.setDateProjected(boostVacDate.plusDays(offset.getTimeOffset()));
                    } else {
                        visit.setDateProjected(primeVacDate.plusDays(offset.getTimeOffset()));
                    }
                }

                visitDataService.update(visit);
            }

            umurinziEnrollmentService.enrollOrReenrollSubject(subject);
        }
    }

    @Override
    public void recalculateBoostRelatedVisitsPlannedDates(Subject subject) {
        LocalDate boostVacDate = subject.getBoosterVaccinationDate();
        LocalDate primeVacDate = subject.getPrimerVaccinationDate();

        if (primeVacDate != null) {
            Map<VisitType, VisitScheduleOffset> offsetMap = visitScheduleOffsetService.getAllAsMap();

            if (boostVacDate == null) {
                VisitScheduleOffset offset = offsetMap.get(VisitType.BOOST_VACCINATION_DAY);
                boostVacDate = primeVacDate.plusDays(offset.getTimeOffset());
            }

            for (Visit visit : subject.getVisits()) {
                if (VisitType.BOOST_VACCINATION_DAY.equals(visit.getType())
                    && subject.getBoosterVaccinationDate() != null) {
                    visit.setDate(boostVacDate);

                    umurinziEnrollmentService.completeCampaign(visit);
                } else if (boostRelatedVisit(visit.getType()) && visit.getDate() == null) {
                    VisitScheduleOffset offset = offsetMap.get(visit.getType());
                    visit.setDateProjected(boostVacDate.plusDays(offset.getTimeOffset()));
                }

                visitDataService.update(visit);
            }

            umurinziEnrollmentService.enrollOrReenrollSubject(subject);
        }
    }

    @Override
    public void removeSubStudyVisits(Subject subject) {
        Iterator<Visit> visitIterator = subject.getVisits().iterator();

        while (visitIterator.hasNext()) {
            Visit visit = visitIterator.next();

            if (visit.getDate() == null) {
                umurinziEnrollmentService.unenrollAndRemoveEnrollment(visit);

                subject.removeVisit(visit);

                visitDataService.delete(visit);
            }
        }
    }

    @Override
    public void createSubStudyVisits(Subject subject) {
    }

    public void removeVisitsPlannedDates(Subject subject) {
        for (Visit visit: subject.getVisits()) {
            if (VisitType.PRIME_VACCINATION_DAY.equals(visit.getType())) {
                visit.setDate(null);
            } else {
                visit.setDateProjected(null);
            }

            umurinziEnrollmentService.unenrollAndRemoveEnrollment(visit);
            visitDataService.update(visit);
        }
    }

    private boolean subStudyVisit(VisitType visitType) {
        List<String> subStudyVisits = configService.getConfig().getSubStudyVisits();

        if (subStudyVisits == null || subStudyVisits.isEmpty()) {
            return false;
        }

        return subStudyVisits.contains(visitType.getDisplayValue());
    }

    private boolean boostRelatedVisit(VisitType visitType) {
        List<String> boostRelatedVisits = configService.getConfig().getBoosterRelatedVisits();

        if (boostRelatedVisits == null || boostRelatedVisits.isEmpty()) {
            return false;
        }

        return boostRelatedVisits.contains(visitType.getDisplayValue());
    }
}
