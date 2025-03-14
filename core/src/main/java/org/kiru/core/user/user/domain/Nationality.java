package org.kiru.core.user.user.domain;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.kiru.core.exception.code.FailureCode;

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
    try{
      ResourceBundle bundle = ResourceBundle.getBundle("nationalities", locale);
      return bundle.getString(this.code);
    } catch (MissingResourceException e){
      throw new IllegalArgumentException(String.valueOf(FailureCode.NATIONALITY_NOT_PROVIDED));
    }
  }
}

