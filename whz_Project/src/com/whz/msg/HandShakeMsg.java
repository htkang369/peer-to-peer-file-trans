package com.whz.msg;

import java.io.Serializable;

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
        this.zeroBits = zeroBits;
        this.peerID = new byte[4];
        this.peerID = peerID;
    }
	
	public int getPeerID() {
		/** 
		 * get peerID as int type
		 */
		String peerID = new String(this.peerID);
		return Integer.valueOf(peerID).intValue();
	}
	
	public String getHandShakeHeader(){
        /***
         * get header field of handshakeMsg as string type
         */
        String handShakeHeader = new String(this.handShakeHeader);
        return handShakeHeader;
    }
	
}
