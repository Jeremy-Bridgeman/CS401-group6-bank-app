package jUnitTests;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import bankapp.Account;
import bankapp.Person;

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


}
