package com.cnt5106c.messagetype;

import com.cnt5106c.message.ActualMessage;

/**
 * The number of concurrent connections on which a peer uploads its pieces is limited. 
 * At a  moment,  each  peer  uploads  its  pieces  to  at  most k preferred neighbors  
 * and  1 optimistically  unchoked  neighbor.  The  value  of k is  given  as  a  parameter  
 * when  the program  starts.  Each  peer  uploads  its  pieces  only  to preferred neighbors  
 * and  an optimistically  unchoked  neighbor.  We  say  these  neighbors  are  unchoked  
 * and  all  other neighbors are choked.
 * @author wangyuanming
 *
 */
public class Choke extends ActualMessage{
	private final Byte message_type = 0;
	private final char[] message_payload = null;//Choke have no payload
	
	public Choke(int message_length, Byte message_type, char[] message_payload) {
		super(message_length, message_type, message_payload);
		// TODO Auto-generated constructor stub
	}

}
