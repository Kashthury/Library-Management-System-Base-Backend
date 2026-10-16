package com.library.lms.enums;

import com.library.lms.exception.BadRequestException;

public enum Gender {
    M("Male"),
    F("Female");

    private final String label;

    Gender(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Parses M/F or Male/Female (case-insensitive). */
    public static Gender parse(String code) {
        if (code != null) {
            String value = code.trim();
            if (value.equalsIgnoreCase("M") || value.equalsIgnoreCase("Male")) return M;
            if (value.equalsIgnoreCase("F") || value.equalsIgnoreCase("Female")) return F;
        }
        throw new BadRequestException("Invalid gender '" + code + "'. Allowed values: Male or Female");
    }
}
