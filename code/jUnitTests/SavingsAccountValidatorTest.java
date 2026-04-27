package jUnitTests;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import bankapp.Account;
import bankapp.AccountValidator;
import bankapp.Person;
import bankapp.SavingsAccount;
import bankapp.SavingsAccountValidator;

public class SavingsAccountValidatorTest {

    private SavingsAccountValidator validator;
    private Person authorizedUser;
    private Person unauthorizedUser;
    private Account account;

    @BeforeEach
    public void setUp() {
        validator = new SavingsAccountValidator();
        authorizedUser = new Person();
        unauthorizedUser = new Person();
        account = ValidatorTestHelper.makeAccount(
        	    new SavingsAccount(
        	        800.0,
        	        Account.ACCOUNT_STATUS.OPEN,
        	        Account.ACCOUNT_TYPE.SAVINGS,
        	        authorizedUser
        	    ),
        	    authorizedUser,
        	    800.0
        	);        account.setTYPE(Account.ACCOUNT_TYPE.SAVINGS);
    }

    @Test
    public void validateDeposit_validDeposit_passes() {
        AccountValidator.ValidationMessage result = validator.validateDeposit(account, authorizedUser, 100.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateDeposit_negativeDeposit_fails() {
        AccountValidator.ValidationMessage result = validator.validateDeposit(account, authorizedUser, -1.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Negative number entered for amount"));
    }

    @Test
    public void validateWithdrawal_sufficientFunds_passes() {
        AccountValidator.ValidationMessage result = validator.validateWithdrawal(account, authorizedUser, 200.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateWithdrawal_insufficientFunds_fails() {
        AccountValidator.ValidationMessage result = validator.validateWithdrawal(account, authorizedUser, 1200.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: Insufficient funds for savings withdrawal"));
    }

    @Test
    public void validateTransferToChecking_sufficientFunds_passes() {
        AccountValidator.ValidationMessage result = validator.validateTransferToChecking(account, authorizedUser, 300.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateTransferToChecking_insufficientFunds_fails() {
        AccountValidator.ValidationMessage result = validator.validateTransferToChecking(account, authorizedUser, 900.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: Insufficient funds for transfer to checking"));
    }

    @Test
    public void validateTransferToCredit_sufficientFunds_passes() {
        AccountValidator.ValidationMessage result = validator.validateTransferToCredit(account, authorizedUser, 300.0);

        assertTrue(result.passed());
    }

    @Test
    public void validateTransferToCredit_insufficientFunds_fails() {
        AccountValidator.ValidationMessage result = validator.validateTransferToCredit(account, authorizedUser, 900.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: Insufficient funds for transfer to credit"));
    }

    @Test
    public void validateWithdrawal_unauthorizedUser_fails() {
        AccountValidator.ValidationMessage result = validator.validateWithdrawal(account, unauthorizedUser, 100.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: User not found in Authorized Users"));
    }

    @Test
    public void validateWithdrawal_closedAccount_fails() {
        account.setSTATUS(Account.ACCOUNT_STATUS.CLOSED);

        AccountValidator.ValidationMessage result = validator.validateWithdrawal(account, authorizedUser, 100.0);

        assertFalse(result.passed());
        assertTrue(result.getMsg().contains("Validation failed: ACCOUNT_STATUS not 'open'"));
    }
}
