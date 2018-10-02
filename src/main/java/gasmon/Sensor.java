package gasmon;

import java.io.IOException;
import java.util.ArrayList;

import gasmon.MessageResponse.Message;

public class Sensor {
	public String humanReadableName;
	public double x;
	public double y;
	private String id;
	private ArrayList<MessageResponse.Message> readings;
	
	public Sensor() throws IOException {
		this.readings = new ArrayList<MessageResponse.Message>();
		this.humanReadableName = new ReadableUUID(3).UUID;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getID() {
		return this.id;
	}
	
	public void addReading(MessageResponse.Message reading) {
		this.readings.add(reading);
	}
	
	public void clearReadings() {
		this.readings.clear();
	}
	
	public ArrayList<Message> getReadings() {
		return this.readings;
	}
	
	public double getAverage() {
		double total = 0;
		int count = 0;
		for (MessageResponse.Message m : this.readings) {
			total += m.value;
			count += 1;
		}
		return total / count;
	}

}
