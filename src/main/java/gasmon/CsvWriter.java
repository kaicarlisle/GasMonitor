package gasmon;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

public class CsvWriter {
	private FileWriter fileWriter;
	private CSVWriter writer;

	public CsvWriter(File outputFile, String[] header) {
		try {
			fileWriter = new FileWriter(outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		writer = new CSVWriter(fileWriter);
		writer.writeNext(header);
	}
	
	public void writeNextLine(String[] values) {
		this.writer.writeNext(values);
	}
	
	public boolean closeWriter() {
		try {
			this.writer.close();
			this.fileWriter.close();
			return true;
		} catch (IOException e) {
			return false;
		}
		
	}
}
