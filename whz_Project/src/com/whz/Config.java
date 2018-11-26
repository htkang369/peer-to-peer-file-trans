package com.whz;

import java.util.HashMap;


public class Config {
	public static int myID = 1001;
	public static final int sPort = 8000;
	public static HashMap<Integer,String> peerIpAddress;
	public static int FileSize = 7000000;
	public static int PieceSize = 100;
	public static int pieceNum = (int) Math.ceil(((double)FileSize/(double)PieceSize));
	public static int bitFieldLength = (int) Math.ceil(((double)pieceNum/(double)8));
//	public static int bitFieldLength = (int) Math.ceil(pieceNum/8);
	public static int k = 1;
	public static int optimistic_unchoking_interval = 2000;
	public static int unchoking_interval = 1000;//second
	
	public static void initiatePeerConfig() {
		peerIpAddress = new HashMap<>();

//		peerIpAddress.put(1001, "10.193.38.76");

	}
}
