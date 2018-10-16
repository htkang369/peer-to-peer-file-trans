package com.cnt5106c;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.*;

public class BitTorrentClient {
	Socket requestSocket;           //socket connect to the server
	ObjectOutputStream out;         //stream write to the socket
 	ObjectInputStream in;          //stream read from the socket
	String message;                //message send to the server
	String MESSAGE;                //capitalized message read from the server

	public BitTorrentClient() {}

	void run()
	{
		try{
			//create a socket to connect to the server
			requestSocket = new Socket("localhost", 8000);
			System.out.println("Connected to localhost in port 8000");
			//initialize inputStream and outputStream
			out = new ObjectOutputStream(requestSocket.getOutputStream());
			out.flush();
			in = new ObjectInputStream(requestSocket.getInputStream());
			
			//get Input from standard input
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
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
				System.out.println("Receive message: " + MESSAGE);
				*/
				System.out.print("TCP connection established!");
				sendHandshakeMessage();//A(Client) sends a handshake to B(Server)
				receiveHandshakeMessage();
				sendBitfield();
				receiveBitfield();
				if(findoutNotHave()) {
					sendInterestedMessage();
				}else {
					sendNotInterestedMessage();
				}
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
				requestSocket.close();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}
	//send a message to the output stream
	void sendMessage(String msg)
	{
		try{
			//stream write the message
			out.writeObject(msg);
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
		return false;
		
	}
	
	/**
	 * A sends interested message to B.
	 */
	void sendInterestedMessage() {
		
	}
	
	void sendNotInterestedMessage(){
		
	}
	
	//main method
	public static void main(String args[])
	{
		BitTorrentClient client = new BitTorrentClient();
		client.run();
	}

}
