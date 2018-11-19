package com.whz;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Calendar calendar = Calendar.getInstance();
		Date time = calendar.getTime();
		System.out.println(time);
		long timeInMillis = calendar.getTimeInMillis();
		System.out.println(timeInMillis);
		for(long i =0; i < 100000000;i++) {
		}
		calendar = Calendar.getInstance();
		long timeInMillis2 = calendar.getTimeInMillis();
		System.out.println(timeInMillis2-timeInMillis);
	}

	
}
