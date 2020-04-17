package org.motechproject.umurinzi.service;

import java.util.List;
import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.umurinzi.web.domain.Records;

public interface LookupService {

    <T> List<T> getEntities(Class<T> entityType, GridSettings settings, QueryParams queryParams);

    <T> Records<T> getEntities(Class<T> entityType, String lookup,
                               String lookupFields, QueryParams queryParams);

    <T> Records<T> getEntities(Class<T> entityDtoType, Class<?> entityType, String lookup,
                               String lookupFields, QueryParams queryParams);

    <T> Records<T> getEntities(String entityClassName, String lookup,
                               String lookupFields, QueryParams queryParams);

    <T> long getEntitiesCount(Class<T> entityType, String lookup, String lookupFields);

    List<LookupDto> getAvailableLookups(String entityName);
}
