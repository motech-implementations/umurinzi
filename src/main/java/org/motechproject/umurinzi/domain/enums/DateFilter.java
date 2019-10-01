package org.motechproject.umurinzi.domain.enums;

import org.joda.time.LocalDate;
import org.motechproject.commons.api.Range;

public enum DateFilter {

    TODAY {
        @Override
        public Range<LocalDate> getRange() {
            LocalDate startDate = LocalDate.now();
            return new Range<>(startDate, startDate);
        }
    },

    TOMORROW {
        @Override
        public Range<LocalDate> getRange() {
            LocalDate startDate = LocalDate.now().plusDays(1);
            return new Range<>(startDate, startDate);
        }
    },

    THIS_WEEK {
        @Override
        public Range<LocalDate> getRange() {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(6); //NO CHECKSTYLE MagicNumber
            return new Range<>(startDate, endDate);
        }
    },

    NEXT_THREE_DAYS {
        @Override
        public Range<LocalDate> getRange() {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(2);
            return new Range<>(startDate, endDate);
        }
    },

    TWO_DAYS_AFTER {
        @Override
        public Range<LocalDate> getRange() {
            LocalDate startDate = LocalDate.now().plusDays(2);
            return new Range<>(startDate, startDate);
        }
    },

    DATE_RANGE {
        @Override
        public Range<LocalDate> getRange() {
            return null;
        }
    };

    public abstract Range<LocalDate> getRange();

}
