package com.epam.gym_crm.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
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

    private List<String> trainerUsernames;
}
