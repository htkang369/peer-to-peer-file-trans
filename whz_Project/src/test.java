import java.net.*;
import java.util.Timer;
import java.util.TimerTask;
import java.io.*;

public class test {
	
	private static final int sport = 8000;
	
    public static void main(String[] args) throws Exception{
        System.out.println("The server is running");
        ServerSocket listener = new ServerSocket(sport);
    }
}