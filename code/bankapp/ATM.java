package bankapp;
// Allows it to work with server
import java.io.*;
import java.net.Socket;

public class ATM {
	// Do not need this
	// Conflicts with server
	//(remove from UML)
	// private double cash;
	
	private int failedAttempts;
	private boolean serviceCompleted;
	
	private Socket socket;
	private ObjectOutputStream out;
	private ObjectInputStream in;
	
	//Place Holders
	private double dailyWithdrawalLimit = 10000;
	private double dailyDepositLimit = 10000;
	
	// Changed so its compatible with Server
	public ATM(String serverIP) {
		try { 
			// Connect to serverIP
			socket = new Socket(serverIP, 7890);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.flush();
			
			in = new ObjectInputStream(socket.getInputStream());
			
		} catch (IOException e) {
			e.printStackTrace();
			close();
		}
		this.failedAttempts = 0;
		this.serviceCompleted = false;
	}
	
	// Debug for IP
	public String getConnectedServerIP() {
		if (socket != null) {
			return socket.getInetAddress().toString();
		}
		return ("Not connected.");
	}
	
	// Basic ATM Functions
	// Changed so its compatible with Server
	public Response withdraw(double amount, Account account, Person person) {
	    try {
	        Request req = new Request(
	                Request.REQUEST_TYPE.WITHDRAW,
	                Request.USER_TYPE.ATM,
	                person,
	                account,
	                null,
	                amount,
	                "withdraw request"
	        );

	        out.writeObject(req);
	        out.flush();

	        return (Response) in.readObject();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return new Response("ATM error", Response.RESPONSE_TYPE.ERROR);
	    }
	}
	
	// An open account function
	public Response openAccount(Account account, Person person) {
	    try {
	        Request req = new Request(
	                Request.REQUEST_TYPE.OPEN_ACCOUNT,
	                Request.USER_TYPE.ATM,
	                person,
	                account,
	                null,
	                0,
	                "open account"
	        );

	        out.writeObject(req);
	        out.flush();

	        return (Response) in.readObject();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return new Response("ATM error", Response.RESPONSE_TYPE.ERROR);
	    }
	}
	
	// Changed so its compatible with Server
	public Response deposit(double amount, Account account, Person person) {
	    try {
	        Request req = new Request(
	                Request.REQUEST_TYPE.DEPOSIT,
	                Request.USER_TYPE.ATM,
	                person,
	                account,
	                null,
	                amount,
	                "deposit request"
	        );

	        out.writeObject(req);
	        out.flush();

	        return (Response) in.readObject();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return new Response("ATM error", Response.RESPONSE_TYPE.ERROR);
	    }
	}
	
	// Changed so its compatible with Server
	public Response checkBalance(Account account, Person person) {
	    try {
	        Request req = new Request(
	                Request.REQUEST_TYPE.VIEW_ACCOUNT,
	                Request.USER_TYPE.ATM,
	                person,
	                account,
	                null,
	                0,
	                "view account"
	        );

	        out.writeObject(req);
	        out.flush();

	        return (Response) in.readObject();

	    } catch (Exception e) {
	        e.printStackTrace();
	        return new Response("ATM error", Response.RESPONSE_TYPE.ERROR);
	    }
	}
	
	public void displayConfirmation() {
		System.out.println("Transaction Successful.");
		
	}
	public void displayError() {
		System.out.println("Transaction Failed.");
		
	}
	public double getCashAmount() {
		return 0;
	}
	
	// Login Functions
	public boolean login(int id, int pin) {
		// Placeholder until server/auth later
		boolean success = true;
		
		if (!success) {
			failedAttempts++;
			
			if (failedAttempts >= 5) {
				serviceCompleted = true;
			}
			displayError();
			return false;
		}
		
		failedAttempts = 0;
		return true;
		
	}
	
	// Cleanup method
	public void close() {
	    try {
	        if (in != null) in.close();
	        if (out != null) out.close();
	        if (socket != null) socket.close();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void logAttempt(Log log) {
		
	}
	
	// Getters 
	public double getDailyWithdrawalLimit() {
		return dailyWithdrawalLimit;
		
	}
	public double getDailyDepositLimit() {
		return dailyDepositLimit;
		
	}
	public int getFailedAttempts() {
		return this.failedAttempts;
		
	}
	public boolean getServiceCompletion() {
		return this.serviceCompleted;
		
	}
	
	// Setters
	
	public void setFailedAttempts(int amount) {
		this.failedAttempts = amount;
	}
	public void setServiceCompletion(boolean result) {
		this.serviceCompleted = result;
	}

}
