package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;


@DatabaseTable(tableName = "hcd_entidadBusqueda")
public final class EntidadBusqueda extends AbstractEntity { 
	
	@DatabaseField(canBeNull = false)
	private String entidad;
	
	@DatabaseField(canBeNull = false)
	private String codigo;
	
	@DatabaseField
	private String descripcion;

	public String getEntidad() {
		return entidad;
	}

	public void setEntidad(String entidad) {
		this.entidad = entidad;
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
}
