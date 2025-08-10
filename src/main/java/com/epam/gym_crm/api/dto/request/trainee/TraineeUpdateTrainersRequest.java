package com.epam.gym_crm.api.dto.request.trainee;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TraineeUpdateTrainersRequest {

    @NotBlank(message = "Trainee username cannot be blank")
    private String traineeUsername;

    @NotNull(message = "Trainers list cannot be null")
    @NotEmpty(message = "Trainers list cannot be empty")
    private List<String> trainerUsernames;
}
