package com.poly.ASM.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.time.Duration;
import java.util.Locale;

@Configuration
public class I18nConfig implements WebMvcConfigurer {

    // 1. Khai báo file nguồn (messages_xx.properties)
    @Bean("messageSource")
    public MessageSource getMessageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasename("classpath:i18n/messages"); // Đường dẫn đến file
        ms.setDefaultEncoding("UTF-8");
        return ms;
    }

    // 2. Lưu ngôn ngữ đã chọn vào Cookie (để lần sau vào web vẫn nhớ)
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("language");
        resolver.setDefaultLocale(new Locale("vi")); // Mặc định là Tiếng Việt
        resolver.setCookieMaxAge(Duration.ofDays(30)); // Nhớ trong 30 ngày
        return resolver;
    }

    // 3. Interceptor để bắt tham số ?lang=en hoặc ?lang=vi trên URL
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang"); // Tham số trên URL là 'lang'
        return lci;
    }

    // 4. Đăng ký Interceptor
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}