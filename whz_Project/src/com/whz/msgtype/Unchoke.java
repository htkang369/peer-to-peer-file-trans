package com.whz.msgtype;

import com.whz.msg.ActualMsg;

@SuppressWarnings("serial")
public class Unchoke extends ActualMsg{
	public Unchoke() {
		super();
		msg_type = (byte)UNCHOKE;
		msg_payload = null;
	}
}
