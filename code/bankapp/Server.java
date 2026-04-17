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
	ArrayList<Account> accounts;
	
	// TODO: create a bucketed hash table for the accounts where the name plus account number is the key and the bucket contains all transactions
	
	public static void main(String[] args) {
		if (args.length != 1) {
			ip = "10.0.0.216";	// this will need to be changed or we will need to write a helper to try to grab the ip dynamically
		}
		else {
			ip = args[0];
		}
		// NOTE: THIS IS NOT NEEDED IN THE SERVER CLASS, BUT THIS IS HOW YOU SHOULD STRUCTURE YOUR CLIENTS.
		// this ensures that we can easily start the program from the command line with a given ip address and
		// dont need to make any changes to the code before running it
		try {
			
			ServerSocket serverSocket = new ServerSocket(7890);
			serverSocket.setReuseAddress(true);
			Socket client = null;
			while (true) {
				client = serverSocket.accept();
				ClientHandler handler = new ClientHandler(client);
				new Thread(handler).start();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static class ClientHandler implements Runnable {
		
		private ClientHandler(Socket c) {
			OutputStream out = null;
			InputStream in = null;
			try {
				// create input streams and upgrade them
				out = c.getOutputStream();
				in = c.getInputStream();
				ObjectOutputStream o = new ObjectOutputStream(out);
				o.flush();
				ObjectInputStream i = new ObjectInputStream(in);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			finally { // cleanup, close everything out
				try {
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
					if (!c.isClosed() && c != null) {
						c.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		@Override
		public void run() {
			
		}
	}
	
	private ArrayList<Log> getLogs() {
		return logger.getLogs();
	}

}
