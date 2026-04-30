package bankapp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WaitingForTellerDialog extends JFrame {
    private static final long serialVersionUID = 1L;

    private final Customer customer;
    private final Account account;
    private final BankClientFacade client;
    private final String sessionId;

    private final JLabel statusLabel;
    private final Timer timer;

    public WaitingForTellerDialog(Customer customer, Account account, BankClientFacade client, String sessionId) {
        this.customer = customer;
        this.account = account;
        this.client = client;
        this.sessionId = sessionId;

        setTitle("Waiting for Teller");
        setSize(420, 180);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        statusLabel = new JLabel("Joining teller queue...", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.add(statusLabel, BorderLayout.CENTER);

        JPanel south = new JPanel();
        south.add(cancelBtn);
        root.add(south, BorderLayout.SOUTH);

        setContentPane(root);

        Response joinResponse = client.joinTellerQueue(customer, account, sessionId);
        updateStatus(joinResponse);

        timer = new Timer(2000, e -> pollQueue());
        timer.start();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                timer.stop();
                if (!assignedToTeller) {
                    try {
                        customer.endSession();
                    } catch (Exception ignored) { }
                }
            }
        });
    }

    private boolean assignedToTeller = false;
    private void pollQueue() {
        Response response = client.checkTellerQueue(customer, sessionId);
        updateStatus(response);

        if (response != null && response.isReady()) {
            timer.stop();
            assignedToTeller = true;

            JOptionPane.showMessageDialog(
                this,
                "Teller " + response.getAssignedTellerName() + " is ready for you.",
                "Teller Ready",
                JOptionPane.INFORMATION_MESSAGE
            );

            CustomerTellerGUI gui = new CustomerTellerGUI(customer, client, sessionId, response.getAssignedTellerName());
            gui.setVisible(true);
            dispose();
        }
    }

    private void updateStatus(Response response) {
        if (response == null) {
            statusLabel.setText("No response from server.");
            return;
        }

        if (response.isReady()) {
            statusLabel.setText("Assigned to teller: " + response.getAssignedTellerName());
        } else if (response.getQueuePosition() > 0) {
            statusLabel.setText("Waiting in teller queue. Position: " + response.getQueuePosition());
        } else {
            statusLabel.setText(response.getMessage());
        }
    }
}