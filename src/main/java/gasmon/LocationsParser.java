package gasmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class LocationsParser extends JSONParser {
	private BufferedReader reader;
	private String line;
	
	public LocationsParser(File file) throws FileNotFoundException, IOException {
		super();
		this.reader = new BufferedReader(new FileReader(file));
	}
	
	public Sensor[] parse() throws IOException {
		while ((this.line = this.reader.readLine()) != null) {
			this.jsonString += this.line;
		}
		return gson.fromJson(this.jsonString, Sensor[].class);
	}
}
