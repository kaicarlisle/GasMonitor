package gasmon;

public class MessageParser extends JSONParser {

	public MessageParser(String jsonstring) {
		super(jsonstring);
	}
	
	public MessageResponse parse() {
		return gson.fromJson(this.jsonString, MessageResponse.class);
	}
}
