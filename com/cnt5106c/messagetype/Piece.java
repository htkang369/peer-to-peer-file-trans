package com.cnt5106c.messagetype;

import com.cnt5106c.message.ActualMessage;

public class Piece extends ActualMessage{
	private final Byte message_type = 7;

	/**
	 * ‘piece’messages  have  a  payload  which  consists  of  a  4-byte  piece  
	 * index  field  and  the content of the piece.
	 * 
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Piece(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
