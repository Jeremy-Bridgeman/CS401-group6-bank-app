import java.util.*;
import java.io.*;

// The logger is a singleton which contains log objects and writes all activities to a txt file.
// This will need to be a multi-threaded eager singleton so we can load the logs into an array list for quick access
// and quick sorting when a teller needs to access the information requested

public class Logger {
	
	// eager singleton instance (created once when class loads)
	private static final Logger instance = new Logger();

	ArrayList<Log> logs = new ArrayList<Log>();
	String filename = "logs.txt";

	// private constructor prevents external instantiation
	// also loads logs immediately when the logger is created
	private Logger() {
		loadLogs();
	}

	// global access point to the singleton
	public static Logger getInstance() {
		return instance;
	}

	ArrayList<Log> getLogs() {
		return logs;
	}
	
	// loads logs from file into memory
	// this allows fast access/sorting without reading file every time
	private void loadLogs() {
		File file = new File(filename);

		// if file doesn't exist yet, nothing to load
		if (!file.exists()) {
			return;
		}

		try {
			Scanner scanner = new Scanner(file);

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine().trim();

				// skip empty lines
				if (line.isEmpty()) continue;

				try {
					// delegate parsing to Log class
					Log log = Log.fromFileString(line);
					logs.add(log);
				} catch (Exception e) {
					// prevents one bad line from crashing everything, but still report the error
					System.out.println("Skipping bad log line: " + line);
				}
			}

			scanner.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void logEvent(Log event) {
		logs.add(event);
	}
	
	// writes all logs to file
	// NOTE: currently overwrites file each time to avoid duplicates
	void saveLogs() {
		try {
			FileWriter writer = new FileWriter(filename); // overwrite each time

			for (Log log: logs) {
				// USE LINESEPARATOR HERE SO WE DONT HAVE STUPID WINDOWS CRLF CHARACTERS THAT CAUSE ISSUES
				writer.write(log.toFileString() + System.lineSeparator());
			}
			
			writer.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}