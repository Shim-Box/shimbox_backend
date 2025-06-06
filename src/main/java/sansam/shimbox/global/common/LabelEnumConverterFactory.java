package sansam.shimbox.global.common;

import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class LabelEnumConverterFactory implements ConverterFactory<String, LabelEnum> {

    @Override
    public <T extends LabelEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new LabelEnumConverter<>(targetType);
    }

    private static class LabelEnumConverter<T extends LabelEnum> implements Converter<String, T> {
        private final Class<T> enumType;

        public LabelEnumConverter(Class<T> enumType) {
            this.enumType = enumType;
        }

        @Override
        public T convert(String source) {
            return LabelEnum.fromLabel(enumType, source);
        }
    }
}