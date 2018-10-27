package com.whz.msgtype;

import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class NotInterested extends ActualMsg{

	public NotInterested(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
	}
	
	/**
	 * do not have pay load
	 * @param message_length
	 */
	public NotInterested(byte[] message_length) {
		msg_type = (byte)NOTINTERESTED;
	}

	public NotInterested(int message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)NOTINTERESTED;
	}
}
