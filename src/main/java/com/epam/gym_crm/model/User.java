package com.epam.gym_crm.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@NotBlank(message = "First name cannot be blank") 
	@Size(min = 3, max = 50, message = "First name must be between 2 and 50 characters") 
	@Column(name = "first_name",nullable = false)
	private String firstName;
	@NotBlank(message = "Last name cannot be blank") 
	@Size(min = 3, max = 50, message = "Last name must be between 2 and 50 characters") 
	@Column(name = "last_name",nullable =  false)
	private String lastName;
	
	@NotBlank(message = "Username cannot be blank")
	@Column(name = "username",nullable = false,unique = true)
	private String username;
	
	@Column(name = "password",nullable = false)
	private String password;
	
	@NotNull(message = "Active status cannot be null")
	@Column(name = "is_active", nullable = false)
	private boolean isActive;
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Trainee trainee;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private Trainer trainer;
	

}
