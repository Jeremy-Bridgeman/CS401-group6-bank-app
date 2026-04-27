package bankapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class TellerGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Teller teller;
    private final Account account;
    private final BankClientFacade client;
    private final Customer customer;

    private JTextField amountField;

    public TellerGUI(Teller teller, Customer customer, Account account, BankClientFacade client) {
        this.teller = Objects.requireNonNull(teller);
        this.customer = Objects.requireNonNull(customer);
        this.account = Objects.requireNonNull(account);
        this.client = Objects.requireNonNull(client);

        buildUi();
    }

    private void buildUi() {
        setTitle("Teller Console - " + teller.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 250);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Teller: " + teller.getName() + " | Register #" + teller.getRegisterNumber());
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel amountPanel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel("Amount:");
        label.setFont(new Font("SansSerif", Font.PLAIN, 18));

        amountField = new JTextField();
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        amountField.setPreferredSize(new Dimension(200, 40));

        amountPanel.add(label, BorderLayout.WEST);
        amountPanel.add(amountField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JButton balanceBtn = new JButton("Check Balance");
        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton startSessionBtn = new JButton("Start Session");
        JButton endSessionBtn = new JButton("End Session");

        styleButton(balanceBtn);
        styleButton(depositBtn);
        styleButton(withdrawBtn);
        styleButton(startSessionBtn);
        styleButton(endSessionBtn);

        balanceBtn.addActionListener(e -> checkBalance());
        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        startSessionBtn.addActionListener(e -> startSession());
        endSessionBtn.addActionListener(e -> endSession());

        buttonPanel.add(balanceBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(startSessionBtn);
        buttonPanel.add(endSessionBtn);

        center.add(amountPanel);
        center.add(buttonPanel);

        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
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
            showResponse(response, "Withdraw");
            amountField.setText("");
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

        String text = response.getText();
        if (text == null || text.trim().isEmpty()) {
            text = "operation completed but response text was empty";
        }

        int messageType =
            response.getType() == Response.RESPONSE_TYPE.ERROR
                ? JOptionPane.ERROR_MESSAGE
                : JOptionPane.INFORMATION_MESSAGE;

        JOptionPane.showMessageDialog(this, text, title, messageType);
        JOptionPane.showMessageDialog(this, response.getMessage(), title, messageType);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setPreferredSize(new Dimension(150, 40));
    }
    
    private void startSession() {
        try {
            teller.beginSession(customer);
            customer.startTellerSession();
            showResponse(
                new Response(
                    "Teller session started for " + customer.getName()
                        + " at register #" + teller.getRegisterNumber(),
                    Response.RESPONSE_TYPE.INFO
                ),
                "Session"
            );
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void endSession() {
        try {
            teller.endSession();
            customer.endSession();
            showResponse(
                new Response(
                    "Teller session ended for register #" + teller.getRegisterNumber(),
                    Response.RESPONSE_TYPE.INFO
                ),
                "Session"
            );
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void requireActiveSession() {
        if (!teller.isCustomerPresent()) {
            throw new IllegalStateException(
                "cannot complete teller operation: no customer is present at the register"
            );
        }
    }
}