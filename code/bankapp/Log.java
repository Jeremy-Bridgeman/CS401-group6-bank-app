package bankapp;
import java.util.Date;

// this class is responsible for creating immutable log objects to be used by the logger.
// the toString returns a comma separated list of values that represent the log
// we also support converting to/from file format so Logger doesn't need to know parsing details
// and in order to simplify the Logger class a little more.

public class Log {
	private final Date date;
	private final TRANSACTION_TYPE type;
	private final String comment;
	private final double amount;
	private final String accountKey;
	
	public enum TRANSACTION_TYPE {
		WITHDRAWAL,
		DEPOSIT,
		TRANSFER,
		ERROR,
		OTHER
	}
	
	// constructor used when creating a NEW log (uses current time)
	public Log(TRANSACTION_TYPE t, String c, double a, String accountKey) {
		date = new Date();
		type = t;
		comment = c;
		amount = a;
		this.accountKey = accountKey;
	}

	// constructor used when LOADING logs from file (uses stored timestamp)
	public Log(Date d, TRANSACTION_TYPE t, String c, double a, String accountKey) {
		date = d;
		type = t;
		comment = c;
		amount = a;
		this.accountKey = accountKey;
	}

	public Date getDate() {
		return date;
	}

	public TRANSACTION_TYPE getType() {
		return type;
	}

	public String getComment() {
		return comment;
	}

	public double getAmount() {
		return amount;
	}

	public String getAccountKey() {
		return accountKey;
	}

	// converts log into a format safe for writing to file
	// format: timestamp|TYPE|comment|amount|accountKey
	// we use timestamp instead of Date string so it is easier to parse and sort later
	public String toFileString() {
		return date.getTime() + "|" + type + "|" + comment + "|" + amount + "|" + accountKey;
	}

	// parses a line from the file and reconstructs a Log object
	// this keeps parsing logic out of Logger
	public static Log fromFileString(String line) {
		String[] parts = line.split("\\|", 5); // split on pipe character

		// basic validation so bad lines don't crash the program
		if (parts.length != 5) {
			throw new IllegalArgumentException("Malformed log line: " + line);
		}

		// convert string --> correct types
		long timestamp = Long.parseLong(parts[0]); // string --> long
		Date date = new Date(timestamp); // long --> Date

		TRANSACTION_TYPE type = TRANSACTION_TYPE.valueOf(parts[1]); // string --> enum

		String comment = parts[2];

		double amount = Double.parseDouble(parts[3]); // string --> double
		String accountKey = parts[4];

		return new Log(date, type, comment, amount, accountKey);
	}

	@Override
	public String toString() {
		return "date=" + date + ", type=" + type + ", comment=" + comment + ", amount=" + amount + ", accountKey=" + accountKey;
	}
}