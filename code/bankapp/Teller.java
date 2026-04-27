package bankapp;

import java.util.Objects;

public class Teller extends Person {
	private static final long serialVersionUID = 1L;
	private int registerNumber;
	private Customer activeCustomer;
	private boolean registerInUse;
	
	public Teller() {
		super();
		this.registerNumber = 0;
	    this.activeCustomer = null;
	    this.registerInUse = false;
	}
	
	public Teller(String firstName, String lastName, Address address, int registerNumber) {
        super(firstName, lastName, address);
        this.registerNumber = registerNumber;
        this.activeCustomer = null;
        this.registerInUse = false;
    }

    public int getRegisterNumber() {
        return registerNumber;
    }

    public void setRegisterNumber(int registerNumber) {
        this.registerNumber = registerNumber;
    }
    
    public Customer getActiveCustomer() {
        return activeCustomer;
    }

    public boolean isCustomerPresent() {
        return activeCustomer != null;
    }

    public boolean isRegisterInUse() {
        return registerInUse;
    }

    public void beginSession(Customer customer) {
        Objects.requireNonNull(customer, "cannot begin teller session: customer is null");

        if (registerInUse) {
            throw new IllegalStateException(
                "cannot begin teller session: register #" + registerNumber
                + " is already in use by " + activeCustomer.getName()
            );
        }

        this.activeCustomer = customer;
        this.registerInUse = true;
        customer.markTellerVisit();
    }

    public void endSession() {
        if (!registerInUse && activeCustomer == null) {
            throw new IllegalStateException(
                "cannot end teller session: register #" + registerNumber + " is not currently in use"
            );
        }

        this.activeCustomer = null;
        this.registerInUse = false;
    }

    protected void requireCustomerPresent() {
        if (activeCustomer == null) {
            throw new IllegalStateException(
                "teller cannot perform operation: no customer is present at register #" + registerNumber
            );
        }
    }
    
    public double withdraw(Account account, double amount) {
        requireCustomerPresent();
        Objects.requireNonNull(account, "Account cannot be null");
        validateAmount(amount);
        return account.withdraw(amount);
    }

    public double deposit(Account account, double amount) {
        requireCustomerPresent();
        Objects.requireNonNull(account, "Account cannot be null");
        validateAmount(amount);
        return account.deposit(amount);
    }

    public double transfer(Account from, Account to, double amount) {
        requireCustomerPresent();
        Objects.requireNonNull(from, "Account [from] cannot be null");
        Objects.requireNonNull(to, "Account [to] cannot be null");
        validateAmount(amount);

        from.withdraw(amount);
        return to.deposit(amount);
    }
    
    public void assistCustomer(Customer customer) {
    	beginSession(customer);
    }
 

    protected static void validateAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount needs to be above 0");
        }
    }
    
    
}
