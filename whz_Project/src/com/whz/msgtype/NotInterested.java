package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class NotInterested extends ActualMsg{
	private static byte msg_type = 3;
	private static byte [] message_payload = null;//notInterested have no payload

	public NotInterested(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
