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
		
		initBitfield();
		
		Config.initiatePeerConfig();
		tryToConnect();	
		addTimerP();
		addTimerM();
		listenTcpConnection();	
	}
	
//    static void initBitfield() {
//		localBitfield.bitfield = new byte[Config.bitFieldLength];
//	}
	
	static void initBitfield() {
		localBitfield.bitfield = new byte[Config.bitFieldLength];
		for(int i=0;i<Config.bitFieldLength;i++) {
			localBitfield.bitfield[i] = (byte) 0xFF;
		}
	}
	
	public static void addTimerP() {
		timerP = new Timer();
		timerP.schedule(new TimerTask() {
			public void run(){
				System.out.println("----set timer-----");
				selectPreferredNeighbors();
				clearSpeed();
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
	
	public static void selectPreferredNeighbors() {
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
				unChokedMap.put(unChokePeerID, interestedList.get(i));
				System.out.println("unChokedMap add new" + unChokePeerID + " speed = " + unChokedMap.get(unChokePeerID).speed);
				sendUnchoke(unChokedMap.get(unChokePeerID));
				chokedMap.remove(unChokePeerID);
			}else {
				System.out.println("unChokedMap already has " + unChokePeerID);
			}
		}
		for(int i = Config.k; i < interestedList.size(); i++) {
			int chokePeerID = interestedList.get(i).peerID;
			if(chokedMap.get(chokePeerID) == null) {
				chokedMap.put(chokePeerID, interestedList.get(i));
				System.out.println("chokedMap add new" + chokePeerID);
				sendChoke(chokedMap.get(chokePeerID));
				unChokedMap.remove(chokePeerID);			
			}else {
				System.out.println("chokedMap already has " + chokePeerID);
			}
		}
	}
	
	public static void selectOptimisticallyUnchokedNeigbor() {
		int size = chokedMap.size();
		if(size > 0) {
			int index = random.nextInt(size);
			optimisticNeighbor = chokedMap.get(index);
			if(optimisticNeighbor != null) {
				System.out.println("selectOptimisticallyUnchokedNeigbor " + optimisticNeighbor.peerID);
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
	
	public static class Handler extends Thread implements Comparable<Handler>{
		private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket
        private boolean isClient;
        private int peerID;
        private boolean isInterested;
        private BitField peerBitfield;
        private Random rand = new Random();
        private boolean unChoked = false;
        public List<Integer> interestedPieceList;
    	private int downloadThroughput;
    	private int upLoadThroughput;
    	private long startTime;
    	private float speed;

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
				System.out.println("middle send receive");
				receiveBitfield();
				findOutInterestedPiece();
				sendInterestedOrNot();
				while(!fileComplete) {			
					ActualMsg rcvMsg = receiveActualMsg();
					replyMsg(rcvMsg);
				}
				System.out.println("receive file completely");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally {
				try {
					System.out.println("close with peerID " + peerID);
					in.close();
					out.close();
					connection.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
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
			BitfieldMsg bitfieldMsg = new BitfieldMsg(Config.bitFieldLength + 1 , localBitfield.bitfield);
			byte[] datagram = BitfieldMsg.toDataGram(bitfieldMsg);
			sendMessage(datagram);	
		}
		
		public void receiveBitfield() throws Exception {
			System.out.println("receive Bitfield Message from:" + peerID);
			BitfieldMsg bitfieldMsg = (BitfieldMsg) receiveActualMsg();
			BitField bitfield = new BitField();
			bitfield.bitfield = bitfieldMsg.getPayLoad();
			int payloadLength = MyUtil.byteArrayToInt(bitfieldMsg.getMsgLength());
			System.out.println("parse Bitfield Message from:" + peerID + " payloadLength = " + payloadLength + " bitfield length =" + Config.bitFieldLength);
			for(int i = 0; i< payloadLength -1; i++) {
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
			interestedList.add(this);
		}
		
		public void receiveNotInterested() {
			System.out.println("receiveNotInterested");
			interestedList.remove(this);
		}
		
		public void sendUnchoke() {
			UnchokeMsg unchoke = new UnchokeMsg();
			byte[] c = ActualMsg.toDataGram(unchoke);
			sendMessage(c);
		}
		
		public void receiveUnchoke() {
			System.out.println("receiveUnchoke");
			Calendar calendar = Calendar.getInstance();
			startTime = calendar.getTimeInMillis();
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
			System.out.println("send Piece Message num: " + pieceNum);
			byte[] payLoad = MyUtil.readFile(pieceNum);
			PieceMsg pieceMsg = new PieceMsg(MyUtil.PieceSize + 5, MyUtil.intToByteArray(pieceNum) , payLoad);
			byte[] c = ActualMsg.toDataGram(pieceMsg);
			sendMessage(c);
			upLoadThroughput += Config.PieceSize;
		}
		
		public void receivePiece() {
			System.out.println("receivePiece");
		}
		
		public void sendHaveToAll(int pieceIndex) {
			System.out.println("send have to all");
			Iterator<Integer> iter = neighbor.keySet().iterator();
			while(iter.hasNext()) {
				int key = iter.next();
				neighbor.get(key).sendHave(pieceIndex);
			}
		}
		
		public void sendHave(int pieceIndex) {
			System.out.println("sendHave to " + peerID);
			HaveMsg haveMsg = new HaveMsg(pieceIndex);
			byte[] c = ActualMsg.toDataGram(haveMsg);
			sendMessage(c);
		}
		
		public void receiveHave(ActualMsg rcvMsg) {
			System.out.println("replyHave");
			HaveMsg haveMsg = (HaveMsg) rcvMsg;
			changePeerBitField(MyUtil.byteArrayToInt(haveMsg.getPayLoad()));
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
			for(int i =0; i< Config.bitFieldLength; i++) {
				System.out.println("localBitfield.bitfield:" + localBitfield.bitfield[i]);
				peerBitfield.bitfield[i] = (byte) (peerBitfield.bitfield[i] & ((byte) ~ localBitfield.bitfield[i]));
				if(peerBitfield.bitfield[i] != 0) {
					t = true;
					for(int j = 0; j < 8;j++) {
						int k = 1; 
						k = (peerBitfield.bitfield[i] >> j) & k;
						if( k == 1) {
							interestedPieceList.add(i*8+j);
							System.out.println("find out interested piece, pieceNum = " + (i*8+j));
							isInterested = true;
						}
					}
				}
			}
			return t;
		}
		
		ActualMsg receiveActualMsg() throws Exception {
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
						System.out.println("receive ChokeMsg");
						break;
					case ActualMsg.UNCHOKE:
						rcvMsg = new UnchokeMsg();
						System.out.println("receive UnchokeMsg");
						break;
					case ActualMsg.INTERESTED:
						rcvMsg = new InterestedMsg();
						System.out.println("receive InterestedMsg");
						break;
					case ActualMsg.NOTINTERESTED:
						rcvMsg = new NotInterestedMsg();
						System.out.println("receive NotInterestedMsge");
						break;
					case ActualMsg.HAVE:
						rcvMsg = new HaveMsg();
						System.out.println("receive HaveMsg");
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
						downloadThroughput += Config.PieceSize;
						sendHaveToAll(piecenum);
						break;
				}
				if(rcvMsg == null) {
					System.out.println("parse Type error");
				}
				ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
				return rcvMsg;
			}else {
				System.out.println("not our message");
				throw new Exception();
			}
		}
		
		public void replyMsg(ActualMsg rcvMsg) {
			if(rcvMsg != null) {
				int msgType = rcvMsg.getMsgType();
				switch(msgType) {
				case ActualMsg.CHOKE:
					receiveChoke();
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
					receiveInterested();
					break;
				case ActualMsg.NOTINTERESTED:
					receiveNotInterested();
					break;
				case ActualMsg.HAVE:
					receiveHave(rcvMsg);
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
		
		public void changePeerBitField(int piecenum) {
			int index = piecenum / 8;
			int offset = piecenum %8;
			int temp = 0x01 << (8 - offset);
			if((~localBitfield.bitfield[index] & temp) != 0) {
				System.out.println("receive interested have from " + peerID + " pieceNum = " + piecenum);
				if(isInterested == false) {
					System.out.println("this is new interest");
					isInterested = true;
					sendInterestedOrNot();
				}
			}
			peerBitfield.bitfield[index] = (byte) (peerBitfield.bitfield[index] | temp);
		}
		
		public void sendMessage(byte[] msg)
		{
			try{
				//stream write the message
				out.write(msg);
				out.flush();
			}
			catch(IOException ioException){
				System.out.println("error send message");
				ioException.printStackTrace();
			}
		}
		
		void computeDownloadRate() {
			Calendar calendar = Calendar.getInstance();
			long timeInMillis = calendar.getTimeInMillis();
			speed = (downloadThroughput + upLoadThroughput) / ( timeInMillis - startTime);
			System.out.println(peerID + " update speed =" + speed);
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
