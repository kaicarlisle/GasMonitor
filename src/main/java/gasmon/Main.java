package gasmon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class Main {
	
	public static void main(String[] args) {
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(".aws/credentials", "default");
		File locationsJSON;
		Sensor[] sensors = new Sensor[0];
		LocationsParser locationsParser;
		SNSTopicReceiver topicReceiver;
		List<String> messages;
		
		//get locations.json from aws
		GetLocationsJSONFromAWS getLocationsFromAWS = new GetLocationsJSONFromAWS("src/main/resources/", credentialsProvider);
		locationsJSON = getLocationsFromAWS.getLocationsFile();
		
		//parse locations.json into Sensors[]
		try {
			locationsParser = new LocationsParser(locationsJSON);
			sensors = locationsParser.parse();
		} catch (Exception e) {
			e.printStackTrace();
		}

		//receive messages from sqs server
		topicReceiver = new SNSTopicReceiver(credentialsProvider);
		messages = topicReceiver.getNextMessages(1);
		for (String message : messages) {
			//parse messages into MessageResponse objects
			System.out.println(message);
		}
		topicReceiver.deleteQueue();
	}
}