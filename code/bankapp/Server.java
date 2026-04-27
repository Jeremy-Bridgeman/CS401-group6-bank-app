package bankapp;
import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
	static String ip;
	Logger logger = Logger.getInstance();	// get our instance of logger so it is ready to use
	CheckingAccountValidator checkingValidator = new CheckingAccountValidator();
	SavingsAccountValidator savingsValidator = new SavingsAccountValidator();
	CreditAccountValidator creditValidator = new CreditAccountValidator();
	List<Account> accounts = Collections.synchronizedList(new ArrayList<Account>()); // use a thread safe data structure
	
	
	public static void main(String[] args) {
		Server server = new Server();
		server.start();
	}
	
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(7890);
            serverSocket.setReuseAddress(true);

            System.out.println("[SERVER] Listening on port 7890...");

            while (true) {
                Socket client = serverSocket.accept();

                System.out.println("[SERVER] New client connected from: " + client.getInetAddress().getHostAddress());

                ClientHandler handler = new ClientHandler(client, this);
                new Thread(handler).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
	
}