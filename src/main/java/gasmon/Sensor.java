package gasmon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gasmon.AwsRequests.Message;

public class Sensor {
	public String humanReadableName;
	public double x;
	public double y;
	private String id;
	public List<Message> readings;
	
	public Sensor() throws IOException {
		this.readings = new ArrayList<Message>();
		this.humanReadableName = new ReadableUUID(3).UUID;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Sensor) {
			Sensor b = (Sensor) o;
			return (this.id.equals(b.id));
		} else {
			return false;
		}
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
	
	public double getAverage() {
		double total = 0;
		
		for (Message m : this.readings) { 
			total += m.value;
		}

		return this.readings.size() > 0 ? total / this.readings.size() : Double.NaN;
	}

}
