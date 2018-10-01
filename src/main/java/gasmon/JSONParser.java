package gasmon;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class JSONParser {
	protected Gson gson;
	protected String jsonString;
	
	public JSONParser() throws IOException, FileNotFoundException {
		this.gson = new GsonBuilder().create();
		this.jsonString = "";
	}
	
	public JSONParser(String jsonstring) {
		this.gson = new GsonBuilder().create();
		this.jsonString = jsonstring;
	}
}
