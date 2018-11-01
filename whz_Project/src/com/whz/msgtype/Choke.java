package com.whz.msgtype;

import com.whz.msg.ActualMsg;

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
@SuppressWarnings("serial")
public class Choke extends ActualMsg{
	public Choke() {
		super();
		msg_type = (byte)CHOKE;
		msg_payload = null;
	}
}
