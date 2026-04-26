package jUnitTests;

import bankapp.Customer;
import bankapp.Teller;
import bankapp.Manager;
import bankapp.Address;
import bankapp.Person;
import bankapp.Account;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.Test;

class CustomerTest {

    private static class TestAccount extends Account {
        TestAccount(double balance) {
            setBalance(balance);
            setSTATUS(Account.ACCOUNT_STATUS.OPEN);
            setTYPE(Account.ACCOUNT_TYPE.CHECKING);
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
    void constructorSetsCoreFields() {
        Address address = new Address(10, null, "Main", "Tempe", "AZ", "85281");
        Customer customer = new Customer("Chris", "Tse", address, "ctse01", 1234);

        assertEquals("Chris Tse", customer.getName());
        assertEquals("ctse01", customer.getUsername());
        assertEquals(1234, customer.getPin());
        assertEquals(LocalDate.now(), customer.getJoinDate());
    }

    @Test
    void verifyPinReturnsTrueOnlyForMatchingPin() {
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);

        assertTrue(customer.verifyPin(1234));
        assertFalse(customer.verifyPin(9999));
    }

    @Test
    void changePinUpdatesPinAndLastTellerVisit() {
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);

        customer.changePin(4321);

        assertEquals(4321, customer.getPin());
        assertEquals(LocalDate.now(), customer.getLastTellerVisit());
    }

    @Test
    void markVisitMethodsSetDates() {
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);

        customer.markTellerVisit();
        customer.markAtmVisit();

        assertEquals(LocalDate.now(), customer.getLastTellerVisit());
        assertEquals(LocalDate.now(), customer.getLastAtmVisit());
    }

    @Test
    void depositAndWithdrawDelegateToAccount() {
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);
        TestAccount account = new TestAccount(100.0);

        double afterDeposit = customer.deposit(account, 50.0);
        double afterWithdraw = customer.withdraw(account, 25.0);

        assertEquals(150.0, afterDeposit, 0.0001);
        assertEquals(125.0, afterWithdraw, 0.0001);
        assertEquals(125.0, account.getBalance(), 0.0001);
    }

    @Test
    void transferMovesFundsBetweenAccounts() {
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);
        TestAccount from = new TestAccount(300.0);
        TestAccount to = new TestAccount(100.0);

        double result = customer.transfer(from, to, 80.0);

        assertEquals(220.0, from.getBalance(), 0.0001);
        assertEquals(180.0, to.getBalance(), 0.0001);
        assertEquals(180.0, result, 0.0001);
    }

    @Test
    void negativeAmountsThrowException() {
        Customer customer = new Customer("Chris", "Tse", new Address(), "ctse01", 1234);
        TestAccount account = new TestAccount(100.0);

        assertThrows(IllegalArgumentException.class, () -> customer.deposit(account, -1.0));
        assertThrows(IllegalArgumentException.class, () -> customer.withdraw(account, 0.0));
    }
}
