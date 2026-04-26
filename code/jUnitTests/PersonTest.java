package jUnitTests;


import bankapp.Customer;
import bankapp.Teller;
import bankapp.Manager;
import bankapp.Address;
import bankapp.Person;
import bankapp.Account;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PersonTest {

    @Test
    void defaultConstructorCreatesBlankPersonWithDefaultAddress() {
        Person person = new Person();

        assertEquals(" ", person.getName());
        assertEquals("", person.getFirstName());
        assertEquals("", person.getLastName());
        assertNotNull(person.getAddress());
    }

    @Test
    void constructorStoresNameAndAddress() {
        Address address = new Address(99, null, "Elm St", "Denver", "CO", "80014");
        Person person = new Person("Chris", "Tse", address);

        assertEquals("Chris Tse", person.getName());
        assertEquals("Chris", person.getFirstName());
        assertEquals("Tse", person.getLastName());
        assertSame(address, person.getAddress());
    }

    @Test
    void settersUpdateFields() {
        Person person = new Person();
        Address newAddress = new Address(77, "12", "Maple", "Seattle", "WA", "98101");

        person.setFirstName("Kayla");
        person.setLastName("Imus");
        person.setAddress(newAddress);

        assertEquals("Kayla Imus", person.getName());
        assertSame(newAddress, person.getAddress());
    }

    @Test
    void toStringContainsNameAndAddress() {
        Address address = new Address(1, null, "Bank Way", "Boston", "MA", "02108");
        Person person = new Person("Jeremy", "Bridgeman", address);

        String text = person.toString();

        assertTrue(text.contains("Jeremy Bridgeman"));
        assertTrue(text.contains("Bank Way"));
    }
}
