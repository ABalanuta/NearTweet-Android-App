package pt.utl.ist.tagus.cmov.neartweetapp.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.provider.Settings.Secure;

public class CmovPreferences {
	SharedPreferences mSharedPreferences;
	public final String USERNAME = "USERNAME";
	public final String DEVICE_ID = "DEVICE_ID";
	public final String IS_TWITTER_LOGGEDIN = "IS_TWITTER_LOGGEDIN";
	public final String PROFILE_IMG_URL = "PROFILE_IMG_URL";
	public final String PREF_KEY_OAUTH_TOKEN = "PREF_KEY_OAUTH_TOKEN";
	public final String PREF_KEY_OAUTH_SECRET = "PREF_KEY_OAUTH_SECRET";
	
	public CmovPreferences(Context appContext){
		mSharedPreferences = appContext.getSharedPreferences("MyPref",1);
		final String android_id = Secure.getString(appContext.getContentResolver(),
	              Secure.ANDROID_ID);
		setDeviceID(android_id);
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
