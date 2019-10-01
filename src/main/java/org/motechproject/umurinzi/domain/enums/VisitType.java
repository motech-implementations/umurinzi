package org.motechproject.umurinzi.domain.enums;

import lombok.Getter;

public enum VisitType {
    PRIME_VACCINATION_DAY("D0 Prime Vaccination"),
    D1_VISIT("D1 Sub-study visit"),
    D3_VISIT("D3 Sub-study visit"),
    D7_VISIT("D7 Phone call"),
    D28_VISIT("D28 Phone call"),
    BOOST_VACCINATION_DAY("D56 Boost Vaccination"),
    D57_VISIT("D57 Sub-study visit"),
    D59_VISIT("D59 Sub-study visit"),
    D63_VISIT("D63 Phone call"),
    D77_VISIT("D77 Physical visit"),
    D84_VISIT("D84 Phone call"),
    D180_VISIT("D180 Phone call"),
    D365_VISIT("D365 Physical visit"),
    D720_VISIT("D720 Phone call");

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
