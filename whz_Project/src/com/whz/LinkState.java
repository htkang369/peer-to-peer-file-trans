package com.whz;

import java.io.DataOutputStream;

import com.whz.BitTorrentServer.Handler;

public class LinkState {
	int peerID_b;
	int peerID_a;
	DataOutputStream out; 
	int downloadThroughput;
	int upLoadThroughput;
	long startTime;
	float speed;
	Handler connection;
	
	public LinkState(int peerID_a, int peerID_b, DataOutputStream out ) {
		this.peerID_a = peerID_a;
		this.peerID_b = peerID_b;
		this.out = out;
	}
}
