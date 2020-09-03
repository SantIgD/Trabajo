package coop.tecso.udaa.base;

/**
 * 
 * Excepción propia de la aplicacion UDAA para manejo controlado de errores
 * 
 * @author tecso
 *
 */
public final class UDAAException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String error;

	public UDAAException(String error) {
		super();
		this.error = error;
	}

	public String getError() {
		return error;
	}

}
