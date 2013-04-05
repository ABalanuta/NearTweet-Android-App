package pt.utl.ist.tagus.cmov.neartweetapp;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandler;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewTweetActivity extends Activity{
	public static Button mSendButton;
	public static EditText mSendTextBox;
	public static ConnectionHandler connectionHandler = null;
	private String MyNickName = "SuperUser";


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_tweet);
		mSendButton = (Button) findViewById(R.id.sendButton);
		mSendTextBox = (EditText) findViewById(R.id.sendTextField);
		
		//TODO send not working on newtweet activity
	mSendButton.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {

			connectionHandler.send(new TweetDTO(MyNickName, mSendTextBox.getText().toString()));
			mSendTextBox.setText(null);
		}
	});	
	}
	
}
