package gasmon;

import com.google.gson.JsonSyntaxException;

public class MessageParser extends JSONParser {

	public MessageParser() {
		super();
	}
	
	public MessageResponse parse(String message) throws JsonSyntaxException{
		MessageResponse messageResponse = gson.fromJson(message, MessageResponse.class);
		messageResponse.messageBody = gson.fromJson(messageResponse.getMessage(), MessageResponse.Message.class);
		return messageResponse;
	}
}
