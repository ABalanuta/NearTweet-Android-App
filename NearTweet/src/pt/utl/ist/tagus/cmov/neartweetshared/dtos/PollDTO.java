package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

public class PollDTO extends BasicDTO {


	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;

	private String srcDeviceID;
	private String destDeviceID;
	private long tweetID;
	private String option;

	public PollDTO(String srcDeviceID, String destDeviceID, long tweetID, String option) {
		super(TypeofDTO.POLL_DTO);
		this.srcDeviceID = srcDeviceID;
		this.destDeviceID = destDeviceID;
		this.tweetID = tweetID;
		this.option = option;
	}

	
	public String getOption() {
		return option;
	}

	public void setOption(String option) {
		this.option = option;
	}

	public String getSrcDeviceID() {
		return srcDeviceID;
	}

	public String getDestDeviceID() {
		return destDeviceID;
	}

	public long getTweetID() {
		return tweetID;
	}
	
	
}
