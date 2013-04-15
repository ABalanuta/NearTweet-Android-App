package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

import java.util.ArrayList;

public class PollResponseDTO extends BasicDTO {


	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;

	private String nickName = null;
	private String response = null;
	private String srcDeviceID = null;
	private String desDeviceID = null;
	private long tweetID = -1;
	
	public PollResponseDTO(String nickName, String response, String desDeviceID, String srcDeviceID, long tweetID) {
		super(TypeofDTO.POLL_RESPONSE_DTO);
		this.nickName = nickName;
		this.response = response;
		this.tweetID = tweetID;
		this.desDeviceID = desDeviceID;
		this.srcDeviceID = srcDeviceID;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getSrcDeviceID() {
		return srcDeviceID;
	}

	public void setSrcDeviceID(String srcDeviceID) {
		this.srcDeviceID = srcDeviceID;
	}

	public String getDesDeviceID() {
		return desDeviceID;
	}

	public void setDesDeviceID(String desDeviceID) {
		this.desDeviceID = desDeviceID;
	}

	public long getTweetID() {
		return tweetID;
	}

	public void setTweetID(long tweetID) {
		this.tweetID = tweetID;
	}

	@Override
	public String toString() {
		return "PollResponseDTO [nickName=" + nickName + ", response="
				+ response + ", srcDeviceID=" + srcDeviceID + ", desDeviceID="
				+ desDeviceID + ", tweetID=" + tweetID + "]";
	}

	
}
