package com.epam.gym_crm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.model.Trainer;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {
	Optional<Trainer> findByUserUsername(String username);

	List<Trainer> findByUserIsActive(boolean isActive);
}
