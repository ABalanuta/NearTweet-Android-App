package pt.utl.ist.tagus.cmov.neartweetapp;

import pt.utl.ist.tagus.cmov.neartweet.R;
import android.provider.Settings.Secure;
import pt.utl.ist.tagus.cmov.neartweet.R.id;
import pt.utl.ist.tagus.cmov.neartweet.R.layout;
import pt.utl.ist.tagus.cmov.neartweet.R.menu;
import pt.utl.ist.tagus.cmov.neartweet.R.string;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		// Set up the login form.
		mUsernameView = (EditText) findViewById(R.id.username);
		
		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		//Check if already logged in
		if(mUsername != null){
			finish();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
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
