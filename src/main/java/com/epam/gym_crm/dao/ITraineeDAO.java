package com.epam.gym_crm.dao;

import java.util.Optional;

import com.epam.gym_crm.model.Trainee;

public interface ITraineeDAO extends BaseDAO<Trainee> {
	Optional<Trainee> findByUsername(String username);
}
