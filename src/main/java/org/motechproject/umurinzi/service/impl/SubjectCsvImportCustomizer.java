package org.motechproject.umurinzi.service.impl;

import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.motechproject.umurinzi.domain.Subject;
import org.motechproject.umurinzi.service.SubjectService;
import org.motechproject.mds.service.DefaultCsvImportCustomizer;
import org.motechproject.mds.service.MotechDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SubjectCsvImportCustomizer extends DefaultCsvImportCustomizer {

    private SubjectService subjectService;

    @Override
    public Object findExistingInstance(Map<String, String> row, MotechDataService motechDataService) {
        String subjectId = row.get(Subject.SUBJECT_ID_FIELD_NAME);

        if (StringUtils.isNotBlank(subjectId)) {
            return subjectService.findSubjectBySubjectId(subjectId);
        }

        subjectId = row.get(Subject.SUBJECT_ID_FIELD_DISPLAY_NAME);

        if (StringUtils.isNotBlank(subjectId)) {
            return subjectService.findSubjectBySubjectId(subjectId);
        }

        return null;
    }

    @Override
    public Object doCreate(Object instance, MotechDataService motechDataService) {
        return subjectService.create((Subject) instance);
    }

    @Override
    public Object doUpdate(Object instance, MotechDataService motechDataService) {
        return subjectService.update((Subject) instance);
    }

    @Autowired
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
}
