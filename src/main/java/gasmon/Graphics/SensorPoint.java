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
	
	public void setAlpha(int numGuesses, int granularity) {
		double x1 = 0;
		double x2 = Math.pow(1000/granularity, 2);
		double y1 = 255;
		double y2 = 0;
		
		double val = y1 + (numGuesses-x1)*(y2-y1)/(x2-x1);
		this.colour = new Color(255, 0, 0, Math.toIntExact(Math.round(val)));
		
	}
	
	public String getValueAsString() {
		return String.format("%.2f", this.value);
	}
	
	public String getPosAsString() {
		return "(" + this.x + "," + this.y + ")";
	}

}
