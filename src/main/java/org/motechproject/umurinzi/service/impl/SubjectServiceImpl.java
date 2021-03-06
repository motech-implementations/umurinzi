package org.motechproject.umurinzi.service.impl;

import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.domain.enums.VisitType;
import org.motechproject.umurinzi.helper.IvrHelper;
import org.motechproject.umurinzi.repository.SubjectDataService;
import org.motechproject.umurinzi.service.SubjectService;
import org.motechproject.umurinzi.service.UmurinziEnrollmentService;
import org.motechproject.umurinzi.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 * Implementation of the {@link org.motechproject.umurinzi.service.SubjectService} interface. Uses
 * {@link org.motechproject.umurinzi.repository.SubjectDataService} in order to retrieve and persist records.
 */
@Service("subjectService")
public class SubjectServiceImpl implements SubjectService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubjectServiceImpl.class);

    @Autowired
    private SubjectDataService subjectDataService;

    @Autowired
    private VisitService visitService;

    @Autowired
    private UmurinziEnrollmentService umurinziEnrollmentService;

    @Autowired
    private IvrHelper ivrHelper;

    @Override
    public Subject findSubjectBySubjectId(String subjectId) {
        return subjectDataService.findBySubjectId(subjectId);
    }

    @Override
    public Subject create(Subject subject) {
        LOGGER.debug("Creating IVR subscriber for subject with id: {} ", subject.getSubjectId());

        String ivrId = ivrHelper.createSubscriber(subject);
        subject.setIvrId(ivrId);
        LOGGER.debug("IVR subscriber with id: {} created for subject {}", ivrId, subject.getSubjectId());

        subjectDataService.create(subject);
        LOGGER.debug("Subject with id: {} created", subject.getSubjectId());

        visitService.createVisitsForSubject(subject);
        LOGGER.debug("Visits for subject with id: {} created", subject.getSubjectId());

        return subject;
    }

    @Override
    public Subject update(Subject subject) {
        subjectDataChanged(subject);

        return subjectDataService.update(subject);
    }

    @Override
    public Subject update(Subject subject, Subject oldSubject) {
        subjectDataChanged(subject, oldSubject, subject);

        return subjectDataService.update(subject);
    }

    @Override
    public void subjectDataChanged(final Subject subject) {
        subjectDataService.doInTransaction(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Subject oldSubject = subjectDataService.findBySubjectId(subject.getSubjectId());
                subjectDataChanged(subject, oldSubject, oldSubject);
                subject.setEnrollment(oldSubject.getEnrollment());
            }
        });
    }

    private void subjectDataChanged(Subject newSubject, Subject oldSubject, Subject subject) { //NO CHECKSTYLE CyclomaticComplexity
        if (oldSubject != null) {
            if (oldSubject.getTransferSubjectId() == null && newSubject.getTransferSubjectId() != null) {
                Visit booster = null;

                for (Visit visit : subject.getVisits()) {
                    if (VisitType.BOOST_VACCINATION_DAY.equals(visit.getType())) {
                        booster = visit;
                    }
                }

                if (booster != null) {
                    umurinziEnrollmentService.unenrollAndRemoveEnrollment(booster);
                    visitService.delete(booster);
                }
            }

            if (oldSubject.getPrimeVaccinationDate() != null && newSubject.getPrimeVaccinationDate() == null) {
                subject.setPrimeVaccinationDate(newSubject.getPrimeVaccinationDate());
                visitService.removeVisitsPlannedDates(subject);
                umurinziEnrollmentService.unenrollAndRemoveEnrollment(subject);
            } else if (!Objects.equals(oldSubject.getPrimeVaccinationDate(), newSubject.getPrimeVaccinationDate())) {
                subject.setPrimeVaccinationDate(newSubject.getPrimeVaccinationDate());
                visitService.calculateVisitsPlannedDates(subject);
            }

            if (!Objects.equals(oldSubject.getBoostVaccinationDate(), newSubject.getBoostVaccinationDate())) {
                subject.setPrimeVaccinationDate(newSubject.getPrimeVaccinationDate());
                subject.setBoostVaccinationDate(newSubject.getBoostVaccinationDate());
                visitService.recalculateBoostRelatedVisitsPlannedDates(subject);
            }

            updateEnrollmentsAfterUpdate(newSubject, oldSubject, subject);
            updateSubscriber(newSubject, oldSubject);
        }
    }

    private void updateEnrollmentsAfterUpdate(Subject newSubject, Subject oldSubject, Subject subject) {
        if (StringUtils.isBlank(oldSubject.getPhoneNumber()) && StringUtils.isNotBlank(newSubject.getPhoneNumber())) {
            subject.setPrimeVaccinationDate(newSubject.getPrimeVaccinationDate());
            subject.setBoostVaccinationDate(newSubject.getBoostVaccinationDate());

            umurinziEnrollmentService.enrollOrReenrollSubject(subject);

            if (subject.getBoostVaccinationDate() != null) {
                umurinziEnrollmentService.enrollOrReenrollCampaignCompletedCampaign(subject);
            }
        } else if (StringUtils.isNotBlank(oldSubject.getPhoneNumber()) && StringUtils.isBlank(newSubject.getPhoneNumber())) {
            umurinziEnrollmentService.unenrollAndRemoveEnrollment(subject);
        }
    }

    private void updateSubscriber(Subject newSubject, Subject oldSubject) {
        if (!StringUtils.equals(oldSubject.getPhoneNumber(), newSubject.getPhoneNumber())
            || !StringUtils.equals(oldSubject.getName(), newSubject.getName())) {
            LOGGER.debug("Updating IVR subscriber for subject with id: {}", newSubject.getSubjectId());
            String ivrId = ivrHelper.updateSubscriber(newSubject);
            newSubject.setIvrId(ivrId);
            LOGGER.debug("IVR subscriber updated for subject with id: {}", newSubject.getSubjectId());
        }
    }
}
