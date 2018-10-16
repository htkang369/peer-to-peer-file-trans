package com.cnt5106c.messagetype;

import com.cnt5106c.message.ActualMessage;

public class Interested extends ActualMessage{
	private final Byte message_type = 2;
	private final char[] message_payload = null;//interested have no payload

	public Interested(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
