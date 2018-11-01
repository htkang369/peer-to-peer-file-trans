package com.whz.msgtype;

import com.whz.msg.ActualMsg;

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
	}
}
