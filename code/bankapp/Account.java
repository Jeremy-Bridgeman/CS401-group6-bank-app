package bankapp;
import java.io.Serializable;
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
		
	}
	
	public void addAuthorizedUser(Customer user, Teller t) {
		// add user to authorizedUser list
		authorizedUsers.add(user);
		
		// update lastUsed
		lastUsed = new Date();
		
	}
	
	public void removeAuthorizedUser(Customer user, Teller t) {
		// remove user from authorizedUser list
		// subtract count
		
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
		STATUS = ACCOUNT_STATUS.FROZEN;
		
	}
	
	public void unfreeze() {
		// open account again
		STATUS = ACCOUNT_STATUS.OPEN;
		
	}
	
	public void closeAccount() {
		// close account status
		STATUS = ACCOUNT_STATUS.CLOSED;
	}
	
	public double withdraw(double amount) {
		// update lastUsed
		lastUsed = new Date();		
		
		// subtract money from account 
		balance -= amount;
		
		return balance;
	}
	
	public double deposit(double amount) {
		// update lastUsed
		lastUsed = new Date();	
		
		// add money to account
		balance += amount;
		return balance;
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
	}
}