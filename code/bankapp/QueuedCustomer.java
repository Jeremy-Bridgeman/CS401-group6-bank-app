package bankapp;

import java.io.Serializable;
import java.util.Objects;

// we can store this in our queue and pull the relevant information when it is needed
public class QueuedCustomer implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sessionId;
    private final Customer customer;
    private final Account account;

    public QueuedCustomer(String sessionId, Customer customer, Account account) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
        this.customer = Objects.requireNonNull(customer, "customer cannot be null");
        this.account = account;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Account getAccount() {
        return account;
    }
}