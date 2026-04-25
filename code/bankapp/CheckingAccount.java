package bankapp;

public class CheckingAccount extends Account {

	public CheckingAccount(double balance, ACCOUNT_STATUS status, ACCOUNT_TYPE type, Person user) {
		super(balance, status, type, user);
		// TODO Auto-generated constructor stub -- keeping for now for testing purposes
	}

	public CheckingAccount(double balance, ACCOUNT_STATUS status, ACCOUNT_TYPE type, Person user, String accountNumber) {
		super(balance, status, type, user, accountNumber);
	}

	private double withdrawalLimit;
	private double depositLimit;
	
	public double transferToSavings(SavingsAccount account, double amount) {
		
		double total = 0.0;
		return total;
	}
	
	public double transferToCredit(CreditAccount account, double amount) {
		
		
		double total = 0.0;
		return total;
	}
}