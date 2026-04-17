import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
	static String ip;
	Logger logger = Logger.getInstance();	// get our instance of logger so it is ready to use
	CheckingAccountValidator checkingValidator = new CheckingAccountValidator();
	SavingsAccountValidator savingsValidator = new SavingsAccountValidator();
	CreditAccountValidator creditValidator = new CreditAccountValidator();
	
	public static void main(String[] args) {
		if (args.length != 1) {
			ip = "10.0.0.216";	// this will need to be changed or we will need to write a helper to try to grab the ip dynamically
		}
		else {
			ip = args[0];
		}

	}
	
	public ArrayList<Log> getLogs() {
		
		return logger.getLogs();
	}

}
