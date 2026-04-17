package bankapp;

public class CreditAccountValidator extends AccountValidator {
	boolean validateCharge(double charge) {
		return false;
	}
}
