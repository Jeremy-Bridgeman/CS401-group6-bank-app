package bankapp;

import javax.swing.*;

import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;
import java.util.List;

public class ATMGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    // Additions and modifications
    private final ATM atm;
    private Customer customer;
    private Account account;
    private List<Account> accounts;
    private JLabel header;
    
    private JTextArea outputArea;
    private JTextField usernameField;
    private JPasswordField pinField;
    private JTextField targetAccountField;
    private JTextField amountField;
    
    private CardLayout cl;
    private JPanel container;

    public ATMGUI(ATM atm) {
        this.atm = Objects.requireNonNull(atm);
        buildUi();
        showLogin();
    }

    public ATMGUI(ATM atm, Customer customer, Account account) {
        this.atm = Objects.requireNonNull(atm);
        this.customer = Objects.requireNonNull(customer);
        this.account = Objects.requireNonNull(account);

        if (this.customer.getActiveChannel() == Customer.ACCESS_CHANNEL.NONE) {
            this.customer.startAtmSession();
        }

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

    private void buildUi() {
    	String name = (customer != null) ? customer.getName() : "Not Logged In";
        setTitle("ATM - " + name);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(640, 260);
        setLocationRelativeTo(null);

        // Card System
        cl = new CardLayout();
        container = new JPanel(cl);
        
        // Login Panel
        JPanel loginPanel = new JPanel(new GridLayout(0,1));

        usernameField = new JTextField();
        pinField = new JPasswordField();
        JButton loginBtn = new JButton("Login");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("PIN:"));
        loginPanel.add(pinField);
        loginPanel.add(loginBtn);

        // ATM Panel
        JPanel atmPanel = new JPanel(new BorderLayout(10,10));

        JPanel top = new JPanel(new GridLayout(0,1));
        header = new JLabel(
        		"Customer: " + name + " | Connected to " + atm.getConnectedServerIP()
        );
        updateHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 16));
        top.add(header);

        top.add(new JLabel("Amount:"));
        amountField = new JTextField();
        top.add(amountField);

        atmPanel.add(top, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout());

        JButton balanceBtn = new JButton("Check Balance");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton transferBtn = new JButton("Transfer");
        JButton quitBtn = new JButton("Quit");

        buttons.add(balanceBtn);
        buttons.add(depositBtn);
        buttons.add(withdrawBtn);
        buttons.add(transferBtn);
        buttons.add(quitBtn);

        atmPanel.add(buttons, BorderLayout.CENTER);

        // Button Logic
        loginBtn.addActionListener(e -> {
            try {
                String username = usernameField.getText().trim();
                int pin = Integer.parseInt(new String(pinField.getPassword()));

                Response response = atm.login(username, pin);

                if (response != null && response.isAuthenticated()) {
                    customer = response.getCustomer();
                    accounts = response.getAccounts();
                    account = chooseAccount(accounts);
                    if (account == null) {
                        JOptionPane.showMessageDialog(this, "No account selected");
                        return;
                    }
                    updateHeader();
                    showATM();
                    updateHeader();
                    showATM();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid login");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Login error: " + ex.getMessage());
            }
        });

        balanceBtn.addActionListener(e -> checkBalance());
        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        quitBtn.addActionListener(e -> quit());
        transferBtn.addActionListener(e -> transfer());
        
        // Adding to container
        container.add(loginPanel, "LOGIN");
        container.add(atmPanel, "ATM");

        setContentPane(container);
        
        // If authenticated, go straight to ATM
        if (customer != null) {
        	showATM();
        } else {
        	showLogin();
        }
    }
    
    // Transfer Method
    private void transfer() {
        try {
            double amount = parseAmount();

            String targetAccountNumber = JOptionPane.showInputDialog(
                this,
                "Enter target account number:"
            );

            if (targetAccountNumber == null || targetAccountNumber.trim().isEmpty()) {
                return; // user cancelled
            }

            Response response = atm.transfer(
                amount,
                account,
                targetAccountNumber.trim(),
                customer
            );

            showResponse(response, "Transfer");

            amountField.setText("");

        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    private void updateHeader() {
        if (customer != null) {
            header.setText(
                "Customer: " + customer.getName() +
                " | Connected to " + atm.getConnectedServerIP()
            );
        } else {
        	header.setText("Customer: Not Logged In | Connected to )"
        			+ atm.getConnectedServerIP());
        }
    }

    private void checkBalance() {
        Response response = atm.checkBalance(account, customer);
        showResponse(response, "Balance");
    }

    private void deposit() {
        try {
            double amount = parseAmount();

            if (amount > atm.getDailyDepositLimit()) {
                throw new IllegalArgumentException(
                    "amount exceeds ATM deposit limit of " + atm.getDailyDepositLimit()
                );
            }

            Response response = atm.deposit(amount, account, customer);
            showResponse(response, "Deposit");
            amountField.setText("");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void withdraw() {
        try {
            double amount = parseAmount();

            if (amount > atm.getDailyWithdrawalLimit()) {
                throw new IllegalArgumentException(
                    "amount exceeds ATM withdrawal limit of " + atm.getDailyWithdrawalLimit()
                );
            }

            Response response = atm.withdraw(amount, account, customer);
            showResponse(response, "Withdraw");
            amountField.setText("");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void quit() {
        try {
            if (customer != null && customer.getActiveChannel() != Customer.ACCESS_CHANNEL.NONE) {
                customer.endSession();
            }
        } catch (Exception ignored) { }

        atm.close();
        dispose();
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
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setPreferredSize(new Dimension(150, 40));
    }
    
    // View Switching
    private void showLogin() {
    	cl.show(container, "LOGIN");
    }
    
    private void showATM() {
    	cl.show(container, "ATM");
    }
}