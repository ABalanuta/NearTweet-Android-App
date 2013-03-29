package pt.utl.ist.tagus.cmov.neatweet.centralizedserver;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class CentralizedServer{

	private static ArrayList<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
	private static ArrayList<BasicDTO> objects = new  ArrayList<BasicDTO>();

	public static void main(String[] args) {

		ConnectionInicializer conHandler = new ConnectionInicializer(connections, objects);
		SwichingHandler sHandler = new SwichingHandler(connections, objects);

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


	public ConnectionInicializer(ArrayList<ConnectionHandler> connections, ArrayList<BasicDTO> objects) {
		this.connections = connections;
		this.objects = objects;
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

				ConnectionHandler ch = new ConnectionHandler(sock, objects);
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

class SwichingHandler extends Thread{

	private ArrayList<ConnectionHandler> connections = null;
	private ArrayList<BasicDTO> objects = null;
	private boolean running = false;

	public SwichingHandler(ArrayList<ConnectionHandler> conn, ArrayList<BasicDTO> obj) {
		this.connections = conn;
		this.objects = obj;
	}

	public void stopSwiching(){
		this.running = false;
	}

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

					for(ConnectionHandler ch : connections){

						System.out.println("Swiching to " + ch.getId());

						if(ch.isRunning()){
							ch.send(oo);
						}
					}
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

