package com.epam.gym_crm.model;

import java.time.LocalDate;
import java.util.Objects;

public class Trainee extends User {

	private LocalDate dateOfBirth;
	private String address;

	public Trainee() {
		super();
	}

	public Trainee(Long id, String firstName, String lastName, String username, String password, boolean isActive,LocalDate dateOfBirth,String address) {
		super(id, firstName, lastName, username, password, isActive);
		this.dateOfBirth = dateOfBirth;
        this.address = address;
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
		if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;
	    if (!super.equals(obj)) return false; 
	    Trainee trainee = (Trainee) obj;
	    return Objects.equals(dateOfBirth, trainee.dateOfBirth) &&
	           Objects.equals(address, trainee.address);
	}

	@Override
	public String toString() {
		return "Trainee{" +
		           super.toString() + // Import User to String
		           ", dateOfBirth=" + dateOfBirth +
		           ", address='" + address + '\'' +
		           '}';
	}

	
	
}
