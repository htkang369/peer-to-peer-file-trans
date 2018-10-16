package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class NotInterested extends ActualMsg{
	private final Byte msg_type = 3;
	private final char[] message_payload = null;//notInterested have no payload

	public NotInterested(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
