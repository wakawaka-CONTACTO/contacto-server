package org.kiru.core.user.user.entity;

import org.springframework.core.convert.converter.Converter;
import org.kiru.core.user.user.domain.Nationality;


public class NationalityConverter implements Converter<Nationality, String> {

  @Override
  public String convert(Nationality source) {
    return source == null ? null : source.getCode();
  }
}
