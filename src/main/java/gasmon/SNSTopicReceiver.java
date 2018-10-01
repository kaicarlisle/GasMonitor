package gasmon;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.util.Topics;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class SNSTopicReceiver {
	private final String ARN = "arn:aws:sns:eu-west-1:552908040772:EventProcessing-RFM-Sept-2018-snsTopicSensorDataPart1-PUR0KBORONQF";
	private AmazonSNS sns;
	private AmazonSQS sqs;
	private String myQueueUrl;
	private List<Message> messages;
	private ArrayList<String> messageBodies;
	
	public SNSTopicReceiver(ProfileCredentialsProvider credentialsProvider) {
		this.sns = AmazonSNSClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(credentialsProvider).build();
		this.sqs = AmazonSQSClientBuilder.standard().withRegion(Regions.EU_WEST_1).withCredentials(credentialsProvider).build();
	
		this.myQueueUrl = this.sqs.createQueue(new CreateQueueRequest("SOMethingExcitIng")).getQueueUrl();
		
		//subscribe the sqs to the sns
		Topics.subscribeQueue(this.sns, this.sqs, this.ARN, this.myQueueUrl);
	}
	
	public List<String> getNextMessages(int number) {
		this.messages = this.sqs.receiveMessage(new ReceiveMessageRequest(this.myQueueUrl).withMaxNumberOfMessages(number)).getMessages();
		if (this.messageBodies != null) {
			this.messageBodies.clear();
		} else {
			this.messageBodies = new ArrayList<String>();
		}
		
		for (Message message : messages) {
			this.sqs.deleteMessage(this.myQueueUrl, message.getReceiptHandle());
		    this.messageBodies.add(message.getBody());
		}
		return messageBodies;
	}
	
	public void deleteQueue() {
		this.sqs.deleteQueue(this.myQueueUrl);
	}
}
