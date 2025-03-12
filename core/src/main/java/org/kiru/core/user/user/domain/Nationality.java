package org.kiru.core.user.user.domain;

import java.util.Locale;
import java.util.ResourceBundle;

public enum Nationality {
  UK("UK"),
  CHINA("CN"),
  JAPAN("JP"),
  USA("US"),
  KOREA("KR"),
  FRANCE("FR"),
  GERMANY("DE"),
  NETHERLANDS("NL"),
  OTHER("OTHER");

  private final String code;

  Nationality(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public String getLocalizedName(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("nationalities", locale);
    return bundle.getString(code);
  }
}
