package gasmon.Graphics;

import java.awt.Color;

import gasmon.Sensor;

public class SensorPoint {
	
	public int x;
	public int y;
	public int w;
	public Color colour;
	public Sensor relatedSensor;
	public int strikes;
	
	public double value;
	public static double MAX_READING;
	
	public SensorPoint(double x, double y, double value, int strikes) {
		this.value = value;
		this.x = Math.toIntExact(Math.round(x));
		this.y = Math.toIntExact(Math.round(y));
		
		this.strikes = strikes;		
		this.w = Math.toIntExact(Math.round(value * 5));
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SensorPoint) {
			SensorPoint b = (SensorPoint) o;
			return (this.relatedSensor.equals(b.relatedSensor));
		} else {
			return false;
		}
	}
	
	public void setColour() {
		int red = Math.toIntExact(Math.round((255/MAX_READING)*this.value));
		int green = 255 - red;
		this.colour = new Color(red, green, 0, 80);
	}
	
	public String getValueAsString() {
		return String.format("%.2f", this.value);
	}
	
	public String getPosAsString() {
		return "(" + this.x + "," + this.y + ")";
	}

}
