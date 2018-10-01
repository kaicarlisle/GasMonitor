package gasmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Random;

import gasmon.MessageResponse.Message;

public class Sensor {
	public String humanReadableName;
	private double x;
	private double y;
	private String id;
	private ArrayList<MessageResponse.Message> readings;
	
	public Sensor() throws IOException {
		this.readings = new ArrayList<MessageResponse.Message>();
		File file = new File("src/main/resources/dictionary.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Random r = new Random();
		ArrayList<String> words = new ArrayList<String>();
		String line;
		while ((line = reader.readLine()) != null) {
			words.add(line.substring(0, 1).toUpperCase() + line.substring(1));
		}
		String word1 = words.get(r.nextInt(words.size()));
		String word2 = words.get(r.nextInt(words.size()));
		String word3 = words.get(r.nextInt(words.size()));
		this.humanReadableName = word1 + word2 + word3;
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

}
