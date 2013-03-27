package pt.utl.ist.tagus.cmov.neartweet.CentralizedServer;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;


public class Client{

	private	final static String serverIP = "127.0.0.1";
	private	final static int serverPort = 4444;
	private static ArrayList<BasicDTO> objects = new ArrayList<BasicDTO>();

	public static void main(String[] args) throws InterruptedException, UnknownHostException {

		InetAddress serverAddr = InetAddress.getByName(serverIP);
		Socket localSock = null;
		ConnectionHandler ch = null;
		
		while(true){

			// Contacting the Server , Retry if error
			while(true){
				try{
					localSock = new Socket(serverAddr, serverPort);
					break;
				}catch(Exception e){
					System.out.println("TCP " + " Sleeping 5s");
					Thread.sleep(5000);
				}
			}

			ch = new ConnectionHandler(localSock, objects);
			ch.start();
			
			Scanner input =  new Scanner(System.in);
			
			
			while(ch.isRunning()){
				System.out.print("-> ");
				String msg = input.next();
				System.out.println("Sending: "+ msg);
				BasicDTO dto = new BasicDTO(msg);
				ch.send(dto);
			}

		}

	}
}