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
	private long tweetID = -1;
	
	public PollResponseDTO(TypeofDTO type, String nickName, String response,long tweetID) {
		super(TypeofDTO.POLL_RESPONSE_DTO);
		this.nickName = nickName;
		this.response = response;
		this.tweetID = tweetID;
	}

	public String getSrcDeviceID() {
		return srcDeviceID;
	}

	public void setSrcDeviceID(String srcDeviceID) {
		this.srcDeviceID = srcDeviceID;
	}

	public String getNickName() {
		return nickName;
	}

	public String getResponse() {
		return response;
	}

	public long getTweetID() {
		return tweetID;
	}
}
