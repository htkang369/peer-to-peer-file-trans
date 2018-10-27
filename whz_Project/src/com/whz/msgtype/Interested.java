package com.whz.msgtype;

import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class Interested extends ActualMsg{

	public Interested(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
	}
	
	public Interested(byte[] message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)INTERESTED;
	}

	public Interested(int message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)INTERESTED;
	}
}
