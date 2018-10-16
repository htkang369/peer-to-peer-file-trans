package com.whz.msg;

public class ActualMsg {
	private int msg_length;
	private final Byte msg_type;
	private char[] msg_payload;
	
	public ActualMsg(int message_length, Byte message_type, char[] message_payload) {
		this.msg_length = message_length;
		this.msg_type = message_type;
		this.msg_payload = message_payload;
	}
	
}
