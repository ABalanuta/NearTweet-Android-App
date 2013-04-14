package pt.utl.ist.tagus.cmov.neartweetapp;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executor;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import pt.utl.ist.tagus.cmov.neartweetapp.models.Tweet;
import pt.utl.ist.tagus.cmov.neartweetapp.models.TweetPoll;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.Encoding;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.agimind.widget.SlideHolder;

public class MainActivity extends ListActivity implements LocationListener{

	public static final String TAG = MainActivity.class.getSimpleName();
	public static ProgressBar mProgressBar;

	private String mUsername = null;
	private int REQUEST_CODE = 42424242; //Used for Login
	public CmovPreferences myPreferences;

	private SlideHolder mSlideHolder;
	public static Button mSendButton;
	public static EditText mSendTextBox;

	protected final String KEY_TEXT = "texto";
	protected final String KEY_TWEETER = "utilizador";

	private String provider;// location stuff
	private static SharedPreferences mSharedPreferences;

	private int REL_SWIPE_MIN_DISTANCE; 
	private int REL_SWIPE_MAX_OFF_PATH;
	private int REL_SWIPE_THRESHOLD_VELOCITY;
	public static int lat;
	public static int lng;

	Executor executor = null;
	ConnectionHandlerTask connectionHandlerTask = null;
	private LocationManager locationManager = null;

	public static ArrayList<Tweet> mTweetsArray = new ArrayList<Tweet>();


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
		getActionBar().setHomeButtonEnabled(true);

		mSlideHolder = (SlideHolder) findViewById(R.id.slideHolder);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		mProgressBar.setVisibility(View.VISIBLE);
		myPreferences = new CmovPreferences(getApplicationContext());

		ListView listView = getListView();
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		listView.setMultiChoiceModeListener(new MultiChoiceModeListener() {

			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
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
					Toast.makeText(getApplicationContext(), "Marcado como spam", Toast.LENGTH_LONG).show();
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

		lat = 0;
		lng = 0;

		/**
		 * Location stuff
		 */
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		boolean enabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);

		if (!enabled) {
			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
		} 
		Criteria criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		Location location = locationManager.getLastKnownLocation(provider);

		if (location != null) {
			System.out.println("Provider " + provider + " has been selected.");
			onLocationChanged(location);
		} else {

		}

		/*
		 * Defining swipe gestures sensetivity & detection
		 */ 
		DisplayMetrics dm = getResources().getDisplayMetrics();
		REL_SWIPE_MIN_DISTANCE = (int)(120.0f * dm.densityDpi / 160.0f + 0.5); 
		REL_SWIPE_MAX_OFF_PATH = (int)(250.0f * dm.densityDpi / 160.0f + 0.5);
		REL_SWIPE_THRESHOLD_VELOCITY = (int)(200.0f * dm.densityDpi / 160.0f + 0.5);

		if (isNetworkAvailable()){
			// Inicia thread que actualiza as messagens

			connectionHandlerTask = new ConnectionHandlerTask();
			connectionHandlerTask.execute();

			/**
			 * offline dummies: NAO APAGAR	
			 */
			//Tweet tweetGenerator = new Tweet();
			//mTweetsArray = tweetGenerator.generateTweets();
			//handleServerResponse();
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
			details.putExtra("tweet_uid", tweet.getUsername());
			details.putExtra("tweet_text", tweet.getText());
			startActivity(details);
		}
		else{
			Intent details = new Intent(this,TweetDetailsActivity.class);
			details.putExtra("tweet_id", tweet.getTweetId());
			details.putExtra("tweet_text", tweet.getText());
			details.putExtra("tweet_uid", tweet.getUsername());
			details.putExtra("tweet_deviceID", tweet.getDeviceID());
			details.putExtra("username", tweet.getUsername());
			details.putExtra("tweet", Encoding.encodeTweet(tweet));

			if (tweet.hasCoordenates()){
				details.putExtra("gps_location_lng", "" + tweet.getLNG());
				details.putExtra("gps_location_lat", "" + tweet.getLNG());
				details.putExtra("username", tweet.getUsername());
			}

			if(tweet.hasImage()){
				details.putExtra("tweet_hasImage", true);
				details.putExtra("tweet_image", tweet.getImage());
			}else{
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
		case android.R.id.home:
			mSlideHolder.toggle();
			return true;

		case R.id.new_tweet:
			Intent newTweetIntent = new Intent(this,NewTweetActivity.class);

			newTweetIntent.putExtra("gps_location_lng",""+lng);
			newTweetIntent.putExtra("gps_location_lat",""+lat);
			newTweetIntent.putExtra("username", mUsername);
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
				else{
					try { 
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace(); 
					}
				}
			}


			while(running){
				if(mService != null){
					if(mService.hasUpdates()){
						Log.e("ServiceP", "Loop Receve");
						mTweetsArray = mService.getAllTweets();
						publishProgress("Reload_Screen");
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
					Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
					return;
				}
				else if(updadeCommand.equals("Reload_Screen")){
					onProgressUpdateAux();
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
		lat = (int) (location.getLatitude());
		lng = (int) (location.getLongitude());
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

	/**
	 * Custom adapter to display pretty tweets
	 */
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
			ImageView userImg = (ImageView) itemLayout.findViewById(R.id.imageViewUserPicTweet);

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


			if (myPreferences.hasProfileImgUrl() && myPreferences.hasUserName()){
				String url = myPreferences.getUsername();
				String username = myPreferences.getProfileImgUrl();

				if(tweet.getUsername()!=null){
					Log.v("tweet username", tweet.getUsername());
				}
				if (username!=null){
					Log.v("username", username);
				}
				if (tweet.getUsername()!=null && username!=null){
					if (tweet.getUsername().equals(username)){
						URL newurl;
						try {
							newurl = new URL(url);
							Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream());
							userImg.setImageBitmap(mIcon_val);
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}


			if(tweet instanceof TweetPoll){
				pollImg.setVisibility(ImageView.VISIBLE);
			}
			else{
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
