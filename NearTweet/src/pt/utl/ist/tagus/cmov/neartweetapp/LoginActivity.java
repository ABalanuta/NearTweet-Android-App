package pt.utl.ist.tagus.cmov.neartweetapp;

import pt.utl.ist.tagus.cmov.neartweet.R;
import android.provider.Settings.Secure;
import pt.utl.ist.tagus.cmov.neartweet.R.id;
import pt.utl.ist.tagus.cmov.neartweet.R.layout;
import pt.utl.ist.tagus.cmov.neartweet.R.menu;
import pt.utl.ist.tagus.cmov.neartweet.R.string;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {


	// Values for email and password at the time of the login attempt.
	private String mUsername = null;

	// UI references.
	private EditText mUsernameView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;

	public CmovPreferences myPreferences;
	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample_login";
	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	public static RequestToken requestToken;
	public static Twitter twitter;
	public static Button mLoginTwitter;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		myPreferences = new CmovPreferences(getApplicationContext());

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
						myPreferences.setLocal();
					}
				});
		mLoginTwitter = (Button) findViewById(R.id.sign_in_button_twitter);
		mLoginTwitter.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						loginToTwitter();
						mLoginTwitter.setVisibility(Button.INVISIBLE);
					}
				});

		//Check if already logged in
		if(mUsername != null){
			finish();
		}
		if (!myPreferences.isTweetLogin()) {


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
					myPreferences.setUsernam(username);
					mUsername = username;
					Log.v("A	UTENTICACAO COM SUCESSO!!",username);
					mUsernameView.setText(username);
					
					startActivity(new Intent(this,MainActivity.class));

				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.toString());
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	private void loginToTwitter(){
		if(!myPreferences.isTweetLogin()){
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



	public void attemptLogin() {
		showProgress(true);
		mUsername = mUsernameView.getText().toString();
		//Simulate network access
		try { Thread.sleep(1000); } catch (InterruptedException e) {  }
		showProgress(false);
		finish();	
	}

	@Override
	public void finish() {
		// Prepare data intent 
		Intent data = new Intent();
		data.putExtra("username", mUsername);
		// Activity finished ok, return the data
		setResult(RESULT_OK, data);

		//put username on shared preferences
		//get shared preferences
		//	  final String android_id = Secure.getString(getApplicationContext().getContentResolver(),
		//              Secure.ANDROID_ID);
		//	  SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref",1);
		//	  Editor e = mSharedPreferences.edit();
		//	  e.putString("username", mUsername);
		//	  e.putString("deviceid", android_id);
		//	  Toast.makeText(getApplicationContext(), android_id, Toast.LENGTH_LONG).show();
		//	  e.putBoolean("loggedIn", true);
		//	  e.commit();
		CmovPreferences myPrefs = new CmovPreferences(getApplicationContext());
		myPrefs.setUsernam(mUsername);
		super.finish();
	} 

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginStatusView.setVisibility(show ? View.VISIBLE
							: View.GONE);
				}
			});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
			.alpha(show ? 0 : 1)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLoginFormView.setVisibility(show ? View.GONE
							: View.VISIBLE);
				}
			});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
}
