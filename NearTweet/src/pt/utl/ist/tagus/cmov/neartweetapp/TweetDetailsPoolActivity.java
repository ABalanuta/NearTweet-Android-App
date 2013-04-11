package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.ArrayList;
import java.util.HashMap;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.models.TweetPoll;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tweet_details_pool);
		
		txtTweet = (TextView) findViewById(R.id.tweet_text_pool);
		txtUserName = (TextView) findViewById(R.id.user_name_pool);
		lstVwOptions = (ListView) findViewById(R.id.listViewOptionVotes);
		
		Bundle bundle = getIntent().getExtras();
		final String tweet_uid = bundle.getString("tweet_uid");
		final String tweet_text = bundle.getString("tweet_text");
		
		txtTweet.setText(tweet_text);
		txtUserName.setText("@ " + tweet_uid);
		
		/**
		 * Dummy contente for options to vote
		 */
		ArrayList<HashMap<String,String>> vote_options = new ArrayList<HashMap<String,String>>();

		TweetPoll dummyPoll = new TweetPoll();
		HashMap<String, ArrayList<String>> dummyAnswers = dummyPoll.generateDummyAnswers();
	
		for (String key : dummyAnswers.keySet()){
			String voters = new String();
			ArrayList<String> voters_array = dummyAnswers.get(key);
			
			if (voters_array != null){
				//se houver mais do que tres pessoas a votar saca os tres primeiros e adiciona o resto como numero
				if (voters_array.size() > 3){
					int n = 0;
					while (n<=2){
						voters = voters + " " + voters_array.get(n);
					}
					voters = voters + " e mais " + String.valueOf((voters_array.size()-3)) + "utilizadores votaram nisto";
				}
				//votaram de 1 a 3 pessoas
				else{
					if (voters_array.size() == 0){
						voters="S o primeiro a votar!";
					}
					else{
						int n = 0;
						for (String voter_aux : voters_array){
							voters = voters + " " + voter_aux;
						}
						voters = voters + " votaram nisto";
					}
				}
			}
			//ninguem votou
			else{
				voters="S o primeiro a votar!";
			}
			HashMap<String,String> vote_interface = new HashMap<String,String>();
			Log.v("option", key);
			Log.v("option", voters);
			vote_interface.put("Option", key);
			vote_interface.put("Voters", voters);
			vote_options.add(vote_interface);
			
		}
		
		String[] keys = {"Option", "Voters"};
		int[] ids = {R.id.textViewOption,R.id.textViewVoters};
		SimpleAdapter mAdapter = new SimpleAdapter(getApplicationContext(), vote_options, R.layout.custom_vote, keys, ids);
		lstVwOptions.setAdapter(mAdapter);
		
		
		lstVwOptions.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Toast.makeText(getApplicationContext(), "votaste nesta opcao", Toast.LENGTH_LONG).show();
				return false;
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.tweet_details_pool, menu);
		return true;
	}

}
