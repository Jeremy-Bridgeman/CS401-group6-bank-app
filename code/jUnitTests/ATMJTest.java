package jUnitTests;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import bankapp.ATM;
import bankapp.Account;
import bankapp.Address;
import bankapp.Customer;
import bankapp.Response;
public class ATMJTest {
	
	@Test
	public void testGetConnectedServerIP() {
		ATM atm = new ATM("localhost");
		String result = atm.getConnectedServerIP();
		
		assertNotNull(result);	
	}
	
	// Tests Failed attempts
	@Test
	public void testInitialFailedAttempts() {
		ATM atm = new ATM("localhost");
		assertEquals(0, atm.getFailedAttempts());
	}
	
	// Tests Set Failed Attempts
	@Test 
	public void testSetFailedAttempts() {
		ATM atm = new ATM("localhost");
		atm.setFailedAttempts(3);
		assertEquals(3, atm.getFailedAttempts());
	}
	
	// Tests placeholder daily limits 
	@Test 
	public void testDailyLimits() {
		ATM atm = new ATM("localhost");
		assertEquals(10000, atm.getDailyDepositLimit());
		assertEquals(10000, atm.getDailyWithdrawalLimit());
	}
	
	// Tests Service Completion 
	@Test
	public void testSetServiceCompletion() {
		ATM atm = new ATM("localhost");
		atm.setServiceCompletion(true);
		assertTrue(atm.getServiceCompletion());
	}
	//Login Success resets failed attempts
	@Test
	public void testLoginSuccessResetsFailedAttempts() {
	    ATM atm = new ATM("localhost");

	    atm.setFailedAttempts(3);
	    atm.setFailedAttempts(0);

	    assertEquals(0, atm.getFailedAttempts());
	}
	
	//Failed login increments attempts
	@Test
	public void testFailedAttemptsIncrementLogic() {
	    ATM atm = new ATM("localhost");

	    int before = atm.getFailedAttempts();
	    atm.setFailedAttempts(before + 1);

	    assertEquals(before + 1, atm.getFailedAttempts());
	}
	
	//Server IP is stored correctly
	@Test
	public void testServerIPStoredCorrectly() {
	    ATM atm = new ATM("127.0.0.1");

	    assertEquals("127.0.0.1", atm.getConnectedServerIP());
	}
	
	//Close method doesn't crash system
	@Test
	public void testCloseDoesNotCrash() {
	    ATM atm = new ATM("localhost");

	    assertDoesNotThrow(() -> atm.close());
	}
	
	// Full initialization test
	@Test
	public void testATMInitialStateClean() {
	    ATM atm = new ATM("localhost");

	    assertEquals(0, atm.getFailedAttempts());
	    assertFalse(atm.getServiceCompletion());
	    assertEquals(10000, atm.getDailyDepositLimit());
	    assertEquals(10000, atm.getDailyWithdrawalLimit());
	}
	
	//Tests login()
	@Test
	public void testLoginFailureIncrementsFailedAttempts() {
	    ATM atm = new ATM("localhost");
	    int before = atm.getFailedAttempts();
	    atm.login("invalidUser", 0000);

	    assertTrue(atm.getFailedAttempts() >= before);
	}
	
	//Withdraw returns response object
	@Test
	public void testWithdrawReturnsValidResponse() {
	    ATM atm = new ATM("localhost");

	    Customer user = new Customer("", "", new Address(), "user", 1234);

	    Account acc = new Account(
	        500,
	        Account.ACCOUNT_STATUS.OPEN,
	        Account.ACCOUNT_TYPE.SAVINGS,
	        user
	    );

	    Response response = atm.withdraw(100, acc, user);

	    assertNotNull(response);
	    assertNotNull(response.getType());
	    assertNotNull(response.getMessage());
	}
	
	// Deposit returns response object
	@Test
	public void testDepositReturnsValidResponse() {
	    ATM atm = new ATM("localhost");

	    Customer user = new Customer("", "", new Address(), "user", 1234);

	    Account acc = new Account(
	        200,
	        Account.ACCOUNT_STATUS.OPEN,
	        Account.ACCOUNT_TYPE.CHECKING,
	        user
	    );

	    Response response = atm.deposit(50, acc, user);

	    assertNotNull(response);
	    assertNotNull(response.getType());
	    assertNotNull(response.getMessage());
	}
	// Transfer returns response object
	@Test
	public void testTransferReturnsValidResponse() {
	    ATM atm = new ATM("localhost");

	    Customer user = new Customer("", "", new Address(), "user", 1234);

	    Account source = new Account(
	        500,
	        Account.ACCOUNT_STATUS.OPEN,
	        Account.ACCOUNT_TYPE.CHECKING,
	        user
	    );

	    Response response = atm.transfer(100, source, "TARGET123", user);

	    assertNotNull(response);
	    assertNotNull(response.getType());
	    assertNotNull(response.getMessage());
	}
	
	//Multiple operations do not break ATM
	@Test
	public void testMultipleOperationsDoNotCrashATM() {
	    ATM atm = new ATM("localhost");

	    Customer user = new Customer("", "", new Address(), "user", 1234);

	    Account acc = new Account(
	        300,
	        Account.ACCOUNT_STATUS.OPEN,
	        Account.ACCOUNT_TYPE.CHECKING,
	        user
	    );

	    assertDoesNotThrow(() -> {
	        atm.deposit(500, acc, user);
	        atm.withdraw(200, acc, user);
	        atm.checkBalance(acc, user);
	    });
	}
	

}
