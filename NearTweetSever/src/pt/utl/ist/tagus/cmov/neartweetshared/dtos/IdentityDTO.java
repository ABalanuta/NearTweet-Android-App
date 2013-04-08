package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

public class IdentityDTO extends BasicDTO {

	
	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;
	private String srcDeviceID;
	
	public IdentityDTO(String SrcDeviceID) {
		super(TypeofDTO.IDENTITY_DTO);
		this.srcDeviceID = SrcDeviceID;
	}

	public String getSourceDeviceID(){
		return this.srcDeviceID;
	}
	

	
}
