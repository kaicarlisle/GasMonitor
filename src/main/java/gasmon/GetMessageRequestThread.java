package gasmon;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import gasmon.AwsRequests.SNSTopicReceiver;

public class GetMessageRequestThread extends Thread {
	private Thread t;
	private String threadName;
	private int sleepTime;
	public List<String> messages;
	private SNSTopicReceiver topicReceiver;
	private int numberOfMessages;
	private boolean workToDo;
	
	public GetMessageRequestThread(String threadName, int sleepTime, ProfileCredentialsProvider credentialsProvider, int numberOfMessages) {
		this.topicReceiver = new SNSTopicReceiver(credentialsProvider);
		this.threadName = threadName;
		this.sleepTime = sleepTime;
		this.numberOfMessages = numberOfMessages;
		this.messages = new ArrayList<String>();
		this.workToDo = true;
	}
	
	public void run() {
		while (this.workToDo) {
			try {
				Thread.sleep(this.sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.messages = this.topicReceiver.getNextMessages(this.numberOfMessages);
		}
	}
	
	public void start() {
		if (t == null) {
			t = new Thread(this, this.threadName);
			t.start();
		}
	}
	
	public boolean kill() {
		if (t.isAlive()) {
			this.topicReceiver.deleteQueue();
			this.workToDo = false;
			return true;
		} else {
			return false;
		}
	}
}
