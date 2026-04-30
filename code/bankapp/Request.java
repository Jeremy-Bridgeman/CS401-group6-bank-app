package bankapp;

import java.io.Serializable;

public class Request implements Serializable {
    private static final long serialVersionUID = 1L;

    private final REQUEST_TYPE type;
    private final USER_TYPE userType;
    private final Person person;
    private final Account sourceAccount;
    private final Account targetAccount;
    private final double amount;
    private final String text;
    private final boolean customerPresent;
    private final String sessionId;

    private final String username;
    private final int pin;

    public enum REQUEST_TYPE {
        WITHDRAW,
        DEPOSIT,
        TRANSFER,
        VIEW_LOGS,
        VIEW_ACCOUNT,
        OPEN_ACCOUNT,
        CLOSE_ACCOUNT,
        AUTHENTICATE_CUSTOMER,
        FIND_CUSTOMER,
        JOIN_TELLER_QUEUE,
        CHECK_TELLER_QUEUE,
        TELLER_READY,
        TELLER_POLL_ASSIGNMENT,
        SUBMIT_TELLER_TRANSACTION_REQUEST,
        TELLER_POLL_CUSTOMER_REQUEST,
        END_TELLER_SESSION,
        OTHER
    }

    public enum USER_TYPE {
        CUSTOMER,
        TELLER,
        MANAGER,
        ATM
    }

    public Request(
        REQUEST_TYPE t,
        USER_TYPE u,
        Person p,
        Account s,
        Account target,
        double a,
        String txt,
        boolean customerPresent,
        String sessionID,
        String username,
        int pin
    ) {
        type = t;
        userType = u;
        person = p;
        sourceAccount = s;
        targetAccount = target;
        amount = a;
        text = txt;
        this.customerPresent = customerPresent;
        sessionId = sessionID;
        this.username = username;
        this.pin = pin;
    }

    public REQUEST_TYPE getType() {
        return type;
    }

    public USER_TYPE getUserType() {
        return userType;
    }

    public Person getPerson() {
        return person;
    }

    public Account getSourceAccount() {
        return sourceAccount;
    }

    public Account getTargetAccount() {
        return targetAccount;
    }

    public double getAmount() {
        return amount;
    }

    public String getText() {
        return text;
    }

    public boolean isCustomerPresent() {
        return customerPresent;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getUsername() {
        return username;
    }

    public int getPin() {
        return pin;
    }
}