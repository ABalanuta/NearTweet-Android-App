package pt.utl.ist.tagus.cmov.neartweet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;


import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

public class NewCommentActivity extends Activity {

	ImageView personImage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_comment);
		personImage = (ImageView) findViewById(R.id.david);
		
		personImage.setVisibility(ImageView.VISIBLE);
		
		SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences("MyPref",0);
		Toast.makeText(getApplicationContext(),String.valueOf( mSharedPreferences.contains("imgurl")), Toast.LENGTH_LONG).show();
		
		
		if (mSharedPreferences.contains("imgurl")){
			String url = new String();
			url = mSharedPreferences.getString("imgurl", "");
			Toast.makeText(getApplicationContext(), "has url: " + url, Toast.LENGTH_LONG).show();
			Log.v("URL",url);
			
			URL newurl;
			try {
				newurl = new URL(url);
				Bitmap mIcon_val = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream()); 
				personImage.setImageBitmap(mIcon_val);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 


		}		
		}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_comment, menu);
		return true;
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

}
