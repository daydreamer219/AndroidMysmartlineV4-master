package eu.mysmartline.appv3.models;

import java.util.Random;

public class GcmTestMessage {
	private String text;
	public static GcmTestMessage buildTestMessage(){
		GcmTestMessage message = new GcmTestMessage();
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 8; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    sb.append(c);
		}
		message.setText(sb.toString());
		return message;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
}
