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
    // For new accounts
    private String firstName;
    private String lastName;
    private Account.ACCOUNT_TYPE accountType;

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
        MARK_TELLER_TRANSACTION_COMPLETE,
        POLL_TELLER_TRANSACTION_RESULT,
        END_TELLER_SESSION,
        GET_ALL_ACCOUNTS,
        CREATE_CUSTOMER_AND_ACCOUNT,
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
        String firstName,
        String lastName,
        Account.ACCOUNT_TYPE accountType,
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
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountType = accountType;
        this.pin = pin;
    }
    // Request 
    public static Request transaction(
    	    REQUEST_TYPE type,
    	    USER_TYPE userType,
    	    Person person,
    	    Account source,
    	    Account target,
    	    double amount,
    	    String text,
    	    String sessionId
    	) {
    	    return new Request(
    	        type,
    	        userType,
    	        person,
    	        source,
    	        target,
    	        amount,
    	        text,
    	        false,
    	        sessionId,
    	        null,
    	        null,
    	        null,
    	        null,
    	        0
    	    );
    	}
    // Authentication
    public static Request auth(String username, int pin) {
        return new Request(
            REQUEST_TYPE.AUTHENTICATE_CUSTOMER,
            USER_TYPE.CUSTOMER,
            null,
            null,
            null,
            0,
            "Auth",
            false,
            null,
            username,
            null,
            null,
            null,
            pin
        );
    }
    //Customer and account
    public static Request createCustomerAccount(
    	    Manager manager,
    	    String first,
    	    String last,
    	    String username,
    	    int pin,
    	    Account.ACCOUNT_TYPE type
    	) {
    	    return new Request(
    	        REQUEST_TYPE.CREATE_CUSTOMER_AND_ACCOUNT,
    	        USER_TYPE.MANAGER,
    	        manager,
    	        null,
    	        null,
    	        0,
    	        "Create account",
    	        false,
    	        null,
    	        username,
    	        first,
    	        last,
    	        type,
    	        pin
    	    );
    	}
    
    
    public String getFirstName() {
    	return firstName;
    }
    ;
    public String getLastName() {
    	return lastName;
    }
    public Account.ACCOUNT_TYPE getAccountType() {
    	return accountType;
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