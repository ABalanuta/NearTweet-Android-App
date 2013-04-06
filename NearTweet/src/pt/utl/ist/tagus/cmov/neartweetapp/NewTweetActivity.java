package pt.utl.ist.tagus.cmov.neartweetapp;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandler;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewTweetActivity extends Activity{
	
///////////////////////////////////<Variables>
	public static Button mSendButton;
	public static EditText mSendTextBox;
	private String MyNickName = "SuperUser";

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
///////////////////////////////////</Variables>
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_tweet);
		mSendButton = (Button) findViewById(R.id.sendButton);
		mSendTextBox = (EditText) findViewById(R.id.sendTextField);

		service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
		bindService(service, mConnection, Context.BIND_AUTO_CREATE);


		//TODO send not working on newtweet activity
		mSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.sendTweet(new TweetDTO(MyNickName, mSendTextBox.getText().toString()));
				mSendTextBox.setText(null);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		Log.e("ServiceP", "Killing New Tweet Activity");
		
		// unbinding from the Service
		if(mBound){
			unbindService(mConnection);
		}
		
		super.onDestroy();
	}

}
