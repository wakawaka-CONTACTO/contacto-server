package org.kiru.core.user.userPurpose.domain;

import lombok.Getter;

@Getter
public enum PurposeType {
    GET_ALONG_WITH_U(0),
    WANT_TO_COLLABORATE(1),
    WANNA_MAKE_NEW_BRAND(2),
    ART_RESIDENCY(3),
    GROUP_EXHIBITION(4);

    private final int index;

    PurposeType(int index) {
        this.index = index;
    }

    public static PurposeType fromIndex(int index) {
        for (PurposeType type : PurposeType.values()) {
            if (type.getIndex() == index) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }
}