package jUnitTests;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import bankapp.Account;
import bankapp.Person;
import bankapp.Teller;
import bankapp.Customer;

class AccountTest {

	// creating account instance
	double balance = 50.30;
	Account.ACCOUNT_STATUS status = Account.ACCOUNT_STATUS.OPEN; // status
	Account.ACCOUNT_TYPE type = Account.ACCOUNT_TYPE.CHECKING; //type
	Person user = new Person(); //user
	
	@Test
	void testConstructor() {
		Account acc = new Account(balance, status, type, user);
		assertEquals(balance, acc.getBalance());
		assertEquals(status, acc.getSTATUS());
		assertEquals(type, acc.getTYPE());
		assertNotNull(acc.getAccountNumber());
	}
	
	@Test
	void testWithdraw() {
		Account acc = new Account(balance, status, type, user);
		assertEquals(30.30, acc.withdraw(20.00)); // 50.30 - 20.00 = 30.30
	}
	
	@Test
	void testDeposit() {
		Account acc = new Account(balance, status, type, user);
		assertEquals(80.30, acc.deposit(30.00)); // 50.30 + 30.00 = 80.30
	}
	
	@Test
	void testCloseAccount() {
		Account acc = new Account(balance, status, type, user);
		acc.closeAccount();
		assertEquals(Account.ACCOUNT_STATUS.CLOSED, acc.getSTATUS());
	}

	@Test
	void testOpenAccount() {
		Account acc = new Account(balance, status, type, user);
		acc.closeAccount();
		acc.unfreeze();
		assertEquals(Account.ACCOUNT_STATUS.OPEN, acc.getSTATUS());
	}
	
	@Test
	void testAddUser() {
		Account acc = new Account(balance, status, type, user);
		Customer user2 = new Customer();
		Teller teller = new  Teller();
		
		acc.addAuthorizedUser(user2, teller);
		ArrayList<Person> accList = acc.getAuthorizedUsers();
		assertEquals(2, accList.size()); // makes sure there are 2 users in one account
	}
	
	@Test
	void testRemoveUser() {
		Account acc = new Account(balance, status, type, user);
		Customer user2 = new Customer();
		Customer user3 = new Customer();
		Teller teller = new  Teller();
		
		acc.addAuthorizedUser(user2, teller);
		acc.addAuthorizedUser(user3, teller);
		acc.removeAuthorizedUser(user2, teller);
		
		ArrayList<Person> accList = acc.getAuthorizedUsers();
		assertEquals(2, accList.size()); // makes sure there are 2 users in one account
		assertTrue(accList.contains(user));
		assertTrue(accList.contains(user3));
		assertFalse(accList.contains(user2)); // makes sure user2 was removed but not user and user3
	}



}
