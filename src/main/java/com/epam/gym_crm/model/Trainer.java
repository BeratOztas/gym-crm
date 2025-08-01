package com.epam.gym_crm.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "trainer")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class Trainer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "training_type_id",nullable = false)
	private TrainingType specialization;
	
	@OneToOne(optional = false, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "user_id",nullable = false,unique = true)
	private User user;
	
	@ManyToMany(mappedBy = "trainers")
    private Set<Trainee> trainees = new HashSet<>();

	@OneToMany(mappedBy = "trainer") 
	private Set<Training> trainings = new HashSet<>();
}
