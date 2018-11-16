package com.whz;

import java.io.DataOutputStream;

public class LinkState {
	int peerID;
	DataOutputStream out; 
	int throughput;
	int linkTime;
	float speed;
	
	public LinkState(int peerID, DataOutputStream out) {
		this.peerID = peerID;
		this.out = out;
	}
}
