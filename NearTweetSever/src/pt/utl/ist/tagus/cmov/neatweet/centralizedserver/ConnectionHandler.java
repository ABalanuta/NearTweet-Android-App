package pt.utl.ist.tagus.cmov.neatweet.centralizedserver;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.*;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class ConnectionHandler extends Thread{

	private Socket localSock = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private InputConnectionHandler inc = null;
	private OutConnectionHandler outc = null;
	private boolean running = false;
	private ArrayList<BasicDTO> objects;

	public ConnectionHandler(Socket sock, ArrayList<BasicDTO> objects) {
		this.localSock = sock;
		this.objects = objects;
	}

	public void send(Object oo){

		if(this.running && outc.isRunning()){
			outc.send(oo);
		}
	}

	public void close(){
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isRunning(){
		return this.running;
	}

	@Override
	public void run() {

		this.running = true;

		System.out.println("Thread with Client"+ localSock.getRemoteSocketAddress().toString() + " started.");

		try {
			out = new  ObjectOutputStream(localSock.getOutputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}


		try {
			localSock.getOutputStream().flush();	//Needed to deBlock the inputStream
			in = new  ObjectInputStream(localSock.getInputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}



		inc = new InputConnectionHandler(in, objects);
		inc.start();
		System.out.println("Input Channel Created");



		outc = new OutConnectionHandler(out);
		outc.start();
		System.out.println("Output Channel Created");

		while(this.running){

			try {
				Thread.sleep(250); // Time for the Channels to Connect
				Thread.yield();	
			} catch (InterruptedException e) {
				//Never Happens
			}


			if(!this.outc.isRunning()){
				System.out.println("Out Channel Down, Killing CONNNECTION_HANDELER");
				this.running = false;
				return;
			}

			if(!this.inc.isRunning()){
				System.out.println("In Channel Down, Killing CONNNECTION_HANDELER");
				this.running = false;
				return;
			}


		}

	}

}

class InputConnectionHandler extends Thread{

	private ObjectInputStream in;
	private boolean running = false;
	private ArrayList<BasicDTO> objects= null;

	public InputConnectionHandler(ObjectInputStream in, ArrayList<BasicDTO> objects) {
		this.in = in;
		this.objects = objects;
	}

	public boolean isRunning(){
		return this.running;
	}

	@Override
	public void run() {
		this.running = true;

		while (this.running) {

			try {

				Object oo = in.readObject();
				if(oo != null){
					
					// System.out.println("cenas");
					
					synchronized (objects) {
						objects.add((BasicDTO) oo);	
					}
					
				}else{
					System.out.println("Null Value Receved");
				}
			}

			catch(EOFException e){
				System.out.println("Channel was closed");
				this.running = false;
				return;
			}

			catch(SocketException e){
				System.out.println("Channel is closed");
				this.running = false;
				return;
			}

			catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

		}
	}
}


class OutConnectionHandler extends Thread{

	private ObjectOutputStream out;
	private boolean running = false;

	public OutConnectionHandler(ObjectOutputStream out) {
		this.out = out;
	}

	public boolean isRunning(){
		return this.running;
	}

	public void send(Object oo){
		try {
			if(this.running){
				out.writeObject(oo);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		this.running = true;

		while (this.running) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//Thread.yield();
		}
	}
}


