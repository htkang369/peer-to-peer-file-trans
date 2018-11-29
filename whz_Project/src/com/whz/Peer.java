package com.whz;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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


public class Peer {
	static int myID = Config.myID;
	static HashMap<Integer, Handler> neighbor;
	static ArrayList<Handler> interestedList;
	static BitField localBitfield;
	static HashMap<Integer, BitField> peerBitfields;
	static HashMap<Integer, Handler> unChokedMap;
	static HashMap<Integer, Handler> chokedMap;
	static ArrayList<Handler> preferredList;
	static Handler optimisticNeighbor;
	static Timer timerP;
	static Timer timerM;
	static boolean fileComplete;
	static Random random = new Random();
	static int clientNum = 1;
	
	public static void main(String[] args) throws IOException {
		System.out.println("The server is running."); 
		peerBitfields = new HashMap<>();
		unChokedMap = new HashMap<>();
		chokedMap = new HashMap<>();
		preferredList = new ArrayList<>();
		neighbor = new HashMap<>();
		interestedList = new ArrayList<>();
		localBitfield = new BitField();
		MyUtil.initiateOut();
		Config.initiatePeerConfig();
		Config.init_variables();
		
		
		initBitfield();
		tryToConnect();	
		addTimerP();
		addTimerM();
		listenTcpConnection();	
	}
	
	static void initBitfield() {
		if(Config.myID == 1001) {
			MyUtil.pw.println("mID" + Config.myID);
			MyUtil.pw.flush();
			localBitfield.bitfield = new byte[Config.bitFieldLength];
			fileComplete = true;
			for(int i=0;i<Config.bitFieldLength;i++) {
				localBitfield.bitfield[i] = (byte) 0xFF;
			}
		}else {
			fileComplete = false;
			MyUtil.pw.println("mID" + Config.myID);
			MyUtil.pw.flush();
			localBitfield.bitfield = new byte[Config.bitFieldLength];
		}
	}
	
	public static void addTimerP() {
		timerP = new Timer();
		timerP.schedule(new TimerTask() {
			public void run(){
				System.out.println("----set preferred timer-----");
				selectPreferredNeighbors();
				//clearSpeed();
			}
		}, Config.unchoking_interval, Config.unchoking_interval);
	}
	
	public static void addTimerM() {
		timerM = new Timer();
		timerM.schedule(new TimerTask() {
			public void run(){
				System.out.println("----set Optimistic timer-----");
				Peer.selectOptimisticallyUnchokedNeigbor();
				Peer.sendOpUnchoke();
			}
		}, Config.optimistic_unchoking_interval, Config.optimistic_unchoking_interval);
	}
	
	public static void clearSpeed() {
		for(int i = 0; i < interestedList.size(); i++) {
			interestedList.get(i).clearSpeed();
		}
	}
	
	public static synchronized void selectPreferredNeighbors() {
		System.out.println("select preferredNeighbors interestedList.size = " + interestedList.size());
		for(int i = 0; i < interestedList.size(); i++) {
			interestedList.get(i).computeDownloadRate();
		}
		Collections.sort(interestedList);
		int size = Config.k;
		if(interestedList.size() < Config.k) {
			size = interestedList.size();
		}
		for(int i = 0; i < size; i++) {
			int unChokePeerID = interestedList.get(i).peerID;
			if(unChokedMap.get(unChokePeerID) == null) {
				if(optimisticNeighbor != null) {
					if(optimisticNeighbor.peerID != unChokePeerID) {
						unChokedMap.put(unChokePeerID, interestedList.get(i));
						System.out.println("unChokedMap add new" + unChokePeerID + " speed = " + unChokedMap.get(unChokePeerID).speed);
						sendUnchoke(unChokedMap.get(unChokePeerID));
						chokedMap.remove(unChokePeerID);
					}else {
						System.out.println(unChokePeerID + "op do not need send unchoke when it is select as preferred too");
					}
				}else {
					unChokedMap.put(unChokePeerID, interestedList.get(i));
					System.out.println("op is null, and unChokedMap add new" + unChokePeerID + " speed = " + unChokedMap.get(unChokePeerID).speed);
					sendUnchoke(unChokedMap.get(unChokePeerID));
					chokedMap.remove(unChokePeerID);
				}
			}else {
				System.out.println("unChokedMap already has " + unChokePeerID);
			}
		}
		System.out.println("select preferredNeighbors interestedList.size = " + interestedList.size() + " Config.k :" + Config.k);
		for(int i = Config.k; i < interestedList.size(); i++) {
			int chokePeerID = interestedList.get(i).peerID;
			if(chokedMap.get(chokePeerID) == null) {
				if(optimisticNeighbor != null) {
					if(chokePeerID != optimisticNeighbor.peerID) {
						chokedMap.put(chokePeerID, interestedList.get(i));
						System.out.println("chokedMap add new" + chokePeerID + "interestedList peerID" + interestedList.get(i).peerID);
						sendChoke(chokedMap.get(chokePeerID));
						unChokedMap.remove(chokePeerID);			
					}else {
						System.out.println(chokePeerID + " is optimisticNeighbor, do not need send choke");
					} 
				}else if(optimisticNeighbor == null){
					System.out.println("optimisticNeighbor is null choke Peer ID: " + chokePeerID + " interestedList peerID" + interestedList.get(i).peerID);
					chokedMap.put(chokePeerID, interestedList.get(i));
					System.out.println("chokedMap add new" + chokePeerID);
					sendChoke(chokedMap.get(chokePeerID));
					unChokedMap.remove(chokePeerID);
				}
			}else {
				System.out.println("chokedMap already has " + chokePeerID);
			}
		}
		MyUtil.time();	// 这里还需要向preferredList 里边添加 元素。
		System.out.println("Peer [" + myID + "] has the preferred neighbors [");
		for (int i=0;i<preferredList.size();i++) {
			System.out.print(preferredList.get(i).peerID);
			if (i!=preferredList.size()-1) {
				System.out.print(",");
			}
		}
		System.out.println("]");
	}
	
	public static synchronized void selectOptimisticallyUnchokedNeigbor() {
		int size = chokedMap.size();
		System.out.println("selectOptimisticallyUnchokedNeigbor chokedMap size = " + size);
		if(size > 0) {
			int index = random.nextInt(size);
			int count = 0;
			Iterator<Integer> iter = chokedMap.keySet().iterator();
			while(iter.hasNext()) {
				int temp = iter.next();
				count ++;
				if(count == size) {
					optimisticNeighbor = chokedMap.get(temp);
				}
			}
			if(optimisticNeighbor != null) {
				System.out.println("selectOptimisticallyUnchokedNeigbor " + optimisticNeighbor.peerID);
				MyUtil.time();
				System.out.println("Peer ["+ myID +"] has the optimistically unchoked neighbor ["+optimisticNeighbor.peerID+"]");
			}else {
				System.out.println("selectOptimisticallyUnchokedNeigbor = null index =" + index + " size = " + size);
			}
		}else {
			System.out.println("do not have choked neighbor");
		}
	}
	
	public static void sendUnchoke(Handler handler) {
		handler.sendUnchoke();
	}
	
	public static void sendChoke(Handler handler) {
		handler.sendChoke();
	}
	
	public static void sendOpUnchoke() {
		if(optimisticNeighbor!= null) {
			optimisticNeighbor.sendUnchoke();
		}
	}
	
	public static void tryToConnect() throws UnknownHostException, IOException {
		Iterator<Integer> iter = Config.peerIpAddress.keySet().iterator();
		Socket requestSocket = null;
		while(iter.hasNext()) {
			int temp = iter.next();
			try {
				requestSocket = new Socket(Config.peerIpAddress.get(temp), 8000);
				System.out.println("Connected to " + Config.peerIpAddress.get(temp) + " in port 8000 ,  this peer ID = " + myID);
				MyUtil.time();  // zhao log
				System.out.println("Peer ["+ myID+"] makes a connection to Peer [" + Config.peerIpAddress.get(temp) + "]");
				Handler handler = new Handler(requestSocket, clientNum, true, temp);
	       		handler.start();
	       		System.out.println("server "  + Config.peerIpAddress.get(temp) + " is connected!");
	           	clientNum++;
			}catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first." + Config.peerIpAddress.get(temp));
    			requestSocket.close();
			}
		}
		
	}
	
	public static void listenTcpConnection() throws IOException {
		ServerSocket listener = new ServerSocket(Config.sPort);
		try {
			while(true) {
	       		Handler handler = new Handler(listener.accept(),clientNum, false, 0);
	       		handler.start();
	       		System.out.println("Client "  + clientNum + " is connected!");
	           	clientNum++;
			}
		}finally {
			listener.close();
			//timerP.cancel();
			//timerM.cancel();
		}
	}
	
	public static class Handler extends Thread implements Comparable<Handler>{
		private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket
        private boolean isClient;
        private int peerID;
        private boolean isInterested = false;
        private BitField peerBitfield;
        private Random rand = new Random();
        private boolean isUnChoked = false;
        public List<Integer> interestedPieceList;
    	private int downloadThroughput;
    	private int upLoadThroughput;
    	private long startTime;
    	private float speed;
    	BitfieldMsg bitfieldMsg;
    	InterestedMsg interestedMsg;
    	NotInterestedMsg notInterested;
    	HandShakeMsg handshakeMsg;
    	UnchokeMsg unchoke;
    	ChokeMsg choke;
    	HaveMsg haveMsg;
    	RequestMsg request_message;

        public Handler(Socket connection, int no, boolean isClient, int peerID) {
            this.connection = connection;
	    	this.isClient = isClient;
	    	this.peerID = peerID;
	    	interestedPieceList = new ArrayList<>();
	    	downloadThroughput = 0;
	    	upLoadThroughput = 0;
        }
        
		public void run() {
			//initialize Input and Output streams
 			//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			try {
				out = new DataOutputStream(connection.getOutputStream());
				in = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
				out.flush();
				sendHandShake();
				receiveHandShake();
				sendBitfield();
				System.out.println("middle send receive" + " peerID: "+peerID);
				receiveBitfield();
				findOutInterestedPiece();
				sendInterestedOrNot();
				
				while(!fileComplete) {			
					ActualMsg rcvMsg = receiveActualMsg();
					replyMsg(rcvMsg);
					rcvMsg = null;
					System.gc();
				}
				System.out.println("-------------------------receive file completely-------------------------------" + " peerID: "+peerID);
				MyUtil.pw.println("receive file completely" + " peerID: "+peerID);
				MyUtil.pw.flush();
				while(true) {			
					ActualMsg rcvMsg = receiveActualMsg();
					replyMsg(rcvMsg);
					rcvMsg = null;
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				try {
					System.out.println("close with peerID: " + peerID);
					MyUtil.pw.println("close with peerID: " + peerID);
					in.close();
					out.close();
					connection.close();
					timerP.cancel();
					timerM.cancel();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}		
		}
		
		public void sendHandShake() {
			System.out.println("send HandShake Message" + " peerID: "+ myID);
			handshakeMsg = new HandShakeMsg(myID);
			sendMessage(HandShakeMsg.toDataGram(handshakeMsg));
		}
		
		/**
		 * A(Client) receives a handshake to B(Server),should have timer or not?
		 * 
		 * check whether the handshake header is right and the peer ID is the expected one
		 * @throws Exception 
		 */
		public synchronized void receiveHandShake() throws Exception {
			byte[] rawMsg = new byte[32];
			try {
				in.read(rawMsg);
				System.out.println("receive handshakeMessage" + " peerID: "+peerID);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("read error" + " peerID: "+peerID);
			}
			HandShakeMsg rcvhandshakeMsg = HandShakeMsg.parseHeaderMsg(rawMsg);
			//check head
			HandShakeMsg.checkHead(rcvhandshakeMsg);
			if(isClient) {
				if(!HandShakeMsg.checkPeerID(peerID, rcvhandshakeMsg)) {
					System.out.println("error peerID:" + rcvhandshakeMsg.getPeerID() + " right peerID:" + peerID);
					throw new Exception("error peerID:" + rcvhandshakeMsg.getPeerID() + " right peerID:" + peerID);
				}else {
					System.out.println("peerID = " + rcvhandshakeMsg.getPeerID());
					neighbor.put(peerID, this);// client add neighbor
				}
			}else {
				System.out.println("peerID = " + rcvhandshakeMsg.getPeerID());
				peerID = rcvhandshakeMsg.getPeerID();
				MyUtil.time();
				System.out.println("Peer ["+ myID + "] is connected from Peer [" + peerID + "]");
				neighbor.put(peerID, this);//server add neighbor
			}
			rawMsg = null;
		}
		
		public void sendBitfield() {
			System.out.println("send Bitfield Message bitFieldLength = " + Config.bitFieldLength + " peerID: "+peerID);
			bitfieldMsg = new BitfieldMsg(Config.bitFieldLength + 1 , localBitfield.bitfield);
			byte[] datagram = BitfieldMsg.toDataGram(bitfieldMsg);
			sendMessage(datagram);	
			datagram = null;
		}
		
		public synchronized void receiveBitfield() throws Exception {
			System.out.println("receive Bitfield Message from:" + peerID);
			BitfieldMsg bitfieldMsg = (BitfieldMsg) receiveActualMsg();
			BitField bitfield = new BitField();
			bitfield.bitfield = bitfieldMsg.getPayLoad();
			int msgLength = MyUtil.byteArrayToInt(bitfieldMsg.getMsgLength());
			System.out.println("parse Bitfield Message from:" + peerID + " msgLength = " + msgLength + " bitfield length =" + Config.bitFieldLength);
			MyUtil.pw.println("parse Bitfield Message from:" + peerID + " msgLength = " + msgLength + " bitfield length =" + Config.bitFieldLength);
			MyUtil.pw.flush();
			for(int i = 0; i< msgLength -1; i++) {
				if(peerID == 1003) {
					System.out.print(String.format("%02X", bitfield.bitfield[i]));
				}else {
					MyUtil.pw.print(String.format("%02X", bitfield.bitfield[i]));
					MyUtil.pw.flush();
				}
			}
			peerBitfield = bitfield;
			peerBitfields.put(peerID, peerBitfield);
			if(peerID == 1003) {
			System.out.println();
			System.out.println("Bitfield msgLength = " + msgLength + " peerID: "+peerID);
			}else {
				MyUtil.pw.println();
				MyUtil.pw.println("Bitfield msgLength = " + msgLength + " peerID: "+peerID);
				MyUtil.pw.flush();
			}
			bitfieldMsg = null;
		
		}
		
		/**
		 * A sends interested message to B.
		 * 
		 * For example, suppose that peer A makes a connection to peer B and receives
		 * a 'bitfield' message to peer B. In another example, suppose that peer A receives
		 * a 'have' message from peer C that contains the index of a piece not in peer A.
		 * Then peer A sends an 'interested' message to peer C.
		 * 
		 * parameters may be important
		 */
		public void sendInterestedOrNot() {
			if(isInterested) {
				System.out.println("send Interested Message" + " peerID: "+peerID);
				interestedMsg = new InterestedMsg();
				byte[] c = ActualMsg.toDataGram(interestedMsg);
				sendMessage(c);
				c = null;
			}else {
				System.out.println("send Not interested Message" + " peerID: "+peerID);
				notInterested = new NotInterestedMsg();
				byte[] c = ActualMsg.toDataGram(notInterested);
				sendMessage(c);
				c = null;
			}
		}
		
		public void sendNotInterestedOrNotSend() {
			if(!isInterested) {
				System.out.println("send a Not interested Message" + " peerID: "+peerID);
				notInterested = new NotInterestedMsg();
				byte[] c = ActualMsg.toDataGram(notInterested);
				sendMessage(c);
				c = null;
			}
		}
		
		public void sendInterestedOrNotSend() {
			if(isInterested) {
				System.out.println("send a Interested Message" + " peerID: "+peerID);
				interestedMsg = new InterestedMsg();
				byte[] c = ActualMsg.toDataGram(interestedMsg);
				sendMessage(c);
				c = null;
			}
		}
		
		public synchronized void receiveInterested(ActualMsg rcvMsg) {
			System.out.println("reply eInterested" + " peerID: "+peerID);
			if(!interestedList.contains(peerID)) {
				System.out.println("this is new Interested peer id = " + peerID);
				MyUtil.time();
				System.out.println("Peer ["+myID+"] received the 'interested' message from ["+peerID+"]");
				if(interestedList.contains(this)) {
					System.out.println("interestedList already have interested neighbor" + " peerID: "+peerID);
				}else {
					System.out.println("interestedList add interested neighbor" + " peerID: "+peerID);
					interestedList.add(this);
				}		
			}else {
				System.out.println("already receive interested from this peer " + peerID);
			}
		}
		
		public synchronized void receiveNotInterested() {
			System.out.println("receiveNotInterested" + " peerID: "+peerID);
			MyUtil.time();
			System.out.println("Peer ["+myID +"] received the 'not interested' message from ["+peerID+"]");
			interestedList.remove(this);
			unChokedMap.remove(this);
			chokedMap.remove(this);
		}
		
		public void sendUnchoke() {
			unchoke = new UnchokeMsg();
			System.out.println("send unchoke to " + " peerID: "+peerID);
			byte[] c = ActualMsg.toDataGram(unchoke);
			sendMessage(c);
			MyUtil.time();
			System.out.println("Peer [" + peerID + "is unchoked by ["+myID+"]");
			c = null;
		}
		
		public void receiveUnchoke() {
			System.out.println("receiveUnchoke" + " peerID: "+peerID);
			Calendar calendar = Calendar.getInstance();
			startTime = calendar.getTimeInMillis();
		}
		
		public void sendChoke() {
			choke = new ChokeMsg();
			System.out.println("send choke to " + " peerID: "+peerID);
			byte[] c = ActualMsg.toDataGram(choke);
			sendMessage(c);
			MyUtil.time();
			System.out.println("Peer [" + peerID + "is choked by ["+myID+"]");
			c = null;
		}
		
		public void receiveChoke() {
			System.out.println("receiveChoke from" + " peerID: "+peerID);
			isUnChoked = false;
		}
		
		public void sendPieceMsg(int pieceNum) {
			System.out.println("send Piece Message num: " + pieceNum + " peerID: "+peerID);
			byte[] payLoad = MyUtil.readFile(pieceNum);
			PieceMsg pieceMsg = new PieceMsg(Config.PieceSize + 5, MyUtil.intToByteArray(pieceNum) , payLoad);
			byte[] c = ActualMsg.toDataGram(pieceMsg);
			if(checkBitfield(pieceNum)) {
				sendMessage(c);
				MyUtil.writeSendContent(pieceMsg.getPayLoad(),20000, pieceNum, peerID);
				upLoadThroughput += Config.PieceSize;
				payLoad = null;
				c = null;
			}
		}
		
		public boolean checkBitfield(int pieceNum) {
			int index = pieceNum / 8;
			int offset = pieceNum % 8;
			int temp = 0x01 << (7 - offset);
			if((localBitfield.bitfield[index] & temp) == 0) {
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!requset a piece "+ pieceNum +" not have from "+peerID );
				return false;
			}
			return true;
		}
		
		public boolean checkBitfieldWhetherHave(int pieceNum) {
			int index = pieceNum / 8;
			int offset = pieceNum % 8;
			int temp = 0x01 << (7 - offset);
			if((localBitfield.bitfield[index] & temp) == 0) {
				return false;
			}
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!receive a piece "+ pieceNum +" already have from "+peerID);
			return true;
		}
		
		public void receivePiece() {
			System.out.println("receivePiece from" + " peerID: "+peerID);
		}
		
		public synchronized void sendHaveToAll(int pieceIndex) {
			System.out.println("send have to all" + " peerID: "+peerID);
			Iterator<Integer> iter = neighbor.keySet().iterator();
			while(iter.hasNext()) {
				int key = iter.next();
				neighbor.get(key).sendHave(pieceIndex);
			}
		}
		
		public void sendHave(int pieceIndex) {
			System.out.println("sendHave to"  + " peerID: "+peerID);
			MyUtil.time();
			System.out.println("Peer ["+peerID+"] received the 'have' message from ["+myID+"] for the piece ["+pieceIndex+"]");
			haveMsg = new HaveMsg(pieceIndex);
			byte[] c = ActualMsg.toDataGram(haveMsg);
			sendMessage(c);
			c = null;
		}
		
		public void receiveHave(ActualMsg rcvMsg) {
			System.out.println("replyHave" + " peerID: "+peerID);
			HaveMsg haveMsg = (HaveMsg) rcvMsg;
			changePeerBitField(MyUtil.byteArrayToInt(haveMsg.getPayLoad()));
		}

		public void sendRequest() {
			System.out.println("sendRequest" + " peerID: "+peerID);
		}
		
		public void receiveRequest() {
			System.out.println("receiveRequest" + " peerID: "+peerID);
		}
		
		/**
		 * if A receives a bitfield message form B, finds out whether B has pieces that it doesn't have
		 */
		synchronized void  findOutInterestedPiece() {
			//compare localBitfield with peerBitfield
			isInterested = false;
			interestedPieceList = new ArrayList<>();
			for(int i =0; i< Config.bitFieldLength; i++) {
//				System.out.println("localBitfield.bitfield:" + localBitfield.bitfield[i] + " peerID: "+peerID);
				byte not = (byte) ~ localBitfield.bitfield[i];
				byte temp = (byte) (peerBitfield.bitfield[i] & not);
				System.out.println(" i = "+ i + " Config.bitFieldLength  " + Config.bitFieldLength + " peerID: "+peerID);
				System.out.println(" not local bitfield value =  " + String.format("%02X", not) + " peerID: "+peerID);
				System.out.println(" peerBitfield bitfield value =  " + String.format("%02X", peerBitfield.bitfield[i]) + " peerID: "+peerID);
				System.out.println(" temp bitfield value =  " + String.format("%02X", temp) + " peerID: "+peerID);
				if(temp != 0) {
					isInterested = true;
					for(int j = 0; j < 8;j++) {
						int k = 1;
						k = (temp >> j) & k;
						if( k == 1) {
							int piecenum = i*8+(7 - j);
							interestedPieceList.add(piecenum);
							System.out.println("find out interested piece, pieceNum = " + piecenum + " peerID: "+peerID);
						}
					}
				}
			}
		}
		
		byte[] rawMsg;
		synchronized ActualMsg receiveActualMsg() throws Exception {
			byte[] length = new byte[4];
			in.read(length);
			

			int msgLength = ActualMsg.parseLength(length);
			if(msgLength > 0 && msgLength < 5000000) {
				System.out.println("msgLength = " + msgLength);
				rawMsg = new byte[msgLength];
				try {
					int count = 0;
					while(count != msgLength) {
//						byte[] oneByte = new byte[1];
//						count += in.read(oneByte);
//						System.out.println("readlly read = " + count);
//						System.arraycopy(oneByte, 0, rawMsg, count, 1);
						byte[] oneByte = new byte[msgLength - count];
						int readCount = in.read(oneByte);
						System.arraycopy(oneByte, 0, rawMsg, count, readCount);
						count += readCount;
						System.out.println("total read = " + count + " read " + readCount + "this time");		
					}
				} catch (IOException e) {
					e.printStackTrace();
				}			
				//should parse type
				int msgType = ActualMsg.parseMsgType(rawMsg);
				ActualMsg rcvMsg = null;
				switch(msgType) {
					case ActualMsg.CHOKE:
						rcvMsg = new ChokeMsg();
						System.out.println("receive ChokeMsg" + " peerID: "+peerID);
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						break;
					case ActualMsg.UNCHOKE:
						rcvMsg = new UnchokeMsg();
						System.out.println("receive UnchokeMsg" + " peerID: "+peerID);
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						break;
					case ActualMsg.INTERESTED:
						rcvMsg = new InterestedMsg();
						System.out.println("receive InterestedMsg" + " peerID: "+peerID);
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						break;
					case ActualMsg.NOTINTERESTED:
						rcvMsg = new NotInterestedMsg();
						System.out.println("receive NotInterestedMsge" + " peerID: "+peerID);
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						break;
					case ActualMsg.HAVE:
						rcvMsg = new HaveMsg();
						System.out.println("receive HaveMsg" + " peerID: "+peerID);
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						break;
					case ActualMsg.BITFIELD:
						System.out.println("receive Bitfield Message" + " peerID: "+peerID);
						rcvMsg = new BitfieldMsg();
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						break;
					case ActualMsg.REQUEST:
						rcvMsg = new RequestMsg();
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						System.out.println("receive Request Message msg Length = " + msgLength + " peerID: "+peerID);
						break;
					case ActualMsg.PIECE:
						System.out.println("receive Piece Message" + " peerID: "+peerID);
						rcvMsg = new PieceMsg();
						
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						byte[] pieceNum = new byte[4];
						byte[] payLoad = rcvMsg.getPayLoad();
						System.arraycopy(payLoad, 0, pieceNum, 0, 4);
						byte[] content = new byte[MyUtil.byteArrayToInt(length) - 5];
						System.arraycopy(payLoad, 4, content, 0, msgLength - 5);
						int piecenum = MyUtil.byteArrayToInt(pieceNum);
						if(!checkBitfieldWhetherHave(piecenum)) {
							System.out.println("receive Piece finished! : number = " + piecenum + " peerID: "+peerID);
							changeLocalBitField(piecenum);
							if(rcvMsg.getPayLoad() !=null) {
	//							System.out.write(content, 0, MyUtil.byteArrayToInt(length) - 5);
	//							System.out.println();
	//							System.out.flush();s
	//							byte[] content =  new byte[msgLength - 5];
	//							System.arraycopy(rcvMsg.getPayLoad(), 4, content, 0, msgLength - 5);
	//							MyUtil.writeToFile(content, msgLength - 5);
								MyUtil.writeToFile(content, MyUtil.byteArrayToInt(length) - 5, piecenum);
								
							}
						}else {
							System.out.println("receive Piece already have! : number = " + piecenum + " peerID: "+peerID);
						}
						MyUtil.time();
						System.out.println("Peer ["+myID+"] has downloaded the piece ["+pieceNum+"] from ["+peerID+"].");
						System.out.println("Now the number of pieces it has is ["+(Config.pieceNum-interestedPieceList.size())+"]");
						
						downloadThroughput += Config.PieceSize;
						sendHaveToAll(piecenum);
						findOutInterestedPiece();
						sendNotInterestedOrNotSend();
						if(interestedPieceList.size() == 0) {
							System.out.println("after receive piece interestedPieceList.size() == 0" + " peerID: "+peerID);
							MyUtil.time();
							System.out.println("Peer ["+myID+"] has downloaded the complete file.");
						}
						checkFileComplete();
						pieceNum = null;
						break;
				}
				if(rcvMsg == null) {
					System.out.println("parse Type error" + " peerID: "+peerID);
				}
				rawMsg = null;
				return rcvMsg;
			}else {
				System.out.println("not our message" + " peerID: "+peerID);
				rawMsg = null;
				throw new Exception();
			}
		}
		
		public boolean checkFileComplete() {
			for(int i =0; i < Config.bitFieldLength - 1; i++) {
				System.out.println("checkFileComplete  localBitfield.bitfield["+i+"]" + String.format("%02X", localBitfield.bitfield[i])+ " peerID: "+peerID);
				byte not = (byte) ~localBitfield.bitfield[i];
				System.out.println("checkFileComplete  not " + String.format("%02X", not)+ " peerID: "+peerID);
				byte temp = (byte) (not & 0xFF);
				System.out.println("checkFileComplete  temp " + String.format("%02X", temp)+ " peerID: "+peerID);
				if(temp != 0) {
					System.out.println("checkFileComplete  temp " + String.format("%02X", temp)+ "!=0 peerID: "+peerID);
				    fileComplete = false;
					return false;
				}
			}
			System.out.println("file compelete checkfileComplete" + " peerID: "+peerID);
			MyUtil.pw.println("file compelete checkfileComplete" + " peerID: "+peerID);
			MyUtil.pw.flush();
			fileComplete = true;;
			return true;
		}
		
		public synchronized void replyMsg(ActualMsg rcvMsg) {
			if(rcvMsg != null) {
				int msgType = rcvMsg.getMsgType();
				switch(msgType) {
				case ActualMsg.CHOKE:
					receiveChoke();
					break;
				case ActualMsg.UNCHOKE:
					System.out.println("reply UnChoke Message" + " peerID: "+peerID);
					isUnChoked = true;
					if(isUnChoked & !fileComplete) {
						sendRequestMsg();
					}
					break;
				case ActualMsg.INTERESTED:
					System.out.println("reply Interested Message" + " peerID: "+peerID);
					//LinkState state = new LinkState(serverPeerID, out);
					//chokedMap.put(serverPeerID,state);
					receiveInterested(rcvMsg);
					break;
				case ActualMsg.NOTINTERESTED:
					receiveNotInterested();
					break;
				case ActualMsg.HAVE:
					receiveHave(rcvMsg);
					break;
				case ActualMsg.BITFIELD:
					System.out.println("reply Bitfield Message" + " peerID: "+peerID);
					break;
				case ActualMsg.REQUEST:
					System.out.println("reply Request Message" + " peerID: "+peerID);
					sendPieceMsg(MyUtil.byteArrayToInt(rcvMsg.getPayLoad()));
					break;
				case ActualMsg.PIECE:
					System.out.println("reply Piece Message" + " peerID: "+peerID);
					if(isUnChoked & !fileComplete) {
						sendRequestMsg();
					}else {
						System.out.println("reply piece . But is choke or file Complete");
					}
					break;
				}
			}
		}
		
		public void sendRequestMsg() {
			
			request_message = new RequestMsg();
			int length_interest = interestedPieceList.size();
			if(length_interest > 0) {
				System.out.println("interestedPieceList size = " + length_interest + " peerID: "+peerID);
				int index = rand.nextInt(length_interest);//select rand row of interestedPieceList
				byte[] a = MyUtil.intToByteArray(interestedPieceList.get(index));
				request_message.setPayLoad(a);
				request_message.setMsgLength(MyUtil.intToByteArray(5));
				byte[] c = ActualMsg.toDataGram(request_message);
				System.out.println("send Request Message" + " peerID: "+peerID + " piecenum = " + MyUtil.byteArrayToInt(a));
				sendMessage(c);
				a = null;
				c = null;
			}else {
				System.out.println("interestedPieceList size = " + length_interest + " peerID: "+peerID);
				System.out.println("interestedPieceList size = 0 , do not need send ");
			}
		}
		
		public synchronized void changeLocalBitField(int piecenum) {
			int index = piecenum / 8;
			int offset = piecenum % 8;
			int temp = 0x01 << (7 - offset);
			System.out.println("change local bitfield piecenum =  " + piecenum + " originl:"+ String.format("%02X", localBitfield.bitfield[index]) + " index = " + index + " offset: " + offset);
			localBitfield.bitfield[index] = (byte) (localBitfield.bitfield[index] | temp);
			System.out.println("change local bitfield piecenum =  " + piecenum + " new:"+ String.format("%02X", localBitfield.bitfield[index]) + " index = " + index + " offset: " + offset);
			System.out.println(" local bitfield value =  " + String.format("%02X", localBitfield.bitfield[index]));
		}
		
		public synchronized void changePeerBitField(int piecenum) {
			int index = piecenum / 8;
			int offset = piecenum % 8;
			int temp = 0x01 << (7 - offset);
			byte not = (byte) ~localBitfield.bitfield[index];
			if((not & temp) != 0) {
				//localBitfield.bitfield[index]  = (byte) (localBitfield.bitfield[index] | temp);
				System.out.println("receive new interested have from " + peerID + " pieceNum = " + piecenum + " peerID: "+peerID);
				isInterested = true;
				if(!interestedPieceList.contains(piecenum)) {
					System.out.println("add new have interested to interested PieceList");
					interestedPieceList.add(piecenum);
				}else {
					System.out.println("already add new have interested to interested PieceList");
				}
				sendInterestedOrNotSend();
			}
			peerBitfield.bitfield[index] = (byte) (peerBitfield.bitfield[index] | temp);
			System.out.println("change peerBitfield piecenum =  " + piecenum + " index = " + index + " offset: " + offset);
			System.out.println("peerBitfield value =  " + String.format("%02X", peerBitfield.bitfield[index]));
		}
		
		public void sendMessage(byte[] msg)
		{
			try{
				//stream write the message
				out.write(msg);
				out.flush();
			}
			catch(IOException ioException){
				System.out.println("error send message" + " peerID: "+peerID);
				ioException.printStackTrace();
			}
		}
		
		void computeDownloadRate() {
			Calendar calendar = Calendar.getInstance();
			long timeInMillis = calendar.getTimeInMillis();
			speed = (downloadThroughput + upLoadThroughput) / ( timeInMillis - startTime);
			System.out.println("peerID: "+peerID + " update speed =" + speed + " downloadThroughput "+downloadThroughput+" upLoadThroughput"+upLoadThroughput+" startTime "+startTime+" now" +timeInMillis);
		}
		
		public void clearSpeed() {
			downloadThroughput = 0;
			upLoadThroughput = 0;
			Calendar calendar = Calendar.getInstance();
			startTime = calendar.getTimeInMillis();
		}

		@Override
		public int compareTo(Handler o) {
			// TODO Auto-generated method stub
			return (int) (this.speed - o.speed);
		}
	}
	
}
