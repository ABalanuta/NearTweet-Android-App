package pt.utl.ist.tagus.cmov.neartweetshared.dtos;
public abstract class BasicDTO implements java.io.Serializable{

	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;
	private TypeofDTO type = null;

	public BasicDTO(TypeofDTO type) {
		super();
		this.type = type;
	}

	public TypeofDTO getType(){
		return type; 
	}
}