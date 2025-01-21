package org.kiru.core.chat.message.domain;

public enum TranslateLanguage {
    KO,EN,JA,ZH;

    public static TranslateLanguage of(String language) {
        return TranslateLanguage.valueOf(language);
    }
}
