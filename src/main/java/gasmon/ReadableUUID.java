package gasmon;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class ReadableUUID {
	public String UUID = "";
	
	public ReadableUUID(int numOfWords) {
		File file = new File("src/main/resources/dictionary.txt");
		BufferedReader reader;
		Random r = new Random();
		ArrayList<String> words = new ArrayList<String>();
		String line;
		
		try {
			reader = new BufferedReader(new FileReader(file));
			while ((line = reader.readLine()) != null) {
				words.add(line.substring(0, 1).toUpperCase() + line.substring(1));
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < numOfWords; i++) {
			this.UUID += words.get(r.nextInt(words.size()));
		}
	}

}
