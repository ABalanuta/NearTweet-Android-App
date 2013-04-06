package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

public class TweetResponseDTO extends BasicDTO {
	
	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;
	private String nickName = null;
	private String resp = null;
	private String srcMacAddr = null;
	private String dstMacAddr = null;
	private boolean isPrivate;
	
	public TweetResponseDTO(String nickName, String resp, String destMac, boolean priv) {
		super(TypeofDTO.TWEET_RESP_DTO);
		this.resp = resp;
		this.nickName = nickName;
		this.dstMacAddr = destMac;
		this.isPrivate = priv;
	}
	
	public boolean isPrivate(){
		return isPrivate;
	}
	
	public String getDstMacAddr() {
		return dstMacAddr;
	}
	public String getSrcMacAddr() {
		return srcMacAddr;
	}
	
	public void setSrcMacAddr(String srcMacAddr) {
		this.srcMacAddr = srcMacAddr;
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
