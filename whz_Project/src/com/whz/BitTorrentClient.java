package com.whz;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;
import com.whz.msg.*;
import com.whz.msgtype.Piece;

public class BitTorrentClient {
	Socket requestSocket;           //socket connect to the server
	DataOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server

    private byte [] clientPeerID;  // client peer id
    private byte [] serverPeerID;  // server peer id
	
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
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
			System.out.print("TCP connection established!");
			sendHandshakeMessage();//A(Client) sends a handshake to B(Server)
			receiveHandshakeMessage();
			sendBitfield();
			receiveBitfield();
			if(findoutNotHave()) {
				sendInterestedMessage(in);
			}else {
				sendNotInterestedMessage();
			}
			while(true)
			{
				/*
				System.out.print("Hello, please input a sentence: ");
				//read a sentence from the standard input
				message = bufferedReader.readLine();
				//Send the sentence to the server
				sendMessage(message);
				//Receive the upperCase sentence from the server
				MESSAGE = (String)in.readObject();
				//show the message to the user
				System.out.println("Receive message: " + MESSAGE);*/
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
//		catch (ClassNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		finally{
			//Close connections
			try{
				in.close();
				out.close();
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
		
		
	}
	
	/**
	 * A(Client) receives a handshake to B(Server),should have timer
	 */
	void receiveHandshakeMessage() {
		//check whether the handshake header is right and the peer ID is the expected one
		
	}
	
	/**
	 * A sends a bitfield message to let B know which file pieces it has
	 */
	void sendBitfield() {
		
	}
	
	/**
	 * B will also send its bitfield message to A, unless it has no pieces
	 */
	void receiveBitfield() {
		
	}
	
	/**
	 * if A receives a bitfield message form B, finds out whether B has pieces that it doesn't have
	 */
	boolean findoutNotHave() {
		return true;
		
	}
	
	/**
	 * A sends interested message to B.
	 */
	void sendInterestedMessage(ObjectInputStream inO) {
//		File file = new File("test/testfile");
		InputStream in = null;
		String MESSAGE;
		try {
			System.out.println("以字节为单位读取文件内容，一次读多个字节：");
			byte[] tempbytes = new byte[100];
			int byteread = 0;
			in = new FileInputStream("test/testfile");
			BitTorrentClient.showAvailableBytes(in);
		
			byte[] a = ActualMsg.intToByteArray(100);;
			byte[] b = new byte[105];
			while((byteread = in.read(tempbytes)) != -1) {
				System.out.write(tempbytes, 0, byteread);
				System.out.println();
				System.out.println("one piece!");
				Piece pieceMsg = new Piece(a,tempbytes);
				//System.out.println(pieceMsg.getMsgType());
				//BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				for(int j=0; j<5; j++) {
					b[j] = 0;
				}
				for(int j=0; j<100; j++) {
					b[j+5] = (pieceMsg.getPayLoad())[j];
				}
				out.flush();
				byte[] c = Piece.toDataGram(pieceMsg);
				sendMessage(c);
				//message = bufferedReader.readLine();
				MESSAGE = (String)inO.readObject();
				System.out.println("Receive message: " + MESSAGE);
			}
		}catch(Exception e1) {
			e1.printStackTrace();
		}finally {
			if(in != null) {
				try {
					in.close();
				}catch(IOException e1) {
					
				}
			}
		}
	}
	
	void sendNotInterestedMessage(){
		
	}
	
	private static void showAvailableBytes(InputStream in) {
		try {
			System.out.println("当前字节输入流中的字节数为:"+ in.available());
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
