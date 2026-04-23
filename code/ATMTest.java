import bankapp.Account;
import bankapp.Person;
import bankapp.Response;

//ATM Test Connection to Server
	public class ATMTest {
	    public static void main(String[] args) {
	        ATM atm = new ATM("localhost");
	        System.out.println("ATM connected to server");
	        
	        // Tests that it can deposit through server
	        Person p = new Person();

	        Account acc = new Account(
	        );

	        // 1. OPEN ACCOUNT (required by server)
	        Response open = atm.openAccount(acc, p);
	        System.out.println("OPEN: " + open.getMessage());

	        // 2. DEPOSIT
	        Response dep = atm.deposit(500, acc, p);
	        System.out.println("DEPOSIT: " + dep.getMessage());

	        // 3. CHECK BALANCE
	        Response bal = atm.checkBalance(acc, p);
	        System.out.println("BALANCE: " + bal.getMessage());

	        // 4. CLOSE SOCKET CLEANLY
	        atm.close();
	    
	    }
	}
