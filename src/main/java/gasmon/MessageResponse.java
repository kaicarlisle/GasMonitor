package gasmon;

public class MessageResponse {
	private String Message;
	
	public void setMessage(String message) {
		this.Message = message;
	}
	
	public String getMessage() {
		return this.Message;
	}
	
	public class Message {
		public String locationId;
		public String eventId;
		public double value;
		public long timestamp;
	}
}
