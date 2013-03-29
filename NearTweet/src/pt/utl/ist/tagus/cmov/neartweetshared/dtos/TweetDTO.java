package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

public class TweetDTO extends BasicDTO{

	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;
	private String tweet = null;
	
	public TweetDTO(String tweet) {
		super(TypeofDTO.TWEET_DTO);
		this.tweet = tweet;
	}

	
	public TypeofDTO getType() {
		return super.getType();
	}
	
	public String getTweet(){
		return this.tweet;
	}

}
