package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweet.R;

import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;

import pt.utl.ist.tagus.cmov.neartweet.TweetDetailsActivity;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandler;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.BasicDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TypeofDTO;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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
	private String MyNickName = "SuperUser";

	public static Button mSendButton;
	public static EditText mSendTextBox;

	protected final String KEY_TEXT = "texto";
	protected final String KEY_TWEETER = "utilizador";
	public static ArrayList<Tweet> mTweetsArray = new ArrayList<Tweet>();
	ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();
	public static ConnectionHandler connectionHandler = null;



	// Connection to Service Vriables
	public boolean mBound = false;
	private Intent service;
	public ConnectionHandlerService mService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);



		setContentView(R.layout.activity_main);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar1);


		//converter tweets de arraylist para hashmap para sse poder mostrar na interface
		if (isNetworkAvailable()){

			mProgressBar.setVisibility(View.VISIBLE);

			// Inicia thread que actualiza as messagens
			new ConnectionHandlerTask().execute();

			//Online
			//new ConnectionHandlerTask().execute();

			//Offline
			//puts dummy tweets
			//Tweet tweetGenerator = new Tweet();
			//mTweetsArray = tweetGenerator.generateTweets();
			//handleServerResponse();

		}  
		else{
			Toast.makeText(this, "nao ha net", Toast.LENGTH_LONG).show();
		}



		mSendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mService.sendTweet(new TweetDTO(MyNickName, mSendTextBox.getText().toString()));
				mSendTextBox.setText(null);
			}		
		});	

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


//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

	@Override
	protected void onDestroy() {
		Log.e("ServiceP", "Killing Main Activity");
		// unbinding from the Service
		unbindService(mConnection);
		super.onDestroy();
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


	class ConnectionHandlerTask extends AsyncTask<String,BasicDTO,String> {

		protected final String KEY_TEXT = "texto";
		protected final String KEY_TWEETER = "utilizador";
		ArrayList<HashMap<String,String>> tweets = new ArrayList<HashMap<String,String>>();



		@Override
		protected String doInBackground(String... message) {

			// Criar um serviço que estabelece a communicação com o server
			service = new Intent(getApplicationContext(), ConnectionHandlerService.class);

			startService(service);
			MainActivity.mProgressBar.setVisibility(View.INVISIBLE);

			// vamos efectuar uma ligação com o servidor
			bindService(service, mConnection, Context.BIND_AUTO_CREATE);
			//startService(service);

			boolean loadedOld = false;

			while(true){

				if(mService != null){

					ArrayList<BasicDTO> objects;

					if(!loadedOld){
						objects  = mService.receveOldTweets();
						for(BasicDTO oo : objects){
							publishProgress(oo);
						}
						loadedOld = true;

					}else if(mService.hasTweets()){
						objects = mService.receveNewTweets();
						for(BasicDTO oo : objects){
							publishProgress(oo);
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
		}


		@Override
		protected void onProgressUpdate(BasicDTO... values) {

			if(values[0].getType().equals(TypeofDTO.TWEET_DTO)){
				TweetDTO t = (TweetDTO) values[0];		

				// get tweets from server
				mTweetsArray.add(new Tweet(t.getTweet(),t.getNickName(),"lalalala"));
				ArrayList<HashMap<String,String>> tweets =  new ArrayList<HashMap<String,String>>();

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
				SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), tweets,
						android.R.layout.simple_list_item_2, keys, ids);
				setListAdapter(adapter);
				handleServerResponse();
			}

		}
	}
}

