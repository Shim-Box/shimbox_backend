package sansam.shimbox.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sansam.shimbox.global.common.LabelEnumConverterFactory;
import sansam.shimbox.global.security.CurrentUserArgumentResolver;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    private final LabelEnumConverterFactory labelEnumConverterFactory;
    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        log.info(">> Registering LabelEnumConverterFactory");
        registry.addConverterFactory(labelEnumConverterFactory);
    }

    @PostConstruct
    public void debug() {
        System.out.println("✅ WebConfig 실행됨");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CurrentUserArgumentResolver());
    }
}