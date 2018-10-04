package gasmon;

import java.awt.Point;
import java.util.Random;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class Main {
	
	private final static Point GOAL = new Point(925, 989);
	private final static int NUMBER_OF_GENERATIONS = 10;
	private final static int NUMBER_OF_CHILDREN_PER_GENERATION = 10;
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

	public static void main(String[] args) throws IOException, InterruptedException {
		runGraphic();
//		runGA();
	}
	
	private static void runGraphic() throws InterruptedException {
		GasMonMain program = new GasMonMain(41, 2, 2, 20, 18);
		program.execute(true);
	}
	
	private static void runGA() throws IOException {
		Integer[] initialArgs = {41, 1, 1, 20, 14};
		
		Integer[] bestArgs = initialArgs;
		Point bestEstimate = new Point(0, 0);
		double bestFitness = 0;
		log(bestArgs);
		
		GasMonMain program = new GasMonMain(initialArgs[0], initialArgs[1], initialArgs[2], initialArgs[3], initialArgs[4]);
		
		loop:
		for  (int i = 0; i < NUMBER_OF_GENERATIONS; i++) {
			System.out.println("\nStarted generation " + i);
			//get the best from previous generation
			boolean updatedEstimate = false;
			initialArgs = bestArgs;
			
			for (int j = 0; j < NUMBER_OF_CHILDREN_PER_GENERATION; j++) {
				System.out.println("Started run " + j);
				Integer[] thisArgs = mutate(initialArgs);
				program.updateParameters(thisArgs[0], thisArgs[1], thisArgs[2], thisArgs[3], thisArgs[4]);
				try {
					Point thisEstimate = program.execute(false);
					log(thisArgs, thisEstimate);
					double thisFitness = getFitness(thisEstimate);
					if (thisFitness > bestFitness) {
						bestEstimate = thisEstimate;
						bestFitness = thisFitness;
						bestArgs = thisArgs;
						updatedEstimate = true;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			log(bestEstimate);
			if (updatedEstimate) {
				log(bestArgs);
			}
			if (bestFitness == 1.0) {
				System.out.println("Found a solution that gets it 100%");
				break loop;
			}
		}
		
		System.out.println("Program FINISHED\nCheck log for results");
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
	
	private static void log(Integer[] args, Point estimate) throws IOException {
		FileWriter fileWriter = new FileWriter(new File("src/main/resources/GA-every-run.txt"), true);
		fileWriter.write("\nEstimate: (" + estimate.getX() + "," + estimate.getY() + ")");
		fileWriter.write("\nNUMBER_OF_SCANS: " + args[0]);
		fileWriter.write("\nGRANULARITY_OF_GUESS: " + args[1]);
		fileWriter.write("\nNUMBER_OF_MESSAGES_PER_REQUEST: " + args[2]);
		fileWriter.write("\nHAMMING_DISTANCE_THRESHHOLD: " + args[3]);
		fileWriter.write("\nGUESS_STRIKES: " + args[4]);
		fileWriter.write("\n----------------");
		fileWriter.close();
	}
	
	private static Integer[] mutate(Integer[] args) {
		Integer[] newArgs = {args[0], args[1], args[2], args[3], args[4]};
		Random r = new Random();
		if (r.nextFloat() < MUTATE_CHANCE) {
			newArgs[0] = r.nextInt(MAX_NUMBER_OF_SCANS - MIN_NUMBER_OF_SCANS) + MIN_NUMBER_OF_SCANS;
		}
		if (r.nextFloat() < MUTATE_CHANCE) {
			newArgs[1] = r.nextInt(MAX_GRANULARITY_OF_GUESS - MIN_GRANULARITY_OF_GUESS) + MIN_GRANULARITY_OF_GUESS;
		}
		if (r.nextFloat() < MUTATE_CHANCE) {
			newArgs[2] = r.nextInt(MAX_NUMBER_OF_MESSAGES_PER_REQUEST - MIN_NUMBER_OF_MESSAGES_PER_REQUEST) + MIN_NUMBER_OF_MESSAGES_PER_REQUEST;
		}
		if (r.nextFloat() < MUTATE_CHANCE) {
			newArgs[3] = r.nextInt(MAX_HAMMING_DISTANCE_THRESHHOLD - MIN_HAMMING_DISTANCE_THRESHHOLD) + MIN_HAMMING_DISTANCE_THRESHHOLD;
		}
		if (r.nextFloat() < MUTATE_CHANCE) {
			newArgs[4] = r.nextInt(MAX_GUESS_STRIKES - MIN_GUESS_STRIKES) + MIN_GUESS_STRIKES;
		}
		return newArgs;
	}
	
	private static double getFitness(Point finalEstimate) {
		double distance = GOAL.distance(finalEstimate);
		double x = distance / 20;
		double power = -(Math.pow(x, 2))/ 2;
		double fitness = Math.pow(Math.E, power);
		return fitness;
	}

}
