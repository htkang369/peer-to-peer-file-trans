package com.cnt5106c.messagetype;

import com.cnt5106c.message.ActualMessage;

public class NotInterested extends ActualMessage{
	private final Byte message_type = 3;
	private final char[] message_payload = null;//notInterested have no payload

	public NotInterested(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
