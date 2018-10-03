package gasmon;

import java.awt.Point;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;

import gasmon.Parsing.*;
import gasmon.Graphics.*;
import gasmon.AwsRequests.*;

public class GasMonMain {
	
	static int NUMBER_OF_SCANS;// = 20;
	static int THREAD_SLEEP_BETWEEN_REQUESTS;// = 100;
	static int GRANULARITY_OF_GUESS;// = 1;
	static int NUMBER_OF_MESSAGES_PER_REQUEST;// = 10; //max = 10
	static int HAMMING_DISTANCE_THRESHHOLD;// = 10;
	static int GUESS_STRIKES;// = 5;
	
	public GasMonMain(int ns, int gg, int nm, int h, int gs) {
		NUMBER_OF_SCANS = ns;
		THREAD_SLEEP_BETWEEN_REQUESTS = 1000;
		GRANULARITY_OF_GUESS = gg;
		NUMBER_OF_MESSAGES_PER_REQUEST = nm;
		HAMMING_DISTANCE_THRESHHOLD = h;
		GUESS_STRIKES = gs;
	}
	
	public void updateParameters(int ns, int gg, int nm, int h, int gs) {
		NUMBER_OF_SCANS = ns;
		THREAD_SLEEP_BETWEEN_REQUESTS = 1000;
		GRANULARITY_OF_GUESS = gg;
		NUMBER_OF_MESSAGES_PER_REQUEST = nm;
		HAMMING_DISTANCE_THRESHHOLD = h;
		GUESS_STRIKES = gs;
	}
	
	public Point execute() throws InterruptedException {
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(".aws/credentials", "default");
		Sensor[] sensors = new Sensor[0];
		
		//get locations.json from aws
		GetLocationsJSONFromAWS getLocationsFromAWS = new GetLocationsJSONFromAWS(credentialsProvider);
		File locationsJSON = getLocationsFromAWS.getLocationsFile();
		
		//parse locations.json into Sensors[]
		LocationsParser locationsParser = new LocationsParser(locationsJSON);
		sensors = locationsParser.parse();
		
		List<Message> parsedMessages = new ArrayList<Message>();
		SNSTopicReceiver topicReceiver = new SNSTopicReceiver(credentialsProvider);
		
		ArrayList<SensorPoint> readings = getReadingsInGraphFormat(sensors);
		ArrayList<SensorPoint> estimates = setupGuesses();
		SensorPoint meanEstimate = getMeanEstimate(estimates);
//		GraphRenderer graphRenderer = new GraphRenderer(readings, estimates, meanEstimate);
		
		//request and handle messages from sqs, associating readings with known scanners
		for (int i = 0; i < NUMBER_OF_SCANS; i++) {
			try {
				List<Message> messages = receiveNextMessages(topicReceiver, sensors, parsedMessages);
				addReadingsToSensors(sensors, messages);
			} catch (QueueDoesNotExistException e) {
				continue;
			}
			readings = getReadingsInGraphFormat(sensors);
			estimates = matchConeToFindSource(readings, estimates);
			meanEstimate = getMeanEstimate(estimates);
//			graphRenderer.updateValues(readings, estimates, meanEstimate);
//			System.out.println("Scanning " + ((i+1)*100/NUMBER_OF_SCANS) + "%");
		}
		try {
			topicReceiver.deleteQueue();
		} catch (QueueDoesNotExistException e) {
			e.printStackTrace();
		}
//		System.out.println("Program terminated successfully");
		System.out.println("Final estimate: " + meanEstimate.getPosAsString());
		return new Point(meanEstimate.x, meanEstimate.y);
	}
	
	private static List<Message> receiveNextMessages(SNSTopicReceiver topicReceiver, Sensor[] sensors, List<Message> parsedMessages) throws InterruptedException {
		MessageParser messageParser = new MessageParser();
		Stream<Message> parsedMessageStream;
		
		//make less frequent requests getting more readings at once
		Thread.sleep(THREAD_SLEEP_BETWEEN_REQUESTS);
		List<String> messages = topicReceiver.getNextMessages(NUMBER_OF_MESSAGES_PER_REQUEST);
		
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
		//add each Message object to the relevant sensor object
		for (Sensor s : sensors) {
			s.clearReadings();
			for (Message m : messages) {
				if (s.getID().equals(m.locationId)) {
					s.addReading(m);
				}
			}
		}
	}
	
	private static ArrayList<SensorPoint> getReadingsInGraphFormat(Sensor[] sensors) {
		ArrayList<SensorPoint> readings = new ArrayList<SensorPoint>();
		double max = 0;
		double average;
		for (Sensor s : sensors) {
			average = s.getAverage();
			max = average > max ? average : max;
			SensorPoint point = new SensorPoint(s.x, s.y, average, GUESS_STRIKES);
			point.relatedSensor = s;
			readings.add(point);
		}
		SensorPoint.MAX_READING = max;
		for (SensorPoint s : readings) {
			s.setColour();
		}
		return readings;
	}
	
	private static ArrayList<SensorPoint> setupGuesses() {
		ArrayList<SensorPoint> initialGuesses = new ArrayList<SensorPoint>();
		
		for (int i = 0; i < 1000; i += GRANULARITY_OF_GUESS) {
			for (int j = 0; j < 1000; j += GRANULARITY_OF_GUESS) {
				initialGuesses.add(new SensorPoint(i, j, 0, GUESS_STRIKES));
			}
		}
		return initialGuesses;
	}
	
	private static ArrayList<SensorPoint> matchConeToFindSource(ArrayList<SensorPoint> readings, ArrayList<SensorPoint> previousEstimates) {
		ArrayList<SensorPoint> validGuesses = new ArrayList<SensorPoint>();
		
		TreeMap<Double, SensorPoint> sortedByValues = new TreeMap<Double, SensorPoint>();
		for (SensorPoint s : readings) {
			if (!Double.isNaN(s.value)) {
				sortedByValues.put(s.value, s);	
			}
		}
		
		for (SensorPoint previousGuess : previousEstimates) {
			if (getHammingDistance(previousGuess, sortedByValues) < HAMMING_DISTANCE_THRESHHOLD) {
				validGuesses.add(previousGuess);
			} else if (previousGuess.strikes > 0) {
				validGuesses.add(previousGuess);
				previousGuess.strikes--;
			}
		}
		if (validGuesses.size() == 0) {
			validGuesses = previousEstimates;
		}
		return validGuesses;
	}
	
	private static int getHammingDistance(SensorPoint guess, TreeMap<Double, SensorPoint> sortedByValues) {
		TreeMap<Double, SensorPoint> distances = new TreeMap<Double, SensorPoint>();
		int hammingDistance = 0;
		
		for (SensorPoint s : sortedByValues.values()) {
			Double distance = Math.pow(s.x - guess.x, 2) + Math.pow(s.y - guess.y, 2);
			distances.put(distance, s);
		}
		
		//this is a valid guess if hamming distance is less than HAMMING_DISTANCE_THRESHHOLD
		//hamming distance = number of sensorpoints that are out of place
		
		for (Double d : sortedByValues.descendingKeySet()) {
			SensorPoint highestValue = sortedByValues.get(d);
			if (distances.size() > 0) {
				SensorPoint lowestDistance = distances.get(distances.firstKey());
				if (!lowestDistance.equals(highestValue)) {
					hammingDistance += 1;
					if (hammingDistance > HAMMING_DISTANCE_THRESHHOLD) {
						return hammingDistance;
					}
				}
				distances.remove(distances.firstKey());
			}
		}
		return hammingDistance;
	}
	
	private static SensorPoint getMeanEstimate(ArrayList<SensorPoint> validGuesses) {
		int mX = 0;
		int mY = 0;
		if (validGuesses.size() > 0) {
			for (SensorPoint s : validGuesses) {
				mX += s.x;
				mY += s.y;
			}
			mX /= validGuesses.size();
			mY /= validGuesses.size();
		}
		
		return new SensorPoint(mX, mY, 0, GUESS_STRIKES);
	}
}