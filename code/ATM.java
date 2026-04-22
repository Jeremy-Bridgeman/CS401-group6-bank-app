
public class ATM {
	private double cash;
	private int failedAttempts;
	private boolean serviceCompleted;
	
	//Place Holders
	private double dailyWithdrawalLimit = 10000;
	private double dailyDepositLimit = 10000;
	
	
	public ATM(String serverIP) {
		this.failedAttempts = 0;
		this.serviceCompleted = false;
		this.cash = 1000; // placeholder
	}
	
	// Basic ATM Functions
	public double withdraw(double amount) {
		if (amount <= 0) {
			displayError();
			return 0;
		}
		if (amount > cash) {
			displayError();
			return 0;
		}
		cash -= amount;
		displayConfirmation();
		return amount;
	}
	
	public double deposit(double amount) {
		if (amount <= 0) { 
			displayError();
			return 0;
		}
		cash += amount;
		displayConfirmation();
		return amount;
	}
	
	public double checkBalance() {
		return cash;
	}
	
	public void displayConfirmation() {
		System.out.println("Transaction Successful.");
		
	}
	public void displayError() {
		System.out.println("Transaction Failed.");
		
	}
	public double getCashAmount() {
		return cash;
	}
	
	// Login Functions
	public boolean login(int id, int pin) {
		// Placeholder until server/auth later
		boolean success = true;
		
		if (!success) {
			failedAttempts++;
			
			if (failedAttempts >= 5) {
				serviceCompleted = true;
			}
			displayError();
			return false;
		}
		
		failedAttempts = 0;
		return true;
		
	}
	
	public void logAttempt(Log log) {
		
	}
	
	// Getters 
	public double getDailyWithdrawalLimit() {
		return dailyWithdrawalLimit;
		
	}
	public double getDailyDepositLimit() {
		return dailyDepositLimit;
		
	}
	public int getFailedAttempts() {
		return this.failedAttempts;
		
	}
	public boolean getServiceCompletion() {
		return this.serviceCompleted;
		
	}
	
	// Setters
	public void setCashAmount(double amount) {
		this.cash = amount;
	}
	public void setFailedAttempts(int amount) {
		this.failedAttempts = amount;
	}
	public void setServiceCompletion(boolean result) {
		this.serviceCompleted = result;
	}

}
