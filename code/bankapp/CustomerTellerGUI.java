package bankapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;

public class CustomerTellerGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Customer customer;
    private final BankClientFacade client;
    private final String sessionId;
    private final String tellerName;

    private JTextField amountField;
    private JLabel statusLabel;
    private JLabel accountLabel;
    private final Timer resultTimer;

    public CustomerTellerGUI(Customer customer, BankClientFacade client, String sessionId, String tellerName) {
        this.customer = Objects.requireNonNull(customer);
        this.client = Objects.requireNonNull(client);
        this.sessionId = Objects.requireNonNull(sessionId);
        this.tellerName = tellerName == null ? "your teller" : tellerName;

        buildUi();

        resultTimer = new Timer(1500, e -> pollTransactionResult());
        resultTimer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                resultTimer.stop();
            }
        });
    }

    private void buildUi() {
        setTitle("Customer Teller Session - " + customer.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(620, 260);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Connected to teller: " + tellerName);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        statusLabel = new JLabel("Choose a request to send to the teller.");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        accountLabel = new JLabel("No completed transaction yet.");
        accountLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        JPanel amountPanel = new JPanel(new BorderLayout(10, 10));
        JLabel amountLabel = new JLabel("Amount:");
        amountLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));

        amountField = new JTextField();
        amountField.setFont(new Font("SansSerif", Font.PLAIN, 16));

        amountPanel.add(amountLabel, BorderLayout.WEST);
        amountPanel.add(amountField, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton depositBtn = new JButton("Request Deposit");
        JButton withdrawBtn = new JButton("Request Withdrawal");
        JButton closeBtn = new JButton("Close");

        depositBtn.addActionListener(e -> submitRequest("DEPOSIT"));
        withdrawBtn.addActionListener(e -> submitRequest("WITHDRAW"));
        closeBtn.addActionListener(e -> dispose());

        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(closeBtn);

        center.add(statusLabel);
        center.add(Box.createVerticalStrut(6));
        center.add(accountLabel);
        center.add(Box.createVerticalStrut(10));
        center.add(amountPanel);
        center.add(buttonPanel);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void submitRequest(String action) {
        try {
            double amount = parseAmount();
            Response response = client.submitTellerTransactionRequest(customer, sessionId, action, amount);

            if (response == null) {
                JOptionPane.showMessageDialog(this, "no response from server", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            statusLabel.setText(response.getMessage());
            JOptionPane.showMessageDialog(
                this,
                response.getMessage(),
                "Customer Request",
                response.getType() == Response.RESPONSE_TYPE.ERROR
                    ? JOptionPane.ERROR_MESSAGE
                    : JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pollTransactionResult() {
        Response response = client.pollTellerTransactionResult(customer, sessionId);
        if (response == null) {
            return;
        }

        if (response.getType() != Response.RESPONSE_TYPE.SUCCESS) {
            return;
        }

        statusLabel.setText(response.getMessage());

        if (response.getAccount() != null) {
            Account updated = response.getAccount();
            accountLabel.setText(
                "Account: " + updated.getTYPE()
                    + " | Status: " + updated.getSTATUS()
                    + " | Balance: " + updated.getBalance()
            );
        }

        JOptionPane.showMessageDialog(
            this,
            response.getMessage(),
            "Transaction Complete",
            JOptionPane.INFORMATION_MESSAGE
        );
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
}