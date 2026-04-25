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

    private JTextField amountField;

    public TellerGUI(Teller teller, Account account, BankClientFacade client) {
        this.teller = Objects.requireNonNull(teller);
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

        styleButton(balanceBtn);
        styleButton(depositBtn);
        styleButton(withdrawBtn);

        balanceBtn.addActionListener(e -> checkBalance());
        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());

        buttonPanel.add(balanceBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);

        center.add(amountPanel);
        center.add(buttonPanel);

        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
    }

    private void checkBalance() {
        Response response = client.viewAccount(teller, Request.USER_TYPE.TELLER, account);
        showResponse(response, "Balance");
    }

    private void deposit() {
        try {
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
            throw new IllegalArgumentException("Enter an amount");
        }
        return Double.parseDouble(text.trim());
    }

    private void showResponse(Response response, String title) {
        int messageType = response.getType() == Response.RESPONSE_TYPE.ERROR ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE;

        JOptionPane.showMessageDialog(this, response.getMessage(), title, messageType);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void styleButton(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 16));
        b.setPreferredSize(new Dimension(150, 40));
    }
}