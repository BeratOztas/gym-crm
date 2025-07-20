package com.epam.gym_crm.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trainee")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trainee  {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Past(message = "Date of birth must be in the past")
	@Column(name = "date_of_birth")
	private LocalDate dateOfBirth;
	
	@Column(name = "address")
	private String address;

	@OneToOne(optional = false)
	@JoinColumn(name = "user_id",nullable = false,unique = true)
	private User user;
	
	@ManyToMany
	@JoinTable(name = "trainee_trainer",
	joinColumns = @JoinColumn(name ="trainee_id"),
	inverseJoinColumns = @JoinColumn(name ="trainer_id"))
	private Set<Trainer> trainers =new HashSet<>();
	
	@OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Training> trainings = new HashSet<>();

}
