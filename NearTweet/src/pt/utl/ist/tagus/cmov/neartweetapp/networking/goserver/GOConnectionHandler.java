package pt.utl.ist.tagus.cmov.neartweetapp.networking.goserver;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.*;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;


public class GOConnectionHandler extends Thread{

	private Socket localSock = null;
	private ObjectInputStream in = null;
	private ObjectOutputStream out = null;
	private InputConnectionHandler inc = null;
	private OutConnectionHandler outc = null;
	private boolean running = false;
	private ArrayList<BasicDTO> objects = null;
	private ArrayList<GOConnectionHandler> connections = null;
	private ArrayList<BasicDTO> sentObjects = null;
	protected String channelDeviceID = null;

	public GOConnectionHandler(Socket sock, ArrayList<BasicDTO> objects, ArrayList<GOConnectionHandler> connections,
			ArrayList<BasicDTO> sentObjects) {
		this.localSock = sock;
		this.objects = objects;
		this.connections = connections;
		this.sentObjects = sentObjects;
	}

	public void send(Object oo){
		// Out Channel mabe not started yet
		if((this.running && !outc.isRunning()) || channelDeviceID != null){
			int x = 30;

			while(this.running && !outc.isRunning() && x > 0){
				try {
					Thread.sleep(100);
					System.out.print("*");
				} catch (InterruptedException e) {e.printStackTrace();}
			}
			x--;
		}
		
		synchronized (this) {
			outc.send(oo);
		}
	}

	public void close(){
		try { out.close(); } catch (IOException e) { }
	}

	public boolean isRunning(){
		return this.running;
	}

	@Override
	public void run() {
		this.running = true;
		System.out.println("Thread with Client"+ localSock.getRemoteSocketAddress().toString() + " started.");

		try { out = new ObjectOutputStream(localSock.getOutputStream()); } catch (IOException e1) { e1.printStackTrace(); }

		try {
			localSock.getOutputStream().flush();	//Needed to deBlock the inputStream
			in = new  ObjectInputStream(localSock.getInputStream());
		} catch (IOException e1) { e1.printStackTrace(); }

		inc = new InputConnectionHandler(in, objects);
		inc.start();
		System.out.println("Input Channel Created");

		outc = new OutConnectionHandler(out);
		outc.start();
		System.out.println("Output Channel Created");

		// Envia o Historico da Conversa
		synchronized (sentObjects) {
			for(BasicDTO oo : sentObjects){ send(oo); }
		}

		while(this.running){

			try {
				Thread.sleep(250); // Time for the Channels to Connect
				Thread.yield();	
			} catch (InterruptedException e) { /*Never Happens*/ }

			if(!this.outc.isRunning()){
				System.out.println("Out Channel Down, Killing CONNNECTION_HANDELER");
				this.running = false;
				break;
			}

			if(!this.inc.isRunning()){
				System.out.println("In Channel Down, Killing CONNNECTION_HANDELER");
				this.running = false;
				break;
			}
		}

		// If conecction was closed remove this socket reference
		synchronized (connections) {
			this.connections.remove(this);
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

						if(oo instanceof IdentityDTO){
							channelDeviceID = ((IdentityDTO) oo).getSourceDeviceID();
							System.out.println("Receive identity: " + channelDeviceID);
						}

						synchronized (objects) { objects.add((BasicDTO) oo); 


						}
					}else{ System.out.println("Null Value Receved"); }
				} catch(EOFException e){
					System.out.println("Channel was closed");
					this.running = false;
					return;
				} catch(SocketException e){
					System.out.println("Channel is closed");
					this.running = false;
					return;
				} catch (IOException e) { e.printStackTrace();
				} catch (ClassNotFoundException e) { e.printStackTrace(); }
			}
		}
	}


	class OutConnectionHandler extends Thread{

		private ObjectOutputStream out;
		private boolean running = false;

		public OutConnectionHandler(ObjectOutputStream out) { this.out = out; }

		public boolean isRunning(){ return this.running; }

		public void send(Object oo){
			try {
				if(this.running){
					out.writeObject(oo);
					out.flush();
				}
			} catch (IOException e) { e.printStackTrace(); }

		}

		@Override
		public void run() {
			this.running = true;

			while (this.running) {
				try { Thread.sleep(250); } catch (InterruptedException e) { e.printStackTrace(); }
			}
		}
	}
}

