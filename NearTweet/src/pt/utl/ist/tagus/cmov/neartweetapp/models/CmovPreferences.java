package pt.utl.ist.tagus.cmov.neartweetapp.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.Log;

public class CmovPreferences {
	SharedPreferences mSharedPreferences;
	public final String USERNAME = "USERNAME";
	public final String DEVICE_ID = "DEVICE_ID";
	public final String IS_TWITTER_LOGGEDIN = "IS_TWITTER_LOGGEDIN";
	public final String PROFILE_IMG_URL = "PROFILE_IMG_URL";
	public final String PREF_KEY_OAUTH_TOKEN = "PREF_KEY_OAUTH_TOKEN";
	public final String PREF_KEY_OAUTH_SECRET = "PREF_KEY_OAUTH_SECRET";
	public final String TWITTER_CONSUMER_KEY = "20o4JfRtmLAQ9v1HpwwHKw";
	public final String TWITTER_CONSUMER_SECRET = "pmLgr4ozXj2Dw8HBk3sqHykuOwAf0mDrjed4fzlkc";
	public final String USER_PROFILE_PICTURE_LOCATION_PATH = "USER_PROFILE_PICTURE_LOCATION";
	public final String USER_PROFILE_PICTURE_LOCATION_IMAGE_NAME = "USER_PROFILE_PICTURE_LOCATION_IMAGE_NAME";
	public final Context mAppContext;
	public final String SHARE_MY_LOCATION = "SHARE_MY_LOCATION";

	public CmovPreferences(Context appContext){
		mSharedPreferences = appContext.getSharedPreferences("MyPref",1);
		final String android_id = Secure.getString(appContext.getContentResolver(),
				Secure.ANDROID_ID);
		mAppContext = appContext;
		setDeviceID(android_id);
		setShareMyLocationTrue();
	}

	public boolean getShareMyLocation(){
		return mSharedPreferences.getBoolean(SHARE_MY_LOCATION,false);
	}
	
	public String getProfilePictureLocation(){
		if (getTwitOautScrt()!=null && getTwitOautTkn()!=null){

			//foto nao existe
			File sdCardDirectory = Environment.getExternalStorageDirectory();
			File file = new File("/sdcard/neartweet/me.jpg");

			if(!file.exists()){

				//get profile url from twitter
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(getConsumerKey());
				builder.setOAuthConsumerSecret(getConsumerSecret());
				AccessToken accessToken = new AccessToken(getTwitOautTkn(),getTwitOautScrt());
				Twitter twitter = new TwitterFactory(builder.build()).getInstance(accessToken);;
				String image_url = new String();
				User user;
				try {
					user = twitter.showUser(twitter.getId());
					image_url = user.getProfileImageURL();
				} catch (IllegalStateException e1) {
					e1.printStackTrace();
				} catch (TwitterException e1) {
					e1.printStackTrace();
				}

				//download image from URL
				URL newurl;
				Bitmap mIcon_val=null;
				try {
					newurl = new URL(image_url);
					mIcon_val = BitmapFactory.decodeStream(newurl.openConnection() .getInputStream()); 
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 

				createDirIfNotExists("neartweet");
				File filename=null;
				FileOutputStream outStream=null;
				try {
					filename = new File(Environment.getExternalStorageDirectory().toString() + "/neartweet/me.jpg");
					
					outStream = new FileOutputStream(filename);
					
					mIcon_val.compress(Bitmap.CompressFormat.PNG, 100, outStream);
					outStream.flush();
					outStream.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				//write image location to shared preferences
				Editor e = mSharedPreferences.edit();
				e.putString(USER_PROFILE_PICTURE_LOCATION_PATH, sdCardDirectory +  "/neartweet/");
				e.putString(USER_PROFILE_PICTURE_LOCATION_IMAGE_NAME, "me.png");
				e.commit();

				return "/sdcard/neartweet/me.jpg";
			}

			//foto ja existe
			else{
				return "/sdcard/neartweet/me.jpg";
			}
		}
		return null;
	}
	public static boolean createDirIfNotExists(String path) {
		boolean ret = true;

		File file = new File(Environment.getExternalStorageDirectory(), path);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.e("blala :: ", "Problem creating Image folder");
				ret = false;
			}
		}
		return ret;
	}

	public void setUsernam(String username){
		Editor e = mSharedPreferences.edit();
		e.putString(USERNAME, username);
		e.commit();
	}
	public void setShareMyLocationTrue(){
		Editor e = mSharedPreferences.edit();
		e.putBoolean(SHARE_MY_LOCATION, true);
		e.commit();
	}
	public void setShareMyLocationFalse(){
		Editor e = mSharedPreferences.edit();
		e.putBoolean(SHARE_MY_LOCATION, false);
		e.commit();
	}
	public void setDeviceID(String deviceId){
		Editor e = mSharedPreferences.edit();
		e.putString(DEVICE_ID, deviceId);
		e.commit();
	}
	public void setTwitOautScrt(String oauthscrt){
		Editor e = mSharedPreferences.edit();
		e.putString(PREF_KEY_OAUTH_SECRET, oauthscrt);
		e.commit();
	}
	public void setTwitOautTkn(String oauth_tkn){
		Editor e = mSharedPreferences.edit();
		e.putString(PREF_KEY_OAUTH_TOKEN, oauth_tkn);
		e.commit();
	}
	public void setTwitLogin(){
		Editor e = mSharedPreferences.edit();
		e.putBoolean(IS_TWITTER_LOGGEDIN, true);
		e.commit();
	}
	public String getUsername(){
		return mSharedPreferences.getString(USERNAME,"");
	}
	public String getProfileImgUrl(){
		return mSharedPreferences.getString(PROFILE_IMG_URL,"");
	}
	public String getDeviceId(){
		return mSharedPreferences.getString(DEVICE_ID,"");
	}

	public String getTwitOautScrt(){
		return mSharedPreferences.getString(PREF_KEY_OAUTH_SECRET,"");

	}
	public String getTwitOautTkn(){
		return mSharedPreferences.getString(PREF_KEY_OAUTH_TOKEN,"");

	}
	public String getConsumerSecret(){
		return TWITTER_CONSUMER_SECRET;
	}
	public String getConsumerKey(){
		return TWITTER_CONSUMER_KEY;
	}

	public boolean hasUserName(){
		if (mSharedPreferences.contains(USERNAME)) return true;
		else return false;
	}
	public boolean hasProfileImgUrl(){
		if (mSharedPreferences.contains(PROFILE_IMG_URL)) return true;
		else return false;
	}
	public boolean isUserTwittLoggin(){
		if (mSharedPreferences.contains(PREF_KEY_OAUTH_TOKEN) && 
				mSharedPreferences.contains(PREF_KEY_OAUTH_SECRET)) return true;
		return false;
	}

}
