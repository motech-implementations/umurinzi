package org.motechproject.umurinzi.domain.enums;

import lombok.Getter;

public enum VisitType {
    PRIME_VACCINATION_DAY("D0 Prime Vaccination"),
    BOOST_VACCINATION_DAY("D56 Boost Vaccination");

    @Getter
    private String displayValue;

    VisitType(String displayValue) {
        this.displayValue = displayValue;
    }

    public static VisitType getByValue(String value) {
        for (VisitType visitType : VisitType.values()) {
            if (visitType.getDisplayValue().equalsIgnoreCase(value)) {
                return visitType;
            }
        }
        return null;
    }
}
