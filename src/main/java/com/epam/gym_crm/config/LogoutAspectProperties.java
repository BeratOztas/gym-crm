package com.epam.gym_crm.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "app.logout.pointcut")
public class LogoutAspectProperties {

    private String basePackage;
    private List<String> excludedMethods;

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public List<String> getExcludedMethods() {
        return excludedMethods;
    }

    public void setExcludedMethods(List<String> excludedMethods) {
        this.excludedMethods = excludedMethods;
    }
}
