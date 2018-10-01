package gasmon;

import java.sql.Timestamp;

public class MessageResponse {
	private String Message;
	public Message messageBody;
	
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
		private long timestamp;
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof MessageResponse.Message) {
				MessageResponse.Message b = (MessageResponse.Message) o;
				return b.eventId.equals(this.eventId);
			} else {
				return false;
			}
		}
		
		public Timestamp getTimestampFromLong() {
			return new Timestamp(this.timestamp);
		}
		
		public String toString() {
			return eventId + " " + getTimestampFromLong();
		}
	}
}
