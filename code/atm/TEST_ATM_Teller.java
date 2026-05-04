package atm;

public class TEST_ATM_Teller extends TEST_ATM_Person {
	
	public TEST_ATM_Teller(String name, TEST_ATM_Address address) {
		super(name, address);
		
	}
	private int registerNumber;
	
	public double withdraw(double amount) {
		return amount;
		
	}
	public double deposit(double amount) {
		return amount;
	}
	
	public int getRegisterNumber() {
		return registerNumber;
	}
}
