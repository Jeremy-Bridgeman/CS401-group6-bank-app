package bankapp;

import java.io.Serializable;
import java.util.List;

public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String message;
    private final RESPONSE_TYPE type;
    private final String sessionId;
    private final boolean ready;
    private final int queuePosition;
    private final String assignedCustomerName;
    private final String assignedTellerName;

    private final Customer customer;
    private final Account account;
    private final boolean authenticated;
    private final List<Account> accounts;
    private final String requestedAction;
    private final double requestedAmount;
    
    public enum RESPONSE_TYPE {
        SUCCESS,
        ERROR,
        WARNING,
        INFO,
        LOG
    }

    public Response(String message, RESPONSE_TYPE type) {
        this(message, type, null, false, -1, null, null, null, null, null, false, null, 0.0);
    }
    
    public Response(
    	    String message,
    	    RESPONSE_TYPE type,
    	    String sessionId,
    	    boolean ready,
    	    int queuePosition,
    	    String assignedCustomerName,
    	    String assignedTellerName
    	) {
    	    this(message, type, sessionId, ready, queuePosition, assignedCustomerName, assignedTellerName, null, null, null, false, null, 0.0);
    	}

    public Response(
    	    String message,
    	    RESPONSE_TYPE type,
    	    String sessionId,
    	    boolean ready,
    	    int queuePosition,
    	    String assignedCustomerName,
    	    String assignedTellerName,
    	    Customer customer,
    	    Account account,
    	    List<Account> accounts,
    	    boolean authenticated
    	) {
    	    this(message, type, sessionId, ready, queuePosition, assignedCustomerName, assignedTellerName, customer, account, accounts, authenticated, null, 0.0);
    	}

    	public Response(
    	    String message,
    	    RESPONSE_TYPE type,
    	    String sessionId,
    	    boolean ready,
    	    int queuePosition,
    	    String assignedCustomerName,
    	    String assignedTellerName,
    	    Customer customer,
    	    Account account,
    	    List<Account> accounts,
    	    boolean authenticated,
    	    String requestedAction,
    	    double requestedAmount
    	) {
    	    this.message = message;
    	    this.type = type;
    	    this.sessionId = sessionId;
    	    this.ready = ready;
    	    this.queuePosition = queuePosition;
    	    this.assignedCustomerName = assignedCustomerName;
    	    this.assignedTellerName = assignedTellerName;
    	    this.customer = customer;
    	    this.account = account;
    	    this.accounts = accounts;
    	    this.authenticated = authenticated;
    	    this.requestedAction = requestedAction;
    	    this.requestedAmount = requestedAmount;
    	}

    public RESPONSE_TYPE getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public boolean isReady() {
        return ready;
    }

    public int getQueuePosition() {
        return queuePosition;
    }

    public String getAssignedCustomerName() {
        return assignedCustomerName;
    }

    public String getAssignedTellerName() {
        return assignedTellerName;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Account getAccount() {
        return account;
    }
    
    public List<Account> getAccounts() {
    	return accounts;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
    public String getRequestedAction() {
        return requestedAction;
    }

    public double getRequestedAmount() {
        return requestedAmount;
    }
}