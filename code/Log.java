import java.util.Date;

// this class is responsible for creating the log objects to be used by the logger.
// the toString returns a comma separated list of values that represent the log
// we also support converting to/from file format so Logger doesn't need to know parsing details
// and in order to simplify the Logger class a little more.

public class Log {
	Date date;
	TRANSACTION_TYPE type;
	String comment;
	double amount;
	
	enum TRANSACTION_TYPE {
		WITHDRAWAL,
		DEPOSIT,
		TRANSFER,
		ERROR,
		OTHER
	}
	
	// constructor used when creating a NEW log (uses current time)
	Log(TRANSACTION_TYPE t, String c, double a) {
		date = new Date();
		type = t;
		comment = c;
		amount = a;
	}

	// constructor used when LOADING logs from file (uses stored timestamp)
	Log(Date d, TRANSACTION_TYPE t, String c, double a) {
		date = d;
		type = t;
		comment = c;
		amount = a;
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

	// converts log into a format safe for writing to file
	// format: timestamp|TYPE|comment|amount
	// we use timestamp instead of Date string so it is easier to parse and sort later
	public String toFileString() {
		return date.getTime() + "|" + type + "|" + comment + "|" + amount;
	}

	// parses a line from the file and reconstructs a Log object
	// this keeps parsing logic OUT of Logger which is cleaner design
	public static Log fromFileString(String line) {
		String[] parts = line.split("\\|"); // split on pipe character

		// basic validation so bad lines don't crash the program
		if (parts.length != 4) {
			throw new IllegalArgumentException("Malformed log line: " + line);
		}

		// convert string --> correct types
		long timestamp = Long.parseLong(parts[0]); // string --> long
		Date date = new Date(timestamp); // long --> Date

		TRANSACTION_TYPE type = TRANSACTION_TYPE.valueOf(parts[1]); // string --> enum

		String comment = parts[2];

		double amount = Double.parseDouble(parts[3]); // string --> double

		return new Log(date, type, comment, amount);
	}

	@Override
	public String toString() {
		return "date=" + date + ", type=" + type + ", comment=" + comment + ", amount=" + amount;
	}
}