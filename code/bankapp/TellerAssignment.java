package bankapp;

import java.io.Serializable;
import java.util.Objects;

// this class helps assign a customer to a teller at a teller window

public class TellerAssignment implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String sessionId;
    private final Teller teller;
    private final Customer customer;
    private final Account account;

    public TellerAssignment(String sessionId, Teller teller, Customer customer, Account account) {
        this.sessionId = Objects.requireNonNull(sessionId, "sessionId cannot be null");
        this.teller = Objects.requireNonNull(teller, "teller cannot be null");
        this.customer = Objects.requireNonNull(customer, "customer cannot be null");
        this.account = account;
    }

    public String getSessionId() {
        return sessionId;
    }

    public Teller getTeller() {
        return teller;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Account getAccount() {
        return account;
    }
}