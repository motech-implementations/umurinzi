package org.motechproject.umurinzi.domain.enums;

import org.joda.time.Period;

public enum EmailSchedulePeriod {
    DAILY(Period.days(1)),
    WEEKLY(Period.weeks(1)),
    MONTHLY(Period.months(1));

    private Period period;

    EmailSchedulePeriod(Period period) {
        this.period = period;
    }

    public Period getPeriod() {
        return period;
    }
}
