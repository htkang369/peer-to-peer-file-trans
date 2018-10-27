package com.whz.msgtype;

import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class Unchoke extends ActualMsg{

	public Unchoke(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
	}

	public Unchoke(byte[] message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)UNCHOKE;
	}
	
	public Unchoke(int message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)UNCHOKE;
	}
	
	public Unchoke() {
		super();
		msg_type = (byte)UNCHOKE;
	}
}
