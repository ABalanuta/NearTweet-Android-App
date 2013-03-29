package pt.utl.ist.tagus.cmov.neartweetapp;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandler;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerTask;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	public static ConnectionHandler connHandler = null;
	
	private Button sendButton;	
	private EditText sendTextBox;
	public static EditText receveTextBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		sendButton = (Button) findViewById(R.id.sendButton);
		sendTextBox = (EditText) findViewById(R.id.sendEditText);
		receveTextBox = (EditText) findViewById(R.id.receveEditText);

		
		receveTextBox.setText("#Calling Home..."+"\n");
		new ConnectionHandlerTask().execute();
		
		sendButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				connHandler.send(new TweetDTO(sendTextBox.getText().toString()));
				sendTextBox.setText(null);
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
