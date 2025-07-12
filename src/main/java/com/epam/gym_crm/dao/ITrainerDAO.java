package com.epam.gym_crm.dao;

import java.util.Optional;

import com.epam.gym_crm.model.Trainer;

public interface ITrainerDAO extends BaseDAO<Trainer> {
	Optional<Trainer> findByUsername(String username);
}
