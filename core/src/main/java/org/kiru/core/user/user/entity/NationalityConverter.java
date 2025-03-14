package org.kiru.core.user.user.entity;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import org.kiru.core.user.user.domain.Nationality;

public class NationalityConverter implements Converter<Nationality, String> {

  @Override
  public String convert(Nationality source) {
    return source == null ? null : source.getCode();
  }

  @Override
  public JavaType getInputType(TypeFactory typeFactory) {
    return typeFactory.constructType(Nationality.class);
  }

  @Override
  public JavaType getOutputType(TypeFactory typeFactory) {
    return typeFactory.constructType(String.class);
  }
}
