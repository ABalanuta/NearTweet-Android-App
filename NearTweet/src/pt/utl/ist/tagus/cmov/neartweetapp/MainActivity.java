package pt.utl.ist.tagus.cmov.neartweetapp;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweet.TweetDetailsActivity;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TypeofDTO;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity implements LocationListener{

	public static final String TAG = MainActivity.class.getSimpleName();
	public static ProgressBar mProgressBar;

	private String mUsername = null;
	private int REQUEST_CODE = 42424242; //Used for Login

	public static Button mSendButton;
	public static EditText mSendTextBox;

	protected final String KEY_TEXT = "texto";
	protected final String KEY_TWEETER = "utilizador";

	private String provider;// location stuff
	private static SharedPreferences mSharedPreferences;
    private int REL_SWIPE_MIN_DISTANCE; 
    private int REL_SWIPE_MAX_OFF_PATH;
    private int REL_SWIPE_THRESHOLD_VELOCITY;

	public static ArrayList<Tweet> mTweetsArray = new ArrayList<Tweet>();
	ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
	ConnectionHandlerTask connectionHandlerTask = null;
	private LocationManager locationManager = null;

	

	// Connection to Service Vriables
	public boolean mBound = false;
	private Intent service;
	public ConnectionHandlerService mService;


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
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

		/**
		 * Location stuff
		 */
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// Check if enabled and if not send user to the GSP settings
		// Better solution would be to display a dialog and suggesting to 
		// go to the settings
		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} 
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		//Initialize the location fields
		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else {
			Toast.makeText(getApplicationContext(), "Localizacao nao disponivel", Toast.LENGTH_LONG).show();
		}

		/*
		 * Defining swipe gestures sensetivity & detection
		 */ 
        DisplayMetrics dm = getResources().getDisplayMetrics();
        REL_SWIPE_MIN_DISTANCE = (int)(120.0f * dm.densityDpi / 160.0f + 0.5); 
        REL_SWIPE_MAX_OFF_PATH = (int)(250.0f * dm.densityDpi / 160.0f + 0.5);
        REL_SWIPE_THRESHOLD_VELOCITY = (int)(200.0f * dm.densityDpi / 160.0f + 0.5);
        
        @SuppressWarnings("deprecation")
		final GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector());
        View.OnTouchListener gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event); 
            }};
        ListView lv = getListView();
        lv.setOnTouchListener(gestureListener);

		
		if (isNetworkAvailable()){
			mProgressBar.setVisibility(View.VISIBLE);
			// Inicia thread que actualiza as messagens
			//connectionHandlerTask = new ConnectionHandlerTask();
			//connectionHandlerTask.execute();
			

			/**
			 * offline dummies
			 */
			Tweet tweetGenerator = new Tweet();
			mTweetsArray = tweetGenerator.generateTweets();
			handleServerResponse();
		}  
		else{
			Toast.makeText(this, "Sem Acesso a Internet", Toast.LENGTH_LONG).show();
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
		mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref",1);
		 if (!mSharedPreferences.contains("username")){

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
		super.onPause();
		// Another activity is taking focus (this activity is about to be "paused").
	}

	@Override
	protected void onDestroy() {
		Log.e("ServiceP", "Killing Main Activity");
		//unbinding from the Service
		//		if(mBound){ unbindService(mConnection); }
		//		connectionHandlerTask.stop();
		//		connectionHandlerTask.cancel(true);
		//		mTweetsArray.removeAll(mTweetsArray);
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Tweet tweet = mTweetsArray.get(position);
		Intent details = new Intent(this,TweetDetailsActivity.class);
		details.putExtra("tweet_id", tweet.getId());
		details.putExtra("tweet_text", tweet.getText());
		details.putExtra("tweet_uid", tweet.getUsername());
		startActivity(details);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	/***************************************************************************************
	 * 
	 * 							 Calls from other activities
	 * 
	 ***************************************************************************************/



	//This method is called when the child activity finishes 
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			mUsername = data.getExtras().getString("username");		
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
		case R.id.new_tweet:
			Intent newTweetIntent = new Intent(this,NewTweetActivity.class);
			//String gps_location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).toString();
			//Toast.makeText(getApplicationContext(), gps_location, Toast.LENGTH_LONG).show();
			//newTweetIntent.putExtra("gps_location",);
			newTweetIntent.putExtra("username", mUsername);
			startActivity(newTweetIntent);
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
		mProgressBar.setVisibility(View.INVISIBLE);
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
			}

			String[] keys = {KEY_TEXT,KEY_TWEETER };
			int[] ids = {android.R.id.text1, android.R.id.text2};
			SimpleAdapter adapter = new SimpleAdapter(this, tweets,
					android.R.layout.simple_list_item_2, keys, ids);
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

			running = true;

			Log.e("ServiceP", "ConnectionHandlerTask Created");
			// Criar um serviço que estabelece a communicação com o server
			service = new Intent(getApplicationContext(), ConnectionHandlerService.class);

			// vamos efectuar uma ligação com o servidor
			bindService(service, mConnection, Context.BIND_AUTO_CREATE);


			// Espera que se ligue ao server
			while(running){
				if(mService != null && mService.isConnected()){
					publishProgress("Connected");
					break;
				}
				else{ try { Thread.sleep(250); } catch (InterruptedException e) { e.printStackTrace(); } }
			}

			boolean loadedOld = false;

			Log.e("ServiceP", "Loop Started");
			while(running){
				if(mService != null){
					ArrayList<BasicDTO> objects;
					if(!loadedOld){
						objects  = mService.receveOldTweets();
						for(BasicDTO oo : objects){
							publishProgress(oo);
						}
						loadedOld = true;

					}else if(mService.hasTweets()){
						Log.e("ServiceP", "Loop Receve");
						objects = mService.receveNewTweets();
						for(BasicDTO oo : objects){
							publishProgress(oo);
						}
					}else{
						try {
							Thread.sleep(250);

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}else{
					try {
						Thread.sleep(250);

					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			return "";
		}


		@Override
		protected void onProgressUpdate(Object... values) {
			if(values[0] instanceof String){
				String updadeCommand = (String)values[0];
				if(updadeCommand.equals("Connected")){
					MainActivity.mProgressBar.setVisibility(View.INVISIBLE);
					Toast.makeText(getApplicationContext(), "Connected ", Toast.LENGTH_LONG).show();
					return;
				}
			}else if(values[0] instanceof BasicDTO){
				onProgressUpdateAux((BasicDTO)values[0]);
			}
		}


		protected void onProgressUpdateAux(BasicDTO dto){

			if(dto.getType().equals(TypeofDTO.TWEET_DTO)){
				TweetDTO t = (TweetDTO) dto;		

				// get tweets from server
				mTweetsArray.add(new Tweet(t.getTweet(),t.getNickName(),t.getSrcMacAddr()));
				ArrayList<HashMap<String,String>> tweets =  new ArrayList<HashMap<String,String>>();

				for (Tweet tweet : mTweetsArray){
					String text = tweet.getText();
					String username = tweet.getUsername();

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
		}
	}
	@Override
	public void onLocationChanged(Location location) {
		int lat = (int) (location.getLatitude());
		int lng = (int) (location.getLongitude());
		Toast.makeText(getApplicationContext(), "latitude: "+ String.valueOf(lat)+ " longitude: "+ String.valueOf(lng), Toast.LENGTH_LONG).show();
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
	
	/*
	 * Gestures to mark tweets as spam
	 */
    class MyGestureDetector extends SimpleOnGestureListener{ 

//        // Detect a single-click and call my own handler.
//        @Override 
//        public boolean onSingleTapUp(MotionEvent e) {
//            ListView lv = getListView();
//            int pos = lv.pointToPosition((int)e.getX(), (int)e.getY());
//            return false;
//        }

        @Override 
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { 
            if (Math.abs(e1.getY() - e2.getY()) > REL_SWIPE_MAX_OFF_PATH) 
                return false; 
            if(e1.getX() - e2.getX() > REL_SWIPE_MIN_DISTANCE && 
                Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) { 
                onRTLFling(); 
            }  else if (e2.getX() - e1.getX() > REL_SWIPE_MIN_DISTANCE && 
                Math.abs(velocityX) > REL_SWIPE_THRESHOLD_VELOCITY) { 
                onLTRFling(); 
            } 
            return false; 
        } 

        private void onLTRFling() {
            Toast.makeText(getApplicationContext(), "Left-to-right fling", Toast.LENGTH_SHORT).show();
        }

        private void onRTLFling() {
            Toast.makeText(getApplicationContext(), "Right-to-left fling", Toast.LENGTH_SHORT).show();
        }
    }
}

