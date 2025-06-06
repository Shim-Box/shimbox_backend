package sansam.shimbox.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sansam.shimbox.global.common.LabelEnumConverterFactory;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    private final LabelEnumConverterFactory labelEnumConverterFactory;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        log.info(">> Registering LabelEnumConverterFactory");
        registry.addConverterFactory(labelEnumConverterFactory);
    }

    @PostConstruct
    public void debug() {
        System.out.println("✅ WebConfig 실행됨");
    }
}