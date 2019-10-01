package org.motechproject.umurinzi.util;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.motechproject.umurinzi.domain.Visit;
import org.motechproject.umurinzi.util.serializer.CustomVisitListSerializer;

import java.util.List;

public abstract class SubjectVisitsMixin {

    @JsonSerialize(using = CustomVisitListSerializer.class)
    public abstract List<Visit> getVisits();
}
