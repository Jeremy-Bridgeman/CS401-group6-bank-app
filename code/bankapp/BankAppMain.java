package bankapp;

import java.awt.Window;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class BankAppMain {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(BankAppMain::launch);
    }

    private static void launch() {
        String serverIp = JOptionPane.showInputDialog(
            null,
            "Enter server IP:",
            "Connect to Bank Server",
            JOptionPane.QUESTION_MESSAGE
        );

        if (serverIp == null || serverIp.trim().isEmpty()) {
            return;
        }

        serverIp = serverIp.trim();
        BankClientFacade client = new BankClientFacade(serverIp, 7890);

        String[] roles = {"Customer", "Teller", "Manager", "Quit"};
        int role = JOptionPane.showOptionDialog(
            null,
            "Choose how to connect.",
            "Bank App",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            roles,
            roles[0]
        );

        if (role == 0) {
            launchCustomer(client, serverIp);
        } else if (role == 1) {
            launchTeller(client);
        } else if (role == 2) {
            launchManager(client);
        } else {
            client.close();
        }
    }

    private static void launchCustomer(BankClientFacade client, String serverIp) {
        String[] choices = {"ATM", "Teller", "Cancel"};
        int mode = JOptionPane.showOptionDialog(
            null,
            "Choose customer access channel.",
            "Customer Access",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            choices,
            choices[0]
        );

        if (mode == 0) {
            ATM atm = new ATM(serverIp);
            ATMGUI gui = new ATMGUI(atm);
            showWindow(gui);
            return;
        }

        if (mode == 1) {
            String username = prompt("Customer username:");
            if (username == null) return;

            Response found = client.findCustomer(username.trim());
            if (found == null) {
                JOptionPane.showMessageDialog(
                    null,
                    "No response from server.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            Customer customer;
            Account account;

            if (found.getCustomer() != null) {
                customer = found.getCustomer();
                account = found.getAccount();
            } else {
                String first = prompt("First name:");
                if (first == null) return;

                String last = prompt("Last name:");
                if (last == null) return;

                customer = new Customer(first.trim(), last.trim(), new Address(), username.trim(), 0);
                account = null;

                JOptionPane.showMessageDialog(
                    null,
                    "No existing account found. You will be placed in the teller queue as a new customer.",
                    "New Customer",
                    JOptionPane.INFORMATION_MESSAGE
                );
            }

            try {
                if (customer.getActiveChannel() == Customer.ACCESS_CHANNEL.NONE) {
                    customer.startTellerSession();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Session Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            String sessionId = client.createSessionId();
            WaitingForTellerDialog waiting = new WaitingForTellerDialog(customer, account, client, sessionId);
            showWindow(waiting);
        }
    }

    private static void launchTeller(BankClientFacade client) {
        String first = prompt("Teller first name:");
        if (first == null) return;

        String last = prompt("Teller last name:");
        if (last == null) return;

        String registerText = prompt("Register number:");
        if (registerText == null) return;

        try {
            int register = Integer.parseInt(registerText.trim());
            Teller teller = new Teller(first, last, new Address(), register);
            TellerGUI gui = new TellerGUI(teller, null, null, client);
            showWindow(gui);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void launchManager(BankClientFacade client) {
        String first = prompt("Manager first name:");
        if (first == null) return;

        String last = prompt("Manager last name:");
        if (last == null) return;

        String registerText = prompt("Manager register number:");
        if (registerText == null) return;

        try {
            int register = Integer.parseInt(registerText.trim());
            Manager manager = new Manager(first, last, new Address(), register);

            Account account = new CheckingAccount(
                0.0,
                Account.ACCOUNT_STATUS.OPEN,
                Account.ACCOUNT_TYPE.CHECKING,
                manager
            );

            ManagerGUI gui = new ManagerGUI(manager, account, client);
            showWindow(gui);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void showWindow(Window window) {
        window.setVisible(true);
        window.toFront();
        window.requestFocus();
        window.setAlwaysOnTop(true);
        window.setAlwaysOnTop(false);
    }

    private static String prompt(String message) {
        String value = JOptionPane.showInputDialog(
            null,
            message,
            "Bank App",
            JOptionPane.QUESTION_MESSAGE
        );
        if (value == null) return null;

        value = value.trim();
        if (value.isEmpty()) return null;

        return value;
    }
}