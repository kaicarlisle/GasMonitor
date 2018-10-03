package gasmon.Parsing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class JSONParser {
	protected Gson gson;
	
	public JSONParser() {
		this.gson = new GsonBuilder().create();
	}
}
