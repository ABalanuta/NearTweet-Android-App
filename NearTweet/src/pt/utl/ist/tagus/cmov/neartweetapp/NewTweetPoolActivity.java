package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.ArrayList;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweet.R.id;
import pt.utl.ist.tagus.cmov.neartweet.R.layout;
import pt.utl.ist.tagus.cmov.neartweet.R.menu;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class NewTweetPoolActivity extends Activity {
	public static Button btnSendPoll;
	public static Button btnAddItemPoll;
	public static EditText edtTxtPergunta;
	public static EditText edtTxtResposta;
	public static ListView lstVwRespostas;
	ArrayList<String> respostas;
	ArrayAdapter<String> adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_tweet_pool);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		btnSendPoll = (Button) findViewById(R.id.buttonSendNowPool); 
		btnAddItemPoll = (Button) findViewById(R.id.buttonAddNewOptionPool);
		edtTxtPergunta = (EditText) findViewById(R.id.editTextPergunta);
		edtTxtResposta = (EditText) findViewById(R.id.editTextResposta);
		lstVwRespostas = (ListView) findViewById(R.id.listViewResponses);
		
		respostas=new ArrayList<String>();
		
		adapter = new ArrayAdapter<String>(this,
	            android.R.layout.simple_list_item_1,
	            respostas);
		lstVwRespostas.setAdapter(adapter);
		
		lstVwRespostas.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				respostas.remove(position);
				adapter.notifyDataSetChanged();
				return false;
			}
		});
		
		btnAddItemPoll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String resposta = edtTxtResposta.getText().toString(); 
				respostas.add(resposta);
				edtTxtResposta.setText("");
				adapter.notifyDataSetChanged();
			}
		});
		btnSendPoll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String username = null;
				String deviceid = null;
				SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref",1);
				mSharedPreferences.getString("username", username);
				mSharedPreferences.getString("deviceid",deviceid);
				
				//TODO
				//TweetPoll mTweetPool = new TweetPoll(edtTxtPergunta.getText().toString(), username, deviceid);
				
				for (String answer : respostas){
				//	mTweetPool.addAnswer(answer);
				}
				
				//TODO send tweetPool to server
				Toast.makeText(getApplicationContext(), "hade enviar par um servidor", Toast.LENGTH_LONG).show();
				startActivity(new Intent(getApplicationContext(), MainActivity.class));
			}
		});
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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


}
