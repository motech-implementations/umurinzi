package org.motechproject.umurinzi.service.impl;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.motechproject.mds.dto.AdvancedSettingsDto;
import org.motechproject.mds.dto.EntityDto;
import org.motechproject.mds.dto.LookupDto;
import org.motechproject.mds.query.QueryParams;
import org.motechproject.mds.service.EntityService;
import org.motechproject.mds.service.MDSLookupService;
import org.motechproject.umurinzi.exception.UmurinziLookupException;
import org.motechproject.umurinzi.service.LookupService;
import org.motechproject.umurinzi.web.domain.GridSettings;
import org.motechproject.umurinzi.web.domain.Records;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("lookupService")
public class LookupServiceImpl implements LookupService {

    @Autowired
    private EntityService entityService;

    @Autowired
    private MDSLookupService mdsLookupService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> List<T> getEntities(Class<T> entityType, GridSettings settings, QueryParams queryParams) {
        String lookup = settings.getLookup();
        String lookupFields = settings.getFields();

        if (StringUtils.isNotBlank(lookup)) {
            try {
                if (queryParams != null) {
                    return mdsLookupService.findMany(entityType.getName(), lookup, getFields(lookupFields), queryParams);
                } else {
                    return mdsLookupService.findMany(entityType.getName(), lookup, getFields(lookupFields));
                }
            } catch (IOException e) {
                throw new UmurinziLookupException("Invalid lookup fields: " + lookupFields, e);
            }
        }

        if (queryParams != null) {
            return mdsLookupService.retrieveAll(entityType.getName(), queryParams);
        } else {
            return mdsLookupService.retrieveAll(entityType.getName());
        }
    }

    @Override
    public <T> Records<T> getEntities(String entityClassName, String lookup, String lookupFields, QueryParams queryParams) {
        List<T> entities;
        long recordCount;
        int rowCount;
        QueryParams newQueryParams = queryParams;
        if (StringUtils.isNotBlank(lookup) && newQueryParams != null) {
            try {
                entities = mdsLookupService.findMany(entityClassName, lookup, getFields(lookupFields), newQueryParams);
                recordCount = mdsLookupService.count(entityClassName, lookup, getFields(lookupFields));
            } catch (IOException e) {
                throw new UmurinziLookupException("Invalid lookup fields: " + lookupFields, e.getCause());
            }

            rowCount = getRowCount(newQueryParams, recordCount);

            if (newQueryParams.getPage() == null) {
                newQueryParams = new QueryParams(1, newQueryParams.getPageSize(), newQueryParams.getOrderList());
            }

            if (entities == null) {
                entities = new ArrayList<>();
            }

            return new Records<>(newQueryParams.getPage(), rowCount, (int) recordCount, entities);
        }

        recordCount = mdsLookupService.countAll(entityClassName);

        int page;
        if (newQueryParams != null && newQueryParams.getPageSize() != null && newQueryParams.getPage() != null) {
            rowCount = (int) Math.ceil(recordCount / (double) newQueryParams.getPageSize());
            page = newQueryParams.getPage();
            entities = mdsLookupService.retrieveAll(entityClassName, newQueryParams);
        } else {
            rowCount = (int) recordCount;
            page = 1;
            entities = mdsLookupService.retrieveAll(entityClassName, newQueryParams);
        }

        if (entities == null) {
            entities = new ArrayList<>();
        }

        return new Records<>(page, rowCount, (int) recordCount, entities);
    }

    @Override
    public <T> Records<T> getEntities(Class<T> entityDtoType, Class<?> entityType, String lookup,
                                      String lookupFields, QueryParams queryParams) {
        List<T> entityDtoList = new ArrayList<>();
        Records baseRecords = getEntities(entityType, lookup, lookupFields, queryParams);
        Constructor<T> reportDtoConstructor;
        try {
            reportDtoConstructor = entityDtoType.getConstructor(entityType);
        } catch (NoSuchMethodException e) {
            throw new UmurinziLookupException("Invalid reportDtoType parametr", e);
        }
        try {
            for (Object entity : baseRecords.getRows()) {
                T entityDto;
                entityDto = reportDtoConstructor.newInstance(entity);
                entityDtoList.add(entityDto);
            }
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            throw new UmurinziLookupException("Can not create: " + entityDtoType.getName() + " using: " + entityType.getName(), e);
        }
        return new Records<>(baseRecords.getPage(), baseRecords.getTotal(), baseRecords.getRecords(), entityDtoList);
    }

    @Override
    public <T> Records<T> getEntities(Class<T> entityType, String lookup, String lookupFields, QueryParams queryParams) {
        return getEntities(entityType.getName(), lookup, lookupFields, queryParams);
    }

    @Override
    public <T> long getEntitiesCount(Class<T> entityType, String lookup, String lookupFields) {
        try {
            if (StringUtils.isBlank(lookup) || StringUtils.isBlank(lookupFields)) {
                return mdsLookupService.countAll(entityType.getName());
            }

            return mdsLookupService.count(entityType.getName(), lookup, getFields(lookupFields));
        } catch (IOException e) {
            throw new UmurinziLookupException("Invalid lookup fields: " + lookupFields, e.getCause());
        }
    }

    @Override
    public List<LookupDto> getAvailableLookups(String entityName) {
        EntityDto entity = getEntityByEntityClassName(entityName);
        AdvancedSettingsDto settingsDto = entityService.getAdvancedSettings(entity.getId(), true);
        return settingsDto.getIndexes();
    }

    private EntityDto getEntityByEntityClassName(String entityName) {
        EntityDto entity = entityService.getEntityByClassName(entityName);
        if (entity == null) {
            throw new UmurinziLookupException("Can not find entity named: " + entityName);
        }
        return entity;
    }

    private Map<String, Object> getFields(String lookupFields) throws IOException {
        return objectMapper.readValue(lookupFields, new TypeReference<HashMap>() {
        }); //NO CHECKSTYLE WhitespaceAround
    }

    private int getRowCount(QueryParams newQueryParams, long recordCount) {
        if (newQueryParams.getPageSize() != null) {
            return (int) Math.ceil(recordCount / (double) newQueryParams.getPageSize());
        } else {
            return (int) recordCount;
        }
    }

}
