package gasmon;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class Main {
	
	public static void main(String[] args) throws InterruptedException {
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(".aws/credentials", "default");
		Sensor[] sensors = new Sensor[0];
		
		//get locations.json from aws
		GetLocationsJSONFromAWS getLocationsFromAWS = new GetLocationsJSONFromAWS(credentialsProvider);
		File locationsJSON = getLocationsFromAWS.getLocationsFile();
		
		//parse locations.json into Sensors[]
		LocationsParser locationsParser = new LocationsParser(locationsJSON);
		sensors = locationsParser.parse();
		

		//request and handle messages from sqs, associating readings with known scanners
		receiveMessages(credentialsProvider, sensors);
	}
	
	private static void receiveMessages(ProfileCredentialsProvider credentialsProvider, Sensor[] sensors) throws InterruptedException {
		MessageParser messageParser = new MessageParser();
		SNSTopicReceiver topicReceiver = new SNSTopicReceiver(credentialsProvider);
		
		//receive messages from sqs server
		for (int i = 0; i < 100; i++) {
			//make less frequent requests getting more readings at once
			Thread.sleep(5000);
//			Stream<String> messages = topicReceiver.getNextMessages(10).stream();
			List<String> message = topicReceiver.getNextMessages(10);
			
			for (String message : messages) {
				//parse messages into MessageResponse objects
				//add each MessageResponse.Message object to the relevant sensor object
				MessageResponse.Message parsedMessage = messageParser.parse(message);
				for (Sensor s : sensors) {
					if (s.getID().equals(parsedMessage.locationId)) {
						s.addReading(parsedMessage);
						System.out.println(parsedMessage.value + " added to sensor " + s.humanReadableName);
					}
				}
			}
		}
		topicReceiver.deleteQueue();
	}
}