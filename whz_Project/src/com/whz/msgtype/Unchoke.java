package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class Unchoke extends ActualMsg{
	private final Byte msg_type = 1;
	private final char[] message_payload = null;//Unchoke have no payload

	public Unchoke(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
