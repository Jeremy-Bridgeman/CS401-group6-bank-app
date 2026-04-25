package bankapp;
import java.net.*;
import java.io.*;
import java.util.*;

import bankapp.Request.REQUEST_TYPE;
import bankapp.AccountValidator.ValidationMessage;
import bankapp.Log.TRANSACTION_TYPE;

public class Server {
	static String ip;
	Logger logger = Logger.getInstance();	// get our instance of logger so it is ready to use
	CheckingAccountValidator checkingValidator = new CheckingAccountValidator();
	SavingsAccountValidator savingsValidator = new SavingsAccountValidator();
	CreditAccountValidator creditValidator = new CreditAccountValidator();
	List<Account> accounts = Collections.synchronizedList(new ArrayList<Account>()); // use a thread safe data structure
	
	
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
	
    public void start() {
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
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    private static class ClientHandler implements Runnable {
        private Socket client;
        private Server server;
        private Logger logger;
        private List<Account> accounts;

        private Response nullRequestError =
                new Response("unable to process: request was null", Response.RESPONSE_TYPE.ERROR);

        private Response nullAccountError =
                new Response("unable to process: account was null", Response.RESPONSE_TYPE.ERROR);

        private Response nonPositiveNumberError =
                new Response("unable to process: input must be a positive number", Response.RESPONSE_TYPE.ERROR);

        private Response accountNotFoundError =
                new Response("unable to process: account not found on server", Response.RESPONSE_TYPE.ERROR);

        private Response insufficientFundsError =
                new Response("unable to process: insufficient funds", Response.RESPONSE_TYPE.ERROR);

        private Response sameAccountTransferError =
                new Response("unable to process: source and target accounts must be different", Response.RESPONSE_TYPE.ERROR);

        private Response managerOnlyLogsError =
                new Response("unable to process: only managers may view logs", Response.RESPONSE_TYPE.ERROR);

        private Response tellerPresenceRequiredError =
                new Response("unable to process: teller access requires customer presence", Response.RESPONSE_TYPE.ERROR);

        private Response atmWithdrawalLimitError =
                new Response("unable to process: atm withdrawals may not exceed 3000.0", Response.RESPONSE_TYPE.ERROR);

        private ClientHandler(Socket c, Server s) {
            client = c;
            server = s;
            logger = s.logger;
            accounts = s.accounts;
        }

        @Override
        public void run() {
            ObjectOutputStream o = null;
            ObjectInputStream i = null;

            try {
                System.out.println("[HANDLER] Started for client: " + client.getInetAddress().getHostAddress());

                o = new ObjectOutputStream(client.getOutputStream());
                o.flush();
                i = new ObjectInputStream(client.getInputStream());

                while (true) {
                    Object request = i.readObject();

                    if (request == null) {
                        break;
                    }

                    System.out.println("[REQUEST RECEIVED] " + formatRequest((Request) request));

                    Response response = handleRequest((Request) request);

                    System.out.println("[RESPONSE SENT] " + formatResponse(response));

                    o.writeObject(response);
                    o.flush();
                }
            }
            catch (EOFException e) {
                System.out.println("[HANDLER] Client disconnected normally: " + client.getInetAddress().getHostAddress());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            catch (Exception e) {
                e.printStackTrace();   // add this
            }
            finally {
                try { if (i != null) i.close(); } catch (IOException e) { e.printStackTrace(); }
                try { if (o != null) o.close(); } catch (IOException e) { e.printStackTrace(); }
                try { if (client != null && !client.isClosed()) client.close(); } catch (IOException e) { e.printStackTrace(); }

                System.out.println("[HANDLER] Closed connection for client: " + client.getInetAddress().getHostAddress());
            }
        }

        // ---------------- DEBUG HELPERS ----------------

        private String formatRequest(Request req) {
            if (req == null) return "null request";

            return "Request{type=" + req.getType()
                    + ", userType=" + req.getUserType()
                    + ", person=" + (req.getPerson() != null ? req.getPerson().getName() : "null")
                    + ", amount=" + req.getAmount()
                    + ", source=" + formatAccount(req.getSourceAccount())
                    + ", target=" + formatAccount(req.getTargetAccount())
                    + "}";
        }

        private String formatResponse(Response res) {
            if (res == null) return "null response";

            return "Response{type=" + res.getType()
                    + ", text=\"" + res.getText() + "\"}";
        }

        private String formatAccount(Account acc) {
            if (acc == null) return "null";

            return "Account{type=" + acc.getTYPE()
                    + ", status=" + acc.getSTATUS()
                    + ", balance=" + acc.getBalance()
                    + "}";
        }

        // ------------------------------------------------

        private Response handleRequest(Request request) {
            REQUEST_TYPE rtype = request.getType();
        	
            if (rtype == REQUEST_TYPE.DEPOSIT) {
                Response r = handleDeposit(request);
                return r;
            }
            else if (rtype == REQUEST_TYPE.WITHDRAW) {
                Response r = handleWithdraw(request);
                return r;
            }
            else if (rtype == REQUEST_TYPE.OPEN_ACCOUNT) {
                Response r = handleOpenAccount(request);
                return r;
            }
            else if (rtype == REQUEST_TYPE.CLOSE_ACCOUNT) {
                Response r = handleCloseAccount(request);
                return r;
            }
            else if (rtype == REQUEST_TYPE.TRANSFER) {
                Response r = handleTransfer(request);
                return r;
            }
            else if (rtype == REQUEST_TYPE.VIEW_ACCOUNT) {
                Response r = handleViewAccount(request);
                return r;
            }
            else if (rtype == REQUEST_TYPE.VIEW_LOGS) {
                Response r = handleViewLogs(request);
                return r;
            }
            else if (rtype == REQUEST_TYPE.OTHER) {
                Response r = handleOther(request);
                return r;
            }
            else {
                return new Response(
                        "Unknown Request, consider adding another RESPONSE_TYPE",
                        Response.RESPONSE_TYPE.INFO
                );
            }
        }

        // logs an event and returns a response in one step to reduce repeated code
        private Response logAndRespond(Account account, TRANSACTION_TYPE logType, String logMessage, double amount,
                String responseText, Response.RESPONSE_TYPE responseType) {

            // add event to logger
            logger.logEvent(new Log(logType, logMessage, amount, account.getLogKey()));

            // persist logs to file
            logger.saveLogs();

            // return response to client
            return new Response(responseText, responseType);
        }

        // converts a validation failure into a logged error response
        private Response validationErrorResponse(Account account, ValidationMessage result, double amount) {

            // log the validation error and return it as a response
            return logAndRespond(
                    account,
                    Log.TRANSACTION_TYPE.ERROR,
                    result.getMsg().toString(),
                    amount,
                    result.getMsg().toString(),
                    Response.RESPONSE_TYPE.ERROR
            );
        }

        // checks whether the requester is an atm
        private boolean isATM(Request req) {
            return req.getUserType() == Request.USER_TYPE.ATM;
        }

        // checks whether the requester is a teller
        private boolean isTeller(Request req) {
            return req.getUserType() == Request.USER_TYPE.TELLER;
        }

        // checks whether the requester is a manager
        private boolean isManager(Request req) {
            return req.getUserType() == Request.USER_TYPE.MANAGER;
        }

        // checks whether a customer is present for teller operations
        private boolean isCustomerPresent(Request req) {
            return req.isCustomerPresent();
        }

        // enforces the teller access rule that a customer must be present
        private Response validateAccess(Request req) {
            if (req == null) {
                return nullRequestError;
            }

            if (isTeller(req) && !isCustomerPresent(req)) {
                return tellerPresenceRequiredError;
            }

            return null;
        }

        // flags the account when a large transaction occurs
        private void flagLargeTransaction(Account account, double amount) {
            if (amount >= 10000.0) {
                account.flag();
            }
        }

        // selects the correct deposit validation based on account type
        private ValidationMessage runDepositValidation(Account account, Person person, double amount) {

            // checking account deposit validation
            if (account.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
                return server.checkingValidator.validateDeposit(account, person, amount);
            }
            // savings account deposit validation
            else if (account.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS) {
                return server.savingsValidator.validateDeposit(account, person, amount);
            }
            // credit account deposit (payment) validation
            else if (account.getTYPE() == Account.ACCOUNT_TYPE.CREDIT) {
                return server.creditValidator.validatePayment(account, person, amount);
            }

            // no validation available for this type
            return null;
        }

        // selects the correct withdrawal validation based on account type
        private ValidationMessage runWithdrawValidation(Account account, Person person, double amount) {

            // checking account withdrawal validation
            if (account.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
                return server.checkingValidator.validateWithdrawal(account, person, amount);
            }
            // savings account withdrawal validation
            else if (account.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS) {
                return server.savingsValidator.validateWithdrawal(account, person, amount);
            }
            // credit account withdrawal (charge) validation
            else if (account.getTYPE() == Account.ACCOUNT_TYPE.CREDIT) {
                return server.creditValidator.validateCharge(account, person, amount);
            }

            // no validation available for this type
            return null;
        }

        // selects the correct transfer validation based on source and target account types
        private ValidationMessage runTransferValidation(Account source, Account target, Person person, double amount) {

            // checking -> savings
            if (source.getTYPE() == Account.ACCOUNT_TYPE.CHECKING && target.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS) {
                return server.checkingValidator.validateTransferToSavings(source, person, amount);
            }
            // checking -> checking
            else if (source.getTYPE() == Account.ACCOUNT_TYPE.CHECKING && target.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
                return server.checkingValidator.validateTransferToChecking(source, person, amount);
            }
            // savings -> checking
            else if (source.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS && target.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
                return server.savingsValidator.validateTransferToChecking(source, person, amount);
            }
            // savings -> credit
            else if (source.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS && target.getTYPE() == Account.ACCOUNT_TYPE.CREDIT) {
                return server.savingsValidator.validateTransferToCredit(source, person, amount);
            }
            // credit -> checking
            else if (source.getTYPE() == Account.ACCOUNT_TYPE.CREDIT && target.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
                return server.creditValidator.validateTransferToChecking(source, person, amount);
            }
            // credit -> savings
            else if (source.getTYPE() == Account.ACCOUNT_TYPE.CREDIT && target.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS) {
                return server.creditValidator.validateTransferToSavings(source, person, amount);
            }

            // unsupported transfer combination
            return new AccountValidator.ValidationMessage("validation failed: unsupported transfer type");
        }

        // handles deposit requests
        private Response handleDeposit(Request req) {
            if (req == null) {
                return nullRequestError;
            }
            if (req.getSourceAccount() == null) {
                return nullAccountError;
            }
            if (req.getAmount() <= 0) {
                return nonPositiveNumberError;
            }

            // enforce access rules before continuing
            Response accessError = validateAccess(req);
            if (accessError != null) {
                return accessError;
            }

            Account account = req.getSourceAccount();

            // use synchronized so that shared account list access is safe
            synchronized (accounts) {
                if (!accounts.contains(account)) {
                    return accountNotFoundError;
                }
            }

            ValidationMessage result = runDepositValidation(account, req.getPerson(), req.getAmount());
            if (result != null && !result.passed()) {
            	return validationErrorResponse(account, result, req.getAmount());            }

            // use synchronized so that this account cannot be modified by two threads at once
            synchronized (account) {
                account.deposit(req.getAmount());
                flagLargeTransaction(account, req.getAmount());
                account.setLastUsed(new Date());
            }

            return logAndRespond(
            		account,
                    Log.TRANSACTION_TYPE.DEPOSIT,
                    "deposit successful",
                    req.getAmount(),
                    "Deposit successful",
                    Response.RESPONSE_TYPE.SUCCESS
            );
        }

        // handles withdrawal requests
        private Response handleWithdraw(Request req) {
            if (req == null) {
                return nullRequestError;
            }
            if (req.getSourceAccount() == null) {
                return nullAccountError;
            }
            if (req.getAmount() <= 0) {
                return nonPositiveNumberError;
            }

            // enforce access rules before continuing
            Response accessError = validateAccess(req);
            if (accessError != null) {
                return accessError;
            }

            // enforce the atm withdrawal limit
            if (isATM(req) && req.getAmount() > 3000.0) {
                return atmWithdrawalLimitError;
            }

            Account account = req.getSourceAccount();

            // use synchronized so that shared account list access is safe
            synchronized (accounts) {
                if (!accounts.contains(account)) {
                    return accountNotFoundError;
                }
            }

            ValidationMessage result = runWithdrawValidation(account, req.getPerson(), req.getAmount());
            if (result != null && !result.passed()) {
                return validationErrorResponse(account, result, req.getAmount());
            }

            // use synchronized so that this account cannot be modified by two threads at once
            synchronized (account) {
                if (account.getBalance() < req.getAmount()) {
                    return insufficientFundsError;
                }

                account.withdraw(req.getAmount());
                flagLargeTransaction(account, req.getAmount());
                account.setLastUsed(new Date());
            }

            return logAndRespond(
            		account,
                    Log.TRANSACTION_TYPE.WITHDRAWAL,
                    "withdrawal successful",
                    req.getAmount(),
                    "Withdrawal successful",
                    Response.RESPONSE_TYPE.SUCCESS
            );
        }

        // handles open account requests
        private Response handleOpenAccount(Request req) {
            if (req == null) {
                return nullRequestError;
            }
            if (req.getSourceAccount() == null) {
                return nullAccountError;
            }

            // enforce access rules before continuing
            Response accessError = validateAccess(req);
            if (accessError != null) {
                return accessError;
            }

            Account account = req.getSourceAccount();

            // use synchronized so that check and add happen safely together
            synchronized (accounts) {
                if (accounts.contains(account)) {
                    return new Response(
                            "unable to process: account already exists on server",
                            Response.RESPONSE_TYPE.ERROR
                    );
                }

                accounts.add(account);
            }

            return logAndRespond(
            		account,
                    Log.TRANSACTION_TYPE.OTHER,
                    "account opened",
                    0.0,
                    "Account opened successfully",
                    Response.RESPONSE_TYPE.SUCCESS
            );
        }

        // handles close account requests
        private Response handleCloseAccount(Request req) {
            if (req == null) {
                return nullRequestError;
            }
            if (req.getSourceAccount() == null) {
                return nullAccountError;
            }

            // enforce access rules before continuing
            Response accessError = validateAccess(req);
            if (accessError != null) {
                return accessError;
            }

            Account account = req.getSourceAccount();

            // use synchronized so that shared account list access is safe
            synchronized (accounts) {
                if (!accounts.contains(account)) {
                    return accountNotFoundError;
                }
            }

            // use synchronized so that this account cannot be modified by two threads at once
            synchronized (account) {
                if (account.getBalance() != 0.0) {
                    return new Response(
                            "unable to process: account balance must be zero before closing",
                            Response.RESPONSE_TYPE.ERROR
                    );
                }

                account.closeAccount();
                account.setLastUsed(new Date());
            }

            // use synchronized so that remove happens safely on the shared list
            synchronized (accounts) {
                accounts.remove(account);
            }

            return logAndRespond(
            		account,
                    Log.TRANSACTION_TYPE.OTHER,
                    "account closed",
                    0.0,
                    "Account closed successfully",
                    Response.RESPONSE_TYPE.SUCCESS
            );
        }

        // handles transfer requests
        private Response handleTransfer(Request req) {
            if (req == null) {
                return nullRequestError;
            }
            if (req.getSourceAccount() == null || req.getTargetAccount() == null) {
                return nullAccountError;
            }
            if (req.getAmount() <= 0) {
                return nonPositiveNumberError;
            }

            // enforce access rules before continuing
            Response accessError = validateAccess(req);
            if (accessError != null) {
                return accessError;
            }

            Account source = req.getSourceAccount();
            Account target = req.getTargetAccount();

            if (source == target) {
                return sameAccountTransferError;
            }

            // use synchronized so that shared account list access is safe
            synchronized (accounts) {
                if (!accounts.contains(source) || !accounts.contains(target)) {
                    return accountNotFoundError;
                }
            }

            ValidationMessage result = runTransferValidation(source, target, req.getPerson(), req.getAmount());
            if (result != null && !result.passed()) {
                return validationErrorResponse(source, result, req.getAmount());
            }

            // use nested synchronized blocks so both accounts are updated together
            synchronized (source) {
                synchronized (target) {
                    if (source.getBalance() < req.getAmount()) {
                        return insufficientFundsError;
                    }

                    source.withdraw(req.getAmount());
                    target.deposit(req.getAmount());

                    Date now = new Date();
                    source.setLastUsed(now);
                    target.setLastUsed(now);
                }
            }
            logger.logEvent(new Log(
                    Log.TRANSACTION_TYPE.TRANSFER,
                    "transfer sent to " + target.getLogKey(),
                    req.getAmount(),
                    source.getLogKey()
            ));

            logger.logEvent(new Log(
                    Log.TRANSACTION_TYPE.TRANSFER,
                    "transfer received from " + source.getLogKey(),
                    req.getAmount(),
                    target.getLogKey()
            ));

            logger.saveLogs();

            return new Response("Transfer successful", Response.RESPONSE_TYPE.SUCCESS);            
        }

        // handles account view requests
        private Response handleViewAccount(Request req) {
            if (req == null) {
                return nullRequestError;
            }
            if (req.getSourceAccount() == null) {
                return nullAccountError;
            }

            // enforce access rules before continuing
            Response accessError = validateAccess(req);
            if (accessError != null) {
                return accessError;
            }

            Account account = req.getSourceAccount();

            // use synchronized so that shared account list access is safe
            synchronized (accounts) {
                if (!accounts.contains(account)) {
                    return accountNotFoundError;
                }
            }

            // use synchronized so that the account is not changed while being read
            synchronized (account) {
                String msg = "Account type: " + account.getTYPE()
                        + ", status: " + account.getSTATUS()
                        + ", balance: " + account.getBalance()
                        + ", last used: " + account.getLastUsed();

                return new Response(msg, Response.RESPONSE_TYPE.INFO);
            }
        }

        // handles log view requests
        private Response handleViewLogs(Request req) {
            if (req == null) {
                return nullRequestError;
            }

            // only managers may view logs
            if (!isManager(req)) {
                return managerOnlyLogsError;
            }

            ArrayList<Log> logs = logger.getLogs();
            return new Response(logs.toString(), Response.RESPONSE_TYPE.LOG);
        }

        // handles uncategorized requests
        private Response handleOther(Request req) {
            return new Response("No handler implemented for OTHER request type", Response.RESPONSE_TYPE.INFO);
        }
    }
}