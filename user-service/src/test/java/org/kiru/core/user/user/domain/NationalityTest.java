package org.kiru.core.user.user.domain;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class NationalityTest {

  static final List<String> languageCodes = new ArrayList<>(List.of(
      "UK", "CN", "JP", "US", "KR", "FR", "DE", "NL", "OTHER"
  ));

  static final List<String> EnNationalKey = new ArrayList<>(List.of(
      "United Kingdom", "China", "Japan", "United States", "South Korea", "France", "Germany",
      "Netherlands", "Other"
  ));

  static final List<String> KoNationalKey = new ArrayList<>(List.of(
      "영국", "중국", "일본", "미국", "대한민국", "프랑스", "독일", "네덜란드", "기타"
  ));


  @DisplayName("[ResourceBundle] 영어로 표기된 국가명을 가져올 수 있다")
  @Test
  void use_English_bundle_all() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("nationalities_en");

    Map<String, String> expectedEnglish = Map.of(
        "UK", "United Kingdom",
        "CN", "China",
        "JP", "Japan",
        "US", "United States",
        "KR", "South Korea",
        "FR", "France",
        "DE", "Germany",
        "NL", "Netherlands",
        "OTHER", "Other"
    );

    expectedEnglish.entrySet().stream()
        .forEach(entry -> {
          String key = entry.getKey();
          String expectedValue = entry.getValue();
          String actualValue = resourceBundle.getString(key);
          Assertions.assertEquals(expectedValue, actualValue, "For key: " + key);
        });
  }

  @DisplayName("[ResourceBundle] 한국어로 표기된 국가명을 가져올 수 있다")
  @Test
  void use_Korean_bundle_all() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("nationalities_ko");
//    ResourceBundle resourceBundle = ResourceBundle.getBundle("nationalities_ko", new Locale("ko"), new UTF8Control());
    Map<String, String> expectedKorean = Map.of(
        "UK", "영국",
        "CN", "중국",
        "JP", "일본",
        "US", "미국",
        "KR", "대한민국",
        "FR", "프랑스",
        "DE", "독일",
        "NL", "네덜란드",
        "OTHER", "기타"
    );


    expectedKorean.entrySet().stream()
        .forEach(entry -> {
          String key = entry.getKey();
          String expectedValue = entry.getValue();
          String actualValue = resourceBundle.getString(key);
          Assertions.assertEquals(expectedValue, actualValue, "For key: " + key);
        });
  }

//  static class UTF8Control extends ResourceBundle.Control {
//    @Override
//    public ResourceBundle newBundle(String baseName, Locale locale, String format,
//        ClassLoader loader, boolean reload)
//        throws IOException {
//      String bundleName = toBundleName(baseName, locale);
//      String resourceName = toResourceName(bundleName, "properties");
//      try (InputStreamReader reader = new InputStreamReader(loader.getResourceAsStream(resourceName), StandardCharsets.UTF_8)) {
//        return new PropertyResourceBundle(reader);
//      }
//    }
//  }
}