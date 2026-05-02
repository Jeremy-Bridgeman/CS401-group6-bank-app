package bankapp;

import java.io.*;
import java.net.*;
import java.util.*;


public class Server {
    static String ip;

    Logger logger = Logger.getInstance();
    CheckingAccountValidator checkingValidator = new CheckingAccountValidator();
    SavingsAccountValidator savingsValidator = new SavingsAccountValidator();
    CreditAccountValidator creditValidator = new CreditAccountValidator();

    List<Account> accounts = Collections.synchronizedList(new ArrayList<Account>());

    private final Queue<QueuedCustomer> tellerQueue = new ArrayDeque<QueuedCustomer>();
    private final Map<Integer, Teller> readyTellersByRegister = new HashMap<Integer, Teller>();
    private final Map<String, TellerAssignment> assignmentsBySessionId = new HashMap<String, TellerAssignment>();
    private final Map<String, String> pendingCustomerRequestActionBySessionId = new HashMap<String, String>();
    private final Map<String, Double> pendingCustomerRequestAmountBySessionId = new HashMap<String, Double>();
    private final Map<String, Response> completedTransactionBySessionId = new HashMap<String, Response>();
    
    
    

    private static final String ACCOUNTS_FILE = "accounts.dat";

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
    }

    private void loadAccounts() {
        File file = new File(ACCOUNTS_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            Object obj = in.readObject();
            if (obj instanceof List<?>) {
                accounts.clear();
                accounts.addAll((List<Account>) obj);
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Creates a Demo Account
    private void ensureDemoAccountExists() {
        synchronized (accounts) {

            for (Account account : accounts) {
                for (Person p : account.getAuthorizedUsers()) {
                    if (p instanceof Customer c) {
                        if ("demo".equals(c.getUsername())) {
                            return; // already exists
                        }
                    }
                }
            }

            System.out.println("[SERVER] Creating demo account...");

            Customer demoCustomer = new Customer(
                "Demo",
                "User",
                new Address(),
                "demo",
                1234
            );

            Account demoAccount = new Account(
                1000.0,
                Account.ACCOUNT_STATUS.OPEN,
                Account.ACCOUNT_TYPE.CHECKING,
                demoCustomer
            );

            accounts.add(demoAccount);
            saveAccounts();

            System.out.println("[SERVER] Demo account created: demo / 1234");
        }
    }
    

    public synchronized void saveAccounts() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ACCOUNTS_FILE))) {
            out.writeObject(new ArrayList<Account>(accounts));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private java.util.List<Account> findAllAccountsForCustomer(Customer customer) {
        java.util.List<Account> matches = new java.util.ArrayList<Account>();

        if (customer == null) {
            return matches;
        }

        synchronized (accounts) {
            for (Account account : accounts) {
                if (account == null) {
                    continue;
                }

                for (Person person : account.getAuthorizedUsers()) {
                    if (person instanceof Customer) {
                        Customer existing = (Customer) person;
                        if (existing.getUsername().equals(customer.getUsername())) {
                            matches.add(account);
                            break;
                        }
                    }
                }
            }
        }

        return matches;
    }
    
    private Account findServerAccountForCustomer(Customer customer, Account requestedAccount) {
        if (customer == null) {
            return null;
        }

        synchronized (accounts) {
            for (Account serverAccount : accounts) {
                if (serverAccount == null) {
                    continue;
                }

                if (requestedAccount != null && serverAccount.equals(requestedAccount)) {
                    return serverAccount;
                }

                for (Person person : serverAccount.getAuthorizedUsers()) {
                    if (person instanceof Customer) {
                        Customer existing = (Customer) person;
                        if (existing.getUsername().equals(customer.getUsername())) {
                            return serverAccount;
                        }
                    }
                }
            }
        }

        return null;
    }

    public synchronized Response joinTellerQueue(Customer customer, Account account, String sessionId) {
        if (customer == null) {
            return new Response("unable to join teller queue: customer was null", Response.RESPONSE_TYPE.ERROR);
        }
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return new Response("unable to join teller queue: session id was blank", Response.RESPONSE_TYPE.ERROR);
        }

        Account serverAccount = null;
        if (account != null) {
            serverAccount = findServerAccountForCustomer(customer, account);
            if (serverAccount == null) {
                return new Response(
                    "unable to join teller queue: account not found on server",
                    Response.RESPONSE_TYPE.ERROR
                );
            }
        }

        if (assignmentsBySessionId.containsKey(sessionId)) {
            TellerAssignment assignment = assignmentsBySessionId.get(sessionId);
            return new Response(
            	    "teller session is already ready",
            	    Response.RESPONSE_TYPE.SUCCESS,
            	    sessionId,
            	    true,
            	    -1,
            	    assignment.getCustomer().getName(),
            	    assignment.getTeller().getName(),
            	    assignment.getCustomer(),
            	    assignment.getAccount(),
            	    null,
            	    false
            	);
        }

        int position = 1;
        for (QueuedCustomer queued : tellerQueue) {
            if (queued.getSessionId().equals(sessionId)) {
                return new Response(
                    "customer is already in the teller queue",
                    Response.RESPONSE_TYPE.INFO,
                    sessionId,
                    false,
                    position,
                    null,
                    null
                );
            }
            position++;
        }

        tellerQueue.add(new QueuedCustomer(sessionId, customer, serverAccount));
        matchReadyTellerWithNextCustomer();

        if (assignmentsBySessionId.containsKey(sessionId)) {
            TellerAssignment assignment = assignmentsBySessionId.get(sessionId);
            return new Response(
            	    "teller is ready now",
            	    Response.RESPONSE_TYPE.SUCCESS,
            	    sessionId,
            	    true,
            	    -1,
            	    assignment.getCustomer().getName(),
            	    assignment.getTeller().getName(),
            	    assignment.getCustomer(),
            	    assignment.getAccount(),
            	    null,
            	    false
            	);
        }

        return new Response(
        	account == null ? "joined teller queue as new customer" : "joined teller queue",
            Response.RESPONSE_TYPE.INFO,
            sessionId,
            false,
            getQueuePosition(sessionId),
            null,
            null
        );
    }

    public synchronized Response checkTellerQueue(String sessionId) {
        TellerAssignment assignment = assignmentsBySessionId.get(sessionId);

        if (assignment != null) {
            return new Response(
                "teller session ready",
                Response.RESPONSE_TYPE.SUCCESS,
                sessionId,
                true,
                -1,
                assignment.getCustomer().getName(),
                assignment.getTeller().getName(),
                assignment.getCustomer(),
                assignment.getAccount(),
                null,
                false
            );
        }

        int position = getQueuePosition(sessionId);
        if (position > 0) {
            return new Response(
                "still waiting in teller queue",
                Response.RESPONSE_TYPE.INFO,
                sessionId,
                false,
                position,
                null,
                null,
                null,
                null,
                null,
                false
            );
        }

        return new Response(
            "session not found in teller queue",
            Response.RESPONSE_TYPE.WARNING,
            sessionId,
            false,
            -1,
            null,
            null,
            null,
            null,
            null,
            false
        );
    }
    
    public synchronized Response tellerReady(Teller teller) {
        readyTellersByRegister.put(teller.getRegisterNumber(), teller);
        matchReadyTellerWithNextCustomer();

        TellerAssignment assignment = findAssignmentForTeller(teller.getRegisterNumber());
        if (assignment != null) {
        	return new Response(
        		    "customer assigned to teller",
        		    Response.RESPONSE_TYPE.SUCCESS,
        		    assignment.getSessionId(),
        		    true,
        		    -1,
        		    assignment.getCustomer().getName(),
        		    teller.getName(),
        		    assignment.getCustomer(),
        		    assignment.getAccount(),
        		    null,
        		    false
        		);
        }

        return new Response(
            "teller marked ready and waiting for next customer",
            Response.RESPONSE_TYPE.INFO,
            null,
            false,
            -1,
            null,
            teller.getName(),
            null,
            null,
            null,
            false
        );
    }
    
    public synchronized Response pollTellerAssignment(Teller teller) {
        if (teller == null) {
            return new Response("unable to poll teller assignment: teller was null", Response.RESPONSE_TYPE.ERROR);
        }

        TellerAssignment assignment = findAssignmentForTeller(teller.getRegisterNumber());
        if (assignment != null) {
        	return new Response(
        		    "customer assigned to teller",
        		    Response.RESPONSE_TYPE.SUCCESS,
        		    assignment.getSessionId(),
        		    true,
        		    -1,
        		    assignment.getCustomer().getName(),
        		    teller.getName(),
        		    assignment.getCustomer(),
        		    assignment.getAccount(),
        		    null,
        		    false
        		);
        }

        return new Response(
        	    "no customer assigned yet",
        	    Response.RESPONSE_TYPE.INFO,
        	    null,
        	    false,
        	    -1,
        	    null,
        	    teller.getName(),
        	    null,
        	    null,
        	    null,
        	    false
        	);
    }
    
    public synchronized Response submitTellerTransactionRequest(String sessionId, String action, double amount) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return new Response("unable to submit teller transaction request: session id was blank", Response.RESPONSE_TYPE.ERROR);
        }

        TellerAssignment assignment = assignmentsBySessionId.get(sessionId);
        if (assignment == null) {
            return new Response("unable to submit teller transaction request: no active teller session found", Response.RESPONSE_TYPE.ERROR);
        }

        if (!"DEPOSIT".equals(action) && !"WITHDRAW".equals(action)) {
            return new Response("unable to submit teller transaction request: unsupported action", Response.RESPONSE_TYPE.ERROR);
        }

        if (amount <= 0) {
            return new Response("unable to submit teller transaction request: amount must be greater than 0", Response.RESPONSE_TYPE.ERROR);
        }

        pendingCustomerRequestActionBySessionId.put(sessionId, action);
        pendingCustomerRequestAmountBySessionId.put(sessionId, amount);

        return new Response("request sent to teller", Response.RESPONSE_TYPE.SUCCESS);
    }

    public synchronized Response pollCustomerRequest(Teller teller) {
        if (teller == null) {
            return new Response("unable to poll customer request: teller was null", Response.RESPONSE_TYPE.ERROR);
        }

        TellerAssignment assignment = findAssignmentForTeller(teller.getRegisterNumber());
        if (assignment == null) {
            return new Response("no active teller session", Response.RESPONSE_TYPE.INFO);
        }

        String sessionId = assignment.getSessionId();
        String action = pendingCustomerRequestActionBySessionId.get(sessionId);
        Double amount = pendingCustomerRequestAmountBySessionId.get(sessionId);

        if (action == null || amount == null) {
            return new Response("no customer request pending", Response.RESPONSE_TYPE.INFO);
        }

        pendingCustomerRequestActionBySessionId.remove(sessionId);
        pendingCustomerRequestAmountBySessionId.remove(sessionId);

        return new Response(
            "customer requested " + action.toLowerCase(),
            Response.RESPONSE_TYPE.SUCCESS,
            sessionId,
            true,
            -1,
            assignment.getCustomer().getName(),
            teller.getName(),
            assignment.getCustomer(),
            assignment.getAccount(),
            null,
            false,
            action,
            amount
        );
    }
    
    public synchronized Response markTellerTransactionComplete(String sessionId, Account account, String message) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return new Response("unable to mark teller transaction complete: session id was blank", Response.RESPONSE_TYPE.ERROR);
        }

        TellerAssignment assignment = assignmentsBySessionId.get(sessionId);
        if (assignment == null) {
            return new Response("unable to mark teller transaction complete: no active teller session found", Response.RESPONSE_TYPE.ERROR);
        }

        Account updatedAccount = account != null ? account : assignment.getAccount();
        if (updatedAccount == null) {
            return new Response("unable to mark teller transaction complete: account was null", Response.RESPONSE_TYPE.ERROR);
        }

        String finalMessage = message;
        if (finalMessage == null || finalMessage.trim().isEmpty()) {
            finalMessage = "Transaction completed. Balance: " + updatedAccount.getBalance()
                + ", Status: " + updatedAccount.getSTATUS();
        }

        Response completion = new Response(
            finalMessage,
            Response.RESPONSE_TYPE.SUCCESS,
            sessionId,
            true,
            -1,
            assignment.getCustomer().getName(),
            assignment.getTeller().getName(),
            assignment.getCustomer(),
            updatedAccount,
            null,
            false
        );

        completedTransactionBySessionId.put(sessionId, completion);
        return new Response("transaction completion stored", Response.RESPONSE_TYPE.SUCCESS);
    }

    public synchronized Response pollTellerTransactionResult(String sessionId) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return new Response("unable to poll teller transaction result: session id was blank", Response.RESPONSE_TYPE.ERROR);
        }

        Response completion = completedTransactionBySessionId.remove(sessionId);
        if (completion != null) {
            return completion;
        }

        return new Response("no completed teller transaction yet", Response.RESPONSE_TYPE.INFO);
    }

    public synchronized Response endTellerSession(String sessionId, Teller teller) {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return new Response("unable to end teller session: session id was blank", Response.RESPONSE_TYPE.ERROR);
        }

        TellerAssignment assignment = assignmentsBySessionId.remove(sessionId);
        pendingCustomerRequestActionBySessionId.remove(sessionId);
        pendingCustomerRequestAmountBySessionId.remove(sessionId);
        completedTransactionBySessionId.remove(sessionId);

        if (assignment == null) {
            return new Response("no active teller session found", Response.RESPONSE_TYPE.WARNING);
        }

        if (teller != null) {
            readyTellersByRegister.put(teller.getRegisterNumber(), teller);
        }

        matchReadyTellerWithNextCustomer();

        return new Response("teller session ended", Response.RESPONSE_TYPE.SUCCESS);
    }
    
    public synchronized TellerAssignment getAssignmentBySessionId(String sessionId) {
        return assignmentsBySessionId.get(sessionId);
    }

    private int getQueuePosition(String sessionId) {
        int position = 1;
        for (QueuedCustomer queued : tellerQueue) {
            if (queued.getSessionId().equals(sessionId)) {
                return position;
            }
            position++;
        }
        return -1;
    }

    private TellerAssignment findAssignmentForTeller(int registerNumber) {
        for (TellerAssignment assignment : assignmentsBySessionId.values()) {
            if (assignment.getTeller().getRegisterNumber() == registerNumber) {
                return assignment;
            }
        }
        return null;
    }

    private void matchReadyTellerWithNextCustomer() {
        if (tellerQueue.isEmpty() || readyTellersByRegister.isEmpty()) {
            return;
        }

        Teller teller = readyTellersByRegister.values().iterator().next();
        if (teller == null) {
            return;
        }

        readyTellersByRegister.remove(teller.getRegisterNumber());

        QueuedCustomer queued = tellerQueue.poll();
        if (queued == null) {
            return;
        }

        TellerAssignment assignment = new TellerAssignment(
            queued.getSessionId(),
            teller,
            queued.getCustomer(),
            queued.getAccount()
        );

        assignmentsBySessionId.put(queued.getSessionId(), assignment);
    }
    
    public synchronized Response authenticateCustomer(String username, int pin) {
        if (username == null || username.trim().isEmpty()) {
            return new Response("username cannot be blank", Response.RESPONSE_TYPE.ERROR);
        }

        String normalized = username.trim();

        synchronized (accounts) {
            for (Account account : accounts) {
                if (account == null) {
                    continue;
                }

                for (Person person : account.getAuthorizedUsers()) {
                    if (person instanceof Customer) {
                        Customer customer = (Customer) person;

                        if (normalized.equals(customer.getUsername()) && customer.verifyPin(pin)) {
                            java.util.List<Account> matches = findAllAccountsForCustomer(customer);

                            return new Response(
                                "customer authenticated",
                                Response.RESPONSE_TYPE.SUCCESS,
                                null,
                                false,
                                -1,
                                null,
                                null,
                                customer,
                                account,
                                matches,
                                true
                            );
                        }
                    }
                }
            }
        }

        return new Response(
            "invalid username or PIN",
            Response.RESPONSE_TYPE.ERROR,
            null,
            false,
            -1,
            null,
            null,
            null,
            null,
            null,
            false
        );
    }

    public synchronized Response findCustomer(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new Response("username cannot be blank", Response.RESPONSE_TYPE.ERROR);
        }

        String normalized = username.trim();

        synchronized (accounts) {
            for (Account account : accounts) {
                if (account == null) {
                    continue;
                }

                for (Person person : account.getAuthorizedUsers()) {
                    if (person instanceof Customer) {
                        Customer customer = (Customer) person;
                        java.util.List<Account> matches = findAllAccountsForCustomer(customer);
                        if (normalized.equals(customer.getUsername())) {
                            return new Response(
                                "customer found",
                                Response.RESPONSE_TYPE.SUCCESS,
                                null,
                                false,
                                -1,
                                null,
                                null,
                                customer,
                                account,
                                matches,
                                false
                            );
                        }
                    }
                }
            }
        }

        return new Response(
	        "customer not found", 
	        Response.RESPONSE_TYPE.WARNING, 
	        null, 
	        false, 
	        -1, 
	        null, 
	        null, 
	        null, 
	        null, 
	        null, 
	        false
        );
    }

    public void start() {
        loadAccounts();
        // For testing purposes
        ensureDemoAccountExists();
        
        try {
            ServerSocket serverSocket = new ServerSocket(7890);
            serverSocket.setReuseAddress(true);

            System.out.println("[SERVER] Listening on port 7890...");

            while (true) {
                Socket client = serverSocket.accept();

                System.out.println("[SERVER] New client connected from: " + client.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(client, this);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}