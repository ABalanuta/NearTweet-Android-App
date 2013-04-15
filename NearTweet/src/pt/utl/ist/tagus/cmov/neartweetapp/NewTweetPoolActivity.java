package pt.utl.ist.tagus.cmov.neartweetapp;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweet.R.id;
import pt.utl.ist.tagus.cmov.neartweet.R.layout;
import pt.utl.ist.tagus.cmov.neartweet.R.menu;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import pt.utl.ist.tagus.cmov.neartweetapp.models.TweetPoll;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.PollDTO;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
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

	// Connection to Service Vriables
	public boolean mBound = false;
	private Intent service;
	public ConnectionHandlerService mService;
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
		setContentView(R.layout.activity_new_tweet_pool);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		btnSendPoll = (Button) findViewById(R.id.buttonSendNowPool); 
		btnAddItemPoll = (Button) findViewById(R.id.buttonAddNewOptionPool);
		edtTxtPergunta = (EditText) findViewById(R.id.editTextPergunta);
		edtTxtResposta = (EditText) findViewById(R.id.editTextResposta);
		lstVwRespostas = (ListView) findViewById(R.id.listViewResponses);

		// Ligação com o serviço
		service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
		bindService(service, mConnection, Context.BIND_AUTO_CREATE);


		respostas = new ArrayList<String>();

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

				CmovPreferences myPreferences = new CmovPreferences(getApplicationContext());
				String username = myPreferences.getUsername();
				String deviceid = myPreferences.getDeviceId();



				if(mBound && mService.isConnected()){

					PollDTO poll = new PollDTO(username, edtTxtPergunta.getText().toString(), respostas);

					mService.sendPoll(poll);

					Toast t = Toast.makeText(getApplicationContext(), "SENT", Toast.LENGTH_SHORT);
					t.show();
					finish();
				}else{
					Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_SHORT).show();
				}

//				Toast.makeText(getApplicationContext(), "hade enviar par um servidor", Toast.LENGTH_LONG).show();
//				startActivity(new Intent(getApplicationContext(), MainActivity.class));
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

	@Override
	protected void onDestroy() {
		Log.e("ServiceP", "Killing New cOMMENT Activity");

		//unbinding from the Service
		// NOTA: nao remover if, utilizado para se destruir a aplicao caso variaveis estejam a null
		if (mConnection != null){
			unbindService(mConnection);
		}
		super.onDestroy();
	}


}
