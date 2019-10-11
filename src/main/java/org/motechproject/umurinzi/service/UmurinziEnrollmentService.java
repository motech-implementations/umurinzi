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

    void unenrollAndRemoveEnrollment(Visit visit);

    void enrollOrReenrollCampaignCompletedCampaign(Subject subject);

    void removeCampaignCompletedCampaign(String subjectId);
}
