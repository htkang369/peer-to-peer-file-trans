package com.whz.util;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import com.whz.BitTorrentClient;
import com.whz.Config;

public class MyUtil {
	//byte array to int 
	public static int oldpieceNum = 70;
	public static int FileSize = 100000232;
	public static int PieceSize = 100;
	public static int k = 1;
	public static int optimistic_unchoking_interval = 2000;
	public static int unchoking_interval = 1000;//second
	public static PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(FileDescriptor.out)));
	
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
	
	public static byte[] readFile(int pieceNum) {
		InputStream inFile = null;
		File directory = new File("");//设定为当前文件夹 
//		try{ 
//		    System.out.println(directory.getCanonicalPath());//获取标准的路径 
//		    System.out.println(directory.getAbsolutePath());//获取绝对路径 
//		}catch(Exception e){} 
		
		byte[] tempbytes = new byte[Config.PieceSize];
		try {		
			inFile = new FileInputStream("Gone_with_the_wind.txt");
			showAvailableBytes(inFile);
			inFile.skip(pieceNum * Config.PieceSize);
			if(inFile.read(tempbytes) != -1) {
//				System.out.write(tempbytes, 0, Config.PieceSize);
				System.out.println();
				System.out.println("one piece!");
			}
		}catch(Exception e1) {
			e1.printStackTrace();
		}finally {
			if(inFile != null) {
				try {
					inFile.close();
				}catch(IOException e1) {
					
				}
			}
		}
		return tempbytes;
	}
	
	public static void showAvailableBytes(InputStream in) {
		try {
			System.out.println("number of bytes in file: " + in.available());
			double c = (double)in.available()/ Config.PieceSize;
			System.out.println("c:"+ c);
			System.out.println("# of pieces:"+ Math.ceil(c));
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initiateOut(){  
	     try {  
	         PrintStream print=new PrintStream("log.txt");
	         System.setOut(print);  
	     }catch (FileNotFoundException e) {  
	         e.printStackTrace();  
	     }  
	}
	
	public static synchronized void writeToFile(byte[] data, int count) {
		try {  
			System.out.println("write to file");  
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("receiveFile.txt"));  
			bos.write(data, 0, count);  
			bos.close();  
				System.out.println("write to receiveFile.txt!");  
		} catch (IOException ioe) {  
			System.out.println("writeToFile IOException!"); 
			System.out.println(ioe);  
		} catch (Exception e) {  
			System.out.println("writeToFile Exception!"); 
			System.out.println(e);  
		}  
	}
}
