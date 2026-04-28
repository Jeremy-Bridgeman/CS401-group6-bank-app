package jUnitTests;

import bankapp.Customer;
import bankapp.Teller;
import bankapp.Manager;
import bankapp.Address;
import bankapp.Person;
import bankapp.Account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AddressTest {

    @Test
    void defaultConstructorCreatesEmptyAddress() {
        Address address = new Address();

        assertEquals(0, address.getStreetNumber());
        assertEquals("", address.getApartmentNumber());
        assertEquals("", address.getStreet());
        assertEquals("", address.getCity());
        assertEquals("", address.getState());
        assertEquals("", address.getZipCode());
    }

    @Test
    void parameterizedConstructorStoresFields() {
        Address address = new Address(123, "4B", "Main St", "Springfield", "IL", "62704");

        assertEquals(123, address.getStreetNumber());
        assertEquals("4B", address.getApartmentNumber());
        assertEquals("Main St", address.getStreet());
        assertEquals("Springfield", address.getCity());
        assertEquals("IL", address.getState());
        assertEquals("62704", address.getZipCode());
    }

    @Test
    void printFormatsFullAddress() {
        Address address = new Address(123, "4B", "Main St", "Springfield", "IL", "62704");

        assertEquals("123 Main St, Apt 4B, Springfield, IL 62704", address.print());
    }

    @Test
    void printOmitsApartmentWhenMissing() {
        Address address = new Address(55, null, "Oak Ave", "Dallas", "TX", "75001");

        assertEquals("55 Oak Ave, Dallas, TX 75001", address.print());
    }

    @Test
    void equalsAndHashCodeMatchForSameValues() {
        Address a1 = new Address(10, "2A", "Pine Rd", "Austin", "TX", "73301");
        Address a2 = new Address(10, "2A", "Pine Rd", "Austin", "TX", "73301");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }
}
