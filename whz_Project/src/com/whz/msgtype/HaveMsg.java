package com.whz.msgtype;

import com.whz.msg.ActualMsg;
import com.whz.util.MyUtil;

@SuppressWarnings("serial")
public class HaveMsg extends ActualMsg{

	/**
	 * ‘have’messages have a payload that contains a 4-byte piece index field. 
	 * 
	 * @param message_length
	 * @param message_type
	 * @param message_payload
	 */
	public HaveMsg(byte[] message_length, byte message_type, byte[] message_payload) {
		super(message_length, message_type, message_payload);
	}
	
	public HaveMsg(byte[] message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)HAVE;
	}

	public HaveMsg(int message_length, byte[] message_payload) {
		super(message_length, message_payload);
		msg_type = (byte)HAVE;
	}
	
	public HaveMsg(int pieceIndex) {
		msg_payload = MyUtil.intToByteArray(pieceIndex);
		msg_length = MyUtil.intToByteArray(5);
		msg_type = (byte)HAVE;
	}
	
	public HaveMsg() {
		super();
		msg_type = (byte)HAVE;
	}
}
