package gasmon.AwsRequests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;


public class GetLocationsJSONFromAWS {
	private ProfileCredentialsProvider credentialsProvider;
	private String bucketName;
	private String keyName;
	private File file;
	private AmazonS3 s3;
	private S3Object o;
	private S3ObjectInputStream s3is;
	private FileOutputStream fos;
	private byte[] read_buf;
    private int read_len;
	
	public GetLocationsJSONFromAWS(ProfileCredentialsProvider credentialsProvider) {
		this.credentialsProvider = credentialsProvider;
		this.bucketName = "eventprocessing-rfm-sept-2018-locationss3bucket-186b0uzd6cf01";
		this.keyName = "locations-part2.json";
		this.file = new File("src/main/resources/" + this.keyName);
		
		if (!this.file.exists()) {
			getLocations();
		}
	}
	
	private void getLocations() {
		this.s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(this.credentialsProvider).build();
		try {
		    this.o = this.s3.getObject(this.bucketName, this.keyName);
		    this.s3is = this.o.getObjectContent();
		    this.fos = new FileOutputStream(this.file);
		    this.read_buf = new byte[1024];
		    this.read_len = 0;
		    while ((this.read_len = this.s3is.read(this.read_buf)) > 0) {
		        this.fos.write(this.read_buf, 0, this.read_len);
		    }
		    this.s3is.close();
		    this.fos.close();
		} catch (AmazonServiceException e) {
		    e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.out.println("File does not exist on aws");
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}
	
	public File getLocationsFile() {
		return this.file;
	}
}
