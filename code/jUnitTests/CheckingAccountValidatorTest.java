package jUnitTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bankapp.Account;
import bankapp.AccountValidator;
import bankapp.CheckingAccount;
import bankapp.CheckingAccountValidator;
import bankapp.Person;

public class CheckingAccountValidatorTest {

    private CheckingAccountValidator validator;
    private Person authorizedUser;
    private Person unauthorizedUser;
    private Account account;

    @BeforeEach
    public void setUp() {
        validator = new CheckingAccountValidator();
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
        	);        account.setTYPE(Account.ACCOUNT_TYPE.CHECKING);
    }

    @Test
    public void validateDeposit_validDeposit_passes() {
        AccountValidator.ValidationMessage result = validator.validateDeposit(account, authorizedUser, 100.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateDeposit_negativeDeposit_fails() {
        AccountValidator.ValidationMessage result = validator.validateDeposit(account, authorizedUser, -100.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Negative number entered for amount"));
    }

    @Test
    public void validateWithdrawal_sufficientFunds_passes() {
        AccountValidator.ValidationMessage result = validator.validateWithdrawal(account, authorizedUser, 100.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateWithdrawal_insufficientFunds_fails() {
        AccountValidator.ValidationMessage result = validator.validateWithdrawal(account, authorizedUser, 700.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: Insufficient funds for withdrawal"));
    }

    @Test
    public void validateTransferToSavings_sufficientFunds_passes() {
        AccountValidator.ValidationMessage result = validator.validateTransferToSavings(account, authorizedUser, 200.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateTransferToSavings_insufficientFunds_fails() {
        AccountValidator.ValidationMessage result = validator.validateTransferToSavings(account, authorizedUser, 600.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: Insufficient funds for transfer to savings"));
    }

    @Test
    public void validateTransferToChecking_sufficientFunds_passes() {
        AccountValidator.ValidationMessage result = validator.validateTransferToChecking(account, authorizedUser, 200.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateTransferToChecking_insufficientFunds_fails() {
        AccountValidator.ValidationMessage result = validator.validateTransferToChecking(account, authorizedUser, 600.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: Insufficient funds for transfer to checking"));
    }

    @Test
    public void validateWithdrawal_closedAccount_fails() {
        account.setSTATUS(Account.ACCOUNT_STATUS.CLOSED);

        AccountValidator.ValidationMessage result = validator.validateWithdrawal(account, authorizedUser, 100.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: ACCOUNT_STATUS not 'open'"));
    }
}