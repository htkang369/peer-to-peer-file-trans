package com.whz;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.whz.BitTorrentServer.Handler;
import com.whz.msg.HandShakeMsg;
import com.whz.msgtype.BitfieldMsg;
import com.whz.util.MyUtil;

public class Peer {
	static int myID;
	HashMap<Integer, Handler> neighbor;
	ArrayList<LinkState> interestedList;
	static BitField localBitfield;
	static HashMap<Integer, BitField> peerBitfield;
	static HashMap<Integer, LinkState> unChokedMap;
	static HashMap<Integer, LinkState> chokedMap;
	static ArrayList<LinkState> preferredList;
	static LinkState optimisticNeighbor;
	static Timer timerP;
	static Timer timerM;
	
	public void selectPreferredNeighbors() {
		
	}
	
	public void selectOptimisticallyUnchokedNeigbor() {
		
	}
	
	public static class Handler extends Thread{
		private Socket connection;
        private DataInputStream in;	//stream read from the socket
        private DataOutputStream out;    //stream write to the socket
        private int no;		//The index number of the client
        private boolean isClient;
        private int peerID;
        Random rand = new Random();

        public Handler(Socket connection, int no, boolean isClient) {
            this.connection = connection;
	    	this.no = no;
	    	this.isClient = isClient;
        }
        
		public void run() {
			sendHandShake();
			receiveHandShake();
			sendBitfield();
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
					System.out.println("error peerID:" + rcvhandshakeMsg.getPeerID());
				}else {
					System.out.println("peerID = " + rcvhandshakeMsg.getPeerID());
				}
			}
		}
		
		public void sendBitfield() {
			System.out.println("send Bitfield Message");
			BitfieldMsg bitfieldMsg = new BitfieldMsg(Config.bitFieldLength , localBitfield);
			byte[] datagram = BitfieldMsg.toDataGram(bitfieldMsg);
			sendMessage(datagram);
		}
		
		public void receiveBitfield() {
			
		}
		
		public void sendInterestedOrNot() {
			
		}
		
		public void receiveInterestedOrNot() {
			
		}
		
		public void receiveActualMsg() {
			
		}
		
		public void sendUnchoke() {
			
		}
		
		public void sendChoke() {
			
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
	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		System.out.println("The server is running."); 
		ServerSocket listener = new ServerSocket(Config.sPort);
		Peer peer = new Peer();
		int clientNum = 1;
		timerP = new Timer();
		timerM = new Timer();
		timerP.schedule(new TimerTask() {
			public void run(){
				System.out.println("----set timer-----");
				peer.selectPreferredNeighbors();
				peer.sendUnchoke();
			}
		}, Config.unchoking_interval, Config.unchoking_interval);
		timerM.schedule(new TimerTask() {
			public void run(){
				System.out.println("----set Optimistic timer-----");
				peer.selectOptimisticallyUnchokedNeigbor();
				peer.sendUnchoke();

			}
		}, Config.optimistic_unchoking_interval, Config.optimistic_unchoking_interval);
		try {
			while(true) {
	       		Handler handler = new Handler(listener.accept(),clientNum, false);
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
	
}
