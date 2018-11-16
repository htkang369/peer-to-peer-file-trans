package com.whz.msgtype;

import com.whz.msg.ActualMsg;
import com.whz.util.MyUtil;

@SuppressWarnings("serial")
public class Piece extends ActualMsg{
	
	private byte[] piece_index;
	private byte[] picee_content;
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
	
	public Piece(int message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type  = (byte)PIECE;
	}
	
	public Piece(byte[] message_length , byte message_type, byte[] message_payload) {
		super(message_length, message_payload);
	}
	
	public Piece(int message_length, byte[] piece_index , byte[] picee_content) {
		this.piece_index = piece_index;
		this.picee_content = picee_content;
		msg_length = MyUtil.intToByteArray(message_length);
		msg_type  = (byte)PIECE;
		msg_payload = new byte[message_length - 1];
		System.arraycopy(piece_index, 0, msg_payload, 0, 4);
		System.arraycopy(picee_content, 0, msg_payload, 4, message_length-5);
	}
	
	
	public Piece() {
		super();
		msg_type  = (byte)PIECE;
	}

}
