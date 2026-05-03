package jUnitTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bankapp.Account;
import bankapp.AccountValidator;
import bankapp.CheckingAccount;
import bankapp.Person;

public class AccountValidatorTest {

    private AccountValidator validator;
    private Person authorizedUser;
    private Person unauthorizedUser;
    private Account account;

    @BeforeEach
    public void setUp() {
        validator = new AccountValidator();
        authorizedUser = new Person();
        unauthorizedUser = new Person();
        account = ValidatorTestHelper.makeAccount(
        	    new CheckingAccount(
        	        500.0,
        	        Account.ACCOUNT_STATUS.OPEN,
        	        Account.ACCOUNT_TYPE.CHECKING,
        	        authorizedUser
        	    ),
        	    authorizedUser,
        	    500.0
        	);    }

    @Test
    public void validateAmount_positiveAmount_passes() {
        AccountValidator.ValidationMessage result = validator.validateAmount(100.0);

        assertTrue(result.passed());
        assertEquals(AccountValidator.VALIDATION_RESULT.PASS, result.getRESULT());
    }

    @Test
    public void validateAmount_zero_passes() {
        AccountValidator.ValidationMessage result = validator.validateAmount(0.0);

        assertTrue(result.passed());
        assertEquals(AccountValidator.VALIDATION_RESULT.PASS, result.getRESULT());
    }

    @Test
    public void validateAmount_negativeAmount_fails() {
        AccountValidator.ValidationMessage result = validator.validateAmount(-1.0);

        assertFalse(result.passed());
        assertEquals(AccountValidator.VALIDATION_RESULT.FAIL, result.getRESULT());
        assertTrue(result.getMsg().contains("Negative number entered for amount"));
    }

    @Test
    public void validateAccountStatus_openAccount_passes() {
        AccountValidator.ValidationMessage result = validator.validateAccountStatus(account);

        assertTrue(result.passed());
    }

    @Test
    public void validateAccountStatus_closedAccount_fails() {
        account.setSTATUS(Account.ACCOUNT_STATUS.CLOSED);

        AccountValidator.ValidationMessage result = validator.validateAccountStatus(account);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: ACCOUNT_STATUS not 'open'"));
    }

    @Test
    public void validateAuthorizedUser_authorizedUser_passes() {
        AccountValidator.ValidationMessage result = validator.validateAuthorizedUser(account, authorizedUser);

        assertTrue(result.passed());
    }

    @Test
    public void validateAuthorizedUser_unauthorizedUser_fails() {
        AccountValidator.ValidationMessage result = validator.validateAuthorizedUser(account, unauthorizedUser);

        assertFalse(result.passed());
    }

    @Test
    public void runAccountValidation_validInput_passes() {
        AccountValidator.ValidationMessage result = validator.runAccountValidation(account, authorizedUser, 50.0);

        assertTrue(result.passed());
        assertEquals(AccountValidator.VALIDATION_RESULT.PASS, result.getRESULT());
    }

    @Test
    public void runAccountValidation_negativeAmount_fails() {
        AccountValidator.ValidationMessage result = validator.runAccountValidation(account, authorizedUser, -50.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Negative number entered for amount"));
    }

    @Test
    public void runAccountValidation_closedAccount_fails() {
        account.setSTATUS(Account.ACCOUNT_STATUS.CLOSED);

        AccountValidator.ValidationMessage result = validator.runAccountValidation(account, authorizedUser, 50.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: ACCOUNT_STATUS not 'open'"));
    }

    @Test
    public void runAccountValidation_unauthorizedUser_fails() {
        AccountValidator.ValidationMessage result = validator.runAccountValidation(account, unauthorizedUser, 50.0);

        assertFalse(result.passed());
    }

    @Test
    public void runAccountValidation_multipleFailures_collectsMessages() {
        account.setSTATUS(Account.ACCOUNT_STATUS.CLOSED);

        AccountValidator.ValidationMessage result = validator.runAccountValidation(account, unauthorizedUser, -10.0);

        assertFalse(result.passed());
        assertEquals(AccountValidator.VALIDATION_RESULT.FAIL, result.getRESULT());
        assertTrue(result.getMsg().contains("Negative number entered for amount"));
        assertTrue(result.getMsg().contains("Validation failed: ACCOUNT_STATUS not 'open'"));
    }
}