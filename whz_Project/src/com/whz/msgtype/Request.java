package com.whz.msgtype;

import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class Request extends ActualMsg{

	/**
	 * ‘request’messages  have  a  payload  which  consists  of  a  4-byte  piece  index  field.
	 *   Note that ‘request’message  payload  defined  here  is  different  from  that  of  
	 *   BitTorrent.  We don’t divide a piece into smaller subpieces
	 *   
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Request(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
	}

	public Request(byte[] message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type  = (byte)REQUEST;
	}
	
	public Request(int message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)REQUEST;
	}
	
	public Request() {
		super();
		msg_type = (byte)REQUEST;
	}
}
