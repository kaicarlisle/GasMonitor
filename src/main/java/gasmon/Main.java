package gasmon;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import gasmon.MessageResponse.Message;

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
		List<MessageResponse.Message> parsedMessages = new ArrayList<MessageResponse.Message>();
		Stream<MessageResponse.Message> parsedMessageStream;
		
		//receive messages from sqs server
		for (int i = 0; i < 10; i++) {
			parsedMessages.clear();
			//make less frequent requests getting more readings at once
			Thread.sleep(5000);
			List<String> messages = topicReceiver.getNextMessages(10);
			
			for (String message : messages) {
				//parse messages into MessageResponse objects
				parsedMessages.add(messageParser.parse(message).messageBody);
			}
			parsedMessageStream = parsedMessages.stream();
			//filter stream to:
			//	only include messages from the past 5 minutes
			//  only include distinct readings (based on eventID)
			Date now = new Date();
			Timestamp nowTimestamp = new Timestamp(now.getTime() - 300000);
			parsedMessageStream.filter(message -> (message.getTimestampFromLong().after(nowTimestamp)))
							   .distinct()
							   .forEach(System.out::println);
//							   .collect(Collectors.toList());
			
			
			

			//add each MessageResponse.Message object to the relevant sensor object
//			for (Sensor s : sensors) {
//				if (s.getID().equals(parsedMessage.locationId)) {
//					s.addReading(parsedMessage);
//					System.out.println(parsedMessage.value + " added to sensor " + s.humanReadableName);
//				}
//			}			
		}
		topicReceiver.deleteQueue();
	}
}