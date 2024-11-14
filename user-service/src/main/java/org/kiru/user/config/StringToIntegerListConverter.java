package org.kiru.user.config;

import java.util.Arrays;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToIntegerListConverter implements Converter<String, List<Integer>> {

    @Override
    public List<Integer> convert(String source) {
        return Arrays.stream(source.replaceAll("[\\[\\]]", "").split(","))
                .map(Integer::parseInt)
                .toList();
    }
}