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

    private Subject oldSubject;

    @Override
    public Object findExistingInstance(Map<String, String> row, MotechDataService motechDataService) {
        oldSubject = null;
        String subjectId = row.get(Subject.SUBJECT_ID_FIELD_NAME);

        if (StringUtils.isNotBlank(subjectId)) {
            Subject subject = subjectService.findSubjectBySubjectId(subjectId);
            if (subject != null) {
                oldSubject = new Subject(subject);
            }
            return subject;
        }

        subjectId = row.get(Subject.SUBJECT_ID_FIELD_DISPLAY_NAME);

        if (StringUtils.isNotBlank(subjectId)) {
            Subject subject = subjectService.findSubjectBySubjectId(subjectId);
            if (subject != null) {
                oldSubject = new Subject(subject);
            }
            return subject;
        }

        return null;
    }

    @Override
    public Object doCreate(Object instance, MotechDataService motechDataService) {
        return subjectService.create((Subject) instance);
    }

    @Override
    public Object doUpdate(Object instance, MotechDataService motechDataService) {
        if (oldSubject != null && oldSubject.getSubjectId().equals(((Subject) instance).getSubjectId())) {
            return subjectService.update((Subject) instance, oldSubject);
        }
        return subjectService.update((Subject) instance);
    }

    @Autowired
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
}
