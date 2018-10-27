package com.whz.msgtype;

import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class Have extends ActualMsg{

	/**
	 * ‘have’messages have a payload that contains a 4-byte piece index field. 
	 * 
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Have(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
	}
	
	public Have(byte[] message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)4;
	}

}
