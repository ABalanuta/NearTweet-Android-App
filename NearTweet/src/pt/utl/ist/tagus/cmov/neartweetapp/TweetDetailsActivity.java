package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.maps.BasicMapActivity;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import pt.utl.ist.tagus.cmov.neartweetapp.models.Comment;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CommentCustomAdapter;
import pt.utl.ist.tagus.cmov.neartweetapp.models.Tweet;
import pt.utl.ist.tagus.cmov.neartweetapp.models.TweetPoll;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.Encoding;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetResponseDTO;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TweetDetailsActivity extends ListActivity {


	public static TextView txtTweet;
	public static TextView txtUserName;
	public static Button btnShareTwitter;
	public static EditText textBox;
	public static Button btnSendReply;
	public static ListView lstVwComments;
	public static TextView txtLat;
	public static TextView txtLong;
	public static ImageView image;
	public static ImageView userImage;

	ProgressDialog pDialog;
	private String TWITTER_CONSUMER_KEY = "20o4JfRtmLAQ9v1HpwwHKw";
	private String TWITTER_CONSUMER_SECRET = "pmLgr4ozXj2Dw8HBk3sqHykuOwAf0mDrjed4fzlkc";

	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	public String tweet_text;

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample_details";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	private static Twitter twitter;
	private static RequestToken requestToken;
	private CmovPreferences myPreferences;
	private ResponseUpdaterTask rut = null;
	private UpdateTwitterStatus rut2 = null;

	// Connection to Service Variables
	public boolean mBound = false;
	private Intent service;
	private ConnectionHandlerService mService;
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};

	private Tweet tweet;
	public static ArrayList<Comment> comments = new ArrayList<Comment>();
	public static  ListView listView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_details);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// Defines the Header and LIstContent
		View header = getLayoutInflater().inflate(R.layout.activity_tweets_details_aux, null);
		listView = getListView();
		listView.addHeaderView(header);
		listView.setAdapter(new CommentCustomAdapter(this, android.R.layout.simple_list_item_1, comments));

		myPreferences = new CmovPreferences(getApplicationContext());

		txtTweet = (TextView) findViewById(R.id.tweet_text);
		txtUserName = (TextView) findViewById(R.id.user_name);
		btnShareTwitter = (Button) findViewById(R.id.share_twitter);
		//textBox = (EditText) findViewById(R.id.editText1);
		//btnSendReply = (Button) findViewById(R.id.send_reply);
		//lstVwComments = (ListView) findViewById(R.id.listViewComments);
		txtLat = (TextView) findViewById(R.id.textViewCoordinateLat);
		txtLong = (TextView) findViewById(R.id.textViewCoordinateLong);
		image = (ImageView) findViewById(R.id.imageViewTweetImage);
		userImage = (ImageView) findViewById(R.id.imageViewUserPicTweet);
		

		Bundle bundle = getIntent().getExtras();

		final String tweet_uid = bundle.getString("tweet_uid");
		// For Demo purposes
		final String location_lng = bundle.getString("gps_location_lng");
		final String location_lat = bundle.getString("gps_location_lat");
		final String area = bundle.getString("location");
		//Log.v("location lat:tweetdet: ",location_lat);
		//Log.v("location lng:tweetdet: ",location_lng);
		//Log.v("location area:tweetdet: ",area);
		//Toast.makeText(getApplicationContext(), "DO I HAVE LAT AND LNG " + location_lat + location_lng, Toast.LENGTH_LONG).show();	
		//final String location_lng = "-9.302851";
		//final String location_lat = "38.7371";
		// ----------		


		final String tweet_deviceID = bundle.getString("tweet_deviceID");
		final long tweet_ID = bundle.getLong("tweet_id");
		tweet_text = bundle.getString("tweet_text");
		final String tweet_text_to_map = tweet_text; // it had to be final to pass on clicl listener

		if(bundle.getByteArray("tweet") == null){
			finish();
		}else{ 
			tweet = Encoding.decodeTweet(bundle.getByteArray("tweet"));
		}

		// If Existes Insrt Image
		if(bundle.getBoolean("tweet_hasImage")){
			image.setImageBitmap(Encoding.decodeImage(bundle.getByteArray("tweet_image")));
			image.setVisibility(View.VISIBLE);
		}

		// If Existes Insert LOcation
		if (location_lat!=null || location_lng!=null){
			txtLat.setText("Lat: " +  location_lat);
			txtLong.setText("Long: " + location_lng);
		}
		txtTweet.setText(tweet_text);
		txtUserName.setText("@ " + tweet_uid);

		txtLat.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {

				Intent map = new Intent(getApplicationContext(),pt.utl.ist.tagus.cmov.neartweetapp.maps.BasicMapActivity.class);
				map.putExtra("gps_location_lat", location_lat);
				map.putExtra("gps_location_lng", location_lng);
				map.putExtra("tweet_text", tweet_text_to_map);
				startActivity(map);
				return false;
			}
		});

		//OFFLINE mudado para o onresume
		//rut = (ResponseUpdaterTask) new ResponseUpdaterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
		//rut.execute();

		//		// Send Reply
		//		btnSendReply.setOnClickListener(new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//
		//
		//				if(mBound && mService.isConnected()){
		//
		//					TweetResponseDTO r = new TweetResponseDTO(tweet_uid, textBox.getText().toString(),
		//							tweet_deviceID, tweet_ID, false);
		//					mService.sendResponseTweet(r);
		//					Toast.makeText(getApplicationContext(), " SENT ", Toast.LENGTH_SHORT).show();
		//				}else{
		//					Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_LONG).show();
		//				}
		//
		//			}
		//		});


		/**
		 * Verifies if user is already logedin to twitter
		 * once redirected form the login page
		 */

		if (!isTwitterLoggedInAlready()) {
			Uri uri = getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				String verifier = uri
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);
				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);

					// Shared Preferences
					//	Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					myPreferences.setTwitOautScrt(accessToken.getTokenSecret());
					myPreferences.setTwitOautTkn(accessToken.getToken());
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
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}
			}
		}
		/**
		 * Lets You access internet on the interface thread
		 */
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

	}
	@Override
	protected void onResume(){
		super.onResume();
		rut = (ResponseUpdaterTask) new ResponseUpdaterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
	}

	
//	@Override
//	protected void onPause(){
//		super.onPause();
//		Log.e("ServiceP", "Killing Details Activity");		
//		rut.kill(); // Stops the assync thread gently the kills it 
//		try {Thread.sleep(25);} catch (InterruptedException e) {}
//		rut.cancel(true);
//
//		//unbinding from the Service
//		if(mBound){ unbindService(mConnection); }
//
//		comments = new ArrayList<Comment>();
//	}
	
	@Override
	protected void onDestroy() {
		Log.e("ServiceP", "Killing Details Activity");
		if (rut!=null){
			rut.kill(); // Stops the assync thread gently the kills it 
			try {Thread.sleep(25);} catch (InterruptedException e) {}
			rut.cancel(true);
		}
		//unbinding from the Service
		if(mBound){ unbindService(mConnection); }

		comments = new ArrayList<Comment>();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		if(myPreferences.isUserTwittLoggin()){
			getMenuInflater().inflate(R.menu.tweet_details, menu);
		}
		else{
			getMenuInflater().inflate(R.menu.tweet_details_no_twitter, menu);
		}
		return true;
	}


	// Function to login twitter
	private void loginToTwitter() {
		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				Log.v("twitter login: request token tweet details",requestToken.toString());
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) { e.printStackTrace(); }
		}
		else{
			Toast.makeText(getApplicationContext(),
					"Already Logged into twitter", Toast.LENGTH_LONG).show();
			//new UpdateTwitterStatus().execute(tweet_text);
			//desligar o rut do artur :P
			if (rut!=null){
				rut.kill(); // Stops the assync thread gently the kills it 
				try {Thread.sleep(25);} catch (InterruptedException e) {}
				rut.cancel(true);
			}
			pDialog = new ProgressDialog(TweetDetailsActivity.this);
			pDialog.setMessage("Retwiting...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			//pDialog.show();
			rut2 = (UpdateTwitterStatus) new UpdateTwitterStatus().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, tweet_text);
		}
	}

	// *
	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return myPreferences.isUserTwittLoggin();
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.share_twitter:
			//login to twitter and post stuff
			loginToTwitter();
			return true;

			// private Comment
		case R.id.send_response:
			Intent newCommentIntent = new Intent(this,NewCommentActivity.class);
			newCommentIntent.putExtra("tweet2", Encoding.encodeTweet(tweet));
			newCommentIntent.putExtra("toAll", false);
			startActivity(newCommentIntent);
			return true;

			// public Comment
		case R.id.send_response_all:
			Intent newCommentIntent2 = new Intent(this,NewCommentActivity.class);
			newCommentIntent2.putExtra("tweet2", Encoding.encodeTweet(tweet));
			newCommentIntent2.putExtra("toAll", true);
			startActivity(newCommentIntent2);
			return true;

		case android.R.id.home:
			Intent parentActivityIntent = new Intent(this, MainActivity.class);
			parentActivityIntent.addFlags(
					Intent.FLAG_ACTIVITY_CLEAR_TOP |
					Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(parentActivityIntent);
			finish();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}



	/**
	 * Function to update status
	 * */
	class UpdateTwitterStatus extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {

			Log.d("Tweet Text", "> " + args[0]);
			String status = args[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

				// Access Token
				//String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				String access_token = myPreferences.getTwitOautTkn();
				// Access Token Secret
				//String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");
				String access_token_secret = myPreferences.getTwitOautScrt();	

				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

				// Update status
				twitter4j.Status response = twitter.updateStatus(status);

				Log.d("Status", "> " + response.getText());
			} catch (TwitterException e) { Log.d("Twitter Update Error", e.getMessage()); }
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			pDialog.dismiss(); // dismiss the dialog after getting all products
			rut = (ResponseUpdaterTask) new ResponseUpdaterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
			runOnUiThread(new Runnable() { // updating UI from Background Thread
				@Override
				public void run() {
				}
			});
		}

	}


	public class ResponseUpdaterTask extends AsyncTask<Void,Void,Void> { 

		private boolean running = false;
		//		ArrayList<HashMap<String,String>> comments = new ArrayList<HashMap<String,String>>();
		Bundle bundle = getIntent().getExtras();
		long tweetID = bundle.getLong("tweet_id");
		String srcDeviceID = bundle.getString("tweet_deviceID");
		ArrayList<Comment> mComments = new ArrayList<Comment>();


		//		HashMap<String,String> commentInterface = new HashMap<String,String>();
		//
		//		String[] keys = {"Comment", "UserName"};
		//		int[] ids = {android.R.id.text1,android.R.id.text2};
		//		SimpleAdapter mAdapter = new SimpleAdapter(getApplicationContext(), comments, android.R.layout.simple_list_item_2, keys, ids);
		//
		//		TweetPoll dummyComments = new TweetPoll();

		public void kill(){ running = false; }

		@Override
		protected void onProgressUpdate(Void... values) {
			//mAdapter = new SimpleAdapter(getApplicationContext(), comments, android.R.layout.simple_list_item_2, keys, ids);
			//lstVwComments.setAdapter(mAdapter);
			Log.e("ServiceP", "**progress Update");
			//	Toast.makeText(getApplicationContext(), ".......", Toast.LENGTH_LONG);
			TweetDetailsActivity.comments = mComments;
			TweetDetailsActivity.listView.setAdapter(new CommentCustomAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, comments));
		}

		@Override
		protected Void doInBackground(Void... params) {
			// Connect with the Service
			service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
			bindService(service, mConnection, Context.BIND_AUTO_CREATE);

			// Testes

			//						Comment user1 = new Comment("Justin", "Mega COmment super mario zeee");
			//						TweetDetailsActivity.comments.add(user1);
			//						Comment user2 = new Comment("Artur", "Mega COmment do Artur mario zeee");
			//						TweetDetailsActivity.comments.add(user2);
			//						Comment user3 = new Comment("David", "Mega COmment super mario zeee");
			//						TweetDetailsActivity.comments.add(user3);
			//						Comment user4 = new Comment("Tufffa", "Mega COmment super mario zeee");
			//						Comment user5 = new Comment("FIM", "FIM");
			//						TweetDetailsActivity.comments.add(user5);

			Log.e("ServiceP", "doInBackground Update");

			running = true;
			// Transformar num assync
			while((mService == null || !mService.isConnected()) && running){
				try { Thread.sleep(250);
				} catch (InterruptedException e) { e.printStackTrace(); }
			}

			Log.e("ServiceP", "Details Activity Conected to Service");

			// primeiro get Preencher
			mComments = new ArrayList<Comment>();
			for(TweetResponseDTO dto : mService.getAllResponses(srcDeviceID, tweetID)){
				//Log.e("ServiceP", "MSG:"+ dto.toString());						
				mComments.add(new Comment(dto.getNickName(), dto.getResponse()));
			}
			publishProgress();

			// verificar por updates
			while(running){
				if(mService.hasResponseUpdates(srcDeviceID, tweetID)){
					mComments = new ArrayList<Comment>();
					for(TweetResponseDTO dto : mService.getAllResponses(srcDeviceID, tweetID)){
						//Log.e("ServiceP", "MSG:"+ dto.toString());						
						mComments.add(new Comment(dto.getNickName(), dto.getResponse()));
					}
					publishProgress();
				}else{
					try {
						Log.e("ServiceP", "Details Activity Sleep");
						Thread.sleep(1500);
					} catch (InterruptedException e) { e.printStackTrace(); }
				}
			}
			return null;
		}
	}
}
