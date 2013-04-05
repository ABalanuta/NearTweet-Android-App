package pt.utl.ist.tagus.cmov.neartweetapp;


import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweet.TweetDetailsActivity;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandler;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TypeofDTO;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	public static final String TAG = MainActivity.class.getSimpleName();
	public static ProgressBar mProgressBar;
	protected final String KEY_TEXT = "texto";
	protected final String KEY_TWEETER = "utilizador";
	public static ArrayList<Tweet> mTweetsArray = new ArrayList<Tweet>();
	ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
	public static ConnectionHandler connectionHandler = null;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);
		
		
		//converter tweets de arraylist para hashmap para sse poder mostrar na interface
		if (isNetworkAvailable()){
			//GetTweetsTask getTweetsTask = new GetTweetsTask();
			//getTweetsTask.execute();

			//Online
			//new ConnectionHandlerTask().execute();
			
			//Offline
			//puts dummy tweets
			Tweet tweetGenerator = new Tweet();
			mTweetsArray = tweetGenerator.generateTweets();
			handleServerResponse();
			
			//mProgressBar.setVisibility(View.VISIBLE);

		}
		else{
			Toast.makeText(this, "nao ha net", Toast.LENGTH_LONG).show();
		}

	}
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Tweet tweet = mTweetsArray.get(position);
		
		Intent details = new Intent(this,TweetDetailsActivity.class);
		details.putExtra("tweet_id", tweet.getId());
		
		startActivity(details);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.new_tweet:
	        	Intent newTweetIntent = new Intent(this,NewTweetActivity.class);
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

	
	public class ConnectionHandlerTask extends AsyncTask<String,BasicDTO,Tweet> {

		private	final static String serverIP = "10.0.2.2";
		protected final String KEY_TEXT = "texto";
		protected final String KEY_TWEETER = "utilizador";
		ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
		//private	final static String serverIP = "172.20.81.13";
		private	final static int serverPort = 4444;

		@Override
		protected Tweet doInBackground(String... message) {

			InetAddress serverAddr = null;
			try {
				serverAddr = InetAddress.getByName(serverIP);
			} catch (UnknownHostException e2) {
				e2.printStackTrace();
			}

			Socket localSock = null;
			ConnectionHandler ch = null;


			// Contacting the Server , Retry if error
			while(true){
				try{
					localSock = new Socket(serverAddr, serverPort);
					break;
				}catch(Exception e){
					System.out.println("TCP " + " Sleeping 5s");
					System.out.println(e.toString());
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {}
				}
			}

			MainActivity.connectionHandler = new ConnectionHandler(localSock);
			MainActivity.connectionHandler.start();
			MainActivity.mProgressBar.setVisibility(View.INVISIBLE);

			while(true){

				if(MainActivity.connectionHandler.recevedObjects()){
					ArrayList<BasicDTO> objects  = MainActivity.connectionHandler.receve();
					for(BasicDTO oo : objects){
						publishProgress(oo);
					}
				}else{
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}	
		}
		@Override
		protected void onProgressUpdate(BasicDTO... values) {

			if(values[0].getType().equals(TypeofDTO.TWEET_DTO)){
				TweetDTO t = (TweetDTO) values[0];		

				// get tweets from server
				mTweetsArray.add(new Tweet(t.getTweet(),t.getNickName(),"lalalala"));
				
				handleServerResponse();
			}

		}
	}
}
