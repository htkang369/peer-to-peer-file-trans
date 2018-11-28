package com.whz;

import java.util.HashMap;


public class Config {
	public static int myID = 1001;
	public static final int sPort = 8000;
	public static HashMap<Integer,String> peerIpAddress;
	public static int FileSize =  3825566 ;
	public static int PieceSize = 5000;
	public static int pieceNum = (int) Math.ceil(((double)FileSize/(double)PieceSize));
	public static int bitFieldLength = (int) Math.ceil(((double)pieceNum/(double)8));
//	public static int bitFieldLength = (int) Math.ceil(pieceNum/8);
	public static int k = 2;
	public static int optimistic_unchoking_interval = 2000;
	public static int unchoking_interval = 1000;//second
	public static String receiveFileName = "receiveFile.txt";
	public static String fileName = "bmp.bmp";
	//public static String fileName = "Gone_with_the_wind.txt";
	
	public static void initiatePeerConfig() {
		peerIpAddress = new HashMap<>();

//		peerIpAddress.put(1001, "10.3.89.216");
//		peerIpAddress.put(1002, "10.3.89.215");
//		peerIpAddress.put(1003, "10.136.166.196");
//		peerIpAddress.put(1004, "10.3.89.211");
//		peerIpAddress.put(1005, "10.3.89.214");

	}
}
