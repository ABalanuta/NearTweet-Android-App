package pt.utl.ist.tagus.cmov.neartweetapp;


import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweet.R;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	public static final String TAG = MainActivity.class.getSimpleName();
	protected ProgressBar mProgressBar;
	protected final String KEY_TEXT = "texto";
	protected final String KEY_TWEETER = "utilizador";
	ArrayList<Tweet> mTweetsArray = new ArrayList<Tweet>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);

		
		//converter tweets de arraylist para hashmap para sse poder mostrar na interface
		
		if (isNetworkAvailable()){
			mProgressBar.setVisibility(View.VISIBLE);
			GetTweetsTask getTweetsTask = new GetTweetsTask();
			getTweetsTask.execute();
			
		}
		else {
			Toast.makeText(this, "nao ha net", Toast.LENGTH_LONG).show();
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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

	private class GetTweetsTask extends AsyncTask<Object, Void, ArrayList<Tweet>> {
		@Override
		protected ArrayList<Tweet> doInBackground(Object... arg0) {
			//TODO: retrieve tweets from the server
			ArrayList<Tweet> tweetsArray = null;
			Tweet stupidTweet = new Tweet();
			tweetsArray = stupidTweet.generateTweets();
			return tweetsArray;
		}	

		@Override 
		protected void onPostExecute(ArrayList<Tweet> result){
			mTweetsArray = result;
			handleServerResponse();
		}
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
				String userId = tweet.getUId();
				
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
}
