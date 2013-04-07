package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

public class TweetResponseDTO extends BasicDTO {
	
	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;
	private String nickName = null;
	private String resp = null;
	private String srcDeviceID = null;
	private String destDeviceID = null;
	private long desTweetID = -1;
	private boolean isPrivate;
	
	public TweetResponseDTO(String nickName, String resp, String destDeviceID, long desTweetID, boolean priv) {
		super(TypeofDTO.TWEET_RESP_DTO);
		this.resp = resp;
		this.nickName = nickName;
		this.destDeviceID = destDeviceID;
		this.isPrivate = priv;
		this.desTweetID = desTweetID;
	}
	
	public long getDesTweetID() {
		return desTweetID;
	}

	public boolean isPrivate(){
		return isPrivate;
	}
	
	
	public String getSrcDeviceID() {
		return srcDeviceID;
	}

	public void setSrcDeviceID(String srcDeviceID) {
		this.srcDeviceID = srcDeviceID;
	}

	public String getDestDeviceID() {
		return destDeviceID;
	}
	
	public void setDesTweetID(long desTweetID) {
		this.desTweetID = desTweetID;
	}

	public TypeofDTO getType() {
		return super.getType();
	}
	
	public String getResponse(){
		return this.resp;
	}
	
	public String getNickName(){
		return this.nickName;
	}

}
