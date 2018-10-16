package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class Interested extends ActualMsg{
	private final Byte msg_type = 2;
	private final char[] message_payload = null;//interested have no payload

	public Interested(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
