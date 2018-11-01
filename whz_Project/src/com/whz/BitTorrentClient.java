package com.whz;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
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

    private byte [] clientPeerID;  // client peer id
    private byte [] serverPeerID;  // server peer id
    private byte [] loaclBitfield;
    private byte [] peerBitfield;
    private List<Integer> interestedPieceList = new ArrayList<>();
    private int bitfieldLength = (int) Math.ceil( MyUtil.pieceNum/8);
    private boolean fileComplete = false;
    private boolean unChoked = true;
	
	private HandShakeMsg sentHandShakeMsg = new HandShakeMsg(clientPeerID); // HandShake Msg send to the server
    private HandShakeMsg receivedHandShakeMsg = new HandShakeMsg(serverPeerID); // HandShake Msg received from the server
    
//    private ActualMsg haveMag = new HaveMsg();
//    private ActualMsg sentActualMsg; //Actual Msg send to the server
//    private ActualMsg receivedActualMsg; //Actual Msg received from the server
	

	public BitTorrentClient() {}

	void run()
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
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
				readFile();
			}else {
				sendNotInterestedMessage();
			}
			while(!fileComplete) {
				if(unChoked) {
					sendRequestMsg();
				}
				readActualMessage();
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
		
		HandShakeMsg handshakeMsg = new HandShakeMsg(1001);
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
		loaclBitfield = new byte[bitfieldLength];
	}
	
	/**
	 * A sends a bitfield message to let B know which file pieces it has
	 */
	void sendBitfield() {
		Bitfield bitfieldMsg = new Bitfield(bitfieldLength + 1,loaclBitfield);
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
			peerBitfield[i] = (byte) (peerBitfield[i]&((byte) ~loaclBitfield[i]));
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
		NotInterested notInterested = new NotInterested();
		byte[] c = ActualMsg.toDataGram(notInterested);
		sendMessage(c);
	}
	
	void readFile() {
		InputStream inFile = null;
		try {
			byte[] tempbytes = new byte[100];
			int byteread = 0;
			inFile = new FileInputStream("test/testfile");
			BitTorrentClient.showAvailableBytes(inFile);
		
			byte[] a = MyUtil.intToByteArray(101);
			byte[] b = new byte[105];
			while((byteread = inFile.read(tempbytes)) != -1) {
				System.out.write(tempbytes, 0, byteread);
				System.out.println();
				System.out.println("one piece!");
				Piece pieceMsg = new Piece(a,tempbytes);
				for(int j=0; j<5; j++) {
					b[j] = 0;
				}
				for(int j=0; j<100; j++) {
					b[j+5] = (pieceMsg.getPayLoad())[j];
				}
				byte[] c = ActualMsg.toDataGram(pieceMsg);
				sendMessage(c);
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
	}
	
	void sendRequestMsg() {
		
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
				break;
			case ActualMsg.PIECE:
				System.out.println("receive Piece Message");
				rcvMsg = new Piece();
				
				int n = ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
				System.out.println("Receive message: " + "" + " from server");
				if(rcvMsg.getPayLoad()!=null) {
					System.out.write(rcvMsg.getPayLoad(), 0, 100);
				}
				System.out.println();
				break;
		}
		if(rcvMsg == null) {
			System.out.println("parse Type error");
		}
		int n = ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
		return rcvMsg;
	}
	
	private static void showAvailableBytes(InputStream in) {
		try {
			System.out.println("number of bytes in file: " + in.available());
			double c = (double)in.available()/ 13.0;
			System.out.println("c:"+ c);
			System.out.println("# of pieces:"+ Math.ceil(c));
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	//main method
	public static void main(String args[])
	{
		BitTorrentClient client = new BitTorrentClient();
		client.run();
	}

}
