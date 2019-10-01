package org.motechproject.umurinzi.service;

import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.Visit;

/**
 * Service interface for CRUD on Visit
 */
public interface VisitService {

    Visit create(Visit visit);

    Visit update(Visit visit);

    void delete(Visit visit);

    void createVisitsForSubject(Subject subject);

    void calculateVisitsPlannedDates(Subject subject);

    void recalculateBoostRelatedVisitsPlannedDates(Subject subject);

    void removeSubStudyVisits(Subject subject);

    void createSubStudyVisits(Subject subject);

    void removeVisitsPlannedDates(Subject subject);
}
