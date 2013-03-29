package pt.utl.ist.tagus.cmov.neartweetapp.networking;

import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import pt.utl.ist.tagus.cmov.neartweetapp.MainActivity;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TypeofDTO;
import android.os.AsyncTask;

public class ConnectionHandlerTask extends AsyncTask<String,BasicDTO,Void> {

	//private	final static String serverIP = "10.0.2.2";
	private	final static String serverIP = "172.20.81.13";
	private	final static int serverPort = 4444;

	@Override
	protected Void doInBackground(String... message) {


		InetAddress serverAddr = null;
		try {
			serverAddr = InetAddress.getByName(serverIP);
		} catch (UnknownHostException e2) {
			e2.printStackTrace();
		}

		Socket localSock = null;
		ConnectionHandler ch = null;


		// Contacting the Server , Retry if error
		while(true){
			try{
				localSock = new Socket(serverAddr, serverPort);
				break;
			}catch(Exception e){
				System.out.println("TCP " + " Sleeping 5s");
				System.out.println(e.toString());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {}
			}
		}
		
		MainActivity.receveTextBox.append("Connected to server \n");
		MainActivity.connHandler = new ConnectionHandler(localSock);
		MainActivity.connHandler.start();


		while(true){

			if(MainActivity.connHandler.recevedObjects()){
				ArrayList<BasicDTO> objects  = MainActivity.connHandler.receve();
				for(BasicDTO oo : objects){
					publishProgress(oo);
				}
			}else{
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@Override
	protected void onProgressUpdate(BasicDTO... values) {

		if(values[0].getType().equals(TypeofDTO.TWEET_DTO)){
			TweetDTO t = (TweetDTO) values[0];
			MainActivity.receveTextBox.append(t.getTweet()+"\n");
		}

	}
}
