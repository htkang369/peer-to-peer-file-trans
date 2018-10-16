package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class Interested extends ActualMsg{
	private static byte msg_type = (byte)2;
	private static byte [] message_payload = null;//interested have no payload

	public Interested(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
