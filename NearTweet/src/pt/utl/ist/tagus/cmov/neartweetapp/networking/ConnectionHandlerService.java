package pt.utl.ist.tagus.cmov.neartweetapp.networking;


import java.util.ArrayList;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import android.R.bool;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class ConnectionHandlerService extends Service {

	private ConnectionHandler mConectionHandler = null;
	private final IBinder mBinder = new LocalBinder();
	private ArrayList<BasicDTO> oldTweetsBuff = new ArrayList<BasicDTO>();
	private int Clients = 0;

	@Override
	public void onCreate() {
		super.onCreate();

		this.mConectionHandler = new ConnectionHandler();
		mConectionHandler.start();
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
		return tmp;
	}


	public void sendTweet(BasicDTO tweet){
		if(mConectionHandler != null){
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
}