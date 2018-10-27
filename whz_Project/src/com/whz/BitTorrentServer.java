package com.whz;

import java.net.*;
import java.io.*;

import com.whz.msg.ActualMsg;
import com.whz.msg.HandShakeMsg;
import com.whz.msgtype.Piece;


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
				while(true)
				{	
					//receive the message sent from the client
					//piece = (Piece)in.readObject();
					//byte[] b = (byte[]) in.readObject();
					
					byte[] length = new byte[4];
					in.read(length);
					int msgLength = ActualMsg.parseLength(length);
					byte[] actualmsg = new byte[msgLength];
					in.read(actualmsg);
		//should parse type
					Piece pieceMsg = new Piece();
					int n = ActualMsg.parseMsgContent(actualmsg, length, pieceMsg);
					//show the message to the user
					System.out.println("Receive message: " + "" + " from client " + no);
					System.out.write(pieceMsg.getPayLoad(), 0, 100);
					System.out.println();
					//System.out.write(b, 0, 100);
					//Capitalize all letters in the message
					//MESSAGE = bufferedReader.readLine();
					//MESSAGE = message.toUpperCase();
					//send MESSAGE back to the client
					//sendMessage(MESSAGE);
					//sendMessage("ack");			
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
    }


}
