package pt.utl.ist.tagus.cmov.neartweetapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


import pt.utl.ist.tagus.cmov.neartweetapp.MainActivity;
import pt.utl.ist.tagus.cmov.neartweet.R;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandler;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService;
import pt.utl.ist.tagus.cmov.neartweetapp.networking.ConnectionHandlerService.LocalBinder;
import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetDTO;
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
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class NewTweetActivity extends Activity{

	///////////////////////////////////<Variables>
	public static Button mSendButton;
	public static EditText mSendTextBox;
	private String mUsername = null;

	public static Button btnPicture;
	public static ImageView imgChoosen;
	private Intent pictureActionIntent = null;
	public static ConnectionHandler connectionHandler = null;
	private static final int CAMERA_PIC_REQUEST = 1337; 
	private final int CAMERA_PICTURE = 1;
	private final int GALLERY_PICTURE = 2;
	private static final String gpsLocation = null;


	// Connection to Service Variables
	public boolean mBound = false;
	private Intent service;
	private ConnectionHandlerService mService;
	private Bitmap bitmap;

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
		setContentView(R.layout.activity_new_tweet);
		mSendButton = (Button) findViewById(R.id.sendButton);
		mSendTextBox = (EditText) findViewById(R.id.sendTextField);
		btnPicture = (Button) findViewById(R.id.cameraButton);
		imgChoosen = (ImageView) findViewById(R.id.imageViewChoosen);

		mUsername = getIntent().getExtras().getString("username");
		imgChoosen.setVisibility(ImageView.INVISIBLE);
		btnPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				startDialog();
			}
		});

		//Bundle bundle = getIntent().getExtras();
		//String gpsLocation = bundle.getString("gps_location");

		//Toast.makeText(getApplicationContext(), gpsLocation, Toast.LENGTH_LONG).show();

		mSendButton.setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View view) {


						service = new Intent(getApplicationContext(), ConnectionHandlerService.class);
						bindService(service, mConnection, Context.BIND_AUTO_CREATE);
						

						if(mBound && mService.isConnected()){
							mService.sendTweet(new TweetDTO(mUsername, mSendTextBox.getText().toString()));
							mSendTextBox.setText(null);
							Toast.makeText(getApplicationContext(), "SENT", Toast.LENGTH_SHORT).show();
						}else{
							Toast.makeText(getApplicationContext(), "server Error", Toast.LENGTH_LONG).show();
						}
					}
				} );
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
				bitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);
				imgChoosen.setImageBitmap(bitmap);
				imgChoosen.setVisibility(ImageView.VISIBLE);
				cursor.close();
			}
			else {
				Toast toast = Toast.makeText(this, "No Image is selected.", Toast.LENGTH_LONG);
				toast.show();
			}
		}
		else if (requestCode == CAMERA_PICTURE) {
			if (data.getExtras() != null) {
				// here is the image from camera
				bitmap = (Bitmap) data.getExtras().get("data");
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
		catch (FileNotFoundException e) {
		}
		return null;
	}
}
