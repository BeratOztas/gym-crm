package com.epam.gym_crm.model;

import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public abstract class User {

	private Long id;
	
	@NotBlank(message = "First name cannot be blank") 
	@Size(min = 3, max = 50, message = "First name must be between 2 and 50 characters") 
	private String firstName;
	@NotBlank(message = "Last name cannot be blank") 
	@Size(min = 3, max = 50, message = "Last name must be between 2 and 50 characters") 
	private String lastName;
	
	private String username;
	private String password;
	@NotNull(message = "Active status cannot be null")
	private boolean isActive;

	public User() {
	}

	public User(Long id, String firstName, String lastName, String username, String password, boolean isActive) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
		this.isActive = isActive;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		User user = (User) obj;
		return Objects.equals(id, user.id);
	}

	@Override
	public String toString() {
		return "User{" + "id=" + id + ", firstName='" + firstName + '\'' + ", lastName='" + lastName + '\''
				+ ", username='" + username + '\'' + ", isActive=" + isActive + '}';
	}

}
