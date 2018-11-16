package com.whz.msgtype;

import com.whz.msg.ActualMsg;
import com.whz.util.MyUtil;

@SuppressWarnings("serial")
public class Interested extends ActualMsg{
	/**
	 * do not have pay load
	 * @param message_length
	 */
	public Interested() {
		super();
		msg_type = (byte)INTERESTED;
		msg_payload = null;
		msg_length = MyUtil.intToByteArray(1);
	}
}
