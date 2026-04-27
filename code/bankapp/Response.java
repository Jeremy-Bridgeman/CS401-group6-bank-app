package bankapp;
import java.io.*;

// this class is responsible for encapsulating response data to send around in our 
// application. Each message is one time use and immutable, once used it will be discarded
public class Response implements Serializable {
	
	private static final long serialVersionUID = 1L; // eclipse said to do this, so i did. I dont think it matters for our project
	private final String message;
	private final RESPONSE_TYPE type;
	
	public enum RESPONSE_TYPE {
		SUCCESS,
		ERROR,
		WARNING,
		INFO,
		LOG
	}
	
	public Response(String message, RESPONSE_TYPE type) {
		this.message = message;
		this.type = type;
		
		// changing this text = t set text
		// changing this too type = r;	set response 
	}
	
	public RESPONSE_TYPE getType() {
		return type;
	}
	public String getMessage() {
		return message;
	}
	// no setters
}