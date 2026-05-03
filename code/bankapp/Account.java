package bankapp;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public class Account implements Serializable {
	public enum ACCOUNT_STATUS {
		OPEN,
		CLOSED,
		SUSPENDED,
		FLAGGED,
		FROZEN
	}
	public enum ACCOUNT_TYPE {
		CHECKING,
		SAVINGS,
		CREDIT
	}
	
	private double balance;
	private Date lastUsed; // set to a new date whenever 
	private ACCOUNT_STATUS STATUS;
	private ACCOUNT_TYPE TYPE;
	private ArrayList<Person> authorizedUsers = new ArrayList<Person>();
	private String accountNumber;
	private int pin;
	static private int count = 999;
	
	
	// constructor
	public Account(double balance, ACCOUNT_STATUS status, ACCOUNT_TYPE type, Person user) {
		this(balance, status, type, user, UUID.randomUUID().toString());
	}

	public Account(double balance, ACCOUNT_STATUS status, ACCOUNT_TYPE type, Person user, String accountNumber) {
		this.balance = balance;
		STATUS = status;
		TYPE = type;
		this.accountNumber = accountNumber;
		// add user to array
		authorizedUsers.add(user);
		// update lastUsed
		lastUsed = new Date();
		pin = count++;
		
	}
	
	public void addAuthorizedUser(Customer user, Teller t) {
		
		
		// add user to authorizedUser list
		authorizedUsers.add(user);
		
		// update lastUsed
		lastUsed = new Date();
		
	}
	
	public void removeAuthorizedUser(Customer user, Teller t) {
		
		// check if user exists in list?
		
		// remove user from authorizedUser list
		authorizedUsers.remove(user);
	
		
		// update lastUsed
		lastUsed = new Date();
	}
	
	public boolean isSuspended() {
		// true if account_status == suspended
		if (STATUS == ACCOUNT_STATUS.SUSPENDED) {	
			return true;
			
		} else {
			// else, false
			return false;
		}
	}
	
	public boolean isFrozen() {
		// true if account_status == frozen
		if (STATUS == ACCOUNT_STATUS.FROZEN) {	
			return true;
			
		} else {
			// else, false
			return false;
		}	
	}
	
	public void freeze() {
		// turn account status to frozen
		setSTATUS(ACCOUNT_STATUS.FROZEN);
		
	}
	
	public void unfreeze() {
		// open account again
		setSTATUS(ACCOUNT_STATUS.OPEN);
		
	}
	
	public void closeAccount() {
		// close account status
		setSTATUS(ACCOUNT_STATUS.CLOSED);
	}
	
	public double withdraw(double amount) {
		// update lastUsed
		lastUsed = new Date();		
		
		// convert to BigDecimal for precision
		BigDecimal balanceBD = BigDecimal.valueOf(balance);
		BigDecimal amountBD = BigDecimal.valueOf(amount);
		
		// subtract money 
		BigDecimal resultBD = balanceBD.subtract(amountBD);
		double result = resultBD.doubleValue(); // convert to double again
		
		return result;
	}
	
	public double deposit(double amount) {
		// update lastUsed
		lastUsed = new Date();	
		
		// convert to BigDecimal for precision
		BigDecimal balanceBD = BigDecimal.valueOf(balance);
		BigDecimal amountBD = BigDecimal.valueOf(amount);
		
		// subtract money 
		BigDecimal resultBD = balanceBD.add(amountBD);
		double result = resultBD.doubleValue(); // convert to double again
		
		return result;
	}
	
	// getters
	public double getBalance() {
		return balance;
	}
	
	public Date getLastUsed() {
		return lastUsed;
	}
	
	public ACCOUNT_STATUS getSTATUS() {
		return STATUS;
	}
	
	public ACCOUNT_TYPE getTYPE() {
		return TYPE;
	}
	
	public ArrayList<Person> getAuthorizedUsers() {
		return authorizedUsers;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public String getLogKey() {
		String owner = "UNKNOWN";
		if (!authorizedUsers.isEmpty() && authorizedUsers.get(0) != null) {
			owner = authorizedUsers.get(0).getName();
		}
		return owner + "|" + accountNumber;
	}
	
	public int getPin() {
		return pin;
	}
	
	// setters
	public void setBalance(double balance) {
		this.balance = balance;
	}
	
	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}
	
	public void setSTATUS(ACCOUNT_STATUS sTATUS) {
		STATUS = sTATUS;
	}
	
	public void setTYPE(ACCOUNT_TYPE tYPE) {
		TYPE = tYPE;
	}
	
	public void setAuthorizedUsers(ArrayList<Person> authorizedUsers) {
		this.authorizedUsers = authorizedUsers;
	}

	public void setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
	}

	public void flag() {
		// logic for flagging the account here
		setSTATUS(ACCOUNT_STATUS.FLAGGED);
	}
	// overrides so we can use comparisons properly
	@Override
	public boolean equals(Object o) {
	    if (this == o) return true;
	    if (!(o instanceof Account other)) return false;
	    return Objects.equals(accountNumber, other.accountNumber);
	}

	@Override
	public int hashCode() {
	    return Objects.hash(accountNumber);
	}
}