package bankapp;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

public class BankClientFacade {
    private final String host;
    private final int port;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public BankClientFacade(String host, int port) {
        if (host == null || host.trim().isEmpty()) {
            throw new IllegalArgumentException("host cannot be blank");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be greater than 0");
        }

        this.host = host.trim();
        this.port = port;
    }

    public void connect() throws Exception {
        if (socket != null && socket.isConnected() && !socket.isClosed()) {
            return;
        }

        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void close() {
        try {
            if (in != null) in.close();
        } catch (Exception ignored) { }

        try {
            if (out != null) out.close();
        } catch (Exception ignored) { }

        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) { }

        in = null;
        out = null;
        socket = null;
    }

    public Response send(Request request) {
        if (request == null) {
            return new Response("communication error: request was null", Response.RESPONSE_TYPE.ERROR);
        }

        try {
            connect();

            if (out == null) {
                return new Response("communication error: output stream was not initialized", Response.RESPONSE_TYPE.ERROR);
            }
            if (in == null) {
                return new Response("communication error: input stream was not initialized", Response.RESPONSE_TYPE.ERROR);
            }

            out.reset();
            out.writeObject(request);
            out.flush();

            Object response = in.readObject();

            if (response == null) {
                return new Response("communication error: server response was null", Response.RESPONSE_TYPE.ERROR);
            }
            if (!(response instanceof Response)) {
                return new Response(
                    "communication error: server returned unexpected object type " + response.getClass().getName(),
                    Response.RESPONSE_TYPE.ERROR
                );
            }

            return (Response) response;
        } catch (EOFException e) {
            close();
            return new Response("connection closed by server", Response.RESPONSE_TYPE.ERROR);
        } catch (Exception e) {
            close();
            return new Response(
                "communication error: " + e.getClass().getName() + ": " + e.getMessage(),
                Response.RESPONSE_TYPE.ERROR
            );
        }
    }

    public String createSessionId() {
        return UUID.randomUUID().toString();
    }

    private boolean resolveCustomerPresent(Person person, Request.USER_TYPE userType) {
        if (userType == null) {
            throw new IllegalArgumentException("user type cannot be null");
        }
        if (person == null) {
            throw new IllegalArgumentException("person cannot be null");
        }

        if (userType == Request.USER_TYPE.TELLER || userType == Request.USER_TYPE.MANAGER) {
            try {
                Teller teller = (Teller) person;
                return teller.isCustomerPresent();
            } catch (ClassCastException e) {
                throw new IllegalStateException("person must be a Teller (or Manager) when user type is " + userType);
            }
        }

        return true;
    }

    private Request buildRequest(
    	    Request.REQUEST_TYPE requestType,
    	    Person person,
    	    Request.USER_TYPE userType,
    	    Account sourceAccount,
    	    Account targetAccount,
    	    double amount,
    	    String text,
    	    String sessionId,
    	    String username,
    	    int pin
    	) {
    	    if (requestType == null) {
    	        throw new IllegalArgumentException("request type cannot be null");
    	    }
    	    if (userType == null) {
    	        throw new IllegalArgumentException("user type cannot be null");
    	    }
    	    if (person == null) {
    	        throw new IllegalArgumentException("person cannot be null");
    	    }

    	    boolean customerPresent = resolveCustomerPresent(person, userType);

    	    return new Request(
    	        requestType,
    	        userType,
    	        person,
    	        sourceAccount,
    	        targetAccount,
    	        amount,
    	        text,
    	        customerPresent,
    	        sessionId,
    	        username,
    	        pin
    	    );
    	}
    
    private Request buildRequest(
    	    Request.REQUEST_TYPE requestType,
    	    Person person,
    	    Request.USER_TYPE userType,
    	    Account sourceAccount,
    	    Account targetAccount,
    	    double amount,
    	    String text,
    	    String sessionId
    	) {
    	    return buildRequest(requestType, person, userType, sourceAccount, targetAccount, amount, text, sessionId, null, 0);
    	}

    	private Request buildRequest(
    	    Request.REQUEST_TYPE requestType,
    	    Person person,
    	    Request.USER_TYPE userType,
    	    Account sourceAccount,
    	    Account targetAccount,
    	    double amount,
    	    String text
    	) {
    	    return buildRequest(requestType, person, userType, sourceAccount, targetAccount, amount, text, null, null, 0);
    	}

    public Response openAccount(Person person, Request.USER_TYPE userType, Account account) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.OPEN_ACCOUNT,
                person,
                userType,
                account,
                null,
                0.0,
                "Open account request"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("open account failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response deposit(Person person, Request.USER_TYPE userType, Account account, double amount) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.DEPOSIT,
                person,
                userType,
                account,
                null,
                amount,
                "Deposit request"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("deposit failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response withdraw(Person person, Request.USER_TYPE userType, Account account, double amount) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.WITHDRAW,
                person,
                userType,
                account,
                null,
                amount,
                "Withdraw request"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("withdraw failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response transfer(Person person, Request.USER_TYPE userType, Account source, Account target, double amount) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.TRANSFER,
                person,
                userType,
                source,
                target,
                amount,
                "Transfer request"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("transfer failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response viewAccount(Person person, Request.USER_TYPE userType, Account account) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.VIEW_ACCOUNT,
                person,
                userType,
                account,
                null,
                0.0,
                "View account request"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("view account failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response closeAccount(Person person, Request.USER_TYPE userType, Account account) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.CLOSE_ACCOUNT,
                person,
                userType,
                account,
                null,
                0.0,
                "Close account request"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("close account failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response viewLogs(Manager manager) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.VIEW_LOGS,
                manager,
                Request.USER_TYPE.MANAGER,
                null,
                null,
                0.0,
                "View logs request"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("view logs failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response joinTellerQueue(Customer customer, Account account, String sessionId) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.JOIN_TELLER_QUEUE,
                customer,
                Request.USER_TYPE.CUSTOMER,
                account,
                null,
                0.0,
                "Join teller queue",
                sessionId
            );
            return send(request);
        } catch (Exception e) {
            return new Response("join teller queue failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response checkTellerQueue(Customer customer, String sessionId) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.CHECK_TELLER_QUEUE,
                customer,
                Request.USER_TYPE.CUSTOMER,
                null,
                null,
                0.0,
                "Check teller queue",
                sessionId
            );
            return send(request);
        } catch (Exception e) {
            return new Response("check teller queue failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }
    
    public Response submitTellerTransactionRequest(Customer customer, String sessionId, String action, double amount) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.SUBMIT_TELLER_TRANSACTION_REQUEST,
                customer,
                Request.USER_TYPE.CUSTOMER,
                null,
                null,
                amount,
                action,
                sessionId
            );
            return send(request);
        } catch (Exception e) {
            return new Response("submit teller transaction request failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response tellerPollCustomerRequest(Teller teller) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.TELLER_POLL_CUSTOMER_REQUEST,
                teller,
                Request.USER_TYPE.TELLER,
                null,
                null,
                0.0,
                "Teller poll customer request"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("teller poll customer request failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }
    
    public Response markTellerTransactionComplete(Teller teller, String sessionId, Account account, String message) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.MARK_TELLER_TRANSACTION_COMPLETE,
                teller,
                Request.USER_TYPE.TELLER,
                account,
                null,
                0.0,
                message,
                sessionId
            );
            return send(request);
        } catch (Exception e) {
            return new Response("mark teller transaction complete failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response pollTellerTransactionResult(Customer customer, String sessionId) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.POLL_TELLER_TRANSACTION_RESULT,
                customer,
                Request.USER_TYPE.CUSTOMER,
                null,
                null,
                0.0,
                "Poll teller transaction result",
                sessionId
            );
            return send(request);
        } catch (Exception e) {
            return new Response("poll teller transaction result failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response tellerReady(Teller teller) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.TELLER_READY,
                teller,
                Request.USER_TYPE.TELLER,
                null,
                null,
                0.0,
                "Teller ready"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("teller ready failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response tellerPollAssignment(Teller teller) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.TELLER_POLL_ASSIGNMENT,
                teller,
                Request.USER_TYPE.TELLER,
                null,
                null,
                0.0,
                "Teller poll assignment"
            );
            return send(request);
        } catch (Exception e) {
            return new Response("teller assignment poll failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    public Response endTellerSession(Teller teller, String sessionId) {
        try {
            Request request = buildRequest(
                Request.REQUEST_TYPE.END_TELLER_SESSION,
                teller,
                Request.USER_TYPE.TELLER,
                null,
                null,
                0.0,
                "End teller session",
                sessionId
            );
            return send(request);
        } catch (Exception e) {
            return new Response("end teller session failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }
    
    public Response authenticateCustomer(String username, int pin) {
        try {
            Customer probe = new Customer("", "", new Address(), username, pin);

            Request request = buildRequest(
                Request.REQUEST_TYPE.AUTHENTICATE_CUSTOMER,
                probe,
                Request.USER_TYPE.CUSTOMER,
                null,
                null,
                0.0,
                "Authenticate customer",
                null,
                username,
                pin
            );

            return send(request);
        } catch (Exception e) {
            return new Response("customer authentication failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }

    
    
    public Response findCustomer(String username) {
        try {
            Customer probe = new Customer("", "", new Address(), username, 0);

            Request request = buildRequest(
                Request.REQUEST_TYPE.FIND_CUSTOMER,
                probe,
                Request.USER_TYPE.CUSTOMER,
                null,
                null,
                0.0,
                "Find customer",
                null,
                username,
                0
            );

            return send(request);
        } catch (Exception e) {
            return new Response("find customer failed: " + e.getMessage(), Response.RESPONSE_TYPE.ERROR);
        }
    }
}