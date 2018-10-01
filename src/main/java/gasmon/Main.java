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
			parsedMessages = parsedMessageStream.filter(message -> (message.getTimestampFromLong().after(nowTimestamp)))
							   					.distinct()
//							   					.forEach(System.out::println);
							   					.collect(Collectors.toList());
			
			//add each MessageResponse.Message object to the relevant sensor object
			for (MessageResponse.Message m : parsedMessages) {
				for (Sensor s : sensors) {
					if (s.getID().equals(m.locationId)) {
						s.addReading(m);
						System.out.println(m.value + " added to sensor " + s.humanReadableName + " at " + m.getTimestampFromLong());
					}
				}	
			}
			
			//TODO get averages over all the readings in the sensor
			//TODO make each sensor a similar stream object that filters based on time, as otherwise it will store every past reading
		}
		topicReceiver.deleteQueue();
	}
}