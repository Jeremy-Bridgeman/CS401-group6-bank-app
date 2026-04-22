package bankapp;
import java.io.Serializable;
import java.util.*;

public class Account implements Serializable {
	public enum ACCOUNT_STATUS {
		OPEN,
		CLOSED,
		SUSPENDED,
		FLAGGED
	}
	public enum ACCOUNT_TYPE {
		CHECKING,
		SAVINGS,
		CREDIT
	}
	
	private double balance;
	private Date lastUsed;
	private ACCOUNT_STATUS STATUS;
	private ACCOUNT_TYPE TYPE;
	private ArrayList<Person>authorizedUsers;
	
	public void addAuthorizedUser(Customer user, Teller t) {
		
	}
	public void removeAuthorizedUser(Customer user, Teller t) {
		
	}
	public boolean isSuspended() {
		return false;
	}
	public boolean isFrozen() {
		return false;
	}
	public void freeze() {
		
	}
	public void unfreeze() {
		
	}
	public void closeAccount() {
		
	}
	public double withdraw(double amount) {
		return 0.0;
	}
	public double deposit(double amount) {
		return 0.0;
	}
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
	public void flag() {
		// logic for flagging the account here
	}
}
