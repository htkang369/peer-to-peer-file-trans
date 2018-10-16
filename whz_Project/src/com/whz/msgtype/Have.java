package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class Have extends ActualMsg{
	private final Byte msg_type = 4;

	/**
	 * ‘have’messages have a payload that contains a 4-byte piece index field. 
	 * 
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Have(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
