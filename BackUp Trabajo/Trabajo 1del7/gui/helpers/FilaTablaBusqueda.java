package coop.tecso.hcd.gui.helpers;

/**
 * @author tecso.coop
 *
 */
public final class FilaTablaBusqueda {
	private int id;
	private String codigo;
	private String descripcion;

	public long getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	@Override
	public String toString() {
		return "FilaTablaBusqueda [id = " + id 
				+ ", codigo = " + codigo
				+ ", descripcion = " + descripcion + "]";
	}
}