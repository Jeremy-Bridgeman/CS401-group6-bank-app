package bankapp;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import bankapp.AccountValidator.ValidationMessage;
import bankapp.Log.TRANSACTION_TYPE;
import bankapp.Request.REQUEST_TYPE;

class ClientHandler implements Runnable {
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

    ClientHandler(Socket c, Server s) {
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
        } catch (EOFException e) {
            System.out.println("[HANDLER] Client disconnected normally: " + client.getInetAddress().getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (i != null) i.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (o != null) o.close(); } catch (IOException e) { e.printStackTrace(); }
            try { if (client != null && !client.isClosed()) client.close(); } catch (IOException e) { e.printStackTrace(); }

            System.out.println("[HANDLER] Closed connection for client: " + client.getInetAddress().getHostAddress());
        }
    }

    private String formatRequest(Request req) {
        if (req == null) return "null request";

        return "Request{type=" + req.getType()
            + ", userType=" + req.getUserType()
            + ", person=" + (req.getPerson() != null ? req.getPerson().getName() : "null")
            + ", sessionId=" + req.getSessionId()
            + ", amount=" + req.getAmount()
            + ", source=" + formatAccount(req.getSourceAccount())
            + ", target=" + formatAccount(req.getTargetAccount())
            + "}";
    }

    private String formatResponse(Response res) {
        if (res == null) return "null response";

        return "Response{type=" + res.getType()
            + ", text=\"" + res.getMessage() + "\""
            + ", ready=" + res.isReady()
            + ", queuePosition=" + res.getQueuePosition()
            + "}";
    }

    private String formatAccount(Account acc) {
        if (acc == null) return "null";

        return "Account{type=" + acc.getTYPE()
            + ", status=" + acc.getSTATUS()
            + ", balance=" + acc.getBalance()
            + "}";
    }

    private Response handleRequest(Request request) {
        REQUEST_TYPE rtype = request.getType();

        if (rtype == REQUEST_TYPE.DEPOSIT) {
            return handleDeposit(request);
        } else if (rtype == REQUEST_TYPE.WITHDRAW) {
            return handleWithdraw(request);
        } else if (rtype == REQUEST_TYPE.OPEN_ACCOUNT) {
            return handleOpenAccount(request);
        } else if (rtype == REQUEST_TYPE.CLOSE_ACCOUNT) {
            return handleCloseAccount(request);
        } else if (rtype == REQUEST_TYPE.TRANSFER) {
            return handleTransfer(request);
        } else if (rtype == REQUEST_TYPE.VIEW_ACCOUNT) {
            return handleViewAccount(request);
        } else if (rtype == REQUEST_TYPE.VIEW_LOGS) {
            return handleViewLogs(request);
        } else if (rtype == REQUEST_TYPE.JOIN_TELLER_QUEUE) {
            return handleJoinTellerQueue(request);
        } else if (rtype == REQUEST_TYPE.CHECK_TELLER_QUEUE) {
            return handleCheckTellerQueue(request);
        } else if (rtype == REQUEST_TYPE.TELLER_READY) {
            return handleTellerReady(request);
        } else if (rtype == REQUEST_TYPE.TELLER_POLL_ASSIGNMENT) {
            return handleTellerPollAssignment(request);
        } else if (rtype == REQUEST_TYPE.SUBMIT_TELLER_TRANSACTION_REQUEST) {
            return handleSubmitTellerTransactionRequest(request);
        } else if (rtype == REQUEST_TYPE.TELLER_POLL_CUSTOMER_REQUEST) {
            return handleTellerPollCustomerRequest(request);
        } else if (rtype == REQUEST_TYPE.END_TELLER_SESSION) {
            return handleEndTellerSession(request);
        } else if (rtype == REQUEST_TYPE.AUTHENTICATE_CUSTOMER) {
            return handleAuthenticateCustomer(request);
        } else if (rtype == REQUEST_TYPE.FIND_CUSTOMER) {
            return handleFindCustomer(request);
        } else if (rtype == REQUEST_TYPE.OTHER) {
            return handleOther(request);
        } else {
            return new Response("Unknown Request", Response.RESPONSE_TYPE.INFO);
        }
    }
    
    private Response handleSubmitTellerTransactionRequest(Request req) {
        if (!(req.getPerson() instanceof Customer)) {
            return new Response("unable to submit teller transaction request: requester was not a customer", Response.RESPONSE_TYPE.ERROR);
        }
        return server.submitTellerTransactionRequest(req.getSessionId(), req.getText(), req.getAmount());
    }

    private Response handleTellerPollCustomerRequest(Request req) {
        if (!(req.getPerson() instanceof Teller)) {
            return new Response("unable to poll customer request: requester was not a teller", Response.RESPONSE_TYPE.ERROR);
        }
        return server.pollCustomerRequest((Teller) req.getPerson());
    }
    
    private Response handleAuthenticateCustomer(Request req) {
        if (req == null) {
            return nullRequestError;
        }

        return server.authenticateCustomer(req.getUsername(), req.getPin());
    }

    private Response handleFindCustomer(Request req) {
        if (req == null) {
            return nullRequestError;
        }

        return server.findCustomer(req.getUsername());
    }

    private Response logAndRespond(Account account, TRANSACTION_TYPE logType, String logMessage, double amount,
            String responseText, Response.RESPONSE_TYPE responseType) {
        logger.logEvent(new Log(logType, logMessage, amount, account.getLogKey()));
        logger.saveLogs();
        return new Response(responseText, responseType);
    }

    private Response validationErrorResponse(Account account, ValidationMessage result, double amount) {
        return logAndRespond(
            account,
            Log.TRANSACTION_TYPE.ERROR,
            result.getMsg().toString(),
            amount,
            result.getMsg().toString(),
            Response.RESPONSE_TYPE.ERROR
        );
    }

    private boolean isATM(Request req) {
        return req.getUserType() == Request.USER_TYPE.ATM;
    }

    private boolean isTeller(Request req) {
        return req.getUserType() == Request.USER_TYPE.TELLER;
    }

    private boolean isManager(Request req) {
        return req.getUserType() == Request.USER_TYPE.MANAGER;
    }

    private boolean isCustomerPresent(Request req) {
        return req.isCustomerPresent();
    }

    private Response validateAccess(Request req) {
        if (req == null) {
            return nullRequestError;
        }

        if (isTeller(req) && !isCustomerPresent(req)) {
            return tellerPresenceRequiredError;
        }

        return null;
    }

    private void flagLargeTransaction(Account account, double amount) {
        if (amount >= 10000.0) {
            account.flag();
        }
    }

    private ValidationMessage runDepositValidation(Account account, Person person, double amount) {
        if (account.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
            return server.checkingValidator.validateDeposit(account, person, amount);
        } else if (account.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS) {
            return server.savingsValidator.validateDeposit(account, person, amount);
        } else if (account.getTYPE() == Account.ACCOUNT_TYPE.CREDIT) {
            return server.creditValidator.validatePayment(account, person, amount);
        }
        return null;
    }

    private ValidationMessage runWithdrawValidation(Account account, Person person, double amount) {
        if (account.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
            return server.checkingValidator.validateWithdrawal(account, person, amount);
        } else if (account.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS) {
            return server.savingsValidator.validateWithdrawal(account, person, amount);
        } else if (account.getTYPE() == Account.ACCOUNT_TYPE.CREDIT) {
            return server.creditValidator.validateCharge(account, person, amount);
        }
        return null;
    }

    private ValidationMessage runTransferValidation(Account source, Account target, Person person, double amount) {
        if (source.getTYPE() == Account.ACCOUNT_TYPE.CHECKING && target.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS) {
            return server.checkingValidator.validateTransferToSavings(source, person, amount);
        } else if (source.getTYPE() == Account.ACCOUNT_TYPE.CHECKING && target.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
            return server.checkingValidator.validateTransferToChecking(source, person, amount);
        } else if (source.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS && target.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
            return server.savingsValidator.validateTransferToChecking(source, person, amount);
        } else if (source.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS && target.getTYPE() == Account.ACCOUNT_TYPE.CREDIT) {
            return server.savingsValidator.validateTransferToCredit(source, person, amount);
        } else if (source.getTYPE() == Account.ACCOUNT_TYPE.CREDIT && target.getTYPE() == Account.ACCOUNT_TYPE.CHECKING) {
            return server.creditValidator.validateTransferToChecking(source, person, amount);
        } else if (source.getTYPE() == Account.ACCOUNT_TYPE.CREDIT && target.getTYPE() == Account.ACCOUNT_TYPE.SAVINGS) {
            return server.creditValidator.validateTransferToSavings(source, person, amount);
        }

        return new AccountValidator.ValidationMessage("validation failed: unsupported transfer type");
    }

    private Response handleJoinTellerQueue(Request req) {
        if (!(req.getPerson() instanceof Customer)) {
            return new Response("unable to join teller queue: requester was not a customer", Response.RESPONSE_TYPE.ERROR);
        }

        return server.joinTellerQueue((Customer) req.getPerson(), req.getSourceAccount(), req.getSessionId());
    }

    private Response handleCheckTellerQueue(Request req) {
        if (!(req.getPerson() instanceof Customer)) {
            return new Response("unable to check teller queue: requester was not a customer", Response.RESPONSE_TYPE.ERROR);
        }
        return server.checkTellerQueue(req.getSessionId());
    }

    private Response handleTellerReady(Request req) {
        if (!(req.getPerson() instanceof Teller)) {
            return new Response("unable to mark teller ready: requester was not a teller", Response.RESPONSE_TYPE.ERROR);
        }
        return server.tellerReady((Teller) req.getPerson());
    }

    private Response handleTellerPollAssignment(Request req) {
        if (!(req.getPerson() instanceof Teller)) {
            return new Response("unable to poll teller assignment: requester was not a teller", Response.RESPONSE_TYPE.ERROR);
        }
        return server.pollTellerAssignment((Teller) req.getPerson());
    }

    private Response handleEndTellerSession(Request req) {
        if (!(req.getPerson() instanceof Teller)) {
            return new Response("unable to end teller session: requester was not a teller", Response.RESPONSE_TYPE.ERROR);
        }
        return server.endTellerSession(req.getSessionId(), (Teller) req.getPerson());
    }

    private Response handleDeposit(Request req) {
        if (req == null) return nullRequestError;
        if (req.getSourceAccount() == null) return nullAccountError;
        if (req.getAmount() <= 0) return nonPositiveNumberError;

        Response accessError = validateAccess(req);
        if (accessError != null) return accessError;

        Account account = req.getSourceAccount();

        synchronized (accounts) {
            if (!accounts.contains(account)) {
                return accountNotFoundError;
            }
        }

        ValidationMessage result = runDepositValidation(account, req.getPerson(), req.getAmount());
        if (result != null && !result.passed()) {
            return validationErrorResponse(account, result, req.getAmount());
        }

        synchronized (account) {
            account.deposit(req.getAmount());
            flagLargeTransaction(account, req.getAmount());
            account.setLastUsed(new Date());
            server.saveAccounts();
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

    private Response handleWithdraw(Request req) {
        if (req == null) return nullRequestError;
        if (req.getSourceAccount() == null) return nullAccountError;
        if (req.getAmount() <= 0) return nonPositiveNumberError;

        Response accessError = validateAccess(req);
        if (accessError != null) return accessError;

        if (isATM(req) && req.getAmount() > 3000.0) {
            return atmWithdrawalLimitError;
        }

        Account account = req.getSourceAccount();

        synchronized (accounts) {
            if (!accounts.contains(account)) {
                return accountNotFoundError;
            }
        }

        ValidationMessage result = runWithdrawValidation(account, req.getPerson(), req.getAmount());
        if (result != null && !result.passed()) {
            return validationErrorResponse(account, result, req.getAmount());
        }

        synchronized (account) {
            if (account.getBalance() < req.getAmount()) {
                return insufficientFundsError;
            }

            account.withdraw(req.getAmount());
            flagLargeTransaction(account, req.getAmount());
            account.setLastUsed(new Date());
            server.saveAccounts();
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

    private Response handleOpenAccount(Request req) {
        if (req == null) return nullRequestError;
        if (req.getSourceAccount() == null) return nullAccountError;

        Response accessError = validateAccess(req);
        if (accessError != null) return accessError;

        Account account = req.getSourceAccount();

        synchronized (accounts) {
            if (accounts.contains(account)) {
                return new Response("unable to process: account already exists on server", Response.RESPONSE_TYPE.ERROR);
            }

            accounts.add(account);
            server.saveAccounts();
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

    private Response handleCloseAccount(Request req) {
        if (req == null) return nullRequestError;
        if (req.getSourceAccount() == null) return nullAccountError;

        Response accessError = validateAccess(req);
        if (accessError != null) return accessError;

        Account account = req.getSourceAccount();

        synchronized (accounts) {
            if (!accounts.contains(account)) {
                return accountNotFoundError;
            }
        }

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

        synchronized (accounts) {
            accounts.remove(account);
            server.saveAccounts();
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

    private Response handleTransfer(Request req) {
        if (req == null) return nullRequestError;
        if (req.getSourceAccount() == null || req.getTargetAccount() == null) return nullAccountError;
        if (req.getAmount() <= 0) return nonPositiveNumberError;

        Response accessError = validateAccess(req);
        if (accessError != null) return accessError;

        Account source = req.getSourceAccount();
        Account target = req.getTargetAccount();

        if (source == target) {
            return sameAccountTransferError;
        }

        synchronized (accounts) {
            if (!accounts.contains(source) || !accounts.contains(target)) {
                return accountNotFoundError;
            }
        }

        ValidationMessage result = runTransferValidation(source, target, req.getPerson(), req.getAmount());
        if (result != null && !result.passed()) {
            return validationErrorResponse(source, result, req.getAmount());
        }

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
        server.saveAccounts();

        return new Response("Transfer successful", Response.RESPONSE_TYPE.SUCCESS);
    }

    private Response handleViewAccount(Request req) {
        if (req == null) return nullRequestError;
        if (req.getSourceAccount() == null) return nullAccountError;

        Response accessError = validateAccess(req);
        if (accessError != null) return accessError;

        Account account = req.getSourceAccount();

        synchronized (accounts) {
            if (!accounts.contains(account)) {
                return accountNotFoundError;
            }
        }

        synchronized (account) {
            String msg = "Account type: " + account.getTYPE()
                + ", status: " + account.getSTATUS()
                + ", balance: " + account.getBalance()
                + ", last used: " + account.getLastUsed();

            return new Response(msg, Response.RESPONSE_TYPE.INFO);
        }
    }

    private Response handleViewLogs(Request req) {
        if (req == null) {
            return nullRequestError;
        }

        if (!isManager(req)) {
            return managerOnlyLogsError;
        }

        ArrayList<Log> logs = logger.getLogs();

        if (logs.isEmpty()) {
            return new Response("No logs available.", Response.RESPONSE_TYPE.LOG);
        }

        String output = "Bank Activity Logs\n";
        output += "========================================\n";

        for (Log log : logs) {
            output += "Date: " + log.getDate() + "\n";
            output += "Type: " + log.getType() + "\n";
            output += "Comment: " + log.getComment() + "\n";
            output += "Amount: " + log.getAmount() + "\n";
            output += "Account: " + log.getAccountKey() + "\n";
            output += "----------------------------------------\n";
        }

        return new Response(output, Response.RESPONSE_TYPE.LOG);
    }

    private Response handleOther(Request req) {
        return new Response("No handler implemented for OTHER request type", Response.RESPONSE_TYPE.INFO);
    }
}