

public class TEST_ATM_Person {
	private String name;
	private TEST_ATM_Address address;
	
	public TEST_ATM_Person(String name, TEST_ATM_Address address) {
		this.name = name;
		this.address = address;
	}
	
	public String getName() {
		return name;
	}
	
	public TEST_ATM_Address getAddress() {
		return address; 
	}
	
}

