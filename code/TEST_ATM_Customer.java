import java.util.Date;

public class TEST_ATM_Customer extends TEST_ATM_Person{
	private Date joinDate;
	private Date lastTellerVisit;
	private Date lastAtmVisit;
	private String username;
	private int pin;
	
	public TEST_ATM_Customer(String name, TEST_ATM_Address address, String username, int pin) {
		super(name, address);
		this.username = username;
		this.pin = pin;
		this.joinDate = new Date();
	}
	
	public void changePin(int newPin) {
		this.pin = newPin;
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getPin() {
		return pin;
	}
	
	public void setLastAtmVisit(Date date) {
		this.lastAtmVisit = date;
	}
	
	public Date getLastAtmVisit() {
		return lastAtmVisit;
	}
	public void setLastTellerVisit(Date date) {
		this.lastTellerVisit = date;
	}
	public Date getLastTellerVisit() {
		return lastTellerVisit;
	}
	public Date getJoinDate() {
		return joinDate;
	}
	
}
