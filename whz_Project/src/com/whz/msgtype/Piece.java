package com.whz.msgtype;

import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class Piece extends ActualMsg{
	/**
	 * ‘piece’messages  have  a  payload  which  consists  of  a  4-byte  piece  
	 * index  field  and  the content of the piece.
	 * 
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Piece(byte[] message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type  = (byte)PIECE;
	}
	
	public Piece(byte[] message_length , byte message_type, byte[] message_payload) {
		super(message_length, message_payload);
	}
	
	public Piece() {
		super();
		msg_type  = (byte)PIECE;
	}

}
