package bankapp;
import java.util.*;

// this class validates the account status of a given account and ensures negative numbers are not passed as an amount
public class AccountValidator {
    public enum VALIDATION_RESULT {
        PASS,
        FAIL
    }

    // use a public nested class for data encapsulation and easy data movement
    public static class ValidationMessage {
        private ArrayList<String> msg = new ArrayList<String>();
        private Date date;
        private VALIDATION_RESULT RESULT;

        ValidationMessage() {
            msg.add("Validation successful");
            date = new Date();
            RESULT = VALIDATION_RESULT.PASS;
        }

        ValidationMessage(String m) {
            msg.add(m);
            date = new Date();
            RESULT = VALIDATION_RESULT.FAIL;
        }

        public ArrayList<String> getMsg() {
            return msg;
        }

        public Date getDate() {
            return date;
        }

        public VALIDATION_RESULT getRESULT() {
            return RESULT;
        }

        public void setMsg(ArrayList<String> msg) {
            this.msg = msg;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setRESULT(VALIDATION_RESULT rESULT) {
            RESULT = rESULT;
        }

        public boolean passed() {
            return RESULT == VALIDATION_RESULT.PASS;
        }
    }

    // check if the account is open. Fail if status is not 'open'
    public ValidationMessage validateAccountStatus(Account a) {
        if (a.getSTATUS() == Account.ACCOUNT_STATUS.OPEN) {
            return new ValidationMessage();
        }
        else {
            return new ValidationMessage("Validation failed: ACCOUNT_STATUS not 'open'");
        }
    }

    // check if the user is authorized to use the account
    public ValidationMessage validateAuthorizedUser(Account a, Person p) {
        if (a.getAuthorizedUsers().contains(p)) {
            return new ValidationMessage();
        }
        else {
            return new ValidationMessage("Validation failed: User not found in Authorized Users");
        }
    }

    // check for non-negative amount
    public ValidationMessage validateAmount(double amount) {
        if (amount >= 0) {
            return new ValidationMessage();
        }
        else {
            return new ValidationMessage("Negative number entered for amount");
        }
    }

    // facade for running all validation
    public ValidationMessage runAccountValidation(Account a, Person p, double amount) {
        ValidationMessage finalMessage = new ValidationMessage();

        ValidationMessage amountCheck = validateAmount(amount);
        ValidationMessage statusCheck = validateAccountStatus(a);
        ValidationMessage userCheck = validateAuthorizedUser(a, p);

        if (!amountCheck.passed() || !statusCheck.passed() || !userCheck.passed()) {
            finalMessage.setRESULT(VALIDATION_RESULT.FAIL);
            finalMessage.getMsg().clear();

            if (!amountCheck.passed()) {
                finalMessage.getMsg().addAll(amountCheck.getMsg());
            }

            if (!statusCheck.passed()) {
                finalMessage.getMsg().addAll(statusCheck.getMsg());
            }

            if (!userCheck.passed()) {
                finalMessage.getMsg().addAll(userCheck.getMsg());
            }
        }

        return finalMessage;
    }

    // helper: run base validation and return failure if it exists. null return value is a passed test
    protected ValidationMessage baseCheck(Account a, Person p, double amount) {
        ValidationMessage result = runAccountValidation(a, p, amount);

        if (result.passed()) {
            return null;
        }
        else {
            return result;
        }
    }

    // helper: check sufficient funds
    protected ValidationMessage validateSufficientFunds(Account a, double amount, String message) {
        if (a.getBalance() >= amount) {
            return new ValidationMessage();
        }
        else {
            return new ValidationMessage(message);
        }
    }
}