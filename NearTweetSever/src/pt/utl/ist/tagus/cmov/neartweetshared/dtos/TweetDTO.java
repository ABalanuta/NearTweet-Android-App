package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

import java.util.Arrays;



public class TweetDTO extends BasicDTO{

	

	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;
	private String nickName = null;
	private String tweet = null;
	private String srcDeviceID = null;
	private long tweetID = -1;
	private byte[] photo = null;
	private byte[] userPhoto = null;
	private String[] mCoordinates = new String[2];
	
	public TweetDTO(String nickName, String tweet) {
		super(TypeofDTO.TWEET_DTO);
		this.tweet = tweet;
		this.nickName = nickName;
	}

	public String[] getCoordenates(){
		return mCoordinates;
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

	public byte[] getUserPhoto() {
		return userPhoto;
	}


	public void setUserPhoto(byte[] userPhoto) {
		this.userPhoto = userPhoto;
	}


	public void setPhoto(byte[] b){
		this.photo = b;
	}
	
	public byte[] getPhoto(){
		return this.photo;
	}
	
	public String getDeviceID() {
		return srcDeviceID;
	}
	
	public void setDeviceID(String deviceID) {
		this.srcDeviceID = deviceID;
	}
	
	public void setTweetID(long id) {
		tweetID = id;
	}
	
	public long getTweetID() {
		return this.tweetID;
	}
	
	public TypeofDTO getType() {
		return super.getType();
	}
	
	public String getTweet(){
		return this.tweet;
	}
	
	public String getNickName(){
		return this.nickName;
	}

	public boolean hasCoordenates(){
		if (mCoordinates[0] == null || mCoordinates[1]==null)return false;
		else return true;
	}
	
	@Override
	public String toString() {
		return "TweetDTO [nickName=" + nickName + ", tweet=" + tweet
				+ ", srcDeviceID=" + srcDeviceID + ", tweetID=" + tweetID
				+ ", photo=" + "$$$" + ", userPhoto="
				+ Arrays.toString(userPhoto) + ", mCoordinates="
				+ Arrays.toString(mCoordinates) + "]";
	}
	
}
