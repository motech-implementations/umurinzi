package org.motechproject.umurinzi.service;

import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.domain.Visit;

public interface UmurinziEnrollmentService {

    void enrollSubject(String subjectId);

    void enrollSubjectToCampaign(String subjectId, String campaignName);

    void enrollOrReenrollSubject(Subject subject);

    void unenrollSubject(String subjectId);

    void unenrollSubject(String subjectId, String campaignName);

    void completeCampaign(Visit visit);

    void completeCampaign(String subjectId, String campaignName);

    void createEnrollmentOrReenrollCampaign(Visit visit, boolean rollbackCompleted);

    void unenrollAndRemoveEnrollment(Visit visit);
}
