package org.kiru.core.user.user.domain;

import java.util.Locale;
import java.util.ResourceBundle;

public enum Nationality {
  UK("UK"),
  CN("CN"),
  JP("JP"),
  US("US"),
  KR("KR"),
  FR("FR"),
  DE("DE"),
  NL("NL"),
  OTHER("OTHER");

  private final String code;

  Nationality(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }

  public String getDisplayName(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("nationalities", locale);
    return bundle.getString(this.code);
  }
}

