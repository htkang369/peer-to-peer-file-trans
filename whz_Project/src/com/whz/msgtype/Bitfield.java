package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class Bitfield extends ActualMsg{
	private static byte msg_type = (byte)5;
	
	/**
	 *  
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Bitfield(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
