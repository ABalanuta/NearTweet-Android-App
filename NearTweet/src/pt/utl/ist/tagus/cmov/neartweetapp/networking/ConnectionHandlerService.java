package pt.utl.ist.tagus.cmov.neartweetapp.networking;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import pt.utl.ist.tagus.cmov.neartweetapp.models.Tweet;
import pt.utl.ist.tagus.cmov.neartweetapp.models.TweetPoll;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.goserver.GOServer;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.IdentityDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.PollDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.PollResponseDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.SpammDetectorDTO;
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

public class ConnectionHandlerService extends Service {

	private ConnectionHandler mConectionHandler = null;
	private GOServer mGOServer= null;
	private final IBinder mBinder = new LocalBinder();
	private int Clients = 0;
	public static String deviceID = null;
	private long tweetID = 0;
	private ArrayList<Tweet> mTweetsArray = new ArrayList<Tweet>();
	private boolean hasPostUpdates = false;
	private boolean hasResponseUpdates = false;
	private SearchingForTweets searcher = null;
	private ServiceKiller killer = null;


	@Override
	public void onCreate() {
		super.onCreate();

		this.mConectionHandler = new ConnectionHandler(this);
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
		Log.e("ServiceP", "TCP Service Binded, now " + (Clients+1) + " are binded################################");
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

			if(killer == null){
				killer = new ServiceKiller();
				killer.start();
			} 

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



	//LOCAL METHODS
	//------------------------------------------------------------------------------------


	public void StartGOServer(){
		this.mGOServer = new GOServer();
		this.mGOServer.start();


		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		this.mConectionHandler = new ConnectionHandler(this);
		mConectionHandler.setServerIP("localhost");
		mConectionHandler.start();

	}




	public void reportSpammer(String destDeviceID, long tweetID){
		if(mConectionHandler != null){
			mConectionHandler.send(new SpammDetectorDTO(deviceID, destDeviceID, tweetID));
		}else{
			Log.e("ServiceP", "Channel is Closed");
		}
	}


	public void sendTweet(TweetDTO tweet){

		if(mConectionHandler != null){
			tweet.setDeviceID(deviceID);
			tweet.setTweetID(++this.tweetID);
			mConectionHandler.send(tweet);
		}else{
			Log.e("ServiceP", "Channel is Closed");
		}
	}


	public void sendPoll(PollDTO poll) {
		if(mConectionHandler != null){
			poll.setSrcDeviceID(deviceID);
			poll.setTweetID(++this.tweetID);
			mConectionHandler.send(poll);
			Log.e("ServiceP", "PollSent");
		}else{
			Log.e("ServiceP", "Channel is Closed");
		}
	}


	public void sendResponseTweet(TweetResponseDTO tweet){

		if(mConectionHandler != null){
			tweet.setSrcDeviceID(deviceID);
			mConectionHandler.send(tweet);
		}else{
			Log.e("ServiceP", "Channel is Closed");
		}
	}

	public void sendResponsePoll(PollResponseDTO response){

		if(mConectionHandler != null){
			response.setSrcDeviceID(deviceID);
			Log.e("ServiceP", "::::-> " + response);

			mConectionHandler.send(response);
		}else{
			Log.e("ServiceP", "Channel is Closed");
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
			return hasPostUpdates;
		}
	}

	public boolean hasResponseUpdates(String srcDeviceID, long tweetID2){
		ArrayList<Tweet> work = null;
		synchronized (mTweetsArray) {
			work = (ArrayList<Tweet>) mTweetsArray.clone();
		}
		for(Tweet t : work){
			if(t.getDeviceID().equals(srcDeviceID)){
				if(t.getTweetId() == tweetID2){
					return t.hasNewResponses();
				}
			}
		}
		return false;
	}

	public boolean hasResponsePollUpdates(String DeviceID, long tweetID){
		ArrayList<Tweet> work = null;
		synchronized (mTweetsArray) {
			work = (ArrayList<Tweet>) mTweetsArray.clone();
		}
		for(Tweet t : work){
			if(t instanceof TweetPoll){
				if(t.getDeviceID().equals(deviceID)){
					if(t.getTweetId() == tweetID){
						return ((TweetPoll) t).hasPollUpdates();
					}
				}
			}
		}
		return false;
	}


	private void addTweet(Tweet t){
		synchronized (mTweetsArray) {
			mTweetsArray.add(t);		
			this.hasPostUpdates = true;
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

			return (ArrayList<Tweet>) mTweetsArray.clone();
		}
	}

	public void setNoUpdates() {
		this.hasPostUpdates = false;
	}


	public ArrayList<TweetResponseDTO> getAllResponses(String srcDeviceID, long tweetID2){

		ArrayList<Tweet> work = null;
		synchronized (mTweetsArray) {
			work = (ArrayList<Tweet>) mTweetsArray.clone();
		}
		for(Tweet t : work){
			if(t.getDeviceID().equals(srcDeviceID)){
				if(t.getTweetId() == tweetID2){
					//Log.e("ServiceP", "deatails for "+srcDeviceID+":"+tweetID2+" Are: "+t.toString());
					return t.getResponses();
				}
			}
		}
		return null;
	}

	public ArrayList<PollResponseDTO> getAllPollResponses(String srcDeviceID, long tweetID2){

		Log.e("ServiceP", "Get my all for : "+ srcDeviceID + " with id " + tweetID2);


		synchronized (mTweetsArray) {

			for(Tweet t : (ArrayList<Tweet>) mTweetsArray.clone()){

				if(t instanceof TweetPoll){
					Log.e("ServiceP", "Got  : "+ t.getDeviceID() +" with id " + t.getTweetId());
					if(t.getDeviceID().equals(srcDeviceID)){
						if(t.getTweetId() == tweetID2){
							Log.e("ServiceP", "fILTRED  : "+ t.getDeviceID() +" with id " + t.getTweetId());
							return ((TweetPoll) t).getAllResponses();
						}
					}
				}
			}
		}
		return null;
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

						Log.e("ServiceP", "R->> "+ dto.toString());

						if(dto.getType().equals(TypeofDTO.TWEET_DTO)){

							TweetDTO t = (TweetDTO) dto;		
							Log.e("ServiceP", "TweetDTO is " + t);

							Tweet tweet = new Tweet(t.getTweet(), t.getNickName(), t.getDeviceID(),t.getTweetID());


							if(t.getUserPhoto() != null){
								tweet.setUserImage(t.getUserPhoto());
							}				

							if(t.getPhoto() != null){
								tweet.setImage(t.getPhoto());
							}

							if(t.hasCoordenates()){
								tweet.setLAT(t.getLAT());
								tweet.setLNG(t.getLNG());
								//tweet.setCoordinates(t.getCoordenates());
							}

							addTweet(tweet);
						}

						// IF response Adds it to the Tweet Object
						else if( dto.getType().equals(TypeofDTO.TWEET_RESP_DTO) ){

							TweetResponseDTO response = (TweetResponseDTO) dto;



							// Filter private Responses not generated by me 
							if(response.isPrivate() && !response.getSrcDeviceID().equals(deviceID)){

								if(!response.getDestDeviceID().equals(deviceID)){
									Log.e("ServiceP", "Filtering Message Not For ME");
									continue;
								}
							}

							synchronized (mTweetsArray) {
								for(Tweet t : mTweetsArray){

									if(response.getDestDeviceID().equals(t.getDeviceID())){
										if(response.getDesTweetID() == t.getTweetId()){
											t.addResponse(response);
											hasResponseUpdates = true;
											break;
										}
									}
								}
							}	

						}


						else if( dto.getType().equals(TypeofDTO.IDENTITY_DTO)){	
						}



						else if( dto.getType().equals(TypeofDTO.SPAMM_DTO)){

							SpammDetectorDTO spam = (SpammDetectorDTO) dto;

							if(spam.getDestDeviceID().equals(deviceID)){

								synchronized (mTweetsArray) {
									for(Tweet t : mTweetsArray){

										if(spam.getTweetID() == t.getTweetId()){
											t.addReporter(spam.getSrcDeviceID());
											hasPostUpdates = true;
											break;
										}
									}
								}	
							}
						}

						else if( dto.getType().equals(TypeofDTO.POLL_DTO)){

							Log.e("ServiceP", "PollReceved " + (PollDTO) dto);

							PollDTO p = (PollDTO) dto;

							TweetPoll poll = new TweetPoll(p.getQuestion(), p.getNickName(), p.getSrcDeviceID(), p.getTweetID());
							poll.setOptions(p.getOptions());
							addTweet(poll);
						}

						else if( dto.getType().equals(TypeofDTO.POLL_RESPONSE_DTO)){



							PollResponseDTO rsp = (PollResponseDTO) dto;

							synchronized (mTweetsArray) {

								for(Tweet t : mTweetsArray){

									if(t.getDeviceID().equals(rsp.getDesDeviceID())){
										if(rsp.getTweetID() == t.getTweetId()){

											((TweetPoll) t).addResponse(rsp);
											Log.e("ServiceP", "PollAded");
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

	class ServiceKiller extends Thread{
		
		@Override
		public void run() {
			int x = 120;
			if (mGOServer != null){
				x = 360;
			}
			
			Log.e("ServiceP", "Waiting for Clients for "+x+"s ");
			
			while(x > 0){

				if(Clients > 0){
					Log.e("ServiceP", "Client Entered, Destroy Aborted");
					return;
					
				}

				try {
					if(x%10 == 0){
						Log.e("ServiceP", x+"Seconds until Service Destroy");
					}
					
					
					Thread.sleep(1000);
					x--;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			onDestroy();
		}
	}
	
}



