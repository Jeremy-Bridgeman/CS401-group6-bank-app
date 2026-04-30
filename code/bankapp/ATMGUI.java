package bankapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.util.Objects;

public class ATMGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final ATM atm;
    private Customer customer;
    private Account account;

    private JTextField amountField;

    public ATMGUI(ATM atm) {
        this.atm = Objects.requireNonNull(atm);
        authenticateAndBuildUi();
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

    private void authenticateAndBuildUi() {
        String username = JOptionPane.showInputDialog(this, "Username:");
        if (username == null || username.trim().isEmpty()) {
            dispose();
            return;
        }

        String pinText = JOptionPane.showInputDialog(this, "PIN:");
        if (pinText == null || pinText.trim().isEmpty()) {
            dispose();
            return;
        }

        try {
            int pin = Integer.parseInt(pinText.trim());
            Response response = atm.login(username.trim(), pin);

            if (response == null
                || !response.isAuthenticated()
                || response.getCustomer() == null
                || response.getAccount() == null) {

                JOptionPane.showMessageDialog(
                    this,
                    response != null ? response.getMessage() : "login failed",
                    "ATM Login",
                    JOptionPane.ERROR_MESSAGE
                );
                dispose();
                return;
            }

            this.customer = response.getCustomer();

            java.util.List<Account> accountList = response.getAccounts();
            if (accountList != null && !accountList.isEmpty()) {
                this.account = chooseAccount(accountList);
                if (this.account == null) {
                    dispose();
                    return;
                }
            } else {
                this.account = response.getAccount();
            }

            if (this.customer.getActiveChannel() == Customer.ACCESS_CHANNEL.NONE) {
                this.customer.startAtmSession();
            }

            buildUi();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "ATM Login", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
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
        setTitle("ATM - " + customer.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(640, 260);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel(
            "Customer: " + customer.getName() + " | Connected to " + atm.getConnectedServerIP()
        );
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel amountPanel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel("Amount:");
        label.setFont(new Font("SansSerif", Font.PLAIN, 18));

        amountField = new JTextField();
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 18));

        amountPanel.add(label, BorderLayout.WEST);
        amountPanel.add(amountField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton balanceBtn = new JButton("Check Balance");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton quitBtn = new JButton("Quit");

        styleButton(balanceBtn);
        styleButton(depositBtn);
        styleButton(withdrawBtn);
        styleButton(quitBtn);

        balanceBtn.addActionListener(e -> checkBalance());
        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        quitBtn.addActionListener(e -> quit());

        buttonPanel.add(balanceBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(quitBtn);

        center.add(amountPanel);
        center.add(buttonPanel);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
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
}