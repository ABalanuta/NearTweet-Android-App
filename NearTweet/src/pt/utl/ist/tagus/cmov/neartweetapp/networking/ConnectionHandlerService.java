package pt.utl.ist.tagus.cmov.neartweetapp.networking;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import pt.utl.ist.tagus.cmov.neartweetapp.aux.Tweet;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.IdentityDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetResponseDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TypeofDTO;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.util.Log;
import android.widget.SimpleAdapter;

public class ConnectionHandlerService extends Service {

	private ConnectionHandler mConectionHandler = null;
	private final IBinder mBinder = new LocalBinder();
	private int Clients = 0;
	private String deviceID = null;
	private long tweetID = 0;
	private ArrayList<Tweet> mTweetsArray = new ArrayList<Tweet>();
	private boolean hasUpdates = false;
	private SearchingForTweets searcher = null;


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

		searcher = new SearchingForTweets();
		searcher.start();


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

	public boolean isConnected(){
		if(mConectionHandler == null){
			return false;
		}

		return mConectionHandler.isConnected();
	}

	public boolean hasUpdates(){
		synchronized (mTweetsArray) {
			return hasUpdates;
		}
	}

	private void addTweet(Tweet t){
		synchronized (mTweetsArray) {
			mTweetsArray.add(t);
			this.hasUpdates = true;
		}
	}

	private boolean hasTweets(){
		if(mConectionHandler == null){
			return false;
		}
		return mConectionHandler.recevedObjects();
	}

	public ArrayList<Tweet> getAllTweets() {
		synchronized (mTweetsArray) {
			this.hasUpdates = false;
			return (ArrayList<Tweet>) mTweetsArray.clone();
		}
	}

	private ArrayList<BasicDTO> receveNewTweets(){		  
		ArrayList<BasicDTO> tmp = this.mConectionHandler.receve();

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

	private Bitmap decodeImage(byte[] b){
		InputStream is = new ByteArrayInputStream(b);
		return BitmapFactory.decodeStream(is);
	}

	class SearchingForTweets extends Thread{

		private boolean running = false;


		public void kill(){
			this.running = false;
		}




		@Override
		public void run() {

			running = true;

			while(running){


				if(isConnected() && hasTweets()){

					ArrayList<BasicDTO> list = receveNewTweets();

					for(BasicDTO dto : list){

						if(dto.getType().equals(TypeofDTO.TWEET_DTO)){

							TweetDTO t = (TweetDTO) dto;		

							Tweet tweet = new Tweet(t.getTweet(), t.getNickName(), t.getDeviceID(),t.getTweetID());


							if(t.getUserPhoto() != null){
								tweet.setUserImage(decodeImage(t.getUserPhoto()));
							}				

							if(t.getPhoto() != null){
								tweet.setImage(decodeImage(t.getPhoto()));
							}

							if(t.hasCoordenates()){
								tweet.setCoordinates(t.getCoordenates());
							}

							addTweet(tweet);
						}

						// IF response Adds it to the Tweet Object
						else if( dto.getType().equals(TypeofDTO.TWEET_RESP_DTO) ){

							TweetResponseDTO response = (TweetResponseDTO) dto;

							Log.e("ServiceP", "Response Receved");
							synchronized (mTweetsArray) {
								for(Tweet t : mTweetsArray){
									
									Log.e("ServiceP", "->" + response.getDestDeviceID() + " froam " );
									
									if(response.getDestDeviceID().equals(t.getDeviceID())){
										if(response.getDesTweetID() == t.getTweetId()){
											Log.e("ServiceP", "ADDING Response Receved");
											Log.e("ServiceP", "TO " + t.getDeviceID() + " at " + t.getResponses());
											t.addResponse(response);
											break;
										}
									}
								}
							}	

						}
						else{
							Log.e("ServiceP", "There is no Such Type of Tweet : "  + dto.getType());	
						}

					}
				}
				else{
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}
	}

}



//class WaitingToDie extends Thread{
//	@Override
//	public void run() {
//
//	}
//}