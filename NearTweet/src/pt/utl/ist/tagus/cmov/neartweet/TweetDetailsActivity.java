package pt.utl.ist.tagus.cmov.neartweet;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetResponseDTO;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class TweetDetailsActivity extends Activity {
	public static TextView txtTweet;
	public static TextView txtUserName;
	public static Button btnShareTwitter;
	public static EditText textBox;
	public static Button btnSendReply;
	public static ListView lstVwComments;
	ProgressDialog pDialog;
	private String TWITTER_CONSUMER_KEY = "20o4JfRtmLAQ9v1HpwwHKw";
	private String TWITTER_CONSUMER_SECRET = "pmLgr4ozXj2Dw8HBk3sqHykuOwAf0mDrjed4fzlkc";

	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	public String tweet_text;

	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	private static Twitter twitter;
	private static RequestToken requestToken;

	private static SharedPreferences mSharedPreferences;



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


	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_details);
		

		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"MyPref", 0);

		txtTweet = (TextView) findViewById(R.id.tweet_text);
		txtUserName = (TextView) findViewById(R.id.user_name);
		btnShareTwitter = (Button) findViewById(R.id.share_twitter);
		textBox = (EditText) findViewById(R.id.editText1);
		btnSendReply = (Button) findViewById(R.id.send_reply);
		lstVwComments = (ListView) findViewById(R.id.listViewComments);

		Bundle bundle = getIntent().getExtras();
		String tweet_uid = bundle.getString("tweet_uid");
		
		tweet_text = bundle.getString("tweet_text");

		txtTweet.setText(tweet_text);
		txtUserName.setText("@ " + tweet_uid);
		
		// Bind to Service
		service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
		bindService(service, mConnection, Context.BIND_AUTO_CREATE);
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		/*
		 * Adding dummy content to the comments
		 */
		ArrayList<HashMap<String,String>> comments = 
				new ArrayList<HashMap<String,String>>();
		HashMap<String,String> commentInterface = new HashMap<String,String>();
		commentInterface.put("Comment","ola");
		commentInterface.put("UserName","Balanuta");
		comments.add(commentInterface);

		String[] keys = {"Comment", "UserName"};
		int[] ids = {android.R.id.text1,android.R.id.text2};
		SimpleAdapter mAdapter = new SimpleAdapter(this, comments,
				android.R.layout.simple_list_item_2, keys, ids);
		lstVwComments.setAdapter(mAdapter);


		// Send Reply
		btnSendReply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				
				if(mBound && mService.isConnected()){
					
					TweetResponseDTO r = new TweetResponseDTO("Resp", textBox.getText().toString(), "", 0, false); 
					mService.sendResponseTweet(r);
					Toast.makeText(getApplicationContext(), "SENT", Toast.LENGTH_SHORT).show();
				}else{
					Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_LONG).show();
				}

			}
		});


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
					Editor e = mSharedPreferences.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.commit(); // save changes

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());

					// Getting user details from twitter
					// For now i am getting his name only
					long userID = accessToken.getUserId();
					User user = twitter.showUser(userID);
					String username = user.getName();
					try {
						String url = user.getProfileImageURL();
						e.putString("imgurl", url);
						Toast.makeText(getApplicationContext(), "has url: " + url, Toast.LENGTH_LONG).show();
						Log.v("URL",url);
						e.commit();
						
					} catch (IllegalStateException ex) {
						ex.printStackTrace();
					}

					Toast.makeText(getApplicationContext(), username, Toast.LENGTH_LONG).show();

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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet_details, menu);

		return true;
	}


	//    * Function to login twitter

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
				requestToken = twitter
						.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				this.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) {
				e.printStackTrace();
			}

		}
		else{
			Toast.makeText(getApplicationContext(),
					"Already Logged into twitter", Toast.LENGTH_LONG).show();
			new updateTwitterStatus().execute(tweet_text);
		}
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.share_twitter:
			//login to twitter and post stuff
			loginToTwitter();
			return true;
		case R.id.send_response:
			Intent newCommentIntent = new Intent(this,NewCommentActivity.class);
			startActivity(newCommentIntent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// *
	private boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	/**
	 * Function to update status
	 * */
	class updateTwitterStatus extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(TweetDetailsActivity.this);
			pDialog.setMessage("Retwiting...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
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
				String access_token = mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token, access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);

				// Update status
				twitter4j.Status response = twitter.updateStatus(status);


				Log.d("Status", "> " + response.getText());
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					btnShareTwitter.setVisibility(Button.GONE);
				}
			});
		}

	}
}