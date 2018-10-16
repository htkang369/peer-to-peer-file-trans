package com.whz.msgtype;

import com.whz.msg.ActualMsg;

public class Request extends ActualMsg{
	private final Byte msg_type = 6;

	/**
	 * ‘request’messages  have  a  payload  which  consists  of  a  4-byte  piece  index  field.
	 *   Note that ‘request’message  payload  defined  here  is  different  from  that  of  
	 *   BitTorrent.  We don’t divide a piece into smaller subpieces
	 *   
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Request(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
