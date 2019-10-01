package org.motechproject.umurinzi.repository;

import org.motechproject.umurinzi.domain.VotoMessage;
import org.motechproject.mds.annotations.Lookup;
import org.motechproject.mds.annotations.LookupField;
import org.motechproject.mds.service.MotechDataService;

/**
 * Interface for repository that persists simple records and allows CRUD.
 * MotechDataService base class will provide the implementation of this class as well
 * as methods for adding, deleting, saving and finding all instances.  In this class we
 * define and custom lookups we may need.
 */
public interface VotoMessageDataService extends MotechDataService<VotoMessage> {

    @Lookup
    VotoMessage findByMessageKey(@LookupField(name = "messageKey") String messageKey);

    @Lookup
    VotoMessage findByIvrId(@LookupField(name = "votoIvrId") String votoIvrId);

    @Lookup
    VotoMessage findBySmsId(@LookupField(name = "votoSmsId") String votoSmsId);
}
