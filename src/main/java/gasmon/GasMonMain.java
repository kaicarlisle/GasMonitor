package gasmon;

import java.awt.Point;
import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;

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
	
	public Point execute(boolean displayGraphics) throws InterruptedException {
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider(".aws/credentials", "default");
		
		//get locations.json from aws
		File locationsJSON = new GetLocationsJSONFromAWS(credentialsProvider).getLocationsFile();
		
		//parse locations.json into Sensors[]
		Sensor[] sensors = new LocationsParser(locationsJSON).parse();
		
		ArrayList<String> messageBodies = new ArrayList<String>();
		
		ArrayList<SensorPoint> readings = getAverageReadingsAsSensorPoints(sensors);
		ArrayList<SensorPoint> estimates = setupGuesses();
		SensorPoint meanEstimate = getMeanEstimate(estimates);
		
		GraphRenderer graphRenderer = null;
		if (displayGraphics) {
			graphRenderer = new GraphRenderer(readings, estimates, meanEstimate);
		}
		
		
		GetMessageRequestThread t1 = new GetMessageRequestThread("t1", THREAD_SLEEP_BETWEEN_REQUESTS, credentialsProvider, NUMBER_OF_MESSAGES_PER_REQUEST);
		t1.start();
		GetMessageRequestThread t2 = new GetMessageRequestThread("t2", THREAD_SLEEP_BETWEEN_REQUESTS, credentialsProvider, NUMBER_OF_MESSAGES_PER_REQUEST);
		t2.start();
		GetMessageRequestThread t3 = new GetMessageRequestThread("t3", THREAD_SLEEP_BETWEEN_REQUESTS, credentialsProvider, NUMBER_OF_MESSAGES_PER_REQUEST);
		t3.start();
		GetMessageRequestThread t4 = new GetMessageRequestThread("t4", THREAD_SLEEP_BETWEEN_REQUESTS, credentialsProvider, NUMBER_OF_MESSAGES_PER_REQUEST);
		t4.start();
		GetMessageRequestThread t5 = new GetMessageRequestThread("t5", THREAD_SLEEP_BETWEEN_REQUESTS, credentialsProvider, NUMBER_OF_MESSAGES_PER_REQUEST);
		t5.start();
		
		//request and handle messages from sqs, associating readings with known scanners
		for (int i = 0; i < NUMBER_OF_SCANS; i++) {
			Thread.sleep(500);
			//add multithreading to the getting of messages
			//all readings get pooled into one List<String> messageBodies, to be parsed etc
			messageBodies.addAll(t1.messages);
			messageBodies.addAll(t2.messages);
			messageBodies.addAll(t3.messages);
			messageBodies.addAll(t4.messages);
			messageBodies.addAll(t5.messages);
			
			
			List<Message> messages = parseMessages(messageBodies);
			addReadingsToSensors(sensors, messages);
			filterMessages(sensors);
			
			readings = getAverageReadingsAsSensorPoints(sensors);
			estimates = matchConeToFindSource(readings, estimates);
			meanEstimate = getMeanEstimate(estimates);
			if (graphRenderer != null) {
				graphRenderer.updateValues(readings, estimates, meanEstimate);
				System.out.println("Scanning " + ((i+1)*100/NUMBER_OF_SCANS) + "%");
			}
		}
		t1.kill();
		t2.kill();
		t3.kill();
		t4.kill();
		t5.kill();
		System.out.println("Program terminated successfully");
		System.out.println("Final estimate: " + meanEstimate.getPosAsString());

		return new Point(meanEstimate.x, meanEstimate.y);
	}
	
	private static List<Message> parseMessages(List<String> messages) {
		MessageParser messageParser = new MessageParser();
		return messages.stream()
					   .map(entry -> messageParser.parse(entry).messageBody)
					   .collect(Collectors.toList());
	}
	
	private static void addReadingsToSensors(Sensor[] sensors, List<Message> messages) {
		//add each Message object to the relevant sensor object
		for (Sensor s : sensors) {
			s.readings.addAll(messages.stream()
								 	  .filter(entry -> s.getID().equals(entry.locationId))
								 	  .collect(Collectors.toList()));
		}
	}
	private static void filterMessages(Sensor[] sensors) {
		for (Sensor s : sensors) {
			//filter sensor readings to:
			//	only include messages from the past 5 minutes
			//  only include distinct readings (based on eventID)
			Date now = new Date();
			Timestamp nowTimestamp = new Timestamp(now.getTime() - 300000);
			
			s.readings = s.readings.stream()
					  			   .filter(message -> (message.getTimestampFromLong().after(nowTimestamp)))
					  			   .distinct()
					  			   .collect(Collectors.toList());
		}
	}
	
	private static ArrayList<SensorPoint> getAverageReadingsAsSensorPoints(Sensor[] sensors) {
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
			previousGuess.setAlpha(GUESS_STRIKES);
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
		
		for (SensorPoint s : validGuesses) {
			mX += s.x;
			mY += s.y;
		}
		mX /= validGuesses.size();
		mY /= validGuesses.size();
		
		return new SensorPoint(mX, mY, 0, GUESS_STRIKES);
	}
}