package org.kiru.user.config;


import org.kiru.core.user.userPurpose.domain.PurposeType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class IntegerBinder
        implements Converter<Integer, PurposeType> {

    @Override
    public PurposeType convert(Integer source) {
        return PurposeType.fromIndex(source);
    }
}
