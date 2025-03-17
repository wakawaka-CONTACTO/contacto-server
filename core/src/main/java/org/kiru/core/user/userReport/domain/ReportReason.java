package org.kiru.core.user.userReport.domain;

import lombok.Getter;

@Getter
public enum ReportReason {
    SPAM(0, "Spam"),
    SEXUAL_OFFENSE(1, "Sexual offense against children and youth"),
    PROFANITY(2, "Profanity, violence, hatred"),
    ILLEGAL_PRODUCTS(3, "Illegal products or services"),
    ABNORMAL_SERVICE(4, "Abnormal service use"),
    FRAUD(5, "Fraud, identity theft"),
    OBSCENE_ACTS(6, "Obscene, sexual acts");

    private final int index;
    private final String description;

    ReportReason(int index, String description) {
        this.index = index;
        this.description = description;
    }

    public static ReportReason fromIndex(int index) {
        for (ReportReason reason : ReportReason.values()) {
            if (reason.getIndex() == index) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Invalid index: " + index);
    }
}