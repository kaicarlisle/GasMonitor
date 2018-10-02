package gasmon;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
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
		ArrayList<SensorPoint> estimate = matchConeToFindSource(readings, 10);
		GraphRenderer graphRenderer = new GraphRenderer(readings, estimate);
		
		//request and handle messages from sqs, associating readings with known scanners
		int maxNumber = 20;
		for (int i = 0; i < maxNumber; i++) {
			List<Message> messages = receiveNextMessages(topicReceiver, sensors, parsedMessages);
			addReadingsToSensors(sensors, messages);
//			writeAllReadingsToCSV(sensors);
			readings = getReadingsInGraphFormat(sensors);
			estimate = matchConeToFindSource(readings, 10);
			graphRenderer.updateValues(readings, estimate);
			System.out.println(getMeanEstimate(estimate).getPosAsString());
			System.out.println("Scanning " + ((i+1)*100/maxNumber) + "%");
		}
		topicReceiver.deleteQueue();
		System.out.println("Program terminated succesfully");
	}
	
	private static List<Message> receiveNextMessages(SNSTopicReceiver topicReceiver, Sensor[] sensors, List<MessageResponse.Message> parsedMessages) throws InterruptedException {
		MessageParser messageParser = new MessageParser();
		Stream<MessageResponse.Message> parsedMessageStream;
		
		//make less frequent requests getting more readings at once
		Thread.sleep(1000);
		List<String> messages = topicReceiver.getNextMessages(4);
		
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
			point.relatedSensor = s;
			readings.add(point);
		}
		SensorPoint.MAX_READING = max;
		for (SensorPoint s : readings) {
			s.setColour();
		}
		return readings;
	}
	
	private static ArrayList<SensorPoint> matchConeToFindSource(ArrayList<SensorPoint> readings, int granularity) {
		ArrayList<SensorPoint> validGuesses = new ArrayList<SensorPoint>();
		SensorPoint guess = new SensorPoint(0, 0, 0);
		
		TreeMap<Double, SensorPoint> sortedByValues = new TreeMap<Double, SensorPoint>();
		for (SensorPoint s : readings) {
			if (!Double.isNaN(s.value)) {
				sortedByValues.put(s.value, s);	
			}
		}
		
		for (int i = 0; i < 1000; i += granularity) {
			for (int j = 0; j < 1000; j += granularity) {
				guess.x = i;
				guess.y = j;
				if (isValidGuess(guess, sortedByValues)) {
					validGuesses.add(new SensorPoint(i, j, 0));
				}
			}
		}
				
		return validGuesses;
	}
	
	private static boolean isValidGuess(SensorPoint guess, TreeMap<Double, SensorPoint> sortedByValues) {
		TreeMap<Double, SensorPoint> distances = new TreeMap<Double, SensorPoint>();
		
		for (SensorPoint s : sortedByValues.values()) {
			Double distance = Math.pow(s.x - guess.x, 2) + Math.pow(s.y - guess.y, 2);
			distances.put(distance, s);
		}
		
		for (Double d : sortedByValues.descendingKeySet()) {
			SensorPoint highestValue = sortedByValues.get(d);
			SensorPoint lowestDistance = distances.get(distances.firstKey());
			if (!lowestDistance.equals(highestValue)) {
				return false;
			}
			distances.remove(distances.firstKey());
		}
		
		return true;
	}
	
	private static SensorPoint getMeanEstimate(ArrayList<SensorPoint> validGuesses) {
		int mX = 0;
		int mY = 0;
		for (SensorPoint s : validGuesses) {
			mX += s.x;
			mY += s.y;
		}
		mX /= validGuesses.size();
		mY /= validGuesses.size();
		
		return new SensorPoint(mX, mY, 0);
	}
}