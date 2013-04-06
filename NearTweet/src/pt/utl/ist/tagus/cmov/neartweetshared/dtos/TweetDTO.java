package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

public class TweetDTO extends BasicDTO{

	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;
	private String nickName = null;
	private String tweet = null;
	private String srcMacAddr = null;
	private long tweetID = -1;
	
	public TweetDTO(String nickName, String tweet) {
		super(TypeofDTO.TWEET_DTO);
		this.tweet = tweet;
		this.nickName = nickName;
	}

	public String getSrcMacAddr() {
		return srcMacAddr;
	}
	
	public void setSrcMacAddr(String srcMacAddr) {
		this.srcMacAddr = srcMacAddr;
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

}
