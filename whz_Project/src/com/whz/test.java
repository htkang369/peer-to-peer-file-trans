package com.whz;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import com.whz.util.MyUtil;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(1/8);
		int msgLength = 20000;
		System.out.println(Math.ceil(((double)1/(double)8)));
		HashMap<Integer, Object> o = new HashMap<>();
		o.put(1,null);
		byte[] rawMsg = new byte[msgLength];
		byte[] rawMsg2 = new byte[msgLength];
		byte[] rawMsg3 = new byte[msgLength];
		byte[] rawMsg4 = new byte[msgLength];

		byte a = (byte) ~(0xff);
		System.out.println(String.format("%02X", 0x01));
		System.out.println(String.format("%02X", 0x01<<2));
		
		
	}

	
}
