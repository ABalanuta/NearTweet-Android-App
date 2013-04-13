package pt.utl.ist.tagus.cmov.neartweetapp.models;

import java.io.Serializable;
import java.util.ArrayList;

import pt.utl.ist.tagus.cmov.neartweetshared.dtos.TweetResponseDTO;

import android.graphics.Bitmap;

public class Tweet implements Serializable, Cloneable{

	/**
	 * Defoult Version ID
	 */
	private static final long serialVersionUID = 1L;

	String mText;
	String mUsername;
	String mDeviceID;
	long mTweetId;
	byte[] mImage = null;
	byte[] mUserImage = null;
	ArrayList<TweetResponseDTO> mResponses = new ArrayList<TweetResponseDTO>();
	String[] mCoordinates = new String[2];


	public Tweet() {
	}


	public Tweet(String mText, String mUsername, String mDeviceID,
			long mTweetId) {
		this.mText = mText;
		this.mUsername = mUsername;
		this.mDeviceID = mDeviceID;
		this.mTweetId = mTweetId;
	}

	public void addResponse(TweetResponseDTO resp){
		this.mResponses.add(resp);
	}

	public String getText() {
		return mText;
	}

	public void setText(String mText) {
		this.mText = mText;
	}

	public String getUsername() {
		return mUsername;
	}

	public void setUsername(String mUsername) {
		this.mUsername = mUsername;
	}

	public String getDeviceID() {
		return mDeviceID;
	}

	public void setDeviceID(String mDeviceID) {
		this.mDeviceID = mDeviceID;
	}

	public long getTweetId() {
		return mTweetId;
	}

	public void setTweetId(long mTweetId) {
		this.mTweetId = mTweetId;
	}

	public byte[] getImage() {
		return mImage;
	}

	public void setImage(byte[] mImage) {
		this.mImage = mImage;
	}

	public byte[] getUserImage() {
		return mUserImage;
	}

	public void setUserImage(byte[] mUserImage) {
		this.mUserImage = mUserImage;
	}

	public ArrayList<TweetResponseDTO> getResponses() {
		return mResponses;
	}

	public String[] getmCoordinates() {
		return mCoordinates;
	}

	public void setCoordinates(String[] mCoordinates) {
		this.mCoordinates = mCoordinates;
	}

	public boolean hasImage(){
		if (mImage == null){
			return false;
		}
		return true;
	}

	public String getLAT() {
		return mCoordinates[0];
	}
	public String getLNG() {
		return mCoordinates[1];
	}

	public void setLAT(String lat) {
		mCoordinates[0] = lat;
	}

	public void setLNG(String lng) {
		mCoordinates[0] = lng;
	}

	public void deleteResponses(){
		this.mResponses = null;
	}

	public boolean hasCoordenates(){
		if (mCoordinates[0] ==null || mCoordinates[1]==null)return false;
		else return true;
	}
	public ArrayList<Tweet> generateTweets(){
		String[] mDummyCoordinates = new String[2];
		mDummyCoordinates[0]="0";
		mDummyCoordinates[1]="1";
		Tweet tweet_with_coordinates = new Tweet("texto do tweet 1, bla bla bla bla bla bla bla bla bla", "Balanuta", "ABCCDEF121836", 3);
		tweet_with_coordinates.setCoordinates(mDummyCoordinates);

		ArrayList<Tweet> tweets = new ArrayList<Tweet>();
		tweets.add(new Tweet("belo dia", "golfadas", "ABCCDEF121836", 1));
		tweets.add(new Tweet("eu apago cenas", "Balanuta", "ABCCDEF121836",2));
		tweets.add(new TweetPoll("pum ptum ptum pa", "David", "ABCCDEF121836", (long) 4));
		tweets.add(tweet_with_coordinates);
		return tweets;
	}

	 protected Object clone() throws CloneNotSupportedException {
	        return super.clone();
	    }
	
}
