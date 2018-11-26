package com.whz.msg;

import java.io.Serializable;
import com.whz.util.MyUtil;

@SuppressWarnings("serial")
public abstract class ActualMsg implements Serializable{
	protected byte[] msg_length = new byte[4];
	protected byte msg_type;
	protected byte[] msg_payload;
	
	public static final int CHOKE = 0;
	public static final int UNCHOKE = 1;
	public static final int INTERESTED = 2;
	public static final int NOTINTERESTED = 3;
	public static final int HAVE = 4;
	public static final int BITFIELD = 5;
	public static final int REQUEST = 6;
	public static final int PIECE = 7;
	
	public static final int MSG_TYPE_LENGTH = 1;
	public static final int MSG_LENGTH_LENGTH = 4;
	
	
	public ActualMsg(byte[] message_length, byte message_type, byte[] message_payload) {
		this.msg_length = message_length;
		this.msg_type = message_type;
		this.msg_payload = message_payload;
	}
	
	public ActualMsg(byte[] message_length, byte[] message_payload) {
		this.msg_length = message_length;
		this.msg_payload = message_payload;
	}
	
	public ActualMsg(int message_length, byte[] message_payload) {
		this.msg_length = MyUtil.intToByteArray(message_length);
		this.msg_payload = message_payload;
	}
	
	public ActualMsg() {
	}
	
	public void setMsgLength(byte[] msg_length) {
		this.msg_length = msg_length;
	}
	
	public byte[] getMsgLength() {
		return msg_length;
	}
	
	public byte[] getPayLoad() {
		return msg_payload;
	}
	
	public void setPayLoad(byte[] message_payload) {
		this.msg_payload = message_payload;
	}
	
	public byte getMsgType() {
		return msg_type;
	}
	
	public void setMsgType(byte msg_type) {
		this.msg_type = msg_type;
	}
	
	public static int parseLength(byte[] msg) {
		int length;
		length = MyUtil.byteArrayToInt(msg);
		
		return length;
	}
	
	public static int parseMsg(byte[] msg, ActualMsg actual) {
		int length = msg.length;
		byte[] tempMsgLength = new byte[4];
		byte temptype;
		byte[] tempMsgPayLoad = new byte[length-5];
		
		for(int i=0;i<4;i++) {
			tempMsgLength[i] = msg[i];
		}
		temptype = msg[4];
		for(int i=0;i<length-5;i++) {
			tempMsgPayLoad[i] = msg[i];
		}
		
		actual.setMsgLength(tempMsgLength);
		actual.setMsgType(temptype);
		actual.setPayLoad(tempMsgPayLoad);
		return length;
	}
	
	public static int parseMsgContent(byte[] msg, byte[] messagetLength, ActualMsg actual) {
		int length = MyUtil.byteArrayToInt(messagetLength);
		byte temptype;
		byte[] tempMsgPayLoad = new byte[length-1];

		temptype = msg[0];
		for(int i=0;i<length-1;i++) {
			tempMsgPayLoad[i] = msg[i+1];
		}
		
		actual.setMsgLength(messagetLength);
		actual.setMsgType(temptype);
		actual.setPayLoad(tempMsgPayLoad);
		return length;
	}
	
	public static int parseMsgType(byte[] rawMsg) {
		byte temptype;
		if(rawMsg != null) {
			temptype = rawMsg[0];
			return (int)temptype;
		}
		return -1;
	}
	
	/**
	 * encapsulate the actual messages to datagram
	 * 
	 * 		4-byte			1-byte			n-byte
	 * | message length| message type| message payload|
	 * 
	 * @param actual
	 * @return
	 */
	public static byte[] toDataGram(ActualMsg actual) {
		int intMessageLength;
		byte[] tempMsgLength = actual.getMsgLength();
		byte temptype = actual.getMsgType();
		byte[] tempMsgPayLoad = actual.getPayLoad();
		intMessageLength = MyUtil.byteArrayToInt(tempMsgLength) - 1;
		
		byte[] dataGram = new byte[MSG_LENGTH_LENGTH + MSG_TYPE_LENGTH + intMessageLength];
		//Encapsulate messagelength
		for(int i=0;i<4;i++) {
			dataGram[i] = tempMsgLength[i];
		}
		//Encapsulate header
		dataGram[4] = temptype;
		if(tempMsgPayLoad != null) {
			for(int i = 5;i < intMessageLength + 5;i++) {
				dataGram[i] = tempMsgPayLoad[i-5];
			}
		}
		return dataGram;
	}
	
}
