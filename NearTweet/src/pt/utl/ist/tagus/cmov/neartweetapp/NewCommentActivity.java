package pt.utl.ist.tagus.cmov.neartweetapp;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


import org.apache.http.util.ByteArrayBuffer;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import pt.utl.ist.tagus.cmov.neartweetapp.models.Tweet;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.Encoding;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetResponseDTO;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NewCommentActivity extends Activity {

	ImageView personImage;
	EditText responseText;
	TextView typeOfResponse;
	Tweet tweet;
	boolean toAll = false;
	String username;
	private CmovPreferences myPreferences;

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
		setContentView(R.layout.activity_new_comment);
		personImage = (ImageView) findViewById(R.id.david);
		responseText = (EditText) findViewById(R.id.editTextAddComment);
		personImage.setVisibility(ImageView.VISIBLE);
		
		myPreferences = new CmovPreferences(getApplicationContext());
		
		BitmapDrawable db = new BitmapDrawable(getResources(), myPreferences.getProfilePictureLocation());
		personImage.setImageDrawable(db);


		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		// Ligação com o serviço
		service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
		bindService(service, mConnection, Context.BIND_AUTO_CREATE);


		Bundle bundle = getIntent().getExtras();
		tweet = Encoding.decodeTweet(bundle.getByteArray("tweet2"));
		toAll = bundle.getBoolean("toAll");
		username = bundle.getString("username");

		
		if(tweet == null){
			finish();
		}
		
		
		if(toAll){
			responseText.setHint("Responde para todos");
		}else{
			responseText.setHint("Responde privado");
		}

		String url = new String();
		url = myPreferences.getProfileImgUrl();
		ConfigurationBuilder builder = new ConfigurationBuilder();
		builder.setOAuthConsumerKey(myPreferences.getConsumerKey());
		builder.setOAuthConsumerSecret(myPreferences.getConsumerSecret());

		AccessToken accessToken = new AccessToken(myPreferences.getTwitOautTkn(), myPreferences.getTwitOautScrt());
		Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);;
		User user;
		String image_url = new String();
		Toast.makeText(getApplicationContext(), "twitter token: " + myPreferences.getTwitOautTkn() +
				" twitter secret: " + myPreferences.getTwitOautTkn(), Toast.LENGTH_LONG).show();
		try {
			user = twitter.showUser(twitter.getId());
			image_url = user.getProfileImageURL();
		} catch (IllegalStateException e1) {
			e1.printStackTrace();
		} catch (TwitterException e1) {
			e1.printStackTrace();
		}
		Toast.makeText(getApplicationContext(), "has url: " + image_url, Toast.LENGTH_LONG).show();
		Log.v("URL",image_url);

		URL newurl;
		try {
			newurl = new URL(image_url);
			Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream()); 
			//personImage.setImageBitmap(mIcon_val);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		//open file from sdcard
		String result = new String();
		result = myPreferences.getProfilePictureLocation();

		//personImage.setImageBitmap(bitmap);
		//String myJpgPath = "/sdcard/neartweet/me.jpg";
		BitmapDrawable d = new BitmapDrawable(getResources(), result);
		if (d!=null){
			Log.v("byte array", String.valueOf(d.toString()));
		}
			Log.v("byte array � null", "I am null");

		personImage.setImageDrawable(d);
		
	}		



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_comment, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.send_response:

			for(int x = 200; x > 0; x--){
				if(mService != null && mService.isConnected()){
					String text = responseText.getText().toString();
					TweetResponseDTO res = new TweetResponseDTO(myPreferences.getUsername(), text, tweet.getDeviceID(), tweet.getTweetId(), !toAll);

					Log.v("user picture scrt",myPreferences.getTwitOautScrt());
					Log.v("user picture tkn",myPreferences.getTwitOautTkn());
					Log.v("user picture location",myPreferences.getProfilePictureLocation());
					if(myPreferences.getProfilePictureLocation()!=null&& !myPreferences.isLocal()){
						File user_photo = new File(myPreferences.getProfilePictureLocation());
						Bitmap bitmap = decodeFile(user_photo);
						res.setUserPhoto(Encoding.encodeImage(bitmap));
					}

					
					mService.sendResponseTweet(res);
					Toast.makeText(getApplicationContext(), "SENT as "+myPreferences.getUsername(), Toast.LENGTH_SHORT).show();
					finish();
					return true;
				}else{
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			Toast.makeText(getApplicationContext(), "Error Sending", Toast.LENGTH_SHORT).show();
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





	public void DownloadFromUrl(String DownloadUrl, String fileName) {

		try {
			File root = android.os.Environment.getExternalStorageDirectory();               

			File dir = new File (root.getAbsolutePath() + "/mnt/sdcard");
			if(dir.exists()==false) {
				dir.mkdirs();
			}

			URL url = new URL(DownloadUrl); //you can write here any link
			File file = new File(dir, fileName);

			long startTime = System.currentTimeMillis();
			Log.d("DownloadManager", "download begining");
			Log.d("DownloadManager", "download url:" + url);
			Log.d("DownloadManager", "downloaded file name:" + fileName);

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			/*
			 * Define InputStreams to read from the URLConnection.
			 */
			InputStream is = ucon.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);

			/*
			 * Read bytes to the Buffer until there is nothing more to read(-1).
			 */
			ByteArrayBuffer baf = new ByteArrayBuffer(5000);
			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}


			/* Convert the Bytes read to a String. */
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(baf.toByteArray());
			fos.flush();
			fos.close();
			Log.d("DownloadManager", "download ready in" + ((System.currentTimeMillis() - startTime) / 1000) + " sec");


		} catch (IOException e) {
			Log.d("DownloadManager", "Error: " + e);
		}

	}
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// Find the correct scale value. It should be the power of 2.
			final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale++;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		}
		catch (FileNotFoundException e) { }
		return null;
	}

}
