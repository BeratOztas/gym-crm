package com.epam.gym_crm.dto.request;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCreateRequest {

    @NotBlank(message = "Training name cannot be blank")
    private String trainingName;

    @NotNull(message = "Training date cannot be null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate trainingDate;

    @NotNull(message = "Training duration cannot be null")
    @Positive(message = "Training duration must be a positive number")
    private Integer trainingDuration;

    @NotBlank(message = "Trainer username cannot be blank for training creation")
    private String trainerUsername;

    @NotBlank(message = "Trainee username cannot be blank for training creation")
    private String traineeUsername;

    @NotBlank(message = "Training type name cannot be blank for training creation") 
    private String trainingTypeName;
}
