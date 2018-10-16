package com.cnt5106c.messagetype;

import com.cnt5106c.message.ActualMessage;

public class Unchoke extends ActualMessage{
	private final Byte message_type = 1;
	private final char[] message_payload = null;//Unchoke have no payload

	public Unchoke(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
