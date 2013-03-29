package pt.utl.ist.tagus.cmov.neartweetapp;

import java.util.ArrayList;

public class Tweet {
	String mText;
	String muId;
	String mMacAddress;
	
	public Tweet(){
	}
	
	public Tweet(String texto, String uId, String macAddress){
		mText = texto;
		muId = uId;
		mMacAddress = macAddress;
	}
	
	public String getUId() {
		return muId;
	}


	public String getMacAddress() {
		return mMacAddress;
	}


	public String getText() {
		return mText;
	}
	
	public ArrayList<Tweet> generateTweets(){
		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		tweets.add(new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 2, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 3, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 4, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 5, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 6, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 7, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 8, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 9, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 10, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 11, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 12, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 13, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 14, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		tweets.add(new Tweet("texto do tweet 15, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836"));
		return tweets;
	}
}
