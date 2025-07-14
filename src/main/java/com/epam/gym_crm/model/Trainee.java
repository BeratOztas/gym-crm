package com.epam.gym_crm.model;

import java.time.LocalDate;
import java.util.Objects;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public class Trainee  {

	
	@NotNull(message = "Date of birth cannot be null")
	@Past(message = "Date of birth must be in the past")
	private LocalDate dateOfBirth;
	@NotBlank(message = "Address cannot be blank")
	@Size(min = 5, max = 255, message = "Address must be between 5 and 255 characters")
	private String address;
	
	private User user;

	public Trainee() {
		this.user = new User();
	}

	public Trainee(Long id, String firstName, String lastName, String username, String password, boolean isActive,
			LocalDate dateOfBirth, String address) {
		
		this.user=new User(id, firstName, lastName, username, password, isActive);
		this.dateOfBirth = dateOfBirth;
		this.address = address;
	}
	
	public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), dateOfBirth, address);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		if (!super.equals(obj))
			return false;
		Trainee trainee = (Trainee) obj;
		return Objects.equals(dateOfBirth, trainee.dateOfBirth) && Objects.equals(address, trainee.address);
	}

	@Override
	public String toString() {
		return "Trainee{" + super.toString() + // Import User to String
				", dateOfBirth=" + dateOfBirth + ", address='" + address + '\'' + '}';
	}

}
