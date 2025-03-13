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

  /**
   * 지정된 Locale에 맞는 ResourceBundle에서 해당 국가의 표기명을 반환합니다.
   * 예를 들어, Locale.KOREAN을 전달하면 nationalities_ko.properties 파일에서 값을 읽어옵니다.
   */
  public String getDisplayName(Locale locale) {
    ResourceBundle bundle = ResourceBundle.getBundle("nationalities", locale);
    return bundle.getString(this.code);
  }
}

