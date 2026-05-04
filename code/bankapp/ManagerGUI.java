package bankapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Objects;

public class ManagerGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Manager manager;
    private Account account;
    private java.util.List<Account> accounts;
    private final BankClientFacade client;

    private JTextField amountField;

    public ManagerGUI(Manager manager, Account account, BankClientFacade client) {
        this.manager = Objects.requireNonNull(manager);
        this.account = Objects.requireNonNull(account);
        this.client = Objects.requireNonNull(client);

        buildUi();
    }

    private void buildUi() {
        setTitle("[MANAGER] " + manager.getName());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1280, 480);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel(
            "Manager: " + manager.getName() + " | Register #" + manager.getRegisterNumber()
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
        JButton logsBtn = new JButton("View Logs");
        JButton exitBtn = new JButton("Exit");

        styleButton(balanceBtn);
        styleButton(depositBtn);
        styleButton(withdrawBtn);
        styleButton(logsBtn);
        styleButton(exitBtn);

        balanceBtn.addActionListener(e -> checkBalance());
        depositBtn.addActionListener(e -> deposit());
        withdrawBtn.addActionListener(e -> withdraw());
        logsBtn.addActionListener(e -> viewLogs());
        exitBtn.addActionListener(e -> exit());
        
        //Select account
        JButton selectAccountBtn = new JButton("Select Account");
        styleButton(selectAccountBtn);
        selectAccountBtn.addActionListener(e -> selectAccount());
        buttonPanel.add(selectAccountBtn);
        
        // creates an account
        JButton createAccountBtn = new JButton("Create Account");
        styleButton(createAccountBtn);
        createAccountBtn.addActionListener(e -> createAccount());
        buttonPanel.add(createAccountBtn);

        buttonPanel.add(balanceBtn);
        buttonPanel.add(depositBtn);
        buttonPanel.add(withdrawBtn);
        buttonPanel.add(logsBtn);
        buttonPanel.add(exitBtn);

        center.add(amountPanel);
        center.add(buttonPanel);

        root.add(center, BorderLayout.CENTER);
        setContentPane(root);
    }
    
    //Account selection
    private void selectAccount() {
        Response response = client.getAllAccounts();

        if (response == null || response.getAccounts() == null) {
            showError("no accounts available");
            return;
        }

        java.util.List<Account> list = response.getAccounts();

        String[] options = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Account a = list.get(i);
            options[i] = a.getTYPE() + " | #" + a.getAccountNumber() + " | $" + a.getBalance();
        }

        int choice = JOptionPane.showOptionDialog(
            this,
            "Select account:",
            "Manager Account Selection",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]
        );

        if (choice >= 0) {
            this.account = list.get(choice);
            showResponse(new Response("Account selected", Response.RESPONSE_TYPE.SUCCESS), "Manager");
        }
    }
    // account created
    private void createAccount() {
        try {
            String first = JOptionPane.showInputDialog(this, "First name:");
            if (first == null) return;

            String last = JOptionPane.showInputDialog(this, "Last name:");
            if (last == null) return;

            String username = JOptionPane.showInputDialog(this, "Username:");
            if (username == null) return;

            String pinText = JOptionPane.showInputDialog(this, "PIN:");
            if (pinText == null) return;

            int pin = Integer.parseInt(pinText.trim());

            String[] types = {"CHECKING", "SAVINGS", "CREDIT"};
            int choice = JOptionPane.showOptionDialog(
                this,
                "Select account type:",
                "Account Type",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                types,
                types[0]
            );

            if (choice < 0) return;

            Account.ACCOUNT_TYPE type = Account.ACCOUNT_TYPE.valueOf(types[choice]);

            Response response = client.createCustomerAndAccount(
                manager,
            	first.trim(),
                last.trim(),
                username.trim(),
                pin,
                type
            );

            showResponse(response, "Create Account");

        } catch (NumberFormatException ex) {
            showError("PIN must be a number");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }
    
    

    private void checkBalance() {
        Response response = client.viewAccount(manager, Request.USER_TYPE.MANAGER, account);
        showResponse(response, "Balance");
    }

    private void deposit() {
        try {
            double amount = parseAmount();
            Response response = client.deposit(manager, Request.USER_TYPE.MANAGER, account, amount);
            showResponse(response, "Deposit");
            amountField.setText("");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void withdraw() {
        try {
            double amount = parseAmount();
            Response response = client.withdraw(manager, Request.USER_TYPE.MANAGER, account, amount);
            showResponse(response, "Withdraw");
            amountField.setText("");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void viewLogs() {
        Response response = client.viewLogs(manager);

        if (response == null) {
            showError("operation failed: response was null");
            return;
        }

        if (response.getType() == Response.RESPONSE_TYPE.ERROR) {
            showError(response.getMessage());
            return;
        }

        String text = response.getMessage();
        if (text == null || text.trim().isEmpty()) {
            text = "No logs available.";
        }

        JTextArea logArea = new JTextArea(text);
        logArea.setEditable(false);
        logArea.setCaretPosition(0);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        logArea.setLineWrap(false);
        logArea.setWrapStyleWord(false);

        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(900, 500));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        JOptionPane.showMessageDialog(
            this,
            scrollPane,
            "Manager Logs",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void exit() {
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