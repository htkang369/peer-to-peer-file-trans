package com.whz.msg;

public class HandShakeMsg {
	private final String handshak_header = "P2PFILESHARINGPROJ";
	private final int zero_bits = 0;
	private int peer_ID;
	
	public HandShakeMsg(int peer_ID) {
		this.peer_ID = peer_ID;
	}
	
	public int getPeerID() {
		return peer_ID;
	}
}
