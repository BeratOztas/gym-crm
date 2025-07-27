package com.epam.gym_crm.dto.request.training;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingUpdateRequest {

    @NotNull(message = "Training ID cannot be null for update")
    @Positive(message = "Training ID must be a positive number")
    private Long id; 

    private String trainingName;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate trainingDate;

    private Integer trainingDuration;

    private String trainerUsername; 
    private String traineeUsername; 

    private String trainingTypeName; 
}
