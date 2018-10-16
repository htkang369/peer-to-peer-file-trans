package com.cnt5106c.message;

public class HandShakeMessage {
	private final String handshak_header = "P2PFILESHARINGPROJ";
	private final int zero_bits = 0;
	private int peer_ID;
	
	public HandShakeMessage(int peer_ID) {
		this.peer_ID = peer_ID;
	}
	
	public int getPeerID() {
		return peer_ID;
	}
}
