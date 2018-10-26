package com.whz.msg;

import java.io.Serializable;

import com.whz.util.MyUtil;

@SuppressWarnings("serial")
public abstract class ActualMsg implements Serializable{
	protected byte[] msg_length = new byte[4];
	protected byte msg_type;
	protected byte[] msg_payload;
	
	public ActualMsg(byte[] message_length, byte message_type, byte[] message_payload) {
		this.msg_length = message_length;
		this.msg_type = message_type;
		this.msg_payload = message_payload;
	}
	
	public ActualMsg(byte[] message_length, byte[] message_payload) {
		this.msg_length = message_length;
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
	
	public static int parseMsgContent(byte[] msg, byte[] contentLength, ActualMsg actual) {
		int length = MyUtil.byteArrayToInt(contentLength);
		byte temptype;
		byte[] tempMsgPayLoad = new byte[length-1];

		temptype = msg[0];
		for(int i=0;i<length-1;i++) {
			tempMsgPayLoad[i] = msg[i+1];
		}
		
		actual.setMsgLength(contentLength);
		actual.setMsgType(temptype);
		actual.setPayLoad(tempMsgPayLoad);
		return length;
	}
	
	public static byte[] toDataGram(ActualMsg actual) {
		int length;
		byte[] tempMsgLength = actual.getMsgLength();
		byte temptype = actual.getMsgType();
		byte[] tempMsgPayLoad = actual.getPayLoad();
		length = 1 + MyUtil.byteArrayToInt(tempMsgLength);
		byte[] lengthcontent = MyUtil.intToByteArray(length);
		
		byte[] dataGram = new byte[4 + length];
		for(int i=0;i<4;i++) {
			dataGram[i] = lengthcontent[i];
		}
		dataGram[4] = temptype;
		for(int i=5;i<length + 4;i++) {
			dataGram[i] = tempMsgPayLoad[i-5];
		}
		return dataGram;
	}
	
}
