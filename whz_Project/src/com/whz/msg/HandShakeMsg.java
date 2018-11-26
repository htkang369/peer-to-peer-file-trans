package com.whz.msg;

import java.io.Serializable;
import java.util.Arrays;

import com.whz.util.MyUtil;

@SuppressWarnings("serial")
public class HandShakeMsg implements Serializable {
	
	
	/** The 18 bytes handshake header. */
    private byte[] handShakeHeader = new byte[18];
    /** The 10 bytes zero bits. */
    private byte[] zeroBits = new byte[10];
    /** The 4bytes peer id. */
    private byte[] peerID = new byte [4];
    
    public HandShakeMsg(byte[] peerID){

        this.handShakeHeader = new byte[18];
        this.handShakeHeader = "P2PFILESHARINGPROJ".getBytes();
        this.zeroBits = new byte[10];
        this.peerID = new byte[4];
        this.peerID = peerID;
    }
    
    public HandShakeMsg(int peerID){

        this.handShakeHeader = new byte[18];
        this.handShakeHeader = "P2PFILESHARINGPROJ".getBytes();
        this.zeroBits = new byte[10];
        this.peerID = new byte[4];
        this.peerID = MyUtil.intToByteArray(peerID);
    }
    
    public HandShakeMsg(){

        this.handShakeHeader = new byte[18];
        this.handShakeHeader = "P2PFILESHARINGPROJ".getBytes();
        this.zeroBits = new byte[10];
    }
    
	/** 
	 * get peerID as int type
	 */	
	public int getPeerID() {
		return MyUtil.byteArrayToInt(peerID);
	}
	
	public void setPeerID(byte[] peerID) {
		this.peerID = peerID;
	}
	
    /***
     * get header field of handshakeMsg as string type
     */
	public String getHandShakeHeader(){
        String handShakeHeader = new String(this.handShakeHeader);
        return handShakeHeader;
    }
	
	/**
	 * parse encapsulated datagram
	 * 
	 * @param rawMsg
	 * @return
	 */
	public static HandShakeMsg parseHeaderMsg(byte[] rawMsg) {
		HandShakeMsg handShakeMsg = new HandShakeMsg();
		byte[] peerID = new byte[4];
		//parse peerID
		System.arraycopy(rawMsg, 28, peerID, 0, 4);
		handShakeMsg.setPeerID(peerID);
		return handShakeMsg;
	}
	
	/**
	 * encapsulate the handshakemsg messages to datagram
	 * 		 18-byte		10-byte   4-byte
	 * | handshake header| zero bits| peer ID|
	 * 
	 * @param handShakeMsg
	 * @return
	 */
	public static byte[] toDataGram(HandShakeMsg handShakeMsg) {
		byte[] dataGram = new byte[32];
		//Encapsulate header
		System.arraycopy(handShakeMsg.handShakeHeader, 0, dataGram, 0, 18);
		//Encapsulate zero bits
		System.arraycopy(handShakeMsg.zeroBits, 0, dataGram, 18, 10);
		//Encapsulate peerID
		System.arraycopy(handShakeMsg.peerID, 0, dataGram, 28, 4);
		return dataGram;
	}
	
	public static boolean checkHead(HandShakeMsg handShakeMsg) { // 这里还没有写，记得不要落下这里的东西。应该是在这里需要与那串字符串进行对比。
		return true;
	}
	
	public static boolean checkPeerID(int peerID, HandShakeMsg handShakeMsg) {
		return checkPeerID(MyUtil.intToByteArray(peerID), handShakeMsg);
	}
	
	public static boolean checkPeerID(byte[] peerID, HandShakeMsg handShakeMsg) {
		if(Arrays.equals(peerID, handShakeMsg.peerID)) {
			return true;
		}
		return false;
	}
}
