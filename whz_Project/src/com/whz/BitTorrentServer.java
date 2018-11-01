package com.whz;

import java.net.*;
import java.io.*;

import com.whz.msg.ActualMsg;
import com.whz.msg.HandShakeMsg;
import com.whz.msgtype.Bitfield;
import com.whz.msgtype.Choke;
import com.whz.msgtype.Have;
import com.whz.msgtype.Interested;
import com.whz.msgtype.NotInterested;
import com.whz.msgtype.Piece;
import com.whz.msgtype.Request;
import com.whz.msgtype.Unchoke;
import com.whz.util.MyUtil;


public class BitTorrentServer {

	private static final int sPort = 8000;   //The server will be listening on this port number


	public static void main(String[] args) throws Exception {
		System.out.println("The server is running."); 
        ServerSocket listener = new ServerSocket(sPort);
		int clientNum = 1;
        	try {
            	while(true) {
                	new Handler(listener.accept(),clientNum).start();
                	System.out.println("Client "  + clientNum + " is connected!");
                	clientNum++;
            	}
        	} finally {
            	listener.close();
        	} 
    	}

	/**
     	* A handler thread class.  Handlers are spawned from the listening
     	* loop and are responsible for dealing with a single client's requests.
    */
    private static class Handler extends Thread {
        private String message;    //message received from the client
        private String MESSAGE;    //uppercase message send to the client
        private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket
        private int no;		//The index number of the client
        private byte [] bitfield;
        private byte [] peerBitfield;
        private int bitfieldLength = (int) Math.ceil( MyUtil.pieceNum/8);

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
					readActualMessage();
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
		 * A sends a bitfield message to let B know which file pieces it has
		 */
		void sendBitfield() {
			Bitfield bitfieldMsg = new Bitfield(bitfieldLength + 1,bitfield);
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
		
		void initBitfield() {
			bitfield = new byte[bitfieldLength];
			for(int i=0;i<bitfieldLength;i++) {
				bitfield[i] = (byte) 0xFF;
			}
		}
		
	    
		void sendHandshakeMessage() {
			
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
					System.out.println("receive Bitfield Msg");
					rcvMsg = new Bitfield();
					break;
				case ActualMsg.REQUEST:
					rcvMsg = new Request();
					break;
				case ActualMsg.PIECE:
					System.out.println("receive Piece Meg");
					rcvMsg = new Piece();
					//show the message to the user
					int n = ActualMsg.parseMsgContent(rawMsg, length, rcvMsg);
					System.out.println("Receive message: " + "" + " from client " + no);
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
    }
    
    


}
