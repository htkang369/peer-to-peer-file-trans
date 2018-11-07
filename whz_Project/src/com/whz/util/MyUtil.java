package com.whz.util;

public class MyUtil {
	//byte array to int 
	public static int pieceNum = 70;
	public static int FileSize = 100000232;
	public static int PieceSize = 100;
	public static int k = 1;
	public static int optimistic_unchoking_interval = 2000;
	public static int unchoking_interval = 1000;//second
	
	public static int byteArrayToInt(byte[] b) {
		return   b[3] & 0xFF |
		         (b[2] & 0xFF) << 8 |
		         (b[1] & 0xFF) << 16 |
		         (b[0] & 0xFF) << 24;
	}
	 
	public static byte[] intToByteArray(int a) {
		return new byte[] {
		        (byte) ((a >> 24) & 0xFF),
		        (byte) ((a >> 16) & 0xFF),   
		        (byte) ((a >> 8) & 0xFF),   
		        (byte) (a & 0xFF)
		};
	}
}
