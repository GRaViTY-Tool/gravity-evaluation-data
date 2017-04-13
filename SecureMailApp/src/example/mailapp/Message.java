package example.mailapp;

import java.util.List;

public class Message {

	private String text;
	private String encoding;
	private List<String> receivers;
	
	
	public Message(String text, List<String> receivers) {
		this.text = text;
		this.receivers = receivers;
		this.encoding = "UTF-8";
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getEncoding() {
		return encoding;
	}
	
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public List<String> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<String> receivers) {
		this.receivers = receivers;
	}
}
