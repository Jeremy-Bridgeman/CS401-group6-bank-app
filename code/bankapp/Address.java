package bankapp;

import java.io.Serializable;
import java.util.Objects;

public class Address implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int streetNumber;
	private String apartmentNumber;
	private String street;
	private String city;
	private String state;
	private String zipCode;
	
	public Address() {
		this(0,null,"","","","");
	}
	
	public Address(int streetNumber, String apartmentNumber, String street, String city, String state, String zipCode) {
		this.streetNumber = streetNumber;
		this.apartmentNumber = apartmentNumber;
		this.street = street;
		this.city = city;
		this.state = state;
		this.zipCode = zipCode;
	}
	
	public int getStreetNumber() {
		return streetNumber;
	}

	public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getApartmentNumber() {
        return apartmentNumber;
    }

    public void setApartmentNumber(String apartmentNumber) {
        this.apartmentNumber = apartmentNumber;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    
    public String print() {
        StringBuilder sb = new StringBuilder();
        if (streetNumber > 0) {
            sb.append(streetNumber).append(' ');
        }
        sb.append(street);

        if (apartmentNumber != null && !apartmentNumber.isBlank()) {
            sb.append(", Apt ").append(apartmentNumber.trim());
        }

        if (!city.isBlank() || !state.isBlank() || !zipCode.isBlank()) {
            sb.append(", ");
            if (!city.isBlank()) {
                sb.append(city);
            }
            if (!state.isBlank()) {
                if (!city.isBlank()) {
                    sb.append(", ");
                }
                sb.append(state);
            }
            if (!zipCode.isBlank()) {
                sb.append(' ').append(zipCode);
            }
        }

        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return print();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Address other)) {
            return false;
        }
        return streetNumber == other.streetNumber && Objects.equals(apartmentNumber, other.apartmentNumber) && Objects.equals(street, other.street)
                && Objects.equals(city, other.city) && Objects.equals(zipCode, other.zipCode) && Objects.equals(state, other.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(streetNumber, apartmentNumber, street, city, zipCode, state);
    }
	
}
