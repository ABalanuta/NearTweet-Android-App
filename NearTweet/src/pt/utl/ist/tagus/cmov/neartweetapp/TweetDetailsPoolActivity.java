package pt.utl.ist.tagus.cmov.neartweetapp;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.TweetDetailsActivity.ResponseUpdaterTask;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import pt.utl.ist.tagus.cmov.neartweetapp.models.Comment;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CommentCustomAdapter;
import pt.utl.ist.tagus.cmov.neartweetapp.models.Tweet;
import pt.utl.ist.tagus.cmov.neartweetapp.models.TweetPoll;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.Encoding;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.PollResponseDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetResponseDTO;
import twitter4j.User;
import android.app.Activity;
import android.content.ClipData.Item;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

public class TweetDetailsPoolActivity extends Activity {


	public static TextView txtTweet;
	public static TextView txtUserName;
	public static ListView lstVwOptions;


	public static TweetPoll tweet;
	public static ArrayList<HashMap<String,String>> vote_options = new ArrayList<HashMap<String,String>>();

	public static HashMap<String,ArrayList<String>> myHashMap = new HashMap<String,ArrayList<String>>();

	// Assync Task Reference
	private ResponseUpdaterTask rut = null;

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
		setContentView(R.layout.activity_tweet_details_pool);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		txtTweet = (TextView) findViewById(R.id.tweet_text_pool);
		txtUserName = (TextView) findViewById(R.id.user_name_pool);
		lstVwOptions = (ListView) findViewById(R.id.listViewOptionVotes);

		Bundle bundle = getIntent().getExtras();
		final String tweet_uid = bundle.getString("tweet_uid");
		final String tweet_text = bundle.getString("tweet_text");
		tweet = (TweetPoll) Encoding.decodeTweet(bundle.getByteArray("tweet"));

		// Limpar Old
		vote_options = new ArrayList<HashMap<String,String>>();
		myHashMap = new HashMap<String,ArrayList<String>>();

		if(tweet != null){
			Log.e("ServiceP", "----Tweet IS Not Null----");
			for(String s : tweet.getOptions()){

				// Cria os Arrays PAra guardar os Dados
				myHashMap.put(s, new ArrayList<String>());

				HashMap<String,String> vote_interface = new HashMap<String,String>();
				vote_interface.put("Option", s);
				vote_options.add(vote_interface);
			}
			UpdatePollView();
		}else{
			Log.e("ServiceP", "Tweet IS null");
		}


		txtTweet.setText(tweet_text);
		txtUserName.setText("@ " + tweet.getUsername());

		// Starts the assync Task
		rut = (ResponseUpdaterTask) new ResponseUpdaterTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);


		lstVwOptions.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

				HashMap<String,String> option = (HashMap<String, String>) arg0.getAdapter().getItem(position);
				String selected = (String) option.values().toArray()[0];
				CmovPreferences myPreferences = new CmovPreferences(getApplicationContext());
				PollResponseDTO rsp = new PollResponseDTO(myPreferences.getUsername(), selected, tweet.getDeviceID(), ConnectionHandlerService.deviceID, tweet.getTweetId());
				Log.e("ServiceP", ":::: " + rsp);


				for(int x = 200; x > 0; x--){
					if(mService != null && mService.isConnected()){

						mService.sendResponsePoll(rsp);
						Log.e("ServiceP", "Sent Selection: " + selected);
						Toast.makeText(getApplicationContext(), "Vote Sent", Toast.LENGTH_LONG).show();
						return true;
					}else{
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}




				return false;
			}
		});
	}

	public void insertVote(String option, ArrayList<String> users){
		//HashMap<String,String> vote_interface = new HashMap<String,String>();

		HashMap<String,String> item = new HashMap<String,String>();
		item.put("Option", option);
		item.put("Voters", transformVotersInString(users));
		vote_options.add(item);

	}


	public void UpdatePollView(){
		//		TweetPoll dummyPoll = new TweetPoll();
		//		HashMap<String, ArrayList<String>> dummyAnswers = dummyPoll.generateDummyAnswers();
		String[] keys = {"Option", "Voters"};
		int[] ids = {R.id.textViewOption,R.id.textViewVoters};
		SimpleAdapter mAdapter = new SimpleAdapter(getApplicationContext(), vote_options, R.layout.custom_vote, keys, ids);
		lstVwOptions.setAdapter(mAdapter);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet_details_pool, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		Log.e("ServiceP", "Killing Poll Activity");

		// Stops the assync thread gently the kills it
		if(rut != null){
			rut.stopLoop();
		}
		try {Thread.sleep(30);} catch (InterruptedException e) {}
		rut.cancel(true);


		//unbinding from the Service
		if(mBound){ unbindService(mConnection);}
		super.onDestroy();
	}


	public String transformVotersInString(ArrayList<String> voters_array) {

		if (voters_array != null){

			String voters = new String();

			//se houver mais do que tres pessoas a votar saca os tres primeiros e adiciona o resto como numero
			if (voters_array.size() > 3){
				int n = 0;
				while (n<=2){
					voters = voters + " " + voters_array.get(n);
				}
				voters = voters + " e mais " + String.valueOf((voters_array.size()-3)) + "utilizadores votaram nisto";
				return voters;
			}
			//votaram de 1 a 3 pessoas
			else{
				if (voters_array.size() == 0){
					voters="Só o primeiro a votar!";
				}
				else{
					int n = 0;
					for (String voter_aux : voters_array){
						voters = voters + " " + voter_aux;
					}
					voters = voters + " votaram nisto";
					return voters;
				}
			}
		}
		//ninguem votou
		else{
			return "Só o primeiro a votar!";
		}
		return null;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
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




	public class ResponseUpdaterTask extends AsyncTask<Void,Object,Void> {  

		private boolean running = false;

		public void stopLoop(){
			running = false;
		}


		@Override
		protected Void doInBackground(Void... params) {

			// Conect with the Service
			service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
			bindService(service, mConnection, Context.BIND_AUTO_CREATE);


			Log.e("ServiceP", "doInBackground Update");

			running = true;


			// Esperar que se ligue ao Server
			while((mService == null || !mService.isConnected()) && running){
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			Log.e("ServiceP", "Poll Activity Conected to Service");

			while(running){

				if(mService != null){
					Log.e("ServiceP", "Loop Receve");




					// Preencher com campos Vazios
					myHashMap = new HashMap<String,ArrayList<String>>();
					ArrayList<String> opt = new ArrayList<String>();
					ArrayList<String> voted = new ArrayList<String>();
					synchronized(myHashMap){
						for(String s : tweet.getOptions()){

							// Cria os Arrays PAra guardar os Dados
							myHashMap.put(s, new ArrayList<String>());
							opt.add(s);
							HashMap<String,String> vote_interface = new HashMap<String,String>();
							vote_interface.put("Option", s);
							vote_options.add(vote_interface);
						}
					}

					ArrayList<Tweet> all = mService.getAllTweets();
					for(Tweet t : all){
						Log.e("ServiceP", t.toString());

						//						if(t instanceof TweetPoll){
						//							//Tweets por este device
						//							if(t.getDeviceID().equals(tweet.getDeviceID())){
						//								// o Tweet
						//								if(t.getTweetId() == tweet.getTweetId()){
						//									Log.e("ServiceP", "------------------------");
						//
						//									
						//									
						//									TweetPoll tp = (TweetPoll) t;
						//									
						//									for(PollResponseDTO r : tp.getAllResponses()){
						//										if(!voted.contains(r.getSrcDeviceID())){
						//											if(opt.contains(r.getResponse())){
						//												synchronized(myHashMap){
						//													Log.e("ServiceP", "»»Add " + r.getResponse()+ "  " +r.getNickName());
						//													myHashMap.get(r.getResponse()).add(r.getNickName());
						//												}
						//											}
						//										}
						//
						//									}
						//									//Log.e("ServiceP", t.toString());
						//									Log.e("ServiceP", "------------------------");
						//									break;
						//								}
						//							}
						//						}
					}


					publishProgress();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else{
					try {
						Thread.sleep(250);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}

			return null;
		}


		@Override
		protected void onProgressUpdate(Object... values) {

			// Renova a lista
			vote_options = new ArrayList<HashMap<String,String>>();
			synchronized(myHashMap){
				Set<String> options = myHashMap.keySet();
				for(String option : options){
					insertVote(option, myHashMap.get(option));
				}
			}
			UpdatePollView();

		}
	}
}