package com.epam.gym_crm.db.entity;

import java.io.Serializable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "`user`")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
public class User  implements Serializable{

	private static final long serialVersionUID = -2309186367973399735L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	 
	@Column(name = "first_name",nullable = false, columnDefinition = "varchar(255)")
	private String firstName;
	 
	@Column(name = "last_name",nullable =  false, columnDefinition = "varchar(255)")
	private String lastName;
	
	@Column(name = "username",nullable = false,unique = true)
	private String username;
	
	@Column(name = "password",nullable = false)
	private String password;
	
	@Column(name = "is_active", nullable = false)
	private boolean isActive;
	
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	private Trainee trainee;

	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
	private Trainer trainer;

}
