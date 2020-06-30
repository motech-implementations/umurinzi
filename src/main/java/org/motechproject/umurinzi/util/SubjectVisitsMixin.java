package org.motechproject.umurinzi.util;

import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.motechproject.umurinzi.domain.Visit;

public abstract class SubjectVisitsMixin {

    @JsonIgnore
    public abstract List<Visit> getVisits();
}
