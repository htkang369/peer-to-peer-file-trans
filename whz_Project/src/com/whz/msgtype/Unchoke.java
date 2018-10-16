package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class Unchoke extends ActualMsg{
	private static byte msg_type = 1;
	private static byte [] message_payload = null;//Unchoke have no payload

	public Unchoke(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
