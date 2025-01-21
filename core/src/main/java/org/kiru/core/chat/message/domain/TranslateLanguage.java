package org.kiru.core.chat.message.domain;

public enum TranslateLanguage {
    KO,EN,JA,ZH;

    /**
     * Converts a language code string to its corresponding TranslateLanguage enum constant.
     *
     * @param language The language code to convert (must exactly match one of the enum constants: KO, EN, JA, ZH)
     * @return The TranslateLanguage enum constant representing the specified language
     * @throws IllegalArgumentException If the provided language code does not match any defined enum constant
     */
    public static TranslateLanguage of(String language) {
        return TranslateLanguage.valueOf(language);
    }
}
