package gasmon;

public class Sensor {
	private double x;
	private double y;
	private String id;
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String toString() {
		return this.x + this.y + this.id;
	}

}
