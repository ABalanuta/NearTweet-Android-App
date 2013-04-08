package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class Tweet {
	String mText;
	String mUsername;
	String mMacAddress;
	String mTweetId;
	Bitmap mImage = null;
	String[] mCoordinates = new String[2] ;
	
	public Tweet(){
	}
	
	public Tweet(String texto, String uId, String macAddress, String id){
		mText = texto;
		mUsername = uId;
		mMacAddress = macAddress;
		mTweetId = id;
		mCoordinates[0]=null;
		mCoordinates[1]=null;
	}
	
	public Tweet(String texto, String uId, String macAddress){
		mText = texto;
		mUsername = uId;
		mMacAddress = macAddress;
		mCoordinates[0]=null;
		mCoordinates[1]=null;
	}

	public String getUsername() {
		return mUsername;
	}

	public String getId(){
		return mTweetId;
	}

	public String getMacAddress() {
		return mMacAddress;
	}
	public boolean hasImage(){
		if (mImage==null) return false;
		else return true;
	}

	public boolean hasCoordenates(){
		if (mCoordinates[0] ==null || mCoordinates[1]==null)return false;
		else return true;
	}

	public String getText() {
		return mText;
	}
	
	public ArrayList<Tweet> generateTweets(){
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "1"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "2"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "3"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "4"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "5"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "6"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "7"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "8"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "9"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "10"));
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", "11"));
		return tweets;
	}
}
