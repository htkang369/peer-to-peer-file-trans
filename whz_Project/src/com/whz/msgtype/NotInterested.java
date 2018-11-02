package com.whz.msgtype;

import com.whz.msg.ActualMsg;
import com.whz.util.MyUtil;

@SuppressWarnings("serial")
public class NotInterested extends ActualMsg{
	/**
	 * do not have pay load
	 * @param message_length
	 */
	public NotInterested() {
		super();
		msg_type = (byte)NOTINTERESTED;
		msg_payload = null;
		msg_length = MyUtil.intToByteArray(1);
	}
}
