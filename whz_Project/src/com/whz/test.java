package com.whz;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.*;
import com.whz.util.MyUtil;
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Random;
//
//import com.whz.util.MyUtil;

public class test {
	
	public static int NumberOfPreferredNeighbors;
	public static int UnchokingInterval;
	public static int OptimisticUnchokingInterval;
	public static String FileName;
	public static int FileSize;
	public static int PieceSize;

	public static void readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            tempString = reader.readLine();
            String[] buff = tempString.split(" ");
            NumberOfPreferredNeighbors = Integer.parseInt(buff[1]);
            System.out.println(buff[1]);
        
            tempString = reader.readLine();
            buff = tempString.split(" ");
            UnchokingInterval = Integer.parseInt(buff[1]);
            System.out.println(buff[1]);
            
            tempString = reader.readLine();
            buff = tempString.split(" ");
            OptimisticUnchokingInterval = Integer.parseInt(buff[1]);
            System.out.println(buff[1]);
            
            tempString = reader.readLine();
            buff = tempString.split(" ");
            FileName = buff[1];
            System.out.println(buff[1]);
            
            tempString = reader.readLine();
            buff = tempString.split(" ");
            FileSize = Integer.parseInt(buff[1]);
            System.out.println(buff[1]);
            
            tempString = reader.readLine();
            buff = tempString.split(" ");
            PieceSize = Integer.parseInt(buff[1]);
            System.out.println(buff[1]);            
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
    }
	
	public static void main(String[] args) throws IOException{
		
		// TODO Auto-generated method stub
//		System.out.println(1/8);
//		int msgLength = 20000;
//		System.out.println(Math.ceil(((double)1/(double)8)));
//		HashMap<Integer, Object> o = new HashMap<>();
//		o.put(1,null);
//		byte[] rawMsg = new byte[msgLength];
//		byte[] rawMsg2 = new byte[msgLength];
//		byte[] rawMsg3 = new byte[msgLength];
//		byte[] rawMsg4 = new byte[msgLength];
//		System.out.println("heelo");
//		for(int i =0;i<100000;i++) {
//			rawMsg4 = new byte[msgLength];
//		}
//		byte a = (byte) ~(0xff);
//		System.out.println(String.format("%02X", a));
//		System.out.println(String.format("%02X", ~(0xff)));
//		
//		Random random = new Random();
//		for(int i =0;i<100000;i++) {
//			int index = random.nextInt(1);
//			System.out.println(index);
//		}
		
		String filename = "C:\\Users\\pande\\Desktop\\whz\\whz_Project\\src\\com\\whz\\common.cfg";
		readFileByLines(filename);
		MyUtil.time();
		System.out.println("test");
		
	}

	
}
