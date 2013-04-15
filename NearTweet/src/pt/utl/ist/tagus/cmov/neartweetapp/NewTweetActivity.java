package pt.utl.ist.tagus.cmov.neartweetapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.models.CmovPreferences;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandler;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.text.Editable;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

public class NewTweetActivity extends Activity{

	///////////////////////////////////<Variables>
	public static EditText mSendTextBox;
	private String mUsername = null;

	public static ImageView imgChoosen;
	public static Button btnGetUrl;
	public static EditText addUrlField;
	public static LinearLayout add_url_layout;
	private Intent pictureActionIntent = null;
	public static ConnectionHandler connectionHandler = null;
	private static final int CAMERA_PIC_REQUEST = 1337; 
	private final int CAMERA_PICTURE = 1;
	private final int GALLERY_PICTURE = 2;
	private static final String gpsLocation = null;
	private Bitmap bitmap = null;
	String lat;
	String lng;

	// Connection to Service Variables
	public boolean mBound = false;
	private Intent service;
	private ConnectionHandlerService mService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_new_tweet);
		
		
		mSendTextBox = (EditText) findViewById(R.id.sendTextField);
		imgChoosen = (ImageView) findViewById(R.id.imageViewChoosen);	
		btnGetUrl = (Button) findViewById(R.id.btnGetUrl);
		addUrlField = (EditText) findViewById(R.id.addUrlField);
		add_url_layout = (LinearLayout) findViewById(R.id.addUrlLayout);

		Bundle bundle = getIntent().getExtras();
		lat = bundle.getString("gps_location_lat");
		lng = bundle.getString("gps_location_lng");
		CmovPreferences myPreferences = new CmovPreferences(getApplicationContext());
		mUsername = myPreferences.getUsername();
		Toast.makeText(getApplicationContext(), mUsername + lat +lng, Toast.LENGTH_LONG).show();
		imgChoosen.setVisibility(ImageView.INVISIBLE);

		// Conect with the Service
		service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
		bindService(service, mConnection, Context.BIND_AUTO_CREATE);

		btnGetUrl.setOnClickListener(new OnClickListener() {
			@Override		
			public void onClick(View v) {
				String imageUrl = addUrlField.getText().toString();
				String originalUrl = imageUrl;
				String format = imageUrl.substring(imageUrl.lastIndexOf('.') + 1);
				
				addUrlField.setFocusable(true);
				
				Log.v("formato: ",format);
				if (android.os.Build.VERSION.SDK_INT > 9) {
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					StrictMode.setThreadPolicy(policy);
				}
				if (format.equals("jpg")){
					//TODO download and show jpeg
				}
				else if(format.equals("png")){
					Log.v("� png, url: ", originalUrl);
					URL newurl;
					Bitmap mIcon_val=null;
					try {
						newurl = new URL(originalUrl);
						mIcon_val = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream()); 
						imgChoosen.setImageBitmap(mIcon_val);
						imgChoosen.setVisibility(ImageView.VISIBLE);
						bitmap = mIcon_val;
					} catch (MalformedURLException e) { e.printStackTrace();
					} catch (IOException e) { e.printStackTrace(); } 
				}
				else{ Toast.makeText(getApplicationContext(), "Formato de imagem nao suportado", Toast.LENGTH_LONG).show();}
				addUrlField.clearComposingText();
				add_url_layout.setVisibility(LinearLayout.INVISIBLE);
			}
		});

		//Bundle bundle = getIntent().getExtras();
		//String gpsLocation = bundle.getString("gps_location");
		//Toast.makeText(getApplicationContext(), gpsLocation, Toast.LENGTH_LONG).show();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_tweet_activity2, menu);
		return true;
	}


	@Override
	protected void onDestroy() {
		Log.e("ServiceP", "Killing New Tweet Activity");

		// unbinding from the Service
		if(mBound){
			unbindService(mConnection);
		}
		super.onDestroy();
	}

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
		case R.id.take_picture:
			startDialog();
            return true;
            
		case R.id.send_tweet:
			if(mSendTextBox.getText().length() == 0){
				Toast t = Toast.makeText(getApplicationContext(), "Insert Text", Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				return true;
			}

			if(mBound && mService.isConnected()){
				TweetDTO tweet = new TweetDTO(mUsername, mSendTextBox.getText().toString());
				if(bitmap != null){
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					byte[] byteArray = stream.toByteArray();
					tweet.setPhoto(byteArray);
				}
				mService.sendTweet(tweet);
				mSendTextBox.setText(null);
				Toast t = Toast.makeText(getApplicationContext(), "SENT", Toast.LENGTH_SHORT);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				finish();
			}else{ Toast.makeText(getApplicationContext(), "server Error", Toast.LENGTH_SHORT).show(); }
            return true;
            
		case R.id.attach_url:
			attatchUrlFotoToTwitt();
            return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == GALLERY_PICTURE) {
			Uri uri = data.getData();
			if (uri != null) {
				// User had pick an image.
				Cursor cursor = getContentResolver().query(uri, new String[] { android.provider.MediaStore.Images.ImageColumns.DATA }, null, null, null);
				cursor.moveToFirst();
				// Link to the image
				final String imageFilePath = cursor.getString(0);
				File photos = new File(imageFilePath);
				bitmap = decodeFile(photos);
				bitmap = Bitmap.createScaledBitmap(bitmap, 400, 3, true);

				imgChoosen.setImageBitmap(bitmap);
				imgChoosen.setVisibility(ImageView.VISIBLE);
				cursor.close();
			}
			else { Toast.makeText(this, "No Image is selected.", Toast.LENGTH_LONG).show(); }
		}
		else if (requestCode == CAMERA_PICTURE) {
			if (data.getExtras() != null) {
				// here is the image from camera
				bitmap = (Bitmap) data.getExtras().get("data");
				bitmap = Bitmap.createScaledBitmap(bitmap, 400, 300, true);

				imgChoosen.setVisibility(ImageView.VISIBLE);
				imgChoosen.setImageBitmap(bitmap);
			}
		}
	}

	private void startDialog() {
		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
		myAlertDialog.setTitle("Upload Pictures Option");
		myAlertDialog.setMessage("How do you want to set your picture?");

		myAlertDialog.setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				pictureActionIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
				pictureActionIntent.setType("image/*");
				pictureActionIntent.putExtra("return-data", true);
				startActivityForResult(pictureActionIntent, GALLERY_PICTURE);
			}
		});

		myAlertDialog.setNegativeButton("Camera", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int arg1) {
				pictureActionIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(pictureActionIntent, CAMERA_PICTURE);
			}
		});
		myAlertDialog.show();
	}
	
	private void attatchUrlFotoToTwitt(){
		add_url_layout = (LinearLayout) findViewById(R.id.addUrlLayout);
		add_url_layout.setVisibility(LinearLayout.VISIBLE);
		
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
