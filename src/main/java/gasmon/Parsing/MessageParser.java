package gasmon.Parsing;

import com.google.gson.JsonSyntaxException;

import gasmon.AwsRequests.MessageResponse;
import gasmon.AwsRequests.Message;

public class MessageParser extends JSONParser {

	public MessageParser() {
		super();
	}
	
	public MessageResponse parse(String message) throws JsonSyntaxException{
		MessageResponse messageResponse = gson.fromJson(message, MessageResponse.class);
		messageResponse.messageBody = gson.fromJson(messageResponse.getMessage(), Message.class);
		return messageResponse;
	}
}
