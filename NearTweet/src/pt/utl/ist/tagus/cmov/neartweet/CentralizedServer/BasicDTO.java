package pt.utl.ist.tagus.cmov.neartweet.CentralizedServer;
public class BasicDTO implements java.io.Serializable{
	
	/**
	 * Default Serial Version
	 */
	private static final long serialVersionUID = 1L;
	private String value;
	
	public BasicDTO(String msg) {
		this.value = msg;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}