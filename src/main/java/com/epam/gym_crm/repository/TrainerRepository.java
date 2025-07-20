package com.epam.gym_crm.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.epam.gym_crm.model.Trainer;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {

}
