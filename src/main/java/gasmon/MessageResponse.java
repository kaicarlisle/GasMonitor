package gasmon;

public class MessageResponse {
	private Message message;
	
	public void setMessage(Message message) {
		this.message = message;
	}
	
	public Message getMessage() {
		return this.message;
	}
	
	public class Message {
		private String locationId;
		private String eventId;
		private double value;
		private long timestamp;
		
		public String getLocationId() {
			return locationId;
		}
		public void setLocationId(String locationId) {
			this.locationId = locationId;
		}
		public String getEventId() {
			return eventId;
		}
		public void setEventId(String eventId) {
			this.eventId = eventId;
		}
		public double getValue() {
			return value;
		}
		public void setValue(double value) {
			this.value = value;
		}
		public long getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}
	}
}
