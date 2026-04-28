package jUnitTests;

import bankapp.Customer;
import bankapp.Teller;
import bankapp.Manager;
import bankapp.Address;
import bankapp.Person;
import bankapp.Account;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.Test;

class TellerTest {

    private static class TestAccount extends Account {
        TestAccount(double balance) {
            super(balance, Account.ACCOUNT_STATUS.OPEN, Account.ACCOUNT_TYPE.CHECKING, null); // make super call the correct constructor
            setAuthorizedUsers(new ArrayList<>());
            setLastUsed(new Date());
        }

        @Override
        public double withdraw(double amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("amount must be non-negative");
            }
            if (getBalance() < amount) {
                throw new IllegalArgumentException("insufficient funds");
            }
            setBalance(getBalance() - amount);
            return getBalance();
        }

        @Override
        public double deposit(double amount) {
            if (amount < 0) {
                throw new IllegalArgumentException("amount must be non-negative");
            }
            setBalance(getBalance() + amount);
            return getBalance();
        }
    }

    @Test
    void constructorSetsRegisterNumberAndInheritedFields() {
        Address address = new Address(100, null, "Market", "San Diego", "CA", "92101");
        Teller teller = new Teller("Alice", "Smith", address, 42);

        assertEquals("Alice Smith", teller.getName());
        assertEquals(42, teller.getRegisterNumber());
        assertSame(address, teller.getAddress());
    }

    @Test
    void depositAndWithdrawDelegateToAccount() {
        Teller teller = new Teller("Alice", "Smith", new Address(), 42);
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);
        TestAccount account = new TestAccount(200.0);

        teller.beginSession(customer);

        double afterDeposit = teller.deposit(account, 25.0);
        double afterWithdraw = teller.withdraw(account, 50.0);

        assertEquals(225.0, afterDeposit, 0.0001);
        assertEquals(175.0, afterWithdraw, 0.0001);
        assertEquals(175.0, account.getBalance(), 0.0001);
    }

    @Test
    void transferMovesFundsBetweenAccounts() {
        Teller teller = new Teller("Alice", "Smith", new Address(), 42);
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);
        TestAccount from = new TestAccount(500.0);
        TestAccount to = new TestAccount(75.0);

        teller.beginSession(customer);

        double result = teller.transfer(from, to, 125.0);

        assertEquals(375.0, from.getBalance(), 0.0001);
        assertEquals(200.0, to.getBalance(), 0.0001);
        assertEquals(200.0, result, 0.0001);
    }

    @Test
    void assistCustomerMarksTellerVisit() {
        Teller teller = new Teller("Alice", "Smith", new Address(), 42);
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);

        teller.assistCustomer(customer);

        assertNotNull(customer.getLastTellerVisit());
    }

    @Test
    void nonPositiveAmountThrowsException() {
        Teller teller = new Teller("Alice", "Smith", new Address(), 42);
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);
        TestAccount account = new TestAccount(200.0);

        teller.beginSession(customer);

        assertThrows(IllegalArgumentException.class, () -> teller.deposit(account, 0.0));
        assertThrows(IllegalArgumentException.class, () -> teller.withdraw(account, -10.0));
    }
}
