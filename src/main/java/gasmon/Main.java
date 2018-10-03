package gasmon;

import java.awt.Point;
import java.util.Random;
import java.util.TreeMap;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class Main {
	
	private final static Point GOAL = new Point(569, 51);
	private final static int NUMBER_OF_GENERATIONS = 10;
	private final static int NUMBER_OF_CHILDREN_PER_GENERATION = 2;
	private final static double MUTATE_CHANCE = 0.1;
	
	private final static int MAX_NUMBER_OF_SCANS = 100;
	private final static int MIN_NUMBER_OF_SCANS = 20;
	private final static int MAX_GRANULARITY_OF_GUESS = 10;
	private final static int MIN_GRANULARITY_OF_GUESS = 1;
	private final static int MAX_NUMBER_OF_MESSAGES_PER_REQUEST = 10;
	private final static int MIN_NUMBER_OF_MESSAGES_PER_REQUEST = 1;
	private final static int MAX_HAMMING_DISTANCE_THRESHHOLD = 20;
	private final static int MIN_HAMMING_DISTANCE_THRESHHOLD = 1;
	private final static int MAX_GUESS_STRIKES = 20;
	private final static int MIN_GUESS_STRIKES = 0;
	
	//TODO
	//mutate x
	//crossover
	//select x

	public static void main(String[] args) throws IOException {
		GasMonMain program;
		
		TreeMap<Double, Integer[]> generationData = new TreeMap<Double, Integer[]>();
		Integer[] initialArgs = {20, 10, 1, 20, 0};
		Integer[] bestArgs = initialArgs;
		Point bestEstimate = new Point(0, 0);
		double bestFitness = 0;
		log(bestArgs);
		
		//for each generation
		for  (int i = 0; i < NUMBER_OF_GENERATIONS; i++) {
			System.out.println("\nStarted generation " + i);
			//get the best from previous generation
			generationData = new TreeMap<Double, Integer[]>();
			generationData.put(bestFitness, bestArgs);
			
			initialArgs = bestArgs;
			
			//mutate
			for (int j = 0; j < NUMBER_OF_CHILDREN_PER_GENERATION; j++) {
				System.out.println("Started run " + j);
				Integer[] gasMonArgs = mutate(initialArgs);
				program = new GasMonMain(gasMonArgs[0], gasMonArgs[1], gasMonArgs[2], gasMonArgs[3], gasMonArgs[4]);
				try {
					Point estimate = program.execute();
					double fitness = getFitness(estimate);
					if (fitness > generationData.lastKey()) {
						bestEstimate = estimate;
					}
					generationData.put(fitness, gasMonArgs);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			bestArgs = generationData.get(generationData.lastKey());
			bestFitness = generationData.lastKey();
			log(bestEstimate);
			log(bestArgs);
		}
		
		System.out.println("Check log for results?");
	}
	
	private static void log(Integer[] bestArgs) throws IOException {
		FileWriter fileWriter = new FileWriter(new File("src/main/resources/GA-results.txt"), true);
		fileWriter.write("\nNUMBER_OF_SCANS: " + bestArgs[0]);
		fileWriter.write("\nGRANULARITY_OF_GUESS: " + bestArgs[1]);
		fileWriter.write("\nNUMBER_OF_MESSAGES_PER_REQUEST: " + bestArgs[2]);
		fileWriter.write("\nHAMMING_DISTANCE_THRESHHOLD: " + bestArgs[3]);
		fileWriter.write("\nGUESS_STRIKES: " + bestArgs[4]);
		fileWriter.write("\n----------------");
		fileWriter.close();
	}
	
	private static void log(Point estimate) throws IOException {
		FileWriter fileWriter = new FileWriter(new File("src/main/resources/GA-results.txt"), true);
		fileWriter.write("\nEstimate: (" + estimate.getX() + "," + estimate.getY() + ")");
		fileWriter.close();
	}
	
	private static Integer[] mutate(Integer[] args) {
		Random r = new Random();
		if (r.nextFloat() < MUTATE_CHANCE) {
			args[0] = r.nextInt(MAX_NUMBER_OF_SCANS - MIN_NUMBER_OF_SCANS) + MIN_NUMBER_OF_SCANS;
		}
		if (r.nextFloat() < MUTATE_CHANCE) {
			args[1] = r.nextInt(MAX_GRANULARITY_OF_GUESS - MIN_GRANULARITY_OF_GUESS) + MIN_GRANULARITY_OF_GUESS;
		}
		if (r.nextFloat() < MUTATE_CHANCE) {
			args[2] = r.nextInt(MAX_NUMBER_OF_MESSAGES_PER_REQUEST - MIN_NUMBER_OF_MESSAGES_PER_REQUEST) + MIN_NUMBER_OF_MESSAGES_PER_REQUEST;
		}
		if (r.nextFloat() < MUTATE_CHANCE) {
			args[3] = r.nextInt(MAX_HAMMING_DISTANCE_THRESHHOLD - MIN_HAMMING_DISTANCE_THRESHHOLD) + MIN_HAMMING_DISTANCE_THRESHHOLD;
		}
		if (r.nextFloat() < MUTATE_CHANCE) {
			args[4] = r.nextInt(MAX_GUESS_STRIKES - MIN_GUESS_STRIKES) + MIN_GUESS_STRIKES;
		}
		return args;
	}
	
	private static double getFitness(Point finalEstimate) {
		double distance = GOAL.distance(finalEstimate);
		double x = distance / 20;
		double power = -(Math.pow(x, 2))/ 2;
		double fitness = Math.pow(Math.E, power);
		return fitness;
	}

}
