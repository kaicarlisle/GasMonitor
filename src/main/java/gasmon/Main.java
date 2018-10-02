package gasmon;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
		
		List<MessageResponse.Message> parsedMessages = new ArrayList<MessageResponse.Message>();
		SNSTopicReceiver topicReceiver = new SNSTopicReceiver(credentialsProvider);
		
		ArrayList<SensorPoint> readings = getReadingsInGraphFormat(sensors);
		GraphRenderer graphRenderer = new GraphRenderer(readings);
		
		//request and handle messages from sqs, associating readings with known scanners
		int maxNumber = 5;
		for (int i = 0; i < maxNumber; i++) {
			List<Message> messages = receiveNextMessages(topicReceiver, sensors, parsedMessages);
			addReadingsToSensors(sensors, messages);
//			writeAllReadingsToCSV(sensors);
			readings = getReadingsInGraphFormat(sensors);
			graphRenderer.updateValues(readings);
			System.out.println("Scanning " + ((i+1)*100/maxNumber) + "%");
		}
		topicReceiver.deleteQueue();
		System.out.println("Program terminated succesfully");
	}
	
	private static List<Message> receiveNextMessages(SNSTopicReceiver topicReceiver, Sensor[] sensors, List<MessageResponse.Message> parsedMessages) throws InterruptedException {
		MessageParser messageParser = new MessageParser();
		Stream<MessageResponse.Message> parsedMessageStream;
		
		//make less frequent requests getting more readings at once
		Thread.sleep(5000);
		List<String> messages = topicReceiver.getNextMessages(10);
		
		//parse messages into MessageResponse objects
		for (String message : messages) {
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
						   					.collect(Collectors.toList());
		
		return parsedMessages;
	}
	
	private static void addReadingsToSensors(Sensor[] sensors, List<Message> messages) {
		//add each MessageResponse.Message object to the relevant sensor object
		for (Sensor s : sensors) {
			s.clearReadings();
			for (MessageResponse.Message m : messages) {
				if (s.getID().equals(m.locationId)) {
					s.addReading(m);
				}
			}
		}
	}
	
	private static void writeAllReadingsToCSV(Sensor[] sensors) {
		for (Sensor s : sensors) {
			File file = new File("src/main/resources/readings/"+s.humanReadableName+".csv");
			String[] header = {"Timestamps", "Readings"};
			String[] position = {String.valueOf(s.x), String.valueOf(s.y)};
			CsvWriter writer = new CsvWriter(file, position);
			writer.writeNextLine(header);
			for (MessageResponse.Message m : s.getReadings()) {
				String[] line = {String.valueOf(m.getTimestampFromLong()), String.valueOf(m.value)};
				writer.writeNextLine(line);
			}
			writer.closeWriter();
		}
	}
	
	private static ArrayList<SensorPoint> getReadingsInGraphFormat(Sensor[] sensors) {
		ArrayList<SensorPoint> readings = new ArrayList<SensorPoint>();
		double max = 0;
		double average;
		for (Sensor s : sensors) {
			average = s.getAverage();
			max = average > max ? average : max;
			SensorPoint point = new SensorPoint(s.x, s.y, average);
			readings.add(point);
		}
		SensorPoint.MAX_READING = max;
		for (SensorPoint s : readings) {
			s.setColour();
		}
		return readings;
	}
	
	private SensorPoint triangulateSource(ArrayList<SensorPoint> readings) {
		return new SensorPoint(1,2,3);
	}
}