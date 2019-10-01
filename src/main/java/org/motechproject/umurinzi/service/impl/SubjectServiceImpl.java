package org.motechproject.umurinzi.service.impl;

import java.util.Objects;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.repository.SubjectDataService;
import org.motechproject.umurinzi.service.SubjectService;
import org.motechproject.umurinzi.service.VisitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override
    public Subject findSubjectBySubjectId(String subjectId) {
        return subjectDataService.findBySubjectId(subjectId);
    }

    @Override
    public Subject create(Subject subject) {
        subjectDataService.create(subject);
        visitService.createVisitsForSubject(subject);

        return subject;
    }

    @Override
    public Subject update(Subject subject) {
        subjectDataChanged(subject);

        return subjectDataService.update(subject);
    }

    //CHECKSTYLE:OFF: checkstyle:cyclomaticcomplexity
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    @Override
    public void subjectDataChanged(Subject subject) {
        Subject oldSubject = findSubjectBySubjectId(subject.getSubjectId());

        if (oldSubject != null) {
            if ((oldSubject.getSubStudy() == null || !oldSubject.getSubStudy())
                && subject.getSubStudy() != null && subject.getSubStudy()) {
                visitService.createSubStudyVisits(oldSubject);
            } else if ((subject.getSubStudy() == null || !subject.getSubStudy())
                && oldSubject.getSubStudy() != null && oldSubject.getSubStudy()) {
                visitService.removeSubStudyVisits(oldSubject);
            }

            if (oldSubject.getPrimerVaccinationDate() != null && subject.getPrimerVaccinationDate() == null) {
                oldSubject.setPrimerVaccinationDate(subject.getPrimerVaccinationDate());
                visitService.removeVisitsPlannedDates(oldSubject);
            } else if (!Objects.equals(oldSubject.getPrimerVaccinationDate(), subject.getPrimerVaccinationDate())) {
                oldSubject.setPrimerVaccinationDate(subject.getPrimerVaccinationDate());
                visitService.calculateVisitsPlannedDates(oldSubject);
            }

            if (!Objects.equals(oldSubject.getBoosterVaccinationDate(), subject.getBoosterVaccinationDate())) {
                oldSubject.setPrimerVaccinationDate(subject.getPrimerVaccinationDate());
                oldSubject.setBoosterVaccinationDate(subject.getBoosterVaccinationDate());
                visitService.recalculateBoostRelatedVisitsPlannedDates(oldSubject);
            }
        }
    }
    //CHECKSTYLE:ON: checkstyle:cyclomaticcomplexity
}
