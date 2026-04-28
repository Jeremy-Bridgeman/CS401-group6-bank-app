package bankapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ATMGUI {
	private ATM atm;
	
	private JFrame frame;
	private JTextField amountField;
	private JTextArea outputArea;
	private JPasswordField pinField;
	private JTextField accountField;
	private JTextField targetAccountField;
	// added more variables
	

	// Creating main frame
	public ATMGUI() {
	atm = new ATM("127.0.0.1");
	frame = new JFrame("ATM Machine");
	frame.setSize(400, 400);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	CardLayout cl = new CardLayout();
	JPanel container = new JPanel(cl);
	frame.setContentPane(container);

	// Panel + GridLayout as auto rows
	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(0,1));
	
	// Input fields
	// Login Panel
	accountField = new JTextField();
	pinField = new JPasswordField();
	JButton loginButton = new JButton("Login");
	
	panel.add(new JLabel("Account ID:"));
	panel.add(accountField);
	
	panel.add(new JLabel("Pin"));
	panel.add(pinField);
	
	panel.add(loginButton);
	
	// ATM Panel
	JPanel atmPanel = new JPanel();
	atmPanel.setLayout(new GridLayout(0,1));
	
	amountField = new JTextField();
	targetAccountField = new JTextField();
	
	// Necessary Buttons
	JButton withdrawButton = new JButton("Withdraw");
	JButton depositButton = new JButton("Deposit");
	JButton balanceButton = new JButton("Check Balance");
	JButton transferButton = new JButton("Transfer");
	
	outputArea = new JTextArea();
	outputArea.setEditable(false);
	
	// Panel
	atmPanel.add(new JLabel("Target Account (Transfer):"));
	atmPanel.add(targetAccountField);
	
	atmPanel.add(new JLabel("Enter Amount:"));
	atmPanel.add(amountField);
	
	atmPanel.add(transferButton);
	atmPanel.add(withdrawButton);
	atmPanel.add(depositButton);
	atmPanel.add(balanceButton);
	
	atmPanel.add(new JScrollPane(outputArea));
	
	// Adding Frame
	container.add(panel, "LOGIN");
	container.add(atmPanel, "ATM");
	
	// Login Logic
	loginButton.addActionListener(e -> {
	    String account = accountField.getText();
	    String pin = new String(pinField.getPassword());

	    // placeholder validation for now
	    if (account.equals("123") && pin.equals("0000")) {
	        cl.show(container, "ATM");
	    } else {
	        JOptionPane.showMessageDialog(frame, "Invalid login");
	    }
	});
	
	frame.setVisible(true);
	} 

public static void main(String[] args) {
	new ATMGUI();
}
}

