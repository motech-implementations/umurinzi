package org.motechproject.umurinzi.domain.enums;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents language of Subject
 */
public enum Language {
    English("eng"),
    Luganda("lu"),
    Runyankole("ru");

    @Getter
    private String code;

    Language(String code) {
        this.code = code;
    }

    public static Language getByCode(String code) {
        for (Language language : Language.values()) {
            if (language.getCode().equals(code)) {
                return language;
            }
        }
        return null;
    }

    public static Set<String> getListOfCodes() {
        Set<String> codes = new HashSet<>();

        for (Language language : values()) {
            codes.add(language.getCode());
        }

        return Collections.unmodifiableSet(codes);
    }
}
