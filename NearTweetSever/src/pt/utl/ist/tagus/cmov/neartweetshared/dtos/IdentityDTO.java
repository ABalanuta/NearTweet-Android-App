package pt.utl.ist.tagus.cmov.neartweetshared.dtos;

public class IdentityDTO extends BasicDTO {

	private String srcMac;
	
	
	public IdentityDTO(String srcMac) {
		super(TypeofDTO.IDENTITY_DTO);
		this.srcMac = srcMac;
	}

	public String getSourceMac(){
		return this.srcMac;
	}
	
	
	
}
