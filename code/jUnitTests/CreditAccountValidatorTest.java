package jUnitTests;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bankapp.Account;
import bankapp.AccountValidator;
import bankapp.CreditAccount;
import bankapp.CreditAccountValidator;
import bankapp.Person;

public class CreditAccountValidatorTest {

    private CreditAccountValidator validator;
    private Person authorizedUser;
    private Person unauthorizedUser;
    private Account account;

    @BeforeEach
    public void setUp() {
        validator = new CreditAccountValidator();
        authorizedUser = new Person();
        unauthorizedUser = new Person();
        account = ValidatorTestHelper.makeAccount(
        	    new CreditAccount(
        	        1000.0,
        	        Account.ACCOUNT_STATUS.OPEN,
        	        Account.ACCOUNT_TYPE.CREDIT,
        	        authorizedUser
        	    ),
        	    authorizedUser,
        	    1000.0
        	);        account.setTYPE(Account.ACCOUNT_TYPE.CREDIT);
    }

    @Test
    public void validatePayment_validPayment_passes() {
        AccountValidator.ValidationMessage result = validator.validatePayment(account, authorizedUser, 150.0);

        assertTrue(result.passed());
    }

    @Test
    public void validatePayment_negativePayment_fails() {
        AccountValidator.ValidationMessage result = validator.validatePayment(account, authorizedUser, -50.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Negative number entered for amount"));
    }

    @Test
    public void validateCharge_validCharge_passes() {
        AccountValidator.ValidationMessage result = validator.validateCharge(account, authorizedUser, 300.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateCharge_closedAccount_fails() {
        account.setSTATUS(Account.ACCOUNT_STATUS.CLOSED);

        AccountValidator.ValidationMessage result = validator.validateCharge(account, authorizedUser, 300.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: ACCOUNT_STATUS not 'open'"));
    }

    @Test
    public void validateTransferToChecking_validTransfer_passes() {
        AccountValidator.ValidationMessage result = validator.validateTransferToChecking(account, authorizedUser, 200.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateTransferToChecking_negativeAmount_fails() {
        AccountValidator.ValidationMessage result = validator.validateTransferToChecking(account, authorizedUser, -10.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Negative number entered for amount"));
    }

    @Test
    public void validateTransferToSavings_validTransfer_passes() {
        AccountValidator.ValidationMessage result = validator.validateTransferToSavings(account, authorizedUser, 200.0);

        assertTrue(result.passed());
    }
}
