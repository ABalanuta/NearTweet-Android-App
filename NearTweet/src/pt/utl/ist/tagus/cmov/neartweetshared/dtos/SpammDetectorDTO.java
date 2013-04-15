package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

public class SpammDetectorDTO extends BasicDTO {


	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;

	private String srcDeviceID;
	private String destDeviceID;
	private long tweetID;

	public SpammDetectorDTO(String srcDeviceID, String destDeviceID, long tweetID) {
		super(TypeofDTO.SPAMM_DTO);
		this.srcDeviceID = srcDeviceID;
		this.destDeviceID = destDeviceID;
		this.tweetID = tweetID;
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

	@Override
	public String toString() {
		return "SpammDetectorDTO [srcDeviceID=" + srcDeviceID
				+ ", destDeviceID=" + destDeviceID + ", tweetID=" + tweetID
				+ "]";
	}



}
