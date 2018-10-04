package gasmon.Parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonSyntaxException;

import gasmon.Sensor;

public class LocationsParser extends JSONParser {
	private BufferedReader reader;
	private String line;
	private String jsonString;
	
	public LocationsParser(File file) {
		super();
		this.jsonString = "";
		try {
			this.reader = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			System.out.println("locations.json file not found");
			e.printStackTrace();
		}
	}
	
	public Sensor[] parse() {
		try {
			while ((this.line = this.reader.readLine()) != null) {
				this.jsonString += this.line;
			}
			return gson.fromJson(this.jsonString, Sensor[].class);
		} catch (IOException e) {
			System.out.println("Error reading locations.json");
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			System.out.println("Json syntax exception - invalid json in locations.json");
		}
		return new Sensor[0];
	}
}
