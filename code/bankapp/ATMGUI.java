package bankapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ATMGUI {
	private ATM atm;
	
	private JFrame frame;
	private JTextField amountField;
	private JTextArea outputArea;
	
	public ATMGUI() {
	atm = new ATM("127.0.0.1");
	frame = new JFrame("ATM Machine");
	frame.setSize(400, 300);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.setLayout(new BorderLayout());

	// Panel
	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(5,1));
	
	amountField = new JTextField();
	JButton withdrawButton = new JButton("Withdraw");
	JButton depositButton = new JButton("Deposit");
	JButton balanceButton = new JButton("Check Balance");
	
	outputArea = new JTextArea();
	outputArea.setEditable(false);
	
	panel.add(new JLabel("Enter Amount:"));
	panel.add(amountField);
	panel.add(withdrawButton);
	panel.add(depositButton);
	panel.add(balanceButton);
	
	frame.add(panel, BorderLayout.NORTH);
	frame.add(new JScrollPane(outputArea), BorderLayout.CENTER);
	
	frame.setVisible(true);
	
	} 

public static void main(String[] args) {
	new ATMGUI();
}
}

