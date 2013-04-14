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
	public final String USER_PROFILE_PICTURE_LOCATION = "USER_PROFILE_PICTURE_LOCATION";
	public final Context mAppContext;

	public CmovPreferences(Context appContext){
		mSharedPreferences = appContext.getSharedPreferences("MyPref",1);
		final String android_id = Secure.getString(appContext.getContentResolver(),
				Secure.ANDROID_ID);
		mAppContext = appContext;
		setDeviceID(android_id);
	}

	public String getProfilePictureLocation(){
		if (getTwitOautScrt()!=null && getTwitOautTkn()!=null){
			
			//foto nao existe
			File sdCardDirectory = Environment.getExternalStorageDirectory();
			File file = mAppContext.getFileStreamPath(sdCardDirectory.getAbsolutePath() +
					"/neartweet/me.png");
			
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
				
				//save image to sdcard
				File image = new File(sdCardDirectory, "/neartweet/me.png");
			    boolean success = false;

			    // Encode the file as a PNG image.
			    FileOutputStream outStream;
			    try {

			        outStream = new FileOutputStream(image);
			        mIcon_val.compress(Bitmap.CompressFormat.PNG, 100, outStream); 
			        /* 100 to keep full quality of the image */

			        outStream.flush();
			        outStream.close();
			        success = true;
			    } catch (FileNotFoundException e) {
			        e.printStackTrace();
			    } catch (IOException e) {
			        e.printStackTrace();
			    }
				
				//write image location to shared preferences
				Editor e = mSharedPreferences.edit();
				e.putString(USER_PROFILE_PICTURE_LOCATION, sdCardDirectory +  "/neartweet/me.png");
				e.commit();
				return sdCardDirectory +  "/neartweet/me.png";
			}
			
			//foto ja existe
			else{
				return mSharedPreferences.getString(USER_PROFILE_PICTURE_LOCATION,"");
			}
		}
		return null;
	}

	public void setUsernam(String username){
		Editor e = mSharedPreferences.edit();
		e.putString(USERNAME, username);
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
