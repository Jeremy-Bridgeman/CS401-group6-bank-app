package jUnitTests;

import bankapp.Log;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LogTest {

    private static final String ACCOUNT_KEY = "Jeremy Tester|ACC-1001";

	@Test
	void testDepositLog() {
		Log log = new Log(Log.TRANSACTION_TYPE.DEPOSIT, "TestComment", 22.22, ACCOUNT_KEY);
		assertEquals(Log.TRANSACTION_TYPE.DEPOSIT, log.getType());
		assertEquals("TestComment", log.getComment());
		assertEquals(22.22, log.getAmount());
		assertNotNull(log.getDate());
        assertEquals(ACCOUNT_KEY, log.getAccountKey());
	}

	@Test
	void testToFileStringFormat() {
	    Log log = new Log(Log.TRANSACTION_TYPE.DEPOSIT, "Test", 10.0, ACCOUNT_KEY);
	    String result = log.toFileString();

	    assertTrue(result.contains("DEPOSIT"));
	    assertTrue(result.contains("Test"));
	    assertTrue(result.contains("10.0"));
        assertTrue(result.contains(ACCOUNT_KEY));
	}

	@Test
	void testFromFileString() {
	    String line = "1000|DEPOSIT|Hello|50.5|" + ACCOUNT_KEY;

	    Log log = Log.fromFileString(line);

	    assertEquals(Log.TRANSACTION_TYPE.DEPOSIT, log.getType());
	    assertEquals("Hello", log.getComment());
	    assertEquals(50.5, log.getAmount(), 0.0001);
        assertEquals(ACCOUNT_KEY, log.getAccountKey());
	}

	@Test
	void testBadFileString() {
	    assertThrows(IllegalArgumentException.class, () -> {
	        Log.fromFileString("bad|data");
	    });
	}

	@Test
	void testSaveThenLoad() {
		Log original = new Log(Log.TRANSACTION_TYPE.DEPOSIT, "TestComment", 22.22, ACCOUNT_KEY);
		String saved = original.toFileString();
		Log loaded = Log.fromFileString(saved);

		assertEquals(saved, loaded.toFileString());
		assertEquals(original.getType(), loaded.getType());
		assertEquals(original.getComment(), loaded.getComment());
		assertEquals(original.getAmount(), loaded.getAmount(), 0.0001);
        assertEquals(original.getAccountKey(), loaded.getAccountKey());
	}
}