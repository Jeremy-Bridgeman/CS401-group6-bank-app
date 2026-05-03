package bankapp;

public class ATM {
    private int failedAttempts;
    private boolean serviceCompleted;

    private final String serverIP;
    private final BankClientFacade client;

    private double dailyWithdrawalLimit = 10000;
    private double dailyDepositLimit = 10000;

    public ATM(String serverIP) {
        if (serverIP == null || serverIP.trim().isEmpty()) {
            throw new IllegalArgumentException("server IP cannot be blank");
        }

        this.serverIP = serverIP.trim();
        this.client = new BankClientFacade(this.serverIP, 7890);
        this.failedAttempts = 0;
        this.serviceCompleted = false;
    }
 
    public String getConnectedServerIP() {
        return serverIP;
    }
    
    public Response transfer(double amount, Account source, String targetAccountNumber, Person person) {
        return client.transfer(person, Request.USER_TYPE.ATM, source, null, amount);
    }

    public Response withdraw(double amount, Account account, Person person) {
        return client.withdraw(person, Request.USER_TYPE.ATM, account, amount);
    }

    public Response openAccount(Account account, Person person) {
        return client.openAccount(person, Request.USER_TYPE.ATM, account);
    }

    public Response deposit(double amount, Account account, Person person) {
        return client.deposit(person, Request.USER_TYPE.ATM, account, amount);
    }

    public Response checkBalance(Account account, Person person) {
        return client.viewAccount(person, Request.USER_TYPE.ATM, account);
    }

    public void displayConfirmation() {
        System.out.println("Transaction Successful.");
    }

    public void displayError() {
        System.out.println("Transaction Failed.");
    }

    public double getCashAmount() {
        return 0;
    }

    public Response login(String username, int pin) {
        Response response = client.authenticateCustomer(username, pin);

        if (response == null || !response.isAuthenticated()) {
            failedAttempts++;

            if (failedAttempts >= 5) {
                serviceCompleted = true;
            }

            displayError();
            if (response == null) {
                return new Response("ATM login failed", Response.RESPONSE_TYPE.ERROR);
            }
            return response;
        }

        failedAttempts = 0;
        return response;
    }

    public void close() {
        client.close();
    }

    public void logAttempt(Log log) {
        // optional local ATM logging hook
    }

    public double getDailyWithdrawalLimit() {
        return dailyWithdrawalLimit;
    }

    public double getDailyDepositLimit() {
        return dailyDepositLimit;
    }

    public int getFailedAttempts() {
        return this.failedAttempts;
    }

    public boolean getServiceCompletion() {
        return this.serviceCompleted;
    }

    public void setFailedAttempts(int amount) {
        this.failedAttempts = amount;
    }

    public void setServiceCompletion(boolean result) {
        this.serviceCompleted = result;
    }
}