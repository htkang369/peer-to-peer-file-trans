package com.whz;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
	static List<Integer> interestedPieceList;
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
		interestedPieceList = new ArrayList<>();
		neighbor = new HashMap<>();
		interestedList = new ArrayList<>();
		localBitfield = new BitField();
		
		initBitfield();
		
		Config.initiatePeerConfig();
		tryToConnect();	
		addTimerP();
		addTimerM();
		listenTcpConnection();	
	}
	
    static void initBitfield() {
		localBitfield.bitfield = new byte[Config.bitFieldLength];
	}
	
//	static void initBitfield() {
//		localBitfield.bitfield = new byte[Config.bitFieldLength];
//		for(int i=0;i<Config.bitFieldLength;i++) {
//			localBitfield.bitfield[i] = (byte) 0xFF;
//		}
//	}
	
	public static void addTimerP() {
		timerP = new Timer();
		timerP.schedule(new TimerTask() {
			public void run(){
				System.out.println("----set timer-----");
				Peer.selectPreferredNeighbors();
				//Peer.sendChokeUnchoke();
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
	
	public static void selectPreferredNeighbors() {
		System.out.println("select preferredNeighbors");
	}
	
	public static void selectOptimisticallyUnchokedNeigbor() {
		int size = chokedMap.size();
		int n = 0;
		int temp = 0;
		if(size > 0) {
			int index = random.nextInt(size);
			Iterator<Integer> iter = chokedMap.keySet().iterator();
			while(iter.hasNext()&& n<index) {
				temp = iter.next();
				n++;
			}
			System.out.println("update optimistic neighbor");
		}
		optimisticNeighbor = chokedMap.get(temp);
	}
	
	public static void sendChokeUnchoke() {
		Iterator<Integer> iter = unChokedMap.keySet().iterator();
		while(iter.hasNext()) {
			int temp = iter.next();
			unChokedMap.get(temp).sendUnchoke();
		}
	}
	
	public static void sendOpUnchoke() {
		if(optimisticNeighbor!= null) {
			optimisticNeighbor.sendUnchoke();
		}
	}
	
	public void sendChoke() {
		Iterator<Integer> iter = chokedMap.keySet().iterator();
		while(iter.hasNext()) {
			int temp = iter.next();
			chokedMap.get(temp).sendChoke();
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
				Handler handler = new Handler(requestSocket, clientNum, true, temp);
	       		handler.start();
	       		neighbor.put(temp, handler);
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
			timerP.cancel();
			timerM.cancel();
		}
	}
	
	public static class Handler extends Thread{
		private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket
        private int no;		//The index number of the client
        private boolean isClient;
        private int peerID;
        private boolean isInterested;
        private BitField peerBitfield;
        private Random rand = new Random();
        private boolean unChoked = false;

        public Handler(Socket connection, int no, boolean isClient, int peerID) {
            this.connection = connection;
	    	this.no = no;
	    	this.isClient = isClient;
	    	this.peerID = peerID;
        }
        
		public void run() {
			//initialize Input and Output streams
 			//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			try {
				out = new DataOutputStream(connection.getOutputStream());
				in = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sendHandShake();
			receiveHandShake();
			sendBitfield();
			System.out.println("middle send receive");
			receiveBitfield();
			findOutInterestedPiece();
			sendInterestedOrNot();
			while(!fileComplete) {			
				ActualMsg rcvMsg = receiveActualMsg();
				replyMsg(rcvMsg);
			}
			
		}
		
		public void sendHandShake() {
			System.out.println("send HandShake Message");
			HandShakeMsg handshakeMsg = new HandShakeMsg(myID);
			sendMessage(HandShakeMsg.toDataGram(handshakeMsg));
		}
		
		/**
		 * A(Client) receives a handshake to B(Server),should have timer or not?
		 * 
		 * check whether the handshake header is right and the peer ID is the expected one
		 */
		public void receiveHandShake() {
			byte[] rawMsg = new byte[32];
			try {
				in.read(rawMsg);
				System.out.println("receive handshakeMessage");
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("read error");
			}
			HandShakeMsg rcvhandshakeMsg = HandShakeMsg.parseHeaderMsg(rawMsg);
			//check head
			HandShakeMsg.checkHead(rcvhandshakeMsg);
			if(isClient) {
				if(!HandShakeMsg.checkPeerID(peerID, rcvhandshakeMsg)) {
					System.out.println("error peerID:" + rcvhandshakeMsg.getPeerID() + " right peerID:" + peerID);
				}else {
					System.out.println("peerID = " + rcvhandshakeMsg.getPeerID());
					neighbor.put(rcvhandshakeMsg.getPeerID(), this);
				}
			}else {
				System.out.println("peerID = " + rcvhandshakeMsg.getPeerID());
				peerID = rcvhandshakeMsg.getPeerID();
			}
		}
		
		public void sendBitfield() {
			System.out.println("send Bitfield Message bitFieldLength = " + Config.bitFieldLength);
			BitfieldMsg bitfieldMsg = new BitfieldMsg(Config.bitFieldLength , localBitfield.bitfield);
			byte[] datagram = BitfieldMsg.toDataGram(bitfieldMsg);
			sendMessage(datagram);	
		}
		
		public void receiveBitfield() {
			System.out.println("receive Bitfield Message from:" + peerID);
			BitfieldMsg bitfieldMsg = (BitfieldMsg) receiveActualMsg();
			BitField bitfield = new BitField();
			bitfield.bitfield = bitfieldMsg.getPayLoad();
			int payloadLength = MyUtil.byteArrayToInt(bitfieldMsg.getMsgLength());
			System.out.println("parse Bitfield Message from:" + peerID);
			for(int i = 0; i< payloadLength-4; i++) {
				System.out.print(bitfield.bitfield[i]);
			}
			peerBitfield = bitfield;
			peerBitfields.put(peerID, peerBitfield);
			System.out.println();
			System.out.println("Bitfield payloadLeng = " + payloadLength);
		
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
				System.out.println("send Interested Message");
				InterestedMsg interestedMsg = new InterestedMsg();
				byte[] c = ActualMsg.toDataGram(interestedMsg);
				sendMessage(c);
			}else {
				System.out.println("send Not interested Message");
				NotInterestedMsg notInterested = new NotInterestedMsg();
				byte[] c = ActualMsg.toDataGram(notInterested);
				sendMessage(c);
			}
		}
		
		public void receiveInterested() {
			System.out.println("receiveInterested");
		}
		
		public void receiveNotInterested() {
			System.out.println("receiveUnchoke");
		}
		
		public void sendUnchoke() {
			UnchokeMsg unchoke = new UnchokeMsg();
			byte[] c = ActualMsg.toDataGram(unchoke);
			sendMessage(c);
		}
		
		public void receiveUnchoke() {
			System.out.println("receiveUnchoke");
		}
		
		public void sendChoke() {
			ChokeMsg choke = new ChokeMsg();
			byte[] c = ActualMsg.toDataGram(choke);
			sendMessage(c);
		}
		
		public void receiveChoke() {
			System.out.println("receiveChoke");
		}
		
		public void sendPieceMsg(int pieceNum) {
			System.out.println("send Piece Message");
			byte[] payLoad = MyUtil.readFile(pieceNum);
			PieceMsg pieceMsg = new PieceMsg(MyUtil.PieceSize + 5, MyUtil.intToByteArray(pieceNum) , payLoad);
			byte[] c = ActualMsg.toDataGram(pieceMsg);
			sendMessage(c);
		}
		
		public void receivePiece() {
			System.out.println("receivePiece");
		}
		
		public void sendHave() {
			System.out.println("sendHave");
		}
		
		public void receiveHave() {
			System.out.println("receiveHave");
		}

		public void sendRequest() {
			System.out.println("sendRequest");
		}
		
		public void receiveRequest() {
			System.out.println("receiveRequest");
		}
		
		/**
		 * if A receives a bitfield message form B, finds out whether B has pieces that it doesn't have
		 */
		boolean findOutInterestedPiece() {
			//compare localBitfield with peerBitfield
			boolean t = false;
			for(int i =0; i<Config.bitFieldLength; i++) {
				peerBitfield.bitfield[i] = (byte) (peerBitfield.bitfield[i]&((byte) ~ localBitfield.bitfield[i]));
				if(peerBitfield.bitfield[i] != 0) {
					t = true;
					for(int j = 0; j < 8;j++) {
						int k = 1; 
						k = (peerBitfield.bitfield[i] >> j) & k;
						if( k == 1) {
							interestedPieceList.add(i*8+j);
							System.out.println("find out interested piece, pieceNum = " + (i*8+j));
						}
					}
				}
			}
			return t;
		}
		
		ActualMsg receiveActualMsg() {
			byte[] length = new byte[4];
			try {
				in.read(length);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
						receiveChoke();
						break;
					case ActualMsg.UNCHOKE:
						rcvMsg = new UnchokeMsg();
						receiveUnchoke();
						break;
					case ActualMsg.INTERESTED:
						rcvMsg = new InterestedMsg();
						receiveInterested();
						break;
					case ActualMsg.NOTINTERESTED:
						rcvMsg = new NotInterestedMsg();
						receiveNotInterested();
						break;
					case ActualMsg.HAVE:
						rcvMsg = new HaveMsg();
						receiveHave();
						break;
					case ActualMsg.BITFIELD:
						System.out.println("receive Bitfield Message");
						rcvMsg = new BitfieldMsg();
						//receiveBitfield();
						break;
					case ActualMsg.REQUEST:
						rcvMsg = new RequestMsg();
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						receiveRequest();
						System.out.println("receive Request Message");
						break;
					case ActualMsg.PIECE:
						System.out.println("receive Piece Message");
						rcvMsg = new PieceMsg();
						
						ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
						if(rcvMsg.getPayLoad()!=null) {
							System.out.write(rcvMsg.getPayLoad(), 4,MyUtil.byteArrayToInt(length) - 5);
						}
						System.out.println();
						byte[] pieceNum = new byte[4];
						byte[] payLoad = rcvMsg.getPayLoad();
						System.arraycopy(payLoad, 0, pieceNum, 0, 4);
						byte[] content = new byte[MyUtil.byteArrayToInt(length) - 5];
						System.arraycopy(payLoad, 4, content, 0, MyUtil.byteArrayToInt(length) - 5);
						int piecenum = MyUtil.byteArrayToInt(pieceNum);
						System.out.println("receive Piece finished! : number = " + piecenum);
						changeLocalBitField(piecenum);
						int t = interestedPieceList.indexOf(piecenum);
						if(t != -1) {
							interestedPieceList.remove(t);
						}else {
							System.out.println("do not need this one");
						}
						if(interestedPieceList.size() == 0) {
							fileComplete = true;
						}
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
		
		public void replyMsg(ActualMsg rcvMsg) {
			if(rcvMsg != null) {
				int msgType = rcvMsg.getMsgType();
				switch(msgType) {
				case ActualMsg.CHOKE:
					break;
				case ActualMsg.UNCHOKE:
					System.out.println("reply UnChoke Message");
					unChoked = true;
					if(unChoked & !fileComplete) {
						sendRequestMsg();
					}
					break;
				case ActualMsg.INTERESTED:
					System.out.println("reply Interested Message");
					//LinkState state = new LinkState(serverPeerID, out);
					//chokedMap.put(serverPeerID,state);
					break;
				case ActualMsg.NOTINTERESTED:
					//delete from map
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
					if(unChoked & !fileComplete) {
						sendRequestMsg();
					}
					break;
				}
			}
		}
		
		public void sendRequestMsg() {
			System.out.println("send Request Message");
			RequestMsg request_message = new RequestMsg();
			int length_interest = interestedPieceList.size();
			System.out.println("interestedPieceList size = " + length_interest);
			int index = rand.nextInt(length_interest);
			byte[] a = MyUtil.intToByteArray(interestedPieceList.get(index));
			request_message.setPayLoad(a);
			request_message.setMsgLength(MyUtil.intToByteArray(5));
			byte[] c = ActualMsg.toDataGram(request_message);
			sendMessage(c);
			
		}
		
		public void changeLocalBitField(int piecenum) {
			int index = piecenum / 8;
			int offset = piecenum %8;
			int temp = 0x01 << (8 - offset);
			localBitfield.bitfield[index] = (byte) (localBitfield.bitfield[index] | temp);
		}
		
		public void sendMessage(byte[] msg)
		{
			try{
				//stream write the message
				System.out.println("try send message");
				out.write(msg);
				System.out.println("try send message2");
				out.flush();
				System.out.println("try send message3");
			}
			catch(IOException ioException){
				System.out.println("error send message");
				ioException.printStackTrace();
			}
		}
	}
	
}
