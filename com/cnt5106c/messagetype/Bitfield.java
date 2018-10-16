package com.cnt5106c.messagetype;

import com.cnt5106c.message.ActualMessage;

public class Bitfield extends ActualMessage{
	private final Byte message_type = 5;
	
	/**
	 * ‘bitfield’messages  is  only  sent  as  the  first  message  right  after  handshaking  is
	 *  done when a connection is established. ‘bitfield’messages have a bitfield as its payload.
	 *  Each bit  in  the  bitfield  payload  represents  whether  the  peer  has  the  
	 *  corresponding  piece  or not. The first byte of the bitfield corresponds to piece indices 
	 *  0 –7 from high bit to low bit,  respectively.  The  next  one  corresponds  to  piece  
	 *  indices  8 –15,  etc.  Spare  bits  at the end are set to zero. Peers thatdon’t have 
	 *  anything yet may skip a ‘bitfield’message. 
	 *  
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public Bitfield(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
