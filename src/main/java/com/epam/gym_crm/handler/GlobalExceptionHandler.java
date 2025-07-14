package com.epam.gym_crm.handler;

import java.util.stream.Collectors;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.epam.gym_crm.exception.BaseException;
import com.epam.gym_crm.exception.ErrorMessage;
import com.epam.gym_crm.exception.MessageType;

import jakarta.validation.ConstraintViolationException;

@Aspect // This class is an Aspect
@Component // Register this as a Spring Bean
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    
    @AfterThrowing(pointcut = "execution(* com.epam.gym_crm.service.impl.*.*(..))", throwing = "ex")
    public void handleServiceExceptions(JoinPoint joinPoint, Exception ex) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Check if the exception is one of our custom BaseExceptions
        if (ex instanceof BaseException) {
            // Since BaseException does not expose getMessageType(), we just log the message.
            // The message itself should be descriptive enough from the BaseException's constructor.
            logger.warn("Caught custom BaseException in {}.{}(): Message={}",
                        className, methodName, ex.getMessage(), ex);
            throw ex; // Re-throw the specific BaseException to propagate it
        } 
        // Handle specific validation exceptions (e.g., from Bean Validation)
        else if (ex instanceof ConstraintViolationException) {
            ConstraintViolationException validationEx = (ConstraintViolationException) ex;
            String violations = validationEx.getConstraintViolations().stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .collect(Collectors.joining("; "));

            logger.warn("Caught validation exception in {}.{}(): {}", className, methodName, violations, validationEx);
            
            // Wrap the ConstraintViolationException into our BaseException using ErrorMessage
            // This ensures all exceptions propagating from the AOP handler are of type BaseException.
            throw new BaseException(new ErrorMessage(MessageType.VALIDATION_ERROR, "Validation error: " + violations));
        } 
        // Catch all other unexpected exceptions
        else {
            logger.error("Caught unexpected exception in {}.{}(): {}", className, methodName, ex.getMessage(), ex);
            
            // Wrap any other unexpected exception into our generic BaseException using ErrorMessage
            throw new BaseException(new ErrorMessage(MessageType.GENERAL_EXCEPTION, "An unhandled error occurred: " + ex.getMessage()));
        }
    }
}