package pt.utl.ist.tagus.cmov.neartweet;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TweetDetailsActivity extends Activity {
	public static Button sendButton;
	public static TextView tweetText;
	public static TextView userNameText;
	public static Button shareTwitter;
	public static EditText textBox;
	public static Button sendReply;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_details);
		Bundle bundle = getIntent().getExtras();
		String tweet_id = bundle.getString("tweet_id");
		Toast.makeText(this, tweet_id, Toast.LENGTH_LONG).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet_details, menu);
		return true;
	}

}
