package com.whz.msgtype;

import com.whz.BitField;
import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class BitfieldMsg extends ActualMsg{
	
	/**
	 *  
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public BitfieldMsg(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
		
	}
	
	public BitfieldMsg(byte[] message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)BITFIELD;
	}
	
	public BitfieldMsg(int message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)BITFIELD;
	}
	
	public BitfieldMsg(int message_length, BitField bitfield) {
		super(message_length, bitfield.bitfield);
		msg_type = (byte)BITFIELD;
	}
	
	public BitfieldMsg() {
		super();
		msg_type = (byte)BITFIELD;
	}
}
