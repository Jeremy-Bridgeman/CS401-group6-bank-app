package bankapp;

public class CreditAccount extends Account {

	public CreditAccount(double balance, ACCOUNT_STATUS status, ACCOUNT_TYPE type, Person user) {
		super(balance, status, type, user);
		// TODO Auto-generated constructor stub -- keeping for now for testing purposes
	}
	public CreditAccount(double balance, ACCOUNT_STATUS status, ACCOUNT_TYPE type, Person user, String accountNumber) {
		super(balance, status, type, user, accountNumber);
	}
	
	private double creditLimit;
	private double interestRate;
	
	public void makePayment(double amount) {
		
	}
	
	public double charge(double amount) {
		
		double total = 0.0;
		return total;
	}
}
