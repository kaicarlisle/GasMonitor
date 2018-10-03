package gasmon.AwsRequests;

import java.sql.Timestamp;

public class Message {
	public String locationId;
	public String eventId;
	public double value;
	private long timestamp;
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Message) {
			Message b = (Message) o;
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
