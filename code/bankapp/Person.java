package bankapp;

import java.io.Serializable;

public class Person implements Serializable{
	private static final long serialVersionUID = 1L;
	private String firstName;
	private String lastName;
	private Address address;
	
	public Person() {
		this("","", new Address());
	}
	
	
	public Person(String firstName, String lastName, Address address) {
	    setFirstName(firstName);
	    setLastName(lastName);
	    setAddress(address);
	}
	
	public String getName() {
	    String fullName = ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
	    if (fullName.isBlank()) {
	        throw new IllegalStateException("name is blank");
	    }
	    return fullName;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
	    if (firstName == null) {
	        throw new IllegalArgumentException("first name cannot be null");
	    }
	    this.firstName = firstName.trim();
	}
	
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
	    if (lastName == null) {
	        throw new IllegalArgumentException("last name cannot be null");
	    }
	    this.lastName = lastName.trim();
	}
	
	public Address getAddress() {
		return address;
	}
	
	public void setAddress(Address address) {
	    if (address == null) {
	        throw new IllegalArgumentException("address cannot be null");
	    }
	    this.address = address;
	}
	
	@Override
	public String toString() {
		return "Person{name='" + getName() + "', address=" + address + "}";
	}
}
