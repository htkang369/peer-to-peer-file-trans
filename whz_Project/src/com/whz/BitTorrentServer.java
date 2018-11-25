package com.whz;

import java.net.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

import com.whz.msg.ActualMsg;
import com.whz.msg.HandShakeMsg;
import com.whz.msgtype.BitfieldMsg;
import com.whz.msgtype.ChokeMsg;
import com.whz.msgtype.HaveMsg;
import com.whz.msgtype.InterestedMsg;
import com.whz.msgtype.NotInterestedMsg;
import com.whz.msgtype.PieceMsg;
import com.whz.msgtype.RequestMsg;
import com.whz.msgtype.UnchokeMsg;
import com.whz.util.MyUtil;


public class BitTorrentServer {

	private static final int sPort = 8000;   //The server will be listening on this port number
	private HashMap<Integer, LinkState> chokedMap = new HashMap<>();
    private static HashMap<Integer, LinkState> unChokedMap = new HashMap<>();
    private LinkState optimisticNeighbor;
    Random rand = new Random();

	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
        ServerSocket listener = new ServerSocket(sPort);
        BitTorrentServer server = new BitTorrentServer();
		int clientNum = 1;
        Timer timer = new Timer();
        Timer timer2 = new Timer();
		timer.schedule(new TimerTask() {
			public void run(){
				System.out.println("----set timer-----");
				server.computeDownloadRate();
				server.selectPreferredNeighbors();
				//sendUnchokeMsg();
			}
		}, MyUtil.unchoking_interval, MyUtil.unchoking_interval);
		timer2.schedule(new TimerTask() {
			public void run(){
				System.out.println("----set Optimistic timer-----");
				server.selectOptimisticallyUnchokedNeigbor();
				try {
					server.sendUnchokeMsg(server.optimisticNeighbor);
				} catch (IOException e) {
					e.printStackTrace();
					
				}
			}
		}, MyUtil.optimistic_unchoking_interval, MyUtil.optimistic_unchoking_interval);
        	try {
            	while(true) {
            		 new Handler(listener.accept(),clientNum).start();
                	
            		System.out.println("Client "  + clientNum + " is connected!");
                	clientNum++;
            	}
        	} finally {
            	listener.close();
    			timer2.cancel();
    			timer.cancel();
        	} 
    	}
	
	void selectPreferredNeighbors() {
		System.out.println("select preferredNeighbors");
		Iterator<Integer> iter = unChokedMap.keySet().iterator();//unchoke
		while(iter.hasNext()) {
			int id = iter.next();
			LinkState s = unChokedMap.get(id);
		}
		Iterator<Integer> iter2 = chokedMap.keySet().iterator();//choke
		while(iter2.hasNext()) {
			int id = iter2.next();
			LinkState s = chokedMap.get(id);
		}
	}
	
	void computeDownloadRate() {
		Iterator<Integer> iter = unChokedMap.keySet().iterator();
		while(iter.hasNext()) {
			int id = iter.next();
			LinkState s = unChokedMap.get(id);
			Calendar calendar = Calendar.getInstance();
			long timeInMillis = calendar.getTimeInMillis();
			s.speed = (s.downloadThroughput + s.upLoadThroughput) / ( timeInMillis - s.startTime);
		}
		System.out.println("update speed");
	}
	
	void selectOptimisticallyUnchokedNeigbor() {
		int size = chokedMap.size();
		int n = 0;
		int temp = 0;
		if(size > 0) {
			int index = rand.nextInt(size);
			Iterator<Integer> iter = chokedMap.keySet().iterator();
			while(iter.hasNext()&& n<index) {
				temp = iter.next();
				n++;
			}
			System.out.println("update optimistic neighbor");
		}
		optimisticNeighbor = chokedMap.get(temp);
	}
	
	void sendUnchokeMsg(LinkState optimisticNeighbor) throws IOException {
		Iterator<Integer> iter = unChokedMap.keySet().iterator();
		while(iter.hasNext()) {
			int id = iter.next();
			LinkState s = unChokedMap.get(id);
			s.connection.sendUnchokeMsg(optimisticNeighbor);
		}
	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
    */
    public static class Handler extends Thread {
        private String message;    //message received from the client
        private String MESSAGE;    //uppercase message send to the client
        private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket
        private int no;		//The index number of the client
        private byte [] bitfield;
        private byte [] peerBitfield;
        private int bitfieldLength = (int) Math.ceil( MyUtil.pieceNum/8);
    	Random rand = new Random();
        
    	private HashMap<Integer,DataInputStream> interestedMap = new HashMap<>();
        
 

        public Handler(Socket connection, int no) {
            this.connection = connection;
	    	this.no = no;
        }

        public void run() {
 		try{
			//initialize Input and Output streams
 			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			out = new DataOutputStream(connection.getOutputStream());
			out.flush();
			in = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
			sendHandshakeMessage();
			initBitfield();
			sendBitfield();
			receiveBitfield();

			while(true)
			{	
					//receive the message sent from the client
					//piece = (Piece)in.readObject();
					//byte[] b = (byte[]) in.readObject();
					ActualMsg rcvMsg = readActualMessage();
					replyMsg(rcvMsg);
					//Capitalize all letters in the message
					//MESSAGE = bufferedReader.readLine();
					//MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					//sendMessage(MESSAGE);		
			}
		}
		catch(IOException ioException){
			System.out.println("Disconnect with Client " + no);
		}
		finally{
			//Close connections
			try{
				System.out.println("close with Client " + no);
				in.close();
				out.close();
				connection.close();
			}
			catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
		}
	}
	    
		void sendMessage(byte[] msg) throws IOException
		{
				//stream write the message
				out.write(msg);
				out.flush();
		}
		
		/**
		 * A sends a bitfield message to let B know which file pieces it has
		 * @throws IOException 
		 */
		void sendBitfield() throws IOException {
			BitfieldMsg bitfieldMsg = new BitfieldMsg(bitfieldLength + 1,bitfield);
			byte[] datagram = BitfieldMsg.toDataGram(bitfieldMsg);
			sendMessage(datagram);
		}
		
		/**
		 * B will also send its bitfield message to A, unless it has no pieces
		 * @throws IOException 
		 */
		void receiveBitfield() throws IOException {
			BitfieldMsg bitfieldMsg = (BitfieldMsg) readActualMessage();
			peerBitfield = bitfieldMsg.getPayLoad();
			int payloadLength = MyUtil.byteArrayToInt(bitfieldMsg.getMsgLength());
			System.out.println("parse Bitfield Message");
			for(int i = 0; i< payloadLength-4; i++) {
				System.out.print(peerBitfield[i]);
			}
			System.out.println();
			System.out.println("Bitfield payloadLeng = " + payloadLength);
		}
		
		void initBitfield() {
			bitfield = new byte[bitfieldLength];
			for(int i=0;i<bitfieldLength;i++) {
				bitfield[i] = (byte) 0xFF;
			}
		}
		
	    
		void sendHandshakeMessage() throws IOException {
			
			HandShakeMsg handshakeMsg = new HandShakeMsg(1002);
			sendMessage(HandShakeMsg.toDataGram(handshakeMsg));
			byte[] rawMsg = new byte[32];
			try {
				in.read(rawMsg, 0, 32);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			HandShakeMsg rcvhandshakeMsg = HandShakeMsg.parseHeaderMsg(rawMsg);
			if(!HandShakeMsg.checkPeerID(1001, rcvhandshakeMsg)) {
				System.out.println("error peerID:" + rcvhandshakeMsg.getPeerID());
			}else {
				System.out.println("peerID = " + rcvhandshakeMsg.getPeerID());
			}
		}
		
		ActualMsg readActualMessage() throws IOException {
			byte[] length = new byte[4];
			in.read(length);

			int msgLength = ActualMsg.parseLength(length);
			if(msgLength > 0) {
				byte[] rawMsg = new byte[msgLength];
				try {
					in.read(rawMsg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				//should parse type
				int msgType = ActualMsg.parseMsgType(rawMsg);
				ActualMsg rcvMsg = null;
				switch(msgType) {
					case ActualMsg.CHOKE:
						rcvMsg = new ChokeMsg();
						break;
					case ActualMsg.UNCHOKE:
						rcvMsg = new UnchokeMsg();
						break;
					case ActualMsg.INTERESTED:
						rcvMsg = new InterestedMsg();
						break;
					case ActualMsg.NOTINTERESTED:
						rcvMsg = new NotInterestedMsg();
						break;
					case ActualMsg.HAVE:
						rcvMsg = new HaveMsg();
						break;
					case ActualMsg.BITFIELD:
						System.out.println("receive Bitfield Message");
						rcvMsg = new BitfieldMsg();
						break;
					case ActualMsg.REQUEST:
						rcvMsg = new RequestMsg();
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						System.out.println("receive Request Message");
						break;
					case ActualMsg.PIECE:
						System.out.println("receive Piece Message");
						break;
				}
				if(rcvMsg == null) {
					System.out.println("parse Type error");
				}
				ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
				return rcvMsg;
			}else {
				System.out.println("not our message");
				return null;
			}
		}
		
		void replyMsg(ActualMsg rcvMsg) throws IOException {
			if(rcvMsg != null) {
				int msgType = rcvMsg.getMsgType();
				switch(msgType) {
				case ActualMsg.CHOKE:
					break;
				case ActualMsg.UNCHOKE:
					break;
				case ActualMsg.INTERESTED:
					break;
				case ActualMsg.NOTINTERESTED:
					break;
				case ActualMsg.HAVE:
					break;
				case ActualMsg.BITFIELD:
					System.out.println("reply Bitfield Message");
					break;
				case ActualMsg.REQUEST:
					System.out.println("reply Request Message");
					sendPieceMsg(MyUtil.byteArrayToInt(rcvMsg.getPayLoad()));
					break;
				case ActualMsg.PIECE:
					System.out.println("reply Piece Message");
					break;
				}
			}
		}
		
		void sendPieceMsg(int pieceNum) throws IOException {
			System.out.println("send Piece Message num = " + pieceNum);
			byte[] payLoad = readFile(pieceNum);
			PieceMsg pieceMsg = new PieceMsg(MyUtil.PieceSize + 5, MyUtil.intToByteArray(pieceNum) , payLoad);
			byte[] c = ActualMsg.toDataGram(pieceMsg);
			sendMessage(c);
			unChokedMap.get(no).upLoadThroughput++;
		}

		
		byte[] readFile(int pieceNum) {
			InputStream inFile = null;
			byte[] tempbytes = new byte[MyUtil.PieceSize];
			try {
				
				int byteread = 0;
				inFile = new FileInputStream("test/testfile");
				showAvailableBytes(inFile);
			
				byte[] a = MyUtil.intToByteArray(MyUtil.PieceSize + 1);
				inFile.skip(pieceNum * MyUtil.PieceSize);
				if((byteread = inFile.read(tempbytes)) != -1) {
					System.out.write(tempbytes, 0, MyUtil.PieceSize);
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
		
		void sendUnchokeMsg(LinkState optimisticNeighbor) throws IOException {
			UnchokeMsg unchoke = new UnchokeMsg();
			byte[] c = ActualMsg.toDataGram(unchoke);
			sendMessage(c);
		}
		

		
		private static void showAvailableBytes(InputStream in) {
			try {
				System.out.println("number of bytes in file: " + in.available());
				double c = (double)in.available()/ MyUtil.PieceSize;
				System.out.println("c:"+ c);
				System.out.println("# of pieces:"+ Math.ceil(c));
			}catch(IOException e) {
				e.printStackTrace();
			}
		}
		
    }
    
    


}
