package pt.utl.ist.tagus.cmov.neartweetapp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.Executor;

import pt.utl.ist.cmov.neartweet.wifidirect.WifiDirectBroadcastReceiver;
import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.TweetDetailsActivity.ResponseUpdaterTask;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import pt.utl.ist.tagus.cmov.neartweetapp.models.Tweet;
import pt.utl.ist.tagus.cmov.neartweetapp.models.TweetPoll;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.Encoding;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.ActionMode;
import android.view.View.OnClickListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.agimind.widget.SlideHolder;

public class MainActivity extends ListActivity implements LocationListener {


	public static final String TAG = MainActivity.class.getSimpleName();
	public static ProgressBar mProgressBar;
	public static ListView mListView;

	private String mUsername = null;
	private int REQUEST_CODE = 42424242; //Used for Login
	public CmovPreferences myPreferences;

	private SlideHolder mSlideHolder;
	public static Button mSendButton;
	public static Button mBtnLoginTwitter;
	public static EditText mSendTextBox;
	public static ImageView mImageLock;
	public static TextView myUserName;
	public static ImageView userImg;

	protected final String KEY_TEXT = "texto";
	protected final String KEY_TWEETER = "utilizador";

	private String provider;// location stuff
	public Location location;
	public Geocoder geo;
	public static double lat;
	public static double lng;

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
	   // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
    public static RequestToken requestToken;
    public static Twitter twitter;

	private boolean isGO = false;
	private boolean isClient = false;


	Executor executor = null;
	ConnectionHandlerTask connectionHandlerTask = null;
	private LocationManager locationManager = null;

	public static ArrayList<Tweet> mTweetsArray = new ArrayList<Tweet>();


	// Connection to Service Vriables
	public boolean mBound = false;
	private Intent service;
	public ConnectionHandlerService mService;


	private static boolean LOCAL_SERVER__ENVYROMENT = true;


	/***************************************************************************************
	 * 
	 * 							 Activity LifeCycle Methods
	 * 
	 ***************************************************************************************/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("ServiceP", "Created Main Activity");

		setContentView(R.layout.activity_main);
		getActionBar().setHomeButtonEnabled(true);

		// Ligar Serviço
		service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
		//startService(service);
		bindService(service, mConnection, Context.BIND_AUTO_CREATE);
		Log.e("ServiceP", "Binding...");

		mSlideHolder = (SlideHolder) findViewById(R.id.slideHolder);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		mListView = (ListView) findViewById(android.R.id.list);
		mImageLock = (ImageView) findViewById(R.id.imageViewMainLockBan);
		mBtnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
		myPreferences = new CmovPreferences(getApplicationContext());


		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		//if (myPreferences.isUserTwittLoggin()){
		GetImageUpdaterTask rut_twitter_image_location = (GetImageUpdaterTask) new GetImageUpdaterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
		//}

		myUserName = (TextView) findViewById(R.id.textViewUsername);
		myUserName.setText(myPreferences.getUsername());

		Switch toggle_gps = (Switch) findViewById(R.id.switchGps);

		if(myPreferences.isUserTwittLoggin()){
			mBtnLoginTwitter.setVisibility(Button.INVISIBLE);
		}

		if(myPreferences.getShareMyLocation())
			toggle_gps.setChecked(true);
		else 
			toggle_gps.setChecked(false);

		toggle_gps.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked) {
					myPreferences.setShareMyLocationTrue();
					//Toast.makeText(getApplicationContext(), "GPS esta: " + String.valueOf(myPreferences.getShareMyLocation()), Toast.LENGTH_LONG).show();
				} else {
					myPreferences.setShareMyLocationFalse();
					//Toast.makeText(getApplicationContext(), "GPS esta: " + String.valueOf(myPreferences.getShareMyLocation()), Toast.LENGTH_LONG).show();
				}

			}
		});


		Log.e("ServiceP", "1");

		Log.e("ServiceP", "2");

		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {
			int calledPosition = -1;

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				calledPosition = position;
				// Here you can do something when items are selected/de-selected,
				// such as update the title in the CAB
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

				// Respond to clicks on the actions in the CAB
				switch (item.getItemId()) {


				case R.id.share_twitter:
					Toast.makeText(getApplicationContext(), "Partilhado no twitter", Toast.LENGTH_LONG).show();
					mode.finish(); // Action picked, so close the CAB
					return true;


				case R.id.mark_as_spam:
					if(mService != null && mService.isConnected()){
						Tweet t = mTweetsArray.get(calledPosition);
						mService.reportSpammer(t.getDeviceID(), t.getTweetId());
						Toast.makeText(getApplicationContext(), "Marcado como spam", Toast.LENGTH_LONG).show();
					}else{
						Toast.makeText(getApplicationContext(), "Erro na Ligação", Toast.LENGTH_LONG).show();
					}
					mode.finish(); // Action picked, so close the CAB
					return true;


				default:
					return false;
				}


			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Inflate the menu for the CAB
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.main_activity_special_actions, menu);
				return true;
			}
			

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// Here you can make any necessary updates to the activity when
				// the CAB is removed. By default, selected items are deselected/unchecked.
			}

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				// Here you can perform updates to the CAB due to
				// an invalidate() request
				return false;
			}
		});

		Log.e("ServiceP", "3");

		mBtnLoginTwitter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				loginToTwitter();
				mBtnLoginTwitter.setVisibility(Button.INVISIBLE);
			}
		});

		/* Location stuff */
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} 
		location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (location!=null){
			Log.v("location coordinates: ", location.toString());
	
			lat = location.getLatitude();
			lng = location.getLongitude();
	
			geo = new Geocoder(getApplicationContext(), Locale.getDefault());
			try {
				Log.v("location geostuffed: ", geo.getFromLocation(lat, lng, 1).toString());
				Log.v("location amadora: ", geo.getFromLocation(lat, lng, 1).get(0).getSubAdminArea().toString());
				Log.v("location Country: ", geo.getFromLocation(lat, lng, 1).get(0).getCountryName().toString());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else { }

		Log.e("ServiceP", "4 -Assync Task Execute");

		connectionHandlerTask = new ConnectionHandlerTask();
		connectionHandlerTask.execute();


		/**
		 * offline dummies: NAO APAGAR	
		 */
		//Tweet tweetGenerator = new Tweet();
		//mTweetsArray = tweetGenerator.generateTweets();
		//handleServerResponse();
		//}
		//else{
		//Toast.makeText(this, "Sem Acesso a Internet", Toast.LENGTH_LONG).show();
		//}
		/**
		 * Verifies if user is already logedin to twitter
		 * once redirected form the login page
		 */

		if (!myPreferences.isUserTwittLoggin()) {
			
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {

				String verifier = uri
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
				Log.v("twitter login:"," estou na maicActivity a sacar cenas do inetent");
				Log.v("twitter login uri:",uri.toString());
				Log.v("twitter login verifier:",verifier.toString());
				Log.v("twitter login requestToken:",requestToken.toString());
				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);
					Log.v("twitter login accessToken: ",accessToken.toString());

					// Shared Preferences
					//	Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					myPreferences.setTwitOautScrt(accessToken.getTokenSecret());
					myPreferences.setTwitOautTkn(accessToken.getToken());
					Log.v("twitter login oauth secret: ",accessToken.getTokenSecret().toString());
					Log.v("twitter login oauth token: ",accessToken.getToken().toString());
					//e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					//					e.putString(PREF_KEY_OAUTH_SECRET,
					//							accessToken.getTokenSecret());
					// Store login status - true
					//					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					//					e.commit(); // save changes

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

					// Getting user details from twitter
					// For now i am getting his name only
					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					String username = user.getName();


				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.toString());
				}
			}
		}

	}

	@Override
	protected void onResume(){
		/**
		 * Get location updates
		 */
		if (locationManager != null){
			locationManager.requestLocationUpdates(provider, 0, 0, this);
		}

		/**
		 * Get login
		 */
		myPreferences = new CmovPreferences(getApplicationContext());
		//Toast.makeText(getApplicationContext(), String.valueOf((myPreferences.hasUserName())), Toast.LENGTH_LONG).show();
		if (!myPreferences.hasUserName()){

			Intent i = new Intent(getApplicationContext(), LoginActivity.class);			
			startActivityForResult(i, REQUEST_CODE);		
		}

		

		super.onResume();
	}




	@Override
	protected void onPause() {
		Log.e("ServiceP", "Pausing Main Activity");
		super.onPause();
		/*
		 * Pause location updates
		 */
		if (locationManager != null){
			locationManager.removeUpdates(this);
		}


	}

	@Override
	protected void onStop() {
		Log.e("ServiceP", "Stoping Main Activity");
		super.onStop();
		// Another activity is taking focus (this activity is about to be "Stoped").
	}

	@Override
	protected void onDestroy() {
		Log.e("ServiceP", "Killing Main Activity");

		//unbinding from the Service
		// NOTA: nao remover if, utilizado para se destruir a aplicão caso variaveis estejam a null
		if (mConnection != null && connectionHandlerTask!=null){
			if(mBound){ unbindService(mConnection); }
			connectionHandlerTask.stop();
			connectionHandlerTask.cancel(true);
		}
		mTweetsArray.removeAll(mTweetsArray);
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Tweet tweet = mTweetsArray.get(position);

		if(tweet instanceof TweetPoll){
			Intent details = new Intent(this,TweetDetailsPoolActivity.class);
			details.putExtra("username", tweet.getUsername());
			details.putExtra("tweet_text", tweet.getText());
			details.putExtra("tweet", Encoding.encodeTweet(tweet));
			startActivity(details);
		}
		else{
			Intent details = new Intent(this,TweetDetailsActivity.class);
			details.putExtra("tweet_id", tweet.getTweetId());
			details.putExtra("tweet_text", tweet.getText());
			details.putExtra("tweet_uid", tweet.getUsername());
			details.putExtra("tweet_deviceID", tweet.getDeviceID());
			details.putExtra("tweet", Encoding.encodeTweet(tweet));

			//Toast.makeText(getApplicationContext(), "BANANAS " +tweet.getLAT(), Toast.LENGTH_LONG).show();
			if (tweet.hasCoordenates()){
				//Toast.makeText(getApplicationContext(), "I HAVE COORDINATES!", Toast.LENGTH_LONG).show();
				details.putExtra("gps_location_lng", "" + tweet.getLNG());
				details.putExtra("gps_location_lat", "" + tweet.getLAT());
				details.putExtra("username", tweet.getUsername());
			}

			if(tweet.hasImage()){
				//Toast.makeText(getApplicationContext(), "I've IMAGE", Toast.LENGTH_LONG).show();
				details.putExtra("tweet_hasImage", true);
				details.putExtra("tweet_image", tweet.getImage());
			}else{
				//Toast.makeText(getApplicationContext(), "I've  DONT IMAGE", Toast.LENGTH_LONG).show();
				details.putExtra("tweet_hasImage", false);
			}

			//Toast.makeText(getApplicationContext(), tweet.getLNG() + " " +tweet.getLNG(), Toast.LENGTH_LONG).show();
			startActivity(details);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	// Method call to find peers :)
	public void findPeers(){
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
			@Override
			public void onSuccess() {
				//If the discovery process succeeds and detects peers, the system broadcasts
				//the WIFI_P2P_PEERS_CHANGED_ACTION intent, which you can listen for in a 
				//broadcast receiver to obtain a list of peers.
			}

			@Override
			public void onFailure(int reasonCode) {
				//...
			}
		});
	}


	// Este método é chamado quando recebemos um connect e tem a informação do peer que se ligou a nós =)
	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {

		Log.e("ServiceP", "Info Receved");
		Log.e("ServiceP", "GO IP is " + info.groupOwnerAddress.getHostAddress());


		//if Server
		if(info.isGroupOwner){

			// verifica se se já era server
			if(this.mService.getGOStatus() == false){
				this.mService.setGOStatus(true);

				Log.e("ServiceP", "I am Server");
				Toast t = Toast.makeText(this, "You are SERVER", Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				t.show();

				this.mService.StartGOServer();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}

				this.mService.startClient(info.groupOwnerAddress.getHostAddress());
			}
		}

		// is Client
		else{
			// verifica se já era cliente
			// caso não estabelece uma coneccao
			if(this.mService.getClientStatus() == false){
				this.mService.setClientStatus(true);

				//mService.cleanOldTweets();

				Log.e("ServiceP", "I am Client");
				Toast t = Toast.makeText(this, "You are CLIENT", Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				t.show();

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}
				this.mService.startClient(info.groupOwnerAddress.getHostAddress());


				// Caso falha da ligaçcao/Servidor 
			}else if(!this.mService.isConnected()){
				Log.e("ServiceP", "Server Probabily Failed");
				Toast t = Toast.makeText(this, "Server Recovered", Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				t.show();

				this.mService.startClient(info.groupOwnerAddress.getHostAddress());

			}
		}


	}

	class GetImageUpdaterTask extends AsyncTask<String, String, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		protected String doInBackground(String... args) {
			return  myPreferences.getProfilePictureLocation();

		}

		protected void onPostExecute(String image_location) {
			userImg = (ImageView) findViewById(R.id.imageViewMeSettings);
			BitmapDrawable d = new BitmapDrawable(getResources(), image_location);
			userImg.setImageDrawable(d);
		}

	}




	/***************************************************************************************
	 * 
	 * 							 Calls from other activities
	 * 
	 ***************************************************************************************/



	//This method is called when the child activity finishes 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v("twitter login:"," estou no onActivity result ");
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			mUsername = data.getExtras().getString("username");		
			myUserName.setText(myPreferences.getUsername());
		}
	} 




	/***************************************************************************************
	 * 
	 * 							 Network Methods
	 * 
	 ***************************************************************************************/




	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case android.R.id.home:
			mSlideHolder.toggle();
			return true;

		case R.id.new_tweet:
			Intent newTweetIntent = new Intent(this,NewTweetActivity.class);

			if(location!=null){
				newTweetIntent.putExtra("gps_location_lng", ((Double)lng).toString());
				newTweetIntent.putExtra("gps_location_lat",((Double)lat).toString());
			}
			String subadminarea = new String();
			if(location!=null){
				try {
					if(geo.getFromLocation(lat, lng, 1).get(0).getSubAdminArea()!=null){
						try {
							subadminarea = geo.getFromLocation(lat, lng, 1).get(0).getSubAdminArea().toString();
							newTweetIntent.putExtra("location", subadminarea);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			//Toast.makeText(getApplicationContext(), "LAT: " + lat + " LNG: " + lng, Toast.LENGTH_LONG).show();

			newTweetIntent.putExtra("username", mUsername);
			newTweetIntent.putExtra("share_location", myPreferences.getShareMyLocation());
			startActivity(newTweetIntent);
			return true;
		case R.id.new_tweet_pool:
			Intent newTweetPoolIntent = new Intent(this,NewTweetPoolActivity.class);
			startActivity(newTweetPoolIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private boolean isNetworkAvailable() {
		ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = manager.getActiveNetworkInfo();
		boolean isAvaylable = false;
		if (networkInfo != null && networkInfo.isConnected()){
			isAvaylable = true;
		}
		return isAvaylable;
	}



	private void updateDisplayForError() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("erro");
		builder.setMessage("erro");
		builder.setPositiveButton(android.R.string.ok, null);
		AlertDialog dialog = builder.create();
		dialog.show();
		TextView emptyTextView = (TextView) getListView().getEmptyView();
		emptyTextView.setText("nao ha tweeets");
	} 

	public void handleServerResponse() {
		if (mTweetsArray == null){
			updateDisplayForError();
		}
		else {
			ArrayList<HashMap<String,String>> tweets = 
					new ArrayList<HashMap<String,String>>();

			for (Tweet tweet : mTweetsArray){
				String text = tweet.getText();
				String userId = tweet.getUsername();

				HashMap<String,String> tweetInterface = new HashMap<String,String>();
				tweetInterface.put(KEY_TEXT,text);
				tweetInterface.put(KEY_TWEETER,userId);
				tweets.add(tweetInterface);


				//---------------------------------------
				//AQUI TEMOS DE POR LÁ TAMBEM AS COORDENADAS SE NÃO, NÃO
				//HÁ GPS PARA NINGUÉM)

				//---------------------------------------
			}

			TweetAdapter adapter = new TweetAdapter(mTweetsArray,this);
			setListAdapter(adapter);
		}
	}

	//Não Mexer
	/** Defines callbacks for service binding, passed to bindService() */
	public ServiceConnection mConnection = new ServiceConnection() {


		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	////////////////////
	//
	//		AsyncTasks
	//
	//


	class ConnectionHandlerTask extends AsyncTask<String,Object,String> {

		protected final String KEY_TEXT = "texto";
		protected final String KEY_TWEETER = "utilizador";
		ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
		private boolean running = false;

		public void stop(){
			running = false;
		}

		@Override
		protected String doInBackground(String... message) {

			Log.e("ServiceP", "do 1");

			running = true;
			mProgressBar.setVisibility(View.VISIBLE);

			// LIgar Serviço
			service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
			bindService(service, mConnection, Context.BIND_AUTO_CREATE);
			Log.e("ServiceP", "do 2");
			Thread.yield();

			Log.e("ServiceP", "Assync Started");
			publishProgress("Waiting...");


			// For local Server Testing
			while(running){
				if(mService != null){
					if(MainActivity.LOCAL_SERVER__ENVYROMENT){

						if(mService.getGOStatus() == false){
							//Start Server
							mService.StartGOServer();
							mService.setGOStatus(true);
						}

						if(mService.getClientStatus() == false){
							//Start Client
							mService.startClient("localhost");
							mService.setClientStatus(true);
						}
					}
					break;
				}
				else{
					try { Thread.sleep(250); } catch (InterruptedException e) {}
				}
			}




			// Espera que se ligue ao server
			while(running){
				if(mService != null && mService.isConnected()){
					publishProgress("Connected");
					break;
				}
				else{
					try { Thread.sleep(250); } catch (InterruptedException e) {}
				}
			}
			Log.e("ServiceP", "do 3");
			Log.e("ServiceP", "Bound with Service");

			while(running){
				if(mService != null){
					// Apagado por tufa � bue verboso
					//Log.e("ServiceP", "Loop Receve1");
					if(mService.hasUpdates()){
						// Apagado por tufa � bue verboso
						//	Log.e("ServiceP", "Loop Receve2");
						mTweetsArray = mService.getAllTweets();
						mService.setNoUpdates();

						for(Tweet t : mTweetsArray){
							if(t.isBanned()){
								publishProgress("Ban_Me");
								break;
							}
						}
						publishProgress("Reload_Screen");
					}else{
						try { Thread.sleep(2000); } catch (InterruptedException e) {}
					}
				}else{
					try { Thread.sleep(2000); } catch (InterruptedException e) {}
				}
			}
			return "";
		}


		@Override
		protected void onProgressUpdate(Object... values) {

			if(values[0] instanceof String){
				String updadeCommand = (String)values[0];

				if(updadeCommand.equals("Connected")){

					Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
					mProgressBar.setVisibility(View.INVISIBLE);
					return;
				}
				else if(updadeCommand.equals("Waiting...")){

					Toast.makeText(getApplicationContext(), "Waiting Connections", Toast.LENGTH_LONG).show();
					return;
				}
				else if(updadeCommand.equals("Reload_Screen")){
					onProgressUpdateAux();
				}else if(updadeCommand.equals("Ban_Me")){
					this.running = false;
					MainActivity.mListView.setVisibility(View.INVISIBLE);
					MainActivity.mImageLock.setVisibility(View.VISIBLE);
					getActionBar().setHomeButtonEnabled(false);
					Toast.makeText(getApplicationContext(), "You Have Been Banned!!!", Toast.LENGTH_LONG).show();
				}
			}
		}


	}

	public void onProgressUpdateAux(){

		// Parte Grafica
		ArrayList<HashMap<String,String>> tweets =  new ArrayList<HashMap<String,String>>();

		for (Tweet tr : mTweetsArray){
			String text = tr.getText();
			String username = tr.getUsername();
			HashMap<String,String> tweetInterface = new HashMap<String,String>();
			tweetInterface.put(KEY_TEXT,text);
			tweetInterface.put(KEY_TWEETER,username);
			tweets.add(tweetInterface);
		}

		String[] keys = {KEY_TEXT,KEY_TWEETER };
		int[] ids = {android.R.id.text1, android.R.id.text2};
		SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), tweets,
				android.R.layout.simple_list_item_2, keys, ids);
		setListAdapter(adapter);
		handleServerResponse();
	}




	@Override
	public void onLocationChanged(Location location) {
		if(location!=null){
			lat = (double) (location.getLatitude());
			lng = (double) (location.getLongitude());
		}
		//Toast.makeText(getApplicationContext(), "latitude: "+ String.valueOf(lat)+ " longitude: "+ String.valueOf(lng), Toast.LENGTH_LONG).show();
	}


	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		Toast.makeText(this, "Enabled new provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		Toast.makeText(this, "Disabled provider " + provider,
				Toast.LENGTH_SHORT).show();

	}

	/**
	 * Custom adapter to display pretty tweets
	 */

	private void loginToTwitter(){
		if(!myPreferences.isUserTwittLoggin()){
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(myPreferences.getConsumerKey());
			builder.setOAuthConsumerSecret(myPreferences.getConsumerSecret());
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				Log.v("twitter login: request token",requestToken.toString());
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) { e.printStackTrace(); }
		}
	}

	private class TweetAdapter extends BaseAdapter {

		private ArrayList<Tweet> mTweets = new ArrayList<Tweet>();
		private Context mContext;


		public TweetAdapter(ArrayList<Tweet> tweets, Context context) {
			mTweets = tweets;
			mContext = context;

		}


		@Override
		public int getCount() {
			return mTweets.size();
		}

		@Override
		public Object getItem(int position) {	
			return mTweets.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LinearLayout itemLayout;

			Tweet tweet = mTweets.get(position);

			itemLayout= (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.custom_tweet, parent, false);

			TextView tweetText = (TextView) itemLayout.findViewById(R.id.tweet);
			TextView tweetUsername = (TextView) itemLayout.findViewById(R.id.username);
			ImageView pollImg = (ImageView) itemLayout.findViewById(R.id.imagePool);
			ImageView gpsImg = (ImageView) itemLayout.findViewById(R.id.imageGps);
			ImageView imgImg = (ImageView) itemLayout.findViewById(R.id.imageImage);
			ImageView twitUserImg = (ImageView) itemLayout.findViewById(R.id.imageViewUserPicTweet);

			gpsImg.setVisibility(ImageView.INVISIBLE);
			pollImg.setVisibility(ImageView.INVISIBLE);
			imgImg.setVisibility(ImageView.INVISIBLE);


			tweetText.setText(tweet.getText());
			tweetUsername.setText("@" + tweet.getUsername());

			/**
			 * Lets You access internet on the interface thread
			 */
			if (android.os.Build.VERSION.SDK_INT > 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}


			//TODO add images
			Log.v("twitter login: ",String.valueOf(myPreferences.isUserTwittLoggin()));
			Log.v("my user name: ", String.valueOf(myPreferences.hasUserName()));
			if (myPreferences.isUserTwittLoggin() && myPreferences.hasUserName()){

				Log.v("tweet_username: ", tweet.getUsername());
				Log.v("user_username: ", myPreferences.getUsername());
				if (tweet.getUsername()!=null && myPreferences.getUsername() != null){
					if (tweet.getUsername().equals(myPreferences.getUsername())){
						String picture_location = myPreferences.getProfilePictureLocation();
						BitmapDrawable d = new BitmapDrawable(getResources(), picture_location);
						twitUserImg.setImageDrawable(d);
					}
				}
			}


			if(tweet instanceof TweetPoll){
				pollImg.setVisibility(ImageView.VISIBLE);
			}
			else{
				//TODO tweets nao teem coordenadas apesar de la serem enfiadas
				//				Log.e("tweet coordenate details lat:","lat: " + tweet.getLAT().toString());
				//				Log.e("tweet coordenate details lng:","long: " + tweet.getLNG().toString());
				//				Log.e("tweet coordenate details:", String.valueOf(tweet.hasCoordenates()));
				if (tweet.hasCoordenates()){

					gpsImg.setVisibility(ImageView.VISIBLE);
				}
				if (tweet.hasImage()){
					imgImg.setVisibility(ImageView.VISIBLE);
				}
			}
			return itemLayout;
		}


	}
}
