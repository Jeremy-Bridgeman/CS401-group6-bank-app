//package bankapp;
//
//import javax.swing.*;
//import java.awt.*;
//
//public class ChrisApp {
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(ChrisApp::launchApp);
//    }
//
//    private static void launchApp() {
//        JTextField hostField = new JTextField();
//        JTextField portField = new JTextField("7890");
//
//        JPanel connectionPanel = new JPanel(new GridLayout(2, 2, 8, 8));
//        connectionPanel.add(new JLabel("Server IP / Hostname:"));
//        connectionPanel.add(hostField);
//        connectionPanel.add(new JLabel("Port:"));
//        connectionPanel.add(portField);
//
//        int connectResult = JOptionPane.showConfirmDialog(
//                null,
//                connectionPanel,
//                "Connect to Server",
//                JOptionPane.OK_CANCEL_OPTION,
//                JOptionPane.PLAIN_MESSAGE
//        );
//
//        if (connectResult != JOptionPane.OK_OPTION) {
//            return;
//        }
//
//        String host = hostField.getText().trim();
//        String portText = portField.getText().trim();
//
//        if (host.isEmpty()) {
//            JOptionPane.showMessageDialog(
//                    null,
//                    "Server IP / hostname is required.",
//                    "Input Error",
//                    JOptionPane.ERROR_MESSAGE
//            );
//            return;
//        }
//
//        int port;
//        try {
//            port = Integer.parseInt(portText);
//        } catch (NumberFormatException e) {
//            JOptionPane.showMessageDialog(
//                    null,
//                    "Port must be a valid number.",
//                    "Input Error",
//                    JOptionPane.ERROR_MESSAGE
//            );
//            return;
//        }
//
//        BankClientFacade client = new BankClientFacade(host, port);
//
//        String[] options = {"Teller", "Manager"};
//        int roleChoice = JOptionPane.showOptionDialog(
//                null,
//                "Choose which temporary GUI to launch:",
//                "Select Role",
//                JOptionPane.DEFAULT_OPTION,
//                JOptionPane.QUESTION_MESSAGE,
//                null,
//                options,
//                options[0]
//        );
//
//        if (roleChoice == JOptionPane.CLOSED_OPTION) {
//            return;
//        }
//
//        Address address = new Address(123, null, "B St", "Hayward", "CA", "94542");
//
//        Account account = new Account();
//        account.setBalance(500.0);
//        account.setSTATUS(Account.ACCOUNT_STATUS.OPEN);
//        account.setTYPE(Account.ACCOUNT_TYPE.CHECKING);
//
//        if (account.getAuthorizedUsers() == null) {
//            account.setAuthorizedUsers(new java.util.ArrayList<>());
//        }
//
//        if (roleChoice == 0) {
//            Teller teller = new Teller("Patrick", "Teller", address, 101);
//            account.getAuthorizedUsers().add(teller);
//
//            Response openResponse = client.openAccount(teller, Request.USER_TYPE.TELLER, account);
//            JOptionPane.showMessageDialog(
//                    null,
//                    openResponse.getText(),
//                    "Open Account",
//                    openResponse.getType() == Response.RESPONSE_TYPE.ERROR
//                            ? JOptionPane.ERROR_MESSAGE
//                            : JOptionPane.INFORMATION_MESSAGE
//            );
//
//            new TellerGUI(teller, account, client).setVisible(true);
//        } else {
//            Manager manager = new Manager("Borris", "Manager", address, 1);
//            account.getAuthorizedUsers().add(manager);
//
//            Response openResponse = client.openAccount(manager, Request.USER_TYPE.MANAGER, account);
//            JOptionPane.showMessageDialog(
//                    null,
//                    openResponse.getText(),
//                    "Open Account",
//                    openResponse.getType() == Response.RESPONSE_TYPE.ERROR
//                            ? JOptionPane.ERROR_MESSAGE
//                            : JOptionPane.INFORMATION_MESSAGE
//            );
//
//            new ManagerGUI(manager, account, client).setVisible(true);
//        }
//    }
//}