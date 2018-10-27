package com.whz.msgtype;

import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class Bitfield extends ActualMsg{
	
	/**
	 *  
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Bitfield(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
		
	}
	
	public Bitfield(byte[] message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)BITFIELD;
	}
	
	public Bitfield(int message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)BITFIELD;
	}
}
