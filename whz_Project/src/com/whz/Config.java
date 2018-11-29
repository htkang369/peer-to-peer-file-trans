package com.whz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class Config {
	public static int myID = 1002;
	public static final int sPort = 8000;
	public static HashMap<Integer,String> peerIpAddress;
	public static HashMap<Integer,Integer> peer_port;
	public static HashMap<Integer,Integer> peer_has;
	public static int FileSize;
	public static int PieceSize;
	public static int pieceNum;
	public static int bitFieldLength;
	public static boolean has;
	public static int myPort = 6008;
//	public static int bitFieldLength = (int) Math.ceil(pieceNum/8);
	public static int k;
	public static int optimistic_unchoking_interval;
	public static int unchoking_interval;//second
	public static String receiveFileName = "receiveFile.txt";
	public static String fileName = "bmp.bmp";
	
	public static String common_filename = "Common.cfg";
	public static String peer_filename = "PeerInfo.cfg";
	

	//public static String fileName = "Gone_with_the_wind.txt";
	
	public static void init_variables() throws IOException {
		
		File directory = new File(common_filename); 	
		readFileByLines(directory.getCanonicalPath());
		pieceNum = (int) Math.ceil(((double)FileSize/(double)PieceSize));
		bitFieldLength = (int) Math.ceil(((double)pieceNum/(double)8));
	}
	
	public static void initiatePeerConfig() {
		peer_has = new HashMap<>();
		peer_port = new HashMap<>();
		peerIpAddress = new HashMap<>();	
		 try {
	            BufferedReader reader = new BufferedReader(new FileReader(peer_filename));
	            String line = reader.readLine();
	            while (line != null) {
	                String[] content = line.trim().split(" ");
	                if(myID < Integer.parseInt(content[0])) {
		                peerIpAddress.put(Integer.parseInt(content[0]), content[1]);	
		                peer_port.put(Integer.parseInt(content[0]), Integer.parseInt(content[2]));
		                peer_has.put(Integer.parseInt(content[0]), Integer.parseInt(content[3]));
	                }else {
	                	if(Integer.parseInt(content[3]) == 1) {
	                		has = true;
	           
	                	}
	                	myPort = Integer.parseInt(content[2]);
	                }
	                line = reader.readLine();
	            }
	            reader.close();
	        } catch (FileNotFoundException fne) {
	            System.out.println("Cannot find this file, please use CfgGenerator first");
	        } catch (IOException ie) {
	            System.out.println("Cannot read this file, please check the file");
	        }
		
//		peerIpAddress.put(1001, "10.3.89.216");
//		peerIpAddress.put(1002, "10.136.102.83");
//		peerIpAddress.put(1003, "10.136.166.196");
//		peerIpAddress.put(1004, "10.3.89.212");
//		peerIpAddress.put(1005, "10.3.89.217");

	}
	
	
	public static void readFileByLines(String fileName2) {
        File file = new File(fileName2);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            tempString = reader.readLine();
            String[] buff = tempString.split(" ");
            k = Integer.parseInt(buff[1]);
            System.out.println(buff[1]);
        
            tempString = reader.readLine();
            buff = tempString.split(" ");
            unchoking_interval = Integer.parseInt(buff[1]);
            System.out.println(buff[1]);
            
            tempString = reader.readLine();
            buff = tempString.split(" ");
            optimistic_unchoking_interval = Integer.parseInt(buff[1]);
            System.out.println(buff[1]);
            
            tempString = reader.readLine();
            buff = tempString.split(" ");
            fileName = buff[1];
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
}
