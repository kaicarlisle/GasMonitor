package gasmon.AwsRequests;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.QueueDoesNotExistException;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import gasmon.*;

public class SNSTopicReceiver {
	
	//ARN for setting 1
//	private final String ARN = "arn:aws:sns:eu-west-1:552908040772:EventProcessing-RFM-Sept-2018-snsTopicSensorDataPart1-PUR0KBORONQF";
	//ARN for setting 2
	private final String ARN = "arn:aws:sns:eu-west-1:552908040772:EventProcessing-RFM-Sept-2018-snsTopicSensorDataPart2-Z3K3NB3PRHGH";
	private AmazonSNS sns;
	private AmazonSQS sqs;
	private String myQueueUrl;
	private List<Message> messages;
	
	public SNSTopicReceiver(ProfileCredentialsProvider credentialsProvider) {
		this.sns = AmazonSNSClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(credentialsProvider).build();
		this.sqs = AmazonSQSClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(credentialsProvider).build();
	
		this.myQueueUrl = this.sqs.createQueue(new CreateQueueRequest(new ReadableUUID(3).UUID)).getQueueUrl();
		
		//subscribe the sqs to the sns
		Topics.subscribeQueue(this.sns, this.sqs, this.ARN, this.myQueueUrl);
	}
	
	public List<String> getNextMessages(int number) {
		this.messages = this.sqs.receiveMessage(new ReceiveMessageRequest(this.myQueueUrl).withMaxNumberOfMessages(number)).getMessages();

		this.messages.forEach(entry -> this.sqs.deleteMessage(this.myQueueUrl, entry.getReceiptHandle()));
		return this.messages.stream().map(entry -> entry.getBody()).collect(Collectors.toList());
	}
	
	public void deleteQueue() throws QueueDoesNotExistException {
		this.sqs.deleteQueue(this.myQueueUrl);
	}
}
