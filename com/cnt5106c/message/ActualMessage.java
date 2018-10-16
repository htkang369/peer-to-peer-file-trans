package com.cnt5106c.message;

public class ActualMessage {
	private int message_length;
	private Byte message_type;
	private final char[] message_payload;
	
	public ActualMessage(int message_length, Byte message_type, char[] message_payload) {
		this.message_length = message_length;
		this.message_type = message_type;
		this.message_payload = message_payload;
	}
	
}
