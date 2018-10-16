package com.whz.msg;

public abstract class ActualMsg {
	protected byte[] msg_length = new byte[4];
	protected byte msg_type;
	protected byte[] msg_payload;
	
	public ActualMsg(byte[] message_length, byte message_type, byte[] message_payload) {
		this.msg_length = message_length;
		this.msg_type = message_type;
		this.msg_payload = message_payload;
	}
	
}
