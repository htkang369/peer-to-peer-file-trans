package com.whz;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReadFileTest {
	
	public static void readFileByBytes(String fileName) {
		File file = new File(fileName);
		InputStream in = null;
//		try {
//			System.out.println("以字节为单位读取文件内容，一次读一个字节：");
//			in = new FileInputStream(file);
//			int tempbyte;
//			while ((tempbyte = in.read())!= -1) {
//				System.out.write(tempbyte);
//			}
//			in.close();
//		}catch(IOException e) {
//			e.printStackTrace();
//			return;
//		}
		try {
			System.out.println("以字节为单位读取文件内容，一次读多个字节：");
			byte[] tempbytes = new byte[100];
			int byteread = 0;
			in = new FileInputStream(fileName);
			ReadFileTest.showAvailableBytes(in);
			while((byteread = in.read(tempbytes)) != -1) {
				System.out.write(tempbytes, 0, byteread);
				System.out.println();
				System.out.println("one piece!");
			}
		}catch(Exception e1) {
			e1.printStackTrace();
		}finally {
			if(in != null) {
				try {
					in.close();
				}catch(IOException e1) {
					
				}
			}
		}
	}

	private static void showAvailableBytes(InputStream in) {
		try {
			System.out.println("当前字节输入流中的字节数为:"+ in.available());
			double c = (double)in.available()/ 13.0;
			System.out.println("c:"+ c);
			System.out.println("# of pieces:"+ Math.ceil(c));
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		String fileName = "test/testfile";
		ReadFileTest.readFileByBytes(fileName);
	}
}
