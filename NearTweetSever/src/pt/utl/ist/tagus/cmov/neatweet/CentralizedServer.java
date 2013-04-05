package pt.utl.ist.tagus.cmov.neatweet;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class CentralizedServer{

	private static ArrayList<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
	private static ArrayList<BasicDTO> objects = new  ArrayList<BasicDTO>();
	private static ArrayList<BasicDTO> sentObjects = new  ArrayList<BasicDTO>();

	public static void main(String[] args) {

		ConnectionInicializer conHandler = new ConnectionInicializer(connections, objects, sentObjects);
		SwitchingHandler sHandler = new SwitchingHandler(connections, objects, sentObjects);

		sHandler.start();
		conHandler.start();

	}
}

class ConnectionInicializer extends Thread{

	public static final int SERVERPORT = 4444;
	private boolean running = false;
	private ArrayList<ConnectionHandler> connections = null;
	ArrayList<BasicDTO> objects = null;
	private ServerSocket serverSocket = null;
	private ArrayList<BasicDTO> sentObjects = null;


	public ConnectionInicializer(ArrayList<ConnectionHandler> connections, ArrayList<BasicDTO> objects,
			ArrayList<BasicDTO> sentObjects) {
		this.connections = connections;
		this.objects = objects;
		this.sentObjects = sentObjects;
	}


	@Override
	public void run() {
		super.run();

		this.running = true;

		try {

			serverSocket = new ServerSocket(SERVERPORT);
			System.out.println("S: Listening...");

			while(true){

				// Cria um socket novo por cada pedido
				Socket sock = serverSocket.accept();
				System.out.println("New Client Request");

				ConnectionHandler ch = new ConnectionHandler(sock, objects, connections, sentObjects);
				ch.start();
				synchronized (connections) {
					connections.add(ch);
				}
				
				System.out.println("Listening Again...");
			}

		} catch (Exception e) {
			System.out.println("S: Error");
			e.printStackTrace();
		} finally{
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}

class SwitchingHandler extends Thread{

	private ArrayList<ConnectionHandler> connections = null;
	private ArrayList<BasicDTO> objects = null;
	private ArrayList<BasicDTO> sentObjects = null;
	private boolean running = false;

	public SwitchingHandler(ArrayList<ConnectionHandler> conn, ArrayList<BasicDTO> obj, ArrayList<BasicDTO> sentObjects) {
		this.connections = conn;
		this.objects = obj;
		this.sentObjects = sentObjects;
	}

	public void stopSwiching(){
		this.running = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		super.run();
		this.running = true;

		while(this.running){

			if(objects.size() > 0){

				System.out.println("Sending to " + connections.size() + " Clients");

				ArrayList<BasicDTO> objclone = null;

				synchronized (objects) {
					objclone = (ArrayList<BasicDTO>) objects.clone();
					objects.removeAll(objects);
				}

				for(BasicDTO oo : objclone){

					synchronized (connections) {
						for(ConnectionHandler ch : connections){

							System.out.println("Swiching to " + ch.getId());
							if(ch.isRunning()){
								ch.send(oo);
							}
						}
					}
				}
				
				synchronized (sentObjects) {
					sentObjects.addAll(objclone);
				}
			}

			else{
				try {
					Thread.sleep(500);
					System.out.print(".");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}
}

