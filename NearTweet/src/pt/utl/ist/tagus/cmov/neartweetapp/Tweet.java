package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.ArrayList;

public class Tweet {
	String mText;
	String muId;
	String mMacAddress;
	String mTweetId;
	
	public Tweet(){
	}
	
	public Tweet(String texto, String uId, String macAddress, String id){
		mText = texto;
		muId = uId;
		mMacAddress = macAddress;
		mTweetId = id;
	}
	
	public Tweet(String texto, String uId, String macAddress){
		mText = texto;
		muId = uId;
		mMacAddress = macAddress;
	}

	public String getUId() {
		return muId;
	}

	public String getId(){
		return mTweetId;
	}

	public String getMacAddress() {
		return mMacAddress;
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
