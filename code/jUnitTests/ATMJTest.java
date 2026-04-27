package jUnitTests;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import bankapp.ATM;
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

}
