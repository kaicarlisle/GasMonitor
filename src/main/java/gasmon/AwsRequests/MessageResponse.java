package gasmon.AwsRequests;

public class MessageResponse {
	private String Message;
	public Message messageBody;
	
	public void setMessage(String message) {
		this.Message = message;
	}
	
	public String getMessage() {
		return this.Message;
	}
}
