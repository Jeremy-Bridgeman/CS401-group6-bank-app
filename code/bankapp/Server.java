package bankapp;
import java.net.*;
import java.io.*;
import java.util.*;

// THIS SERVER DOES NOT DO ANYTHING YET. NEED TO ADD ALL VALIDATION AND LOGIC FOR THAT

public class Server {
	static String ip;
	Logger logger = Logger.getInstance();	// get our instance of logger so it is ready to use
	CheckingAccountValidator checkingValidator = new CheckingAccountValidator();
	SavingsAccountValidator savingsValidator = new SavingsAccountValidator();
	CreditAccountValidator creditValidator = new CreditAccountValidator();
	ArrayList<Account> accounts = new ArrayList<Account>();
	
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
		Server server = new Server();
		server.start();
	}
	
    public void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(7890);
            serverSocket.setReuseAddress(true);

            while (true) {
                Socket client = serverSocket.accept();
                ClientHandler handler = new ClientHandler(client, this);
                new Thread(handler).start();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    private static class ClientHandler implements Runnable {
        private Socket client;
        private Server server;

        private ClientHandler(Socket c, Server s) {
            client = c;
            server = s;
        }

        @Override
        public void run() {
            ObjectOutputStream o = null;
            ObjectInputStream i = null;

            try {
                // create input streams and upgrade them
                o = new ObjectOutputStream(client.getOutputStream());
                o.flush();
                i = new ObjectInputStream(client.getInputStream());

                // request loop
                while (true) {
                    Object request = i.readObject();

                    if (request == null) {
                        break;
                    }
                    Response response = handleRequest(request);
                    o.writeObject(response);
                    o.flush();
                }
            }
            catch (EOFException e) {
                // client disconnected normally
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            finally { // cleanup, close everything out
                try {
                    if (i != null) {
                        i.close();
                    }
                    if (o != null) {
                        o.close();
                    }
                    if (client != null && !client.isClosed()) {
                        client.close();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // placeholder for real server logic when we get to it -- this is where all the final validation will happen
        private Response handleRequest(Object request) {
            return new Response("request received", Response.RESPONSE_TYPE.INFO);
        }
    }
	
	private ArrayList<Log> getLogs() {
		return logger.getLogs();
	}

}
