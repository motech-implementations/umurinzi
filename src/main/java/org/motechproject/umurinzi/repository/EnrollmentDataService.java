package org.motechproject.umurinzi.repository;

import java.util.List;
import org.motechproject.umurinzi.domain.Enrollment;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.MotechDataService;

public interface EnrollmentDataService extends MotechDataService<Enrollment> {

    @Lookup(name = "Find By Participant Id")
    List<Enrollment> findBySubjectId(@LookupField(name = "externalId") String externalId);

    @Lookup(name = "Find By Participant Id")
    List<Enrollment> findBySubjectId(@LookupField(name = "externalId") String externalId,
        QueryParams queryParams);

    long countFindBySubjectId(@LookupField(name = "externalId") String externalId);

    @Lookup
    List<Enrollment> findByCampaignName(@LookupField(name = "campaignName") String campaignName);

    @Lookup(name = "Find By Participant Id And Campaign Name")
    Enrollment findBySubjectIdAndCampaignName(@LookupField(name = "externalId") String externalId,
        @LookupField(name = "campaignName") String campaignName);
}
