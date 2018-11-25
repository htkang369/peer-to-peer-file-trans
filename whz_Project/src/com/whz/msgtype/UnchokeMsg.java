package com.whz.msgtype;

import com.whz.msg.ActualMsg;
import com.whz.util.MyUtil;

@SuppressWarnings("serial")
public class UnchokeMsg extends ActualMsg{
	public UnchokeMsg() {
		super();
		msg_type = (byte)UNCHOKE;
		msg_payload = null;
		msg_length = MyUtil.intToByteArray(1);
	}
}
