package com.epam.gym_crm.config;

import java.util.stream.Collectors;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {

	private final LogoutAspectProperties properties;

    public AopConfig(LogoutAspectProperties properties) {
        this.properties = properties;
    }

    @Bean
    public AspectJExpressionPointcut logoutPointcut() {
        String baseExecution = "execution(* " + properties.getBasePackage() + ".*ServiceImpl.*(..))";

        String excludedExecution = properties.getExcludedMethods().stream()
                .map(method -> "execution(* " + properties.getBasePackage() + "." + method + "(..))")
                .collect(Collectors.joining(" || "));

        String finalPointcutExpression = baseExecution + " && !" + "(" + excludedExecution + ")";
        

        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(finalPointcutExpression);
        return pointcut;
    }
}
