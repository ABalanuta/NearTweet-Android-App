package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

import java.util.ArrayList;

public class PollDTO extends BasicDTO {


	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;

	private String nickName = null;
	private String question = null;
	private String srcDeviceID = null;
	private long tweetID = -1;
	private ArrayList<String> options = null;

	public PollDTO(String nickName, String question, ArrayList<String> options) {
		super(TypeofDTO.POLL_DTO);
		this.nickName = nickName;
		this.question = question;
		this.options = options;
	}

	public String getSrcDeviceID() {
		return srcDeviceID;
	}

	public void setSrcDeviceID(String srcDeviceID) {
		this.srcDeviceID = srcDeviceID;
	}

	public long getTweetID() {
		return tweetID;
	}

	public void setTweetID(long tweetID) {
		this.tweetID = tweetID;
	}

	public String getNickName() {
		return nickName;
	}

	public String getQuestion() {
		return question;
	}

	public ArrayList<String> getOptions() {
		return options;
	}

	@Override
	public String toString() {
		return "PollDTO [nickName=" + nickName + ", question=" + question
				+ ", srcDeviceID=" + srcDeviceID + ", tweetID=" + tweetID
				+ ", options=" + options + "]";
	}
	
	
}
