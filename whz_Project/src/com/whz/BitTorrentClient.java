package com.whz;
import java.net.*;
import java.io.*;
import java.util.*;
import com.whz.msg.*;
import com.whz.msgtype.*;
import com.whz.util.MyUtil;

public class BitTorrentClient {
	Socket requestSocket;           //socket connect to the server
	DataOutputStream out;         //stream write to the socket
	DataInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server
	
	Random rand = new Random();

    private int clientPeerID = 1000;  // client peer id
    private int serverPeerID = 1002;  // server peer id
    private byte [] locallBitfield;
    private byte [] peerBitfield;
    private List<Integer> interestedPieceList = new ArrayList<>();
    private int bitfieldLength = (int) Math.ceil( MyUtil.pieceNum/8);
    private boolean fileComplete = false;
    private boolean unChoked = false;
    
    private HashMap<Integer, LinkState> chokedMap = new HashMap<>();
    private HashMap<Integer, LinkState> unChokedMap = new HashMap<>();
    private LinkState optimisticNeighbor;
    
	
//	private HandShakeMsg sentHandShakeMsg = new HandShakeMsg(clientPeerID); // HandShake Msg send to the server
//	private HandShakeMsg receivedHandShakeMsg = new HandShakeMsg(serverPeerID); // HandShake Msg received from the server
    
//    private ActualMsg haveMag = new HaveMsg();
//    private ActualMsg sentActualMsg; //Actual Msg send to the server
//    private ActualMsg receivedActualMsg; //Actual Msg received from the server
	

	public BitTorrentClient() {}

	void run()
	{
		try{
			//create a socket to connect to the server
			
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000 ,  this peer ID = " + clientPeerID);
			//initialize inputStream and outputStream
			out = new DataOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new DataInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("TCP connection established!");
			sendHandshakeMessage();//A(Client) sends a handshake to B(Server)
			receiveHandshakeMessage();
			initBitfield();
			sendBitfield();
			receiveBitfield();
			if(findOutInterestedPiece()) {
				sendInterestedMessage();
			}else {
				sendNotInterestedMessage();
			}
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				public void run(){
					System.out.println("----set timer-----");
					computeDownloadRate();
					selectPreferredNeighbors();
				}
			}, MyUtil.unchoking_interval, MyUtil.unchoking_interval);
			Timer timer2 = new Timer();
			timer2.schedule(new TimerTask() {
				public void run(){
					System.out.println("----set Optimistic timer-----");
					selectOptimisticallyUnchokedNeigbor();
				}
			}, MyUtil.optimistic_unchoking_interval, MyUtil.optimistic_unchoking_interval);
			while(!fileComplete) {
				
				ActualMsg rcvMsg = readActualMessage();
				
				replyMsg(rcvMsg);
			}
			
			while(true) {
				
			}
		}
		catch (ConnectException e) {
    			System.err.println("Connection refused. You need to initiate a server first.");
		} 
		catch(UnknownHostException unknownHost){
			System.err.println("You are trying to connect to an unknown host!");
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		} 
		finally{
			//Close connections
			try{
				out.flush();
				in.close();
				out.close();
				System.out.println("close connection ");
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	
	void sendMessage(byte[] msg)
	{
		try{
			//stream write the message
			out.write(msg);
			out.flush();
		}
		catch(IOException ioException){
			ioException.printStackTrace();
		}
	}
	
	/**
	 * A(Client) sends a handshake to B(Server)
	 */
	void sendHandshakeMessage() {
		System.out.println("send HandShake Message");
		HandShakeMsg handshakeMsg = new HandShakeMsg(clientPeerID);
		sendMessage(HandShakeMsg.toDataGram(handshakeMsg));
	}
	
	/**
	 * A(Client) receives a handshake to B(Server),should have timer or not?
	 * 
	 * check whether the handshake header is right and the peer ID is the expected one
	 */
	void receiveHandshakeMessage() {
		byte[] rawMsg = new byte[32];
		try {
			in.read(rawMsg);
			System.out.println("receive handshakeMessage");
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("read error");
		}
		HandShakeMsg rcvhandshakeMsg = HandShakeMsg.parseHeaderMsg(rawMsg);
		if(!HandShakeMsg.checkPeerID(1002, rcvhandshakeMsg)) {
			System.out.println("error peerID:" + rcvhandshakeMsg.getPeerID());
		}else {
			System.out.println("peerID = " + rcvhandshakeMsg.getPeerID());
		}
	}
	
	
	void initBitfield() {
		locallBitfield = new byte[bitfieldLength];
	}
	
	/**
	 * A sends a bitfield message to let B know which file pieces it has
	 */
	void sendBitfield() {
		System.out.println("send Bitfield Message");
		Bitfield bitfieldMsg = new Bitfield(bitfieldLength + 1,locallBitfield);
		byte[] datagram = Bitfield.toDataGram(bitfieldMsg);
		sendMessage(datagram);
	}
	
	/**
	 * B will also send its bitfield message to A, unless it has no pieces
	 */
	void receiveBitfield() {
		Bitfield bitfieldMsg = (Bitfield) readActualMessage();
		peerBitfield = bitfieldMsg.getPayLoad();
		int payloadLength = MyUtil.byteArrayToInt(bitfieldMsg.getMsgLength());
		System.out.println("parse Bitfield Message");
		for(int i = 0; i< payloadLength-4; i++) {
			System.out.print(peerBitfield[i]);
		}
		System.out.println();
		System.out.println("Bitfield payloadLeng = " + payloadLength);
	}
	
	/**
	 * if A receives a bitfield message form B, finds out whether B has pieces that it doesn't have
	 */
	boolean findOutInterestedPiece() {
		//compare localBitfield with peerBitfield
		boolean t = false;
		for(int i =0; i<bitfieldLength; i++) {
			peerBitfield[i] = (byte) (peerBitfield[i]&((byte) ~locallBitfield[i]));
			if(peerBitfield[i] != 0) {
				t = true;
				for(int j = 0; j < 8;j++) {
					int k = 1; 
					k = (peerBitfield[i] >> j) & k;
					if( k == 1) {
						interestedPieceList.add(i*8+j);
						System.out.println("find out interested piece, pieceNum = " + (i*8+j));
					}
				}
			}
		}
		return t;
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
	void sendInterestedMessage() {
		System.out.println("send Interested Message");
		Interested interestedMsg = new Interested();
		byte[] c = ActualMsg.toDataGram(interestedMsg);
		sendMessage(c);
	}
	
	/**
	 *  whenever a peer receives a piece completely, it checks the bitfields
	 *  of its neighbors and decides whether it should send 'not interested'
	 *  messages to some neighbors
	 */
	void sendNotInterestedMessage(){
		System.out.println("send Not interested Message");
		NotInterested notInterested = new NotInterested();
		byte[] c = ActualMsg.toDataGram(notInterested);
		sendMessage(c);
	}
	
	byte[] readFile(int pieceNum) {
		InputStream inFile = null;
		byte[] tempbytes = new byte[MyUtil.PieceSize];
		try {		
			inFile = new FileInputStream("test/testfile");
			BitTorrentClient.showAvailableBytes(inFile);
			inFile.skip(pieceNum * MyUtil.PieceSize);
			if(inFile.read(tempbytes) != -1) {
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
	
	void replyMsg(ActualMsg rcvMsg) {
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
	
	void sendRequestMsg() {
		System.out.println("send Request Message");
		Request request_message = new Request();
		int length_interest = interestedPieceList.size();
		System.out.println("interestedPieceList size = " + length_interest);
		int index = rand.nextInt(length_interest);
		byte[] a = MyUtil.intToByteArray(interestedPieceList.get(index));
		request_message.setPayLoad(a);
		request_message.setMsgLength(MyUtil.intToByteArray(5));
		byte[] c = ActualMsg.toDataGram(request_message);
		sendMessage(c);
		
	}
	
	void sendPieceMsg(int pieceNum) {
		System.out.println("send Piece Message");
		byte[] payLoad = readFile(pieceNum);
		Piece pieceMsg = new Piece(MyUtil.PieceSize + 5, MyUtil.intToByteArray(pieceNum) , payLoad);
		byte[] c = ActualMsg.toDataGram(pieceMsg);
		sendMessage(c);
	}

	ActualMsg readActualMessage() {
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
					rcvMsg = new Choke();
					break;
				case ActualMsg.UNCHOKE:
					rcvMsg = new Unchoke();
					break;
				case ActualMsg.INTERESTED:
					rcvMsg = new Interested();
					break;
				case ActualMsg.NOTINTERESTED:
					rcvMsg = new NotInterested();
					break;
				case ActualMsg.HAVE:
					rcvMsg = new Have();
					break;
				case ActualMsg.BITFIELD:
					System.out.println("receive Bitfield Message");
					rcvMsg = new Bitfield();
					break;
				case ActualMsg.REQUEST:
					rcvMsg = new Request();
					ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
					System.out.println("receive Request Message");
					break;
				case ActualMsg.PIECE:
					System.out.println("receive Piece Message");
					rcvMsg = new Piece();
					
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
	
	void changeLocalBitField(int piecenum) {
		int index = piecenum / 8;
		int offset = piecenum %8;
		int temp = 0x01 << (8 - offset);
		locallBitfield[index] = (byte) (locallBitfield[index] | temp);
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
	
	void computeDownloadRate() {
		Iterator<Integer> iter = unChokedMap.keySet().iterator();
		while(iter.hasNext()) {
			int id = iter.next();
			LinkState s = unChokedMap.get(id);
			//s.speed = s.throughput / MyUtil.unchoking_interval;
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
	
	void selectPreferredNeighbors() {
		System.out.println("select preferredNeighbors");
	}
	
	void sendUnchokeMsg(LinkState optimisticNeighbor) {
		Unchoke unchoke = new Unchoke();
		byte[] c = ActualMsg.toDataGram(unchoke);
		sendMessage(c);
	}
	
	//main method
	public static void main(String args[])
	{
		BitTorrentClient client = new BitTorrentClient();
		client.run();
	}

}
