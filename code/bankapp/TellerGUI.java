package bankapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class TellerGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Teller teller;
    private Customer customer;
    private Account account;
    private final BankClientFacade client;

    private JTextField amountField;
    private JLabel sessionLabel;
    private JLabel accountLabel;
    private String currentSessionId;
    private java.util.List<Account> customerAccounts;
    
    public TellerGUI(Teller teller, Customer customer, Account account, BankClientFacade client) {
        this.teller = Objects.requireNonNull(teller);
        this.customer = customer;
        this.account = account;
        this.client = Objects.requireNonNull(client);

        buildUi();
    }
    
    private Account chooseAccount(java.util.List<Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return null;
        }

        String[] options = new String[accounts.size()];
        for (int i = 0; i < accounts.size(); i++) {
            Account a = accounts.get(i);
            options[i] = a.getTYPE() + " | #" + a.getAccountNumber() + " | Balance: " + a.getBalance();
        }

        int selected = JOptionPane.showOptionDialog(
            this,
            "Choose an account:",
            "Select Account",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (selected < 0) {
            return null;
        }

        return accounts.get(selected);
    }
    
    private Account.ACCOUNT_TYPE promptAccountType() {
        Object[] options = {"Checking", "Savings", "Credit"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Choose account type:",
            "Account Type",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice == 0) return Account.ACCOUNT_TYPE.CHECKING;
        if (choice == 1) return Account.ACCOUNT_TYPE.SAVINGS;
        if (choice == 2) return Account.ACCOUNT_TYPE.CREDIT;
        return null;
    }
    
    private Account buildAccountForCustomer(Customer customer, Account.ACCOUNT_TYPE type) {
        if (type == Account.ACCOUNT_TYPE.CHECKING) {
            return new CheckingAccount(0.0, Account.ACCOUNT_STATUS.OPEN, Account.ACCOUNT_TYPE.CHECKING, customer);
        }
        if (type == Account.ACCOUNT_TYPE.SAVINGS) {
            return new SavingsAccount(0.0, Account.ACCOUNT_STATUS.OPEN, Account.ACCOUNT_TYPE.SAVINGS, customer);
        }
        if (type == Account.ACCOUNT_TYPE.CREDIT) {
            return new CreditAccount(0.0, Account.ACCOUNT_STATUS.OPEN, Account.ACCOUNT_TYPE.CREDIT, customer);
        }
        return null;
    }
    
    private void loadOrOnboardCustomer() {
        try {
            if (teller.isCustomerPresent()) {
                throw new IllegalStateException("end the current session before loading another customer");
            }

            String username = JOptionPane.showInputDialog(this, "Customer username:");
            if (username == null || username.trim().isEmpty()) {
                return;
            }

            Response response = client.findCustomer(username.trim());
            if (response == null) {
                showError("no response from server");
                return;
            }

            if (response.getCustomer() != null) {
                this.customer = response.getCustomer();
                this.customerAccounts = response.getAccounts();

                if (customerAccounts == null || customerAccounts.isEmpty()) {
                    this.account = response.getAccount();
                } else {
                    this.account = chooseAccount(customerAccounts);
                }

                if (this.account == null) {
                    return;
                }

                teller.beginSession(customer);
                if (customer.getActiveChannel() == Customer.ACCESS_CHANNEL.NONE) {
                    customer.startTellerSession();
                }
                sessionLabel.setText("Serving: " + customer.getName());
                showResponse(new Response("Loaded existing customer account", Response.RESPONSE_TYPE.SUCCESS), "Customer");
                return;
            }

            int create = JOptionPane.showConfirmDialog(
                this,
                "No customer found. Create new customer?",
                "Create Customer",
                JOptionPane.YES_NO_OPTION
            );

            if (create == JOptionPane.YES_OPTION) {
                createNewCustomerAndFirstAccount(username.trim());
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    private void createNewCustomerAndFirstAccount(String defaultUsername) {
        try {
            String first = JOptionPane.showInputDialog(this, "Customer first name:");
            if (first == null || first.trim().isEmpty()) return;

            String last = JOptionPane.showInputDialog(this, "Customer last name:");
            if (last == null || last.trim().isEmpty()) return;

            String username = defaultUsername;
            if (username == null || username.trim().isEmpty()) {
                username = JOptionPane.showInputDialog(this, "Customer username:");
                if (username == null || username.trim().isEmpty()) return;
            }

            String pinText = JOptionPane.showInputDialog(this, "Customer PIN:");
            if (pinText == null || pinText.trim().isEmpty()) return;

            Account.ACCOUNT_TYPE type = promptAccountType();
            if (type == null) return;

            Response existing = client.findCustomer(username.trim());
            if (existing != null && existing.getCustomer() != null) {
                showError("customer already exists; load them instead");
                return;
            }

            int pin = Integer.parseInt(pinText.trim());
            Customer newCustomer = new Customer(first.trim(), last.trim(), new Address(), username.trim(), pin);
            Account newAccount = buildAccountForCustomer(newCustomer, type);

            Response response = client.openAccount(teller, Request.USER_TYPE.TELLER, newAccount);
            if (response != null && response.getType() == Response.RESPONSE_TYPE.SUCCESS) {
                this.customer = newCustomer;
                this.account = newAccount;
                this.customerAccounts = new java.util.ArrayList<Account>();
                this.customerAccounts.add(newAccount);

                teller.beginSession(newCustomer);
                newCustomer.startTellerSession();
                sessionLabel.setText("Serving: " + newCustomer.getName());
            }

            showResponse(response, "Create Account");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    private void openAdditionalAccount() {
        try {
            if (customer == null) {
                throw new IllegalStateException("load a customer first");
            }

            Account.ACCOUNT_TYPE type = promptAccountType();
            if (type == null) return;

            Account newAccount = buildAccountForCustomer(customer, type);
            Response response = client.openAccount(teller, Request.USER_TYPE.TELLER, newAccount);

            if (response != null && response.getType() == Response.RESPONSE_TYPE.SUCCESS) {
                Response refreshed = client.findCustomer(customer.getUsername());
                if (refreshed != null && refreshed.getCustomer() != null) {
                    this.customer = refreshed.getCustomer();
                    this.customerAccounts = refreshed.getAccounts();
                    if (customerAccounts != null && !customerAccounts.isEmpty()) {
                        this.account = chooseAccount(customerAccounts);
                    }
                }
            }

            showResponse(response, "Open New Account");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    private void chooseExistingAccount() {
        try {
            if (customer == null) {
                throw new IllegalStateException("load a customer first");
            }

            Response refreshed = client.findCustomer(customer.getUsername());
            if (refreshed == null || refreshed.getCustomer() == null) {
                throw new IllegalStateException("unable to refresh customer accounts");
            }

            this.customer = refreshed.getCustomer();
            this.customerAccounts = refreshed.getAccounts();

            Account selected = chooseAccount(customerAccounts);
            if (selected != null) {
                this.account = selected;
                sessionLabel.setText("Serving: " + customer.getName() + " | Active: " + account.getTYPE());
                refreshAccountLabel();
            }
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void buildUi() {
        setTitle("Teller Console - " + teller.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(960, 420);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Teller: " + teller.getName() + " | Register #" + teller.getRegisterNumber());
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        sessionLabel = new JLabel("No active customer.");
        sessionLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        accountLabel = new JLabel("No active account.");
        accountLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel amountPanel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel("Amount:");
        label.setFont(new Font("SansSerif", Font.PLAIN, 18));

        amountField = new JTextField();
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        amountField.setPreferredSize(new Dimension(200, 40));

        amountPanel.add(label, BorderLayout.WEST);
        amountPanel.add(amountField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        
        JButton readyBtn = new JButton("Ready for Next Customer");
        JButton loadCustomerBtn = new JButton("Load / Onboard Customer");
        JButton chooseAccountBtn = new JButton("Choose Account");
        JButton openNewAccountBtn = new JButton("Open New Account");
        JButton checkRequestBtn = new JButton("Check Customer Request");
        JButton balanceBtn = new JButton("Check Balance");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton endSessionBtn = new JButton("End Session");

        styleButton(readyBtn);
        styleButton(loadCustomerBtn);
        styleButton(chooseAccountBtn);
        styleButton(openNewAccountBtn);
        styleButton(checkRequestBtn);
        styleButton(balanceBtn);
        styleButton(depositBtn);
        styleButton(withdrawBtn);
        styleButton(endSessionBtn);

        readyBtn.addActionListener(e -> readyForNextCustomer());
        loadCustomerBtn.addActionListener(e -> loadOrOnboardCustomer());
        chooseAccountBtn.addActionListener(e -> chooseExistingAccount());
        openNewAccountBtn.addActionListener(e -> openAdditionalAccount());
        checkRequestBtn.addActionListener(e -> checkCustomerRequest());
        balanceBtn.addActionListener(e -> checkBalance());
        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        endSessionBtn.addActionListener(e -> endSession());
        
        buttonPanel.add(readyBtn);
        buttonPanel.add(loadCustomerBtn);
        buttonPanel.add(chooseAccountBtn);
        buttonPanel.add(openNewAccountBtn);
        buttonPanel.add(checkRequestBtn);
        buttonPanel.add(balanceBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(endSessionBtn);
        
        center.add(sessionLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(accountLabel);
        center.add(Box.createVerticalStrut(10));
        center.add(amountPanel);
        JScrollPane scrollPane = new JScrollPane(buttonPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        center.add(scrollPane);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
    }
    
    private void refreshAccountLabel() {
        if (account == null) {
            accountLabel.setText("No active account.");
            return;
        }

        accountLabel.setText(
            "Account: " + account.getTYPE()
            + " | Status: " + account.getSTATUS()
            + " | Balance: " + account.getBalance()
        );
    }
    
    private void checkCustomerRequest() {
        try {
            requireActiveSession();

            Response response = client.tellerPollCustomerRequest(teller);
            if (response == null) {
                showError("no response from server");
                return;
            }

            if (response.getType() != Response.RESPONSE_TYPE.SUCCESS) {
                showResponse(response, "Customer Request");
                return;
            }

            String action = response.getRequestedAction();
            double amount = response.getRequestedAmount();

            if (action == null || amount <= 0) {
                showResponse(new Response("customer request payload was incomplete", Response.RESPONSE_TYPE.ERROR), "Customer Request");
                return;
            }

            amountField.setText(String.valueOf(amount));
            showResponse(
                new Response("Customer requested " + action.toLowerCase() + " of " + amount, Response.RESPONSE_TYPE.INFO),
                "Customer Request"
            );
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void readyForNextCustomer() {
        try {
            if (teller.isCustomerPresent()) {
                throw new IllegalStateException("end the current session before taking the next customer");
            }

            Response response = client.tellerReady(teller);
            if (response == null) {
                showError("no response from server");
                return;
            }

            if (response.isReady()) {
                assignCustomerFromResponse(response);
                showResponse(response, "Assigned");
                return;
            }

            showResponse(response, "Waiting");

            Timer timer = new Timer(2000, e -> {
                Response poll = client.tellerPollAssignment(teller);
                if (poll != null && poll.isReady()) {
                    ((Timer) e.getSource()).stop();
                    try {
                        assignCustomerFromResponse(poll);
                        showResponse(poll, "Assigned");
                    } catch (Exception ex) {
                        showError(ex.getMessage());
                    }
                }
            });
            timer.start();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    private void assignCustomerFromResponse(Response response) {
        currentSessionId = response.getSessionId();

        if (response.getCustomer() == null) {
            throw new IllegalStateException("server did not return assigned customer data");
        }

        this.customer = response.getCustomer();
        this.account = response.getAccount();
        this.customerAccounts = response.getAccounts();

        teller.beginSession(customer);
        if (customer.getActiveChannel() == Customer.ACCESS_CHANNEL.NONE) {
            customer.startTellerSession();
        }

        if (this.account == null) {
            sessionLabel.setText("Serving: " + customer.getName() + " | No account yet - click Open New Account");
        } else {
            sessionLabel.setText("Serving: " + customer.getName());
        }

        refreshAccountLabel();
    }

    private void checkBalance() {
        try {
            requireActiveSession();
            Response response = client.viewAccount(teller, Request.USER_TYPE.TELLER, account);
            showResponse(response, "Balance");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void deposit() {
        try {
            requireActiveSession();
            double amount = parseAmount();
            Response response = client.deposit(teller, Request.USER_TYPE.TELLER, account, amount);

            if (response != null && response.getType() != Response.RESPONSE_TYPE.ERROR) {
                if (response.getAccount() != null) {
                    this.account = response.getAccount();
                }
                refreshAccountLabel();

                if (currentSessionId != null) {
                    client.markTellerTransactionComplete(
                        teller,
                        currentSessionId,
                        account,
                        "Deposit completed. Account: " + account.getTYPE()
                            + ", Status: " + account.getSTATUS()
                            + ", Balance: " + account.getBalance()
                    );
                }
            }

            showResponse(response, "Deposit");
            amountField.setText("");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void withdraw() {
        try {
            requireActiveSession();
            double amount = parseAmount();
            Response response = client.withdraw(teller, Request.USER_TYPE.TELLER, account, amount);

            if (response != null && response.getType() != Response.RESPONSE_TYPE.ERROR) {
                if (response.getAccount() != null) {
                    this.account = response.getAccount();
                }
                refreshAccountLabel();

                if (currentSessionId != null) {
                    client.markTellerTransactionComplete(
                        teller,
                        currentSessionId,
                        account,
                        "Withdrawal completed. Account: " + account.getTYPE()
                            + ", Status: " + account.getSTATUS()
                            + ", Balance: " + account.getBalance()
                    );
                }
            }

            showResponse(response, "Withdraw");
            amountField.setText("");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void endSession() {
        try {
            if (currentSessionId != null) {
                Response response = client.endTellerSession(teller, currentSessionId);
                if (teller.isCustomerPresent()) {
                    teller.endSession();
                }
                if (customer != null && customer.getActiveChannel() != Customer.ACCESS_CHANNEL.NONE) {
                    customer.endSession();
                }
                customer = null;
                account = null;
                currentSessionId = null;
                sessionLabel.setText("No active customer.");
                refreshAccountLabel();
                showResponse(response, "Session");
                readyForNextCustomer();
                return;
            }

            if (teller.isCustomerPresent()) {
                teller.endSession();
            }
            if (customer != null && customer.getActiveChannel() != Customer.ACCESS_CHANNEL.NONE) {
                customer.endSession();
            }
            customer = null;
            account = null;
            currentSessionId = null;
            sessionLabel.setText("No active customer.");
            refreshAccountLabel();
            readyForNextCustomer();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private double parseAmount() {
        String text = amountField.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("enter an amount");
        }

        try {
            double amount = Double.parseDouble(text.trim());
            if (amount <= 0) {
                throw new IllegalArgumentException("amount must be greater than 0");
            }
            return amount;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("amount must be a valid number");
        }
    }

    private void showResponse(Response response, String title) {
        if (response == null) {
            showError("operation failed: response was null");
            return;
        }

        String text = response.getMessage();
        if (text == null || text.trim().isEmpty()) {
            text = "operation completed but response text was empty";
        }

        int messageType =
            response.getType() == Response.RESPONSE_TYPE.ERROR
                ? JOptionPane.ERROR_MESSAGE
                : JOptionPane.INFORMATION_MESSAGE;

        JOptionPane.showMessageDialog(this, text, title, messageType);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setPreferredSize(new Dimension(170, 40));
    }

    private void requireActiveSession() {
        if (!teller.isCustomerPresent()) {
            throw new IllegalStateException("cannot complete teller operation: no customer is present at the register");
        }
        if (account == null) {
            throw new IllegalStateException("no customer account is assigned to this teller session");
        }
    }
}