package com.epam.gym_crm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.epam.gym_crm.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	 Optional<User> findByUsername(String username);
	 
}
