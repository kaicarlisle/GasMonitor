package gasmon;

import com.google.gson.JsonSyntaxException;

public class MessageParser extends JSONParser {

	public MessageParser() {
		super();
	}
	
	public MessageResponse.Message parse(String message) throws JsonSyntaxException{
		MessageResponse messageResponse = gson.fromJson(message, MessageResponse.class);
		MessageResponse.Message messageBody = gson.fromJson(messageResponse.getMessage(), MessageResponse.Message.class);
		return messageBody;
	}
}
