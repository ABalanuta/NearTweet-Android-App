package pt.utl.ist.tagus.cmov.neartweetapp.networking;


import java.util.ArrayList;
import java.util.Random;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.IdentityDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetResponseDTO;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;

public class ConnectionHandlerService extends Service {

	private ConnectionHandler mConectionHandler = null;
	private final IBinder mBinder = new LocalBinder();
	private ArrayList<BasicDTO> oldTweetsBuff = new ArrayList<BasicDTO>();
	private int Clients = 0;
	private String deviceID = null;
	private long tweetID = 0;


	@Override
	public void onCreate() {
		super.onCreate();

		this.mConectionHandler = new ConnectionHandler();
		mConectionHandler.start();
		
		deviceID = Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
		if(deviceID == null){
			deviceID = "BogusID"+(new Random()).nextLong();
		}
		
		while(!this.isConnected()){
			try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }
		}
		
		mConectionHandler.send(new IdentityDTO(deviceID));
		
		Log.e("ServiceP", "MyDeviceID is " + deviceID);
		Log.e("ServiceP", "TCP Service Created");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.e("ServiceP", "TCP Service Started");

		// If we get killed, after returning from here, restart
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.e("ServiceP", "TCP Service Binded, now " + (Clients+1) + " are binded");
		Clients++;
		return mBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e("ServiceP", "TCP Service unBinded, now " + (Clients-1) + " are binded");
		Clients--;
		super.onUnbind(intent);
		if(Clients == 0){
			Log.e("ServiceP", "No Clients Binded to Service Killing Service");
			this.stopSelf();
		}
		return 	true;
	}


	@Override
	public void onDestroy() {

		
		// Mais Eficiente
//		Log.e("ServiceP", "Waiting for Clients for 60s ");
//		
//		int x = 60;
//		
//		while(x > 0){
//			
//			if(Clients > 0){
//				Log.e("ServiceP", "Client Entered, Destroy Aborted");
//			}
//			
//			try {
//				Thread.sleep(1000);
//				x--;
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		
		
		
		Log.e("ServiceP", "TCP Service Destroy");

		this.mConectionHandler.close();
		super.onDestroy();

	}

	public class LocalBinder extends Binder {

		public ConnectionHandlerService getService() {
			// Return this instance of LocalService so clients can call public methods
			return ConnectionHandlerService.this;
		}
	}

	public ArrayList<BasicDTO> receveOldTweets(){		  
		return oldTweetsBuff;
	}

	public ArrayList<BasicDTO> receveNewTweets(){		  
		ArrayList<BasicDTO> tmp = this.mConectionHandler.receve();
		oldTweetsBuff.addAll(tmp);
		
		
		// Actualizar O tweetID
		for(BasicDTO b : tmp){
			if(b instanceof TweetDTO){
				if( ((TweetDTO)b).getTweetID() > this.tweetID){
					this.tweetID = ((TweetDTO) b).getTweetID();
				}
			}
		}
		
		return tmp;
	}


	public void sendTweet(TweetDTO tweet){

		if(mConectionHandler != null){
			tweet.setDeviceID(deviceID);
			tweet.setTweetID(this.tweetID++);
			mConectionHandler.send(tweet);
		}
	}
	
	public void sendResponseTweet(TweetResponseDTO tweet){

		if(mConectionHandler != null){
			tweet.setSrcDeviceID(deviceID);
			mConectionHandler.send(tweet);
		}
	}

	public boolean hasTweets(){
		if(mConectionHandler == null){
			return false;
		}
		return mConectionHandler.recevedObjects();
	}

	public boolean isConnected(){
		if(mConectionHandler == null){
			return false;
		}

		return mConectionHandler.isConnected();
	}
	
	class WaitingToDie extends Thread{
		@Override
		public void run() {
			
		}
	}
	
}