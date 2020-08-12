package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;


@DatabaseTable(tableName = "hcd_estadoAtencion")
public final class EstadoAtencion extends AbstractEntity { 
	
	public final static int ID_EN_PREPARACION = 1;
	public final static int ID_CERRADA_PROVISORIA = 2;
	public final static int ID_CERRADA_DEFINITIVA = 3;
	public final static int ID_ANULADA = 4;
	
	@DatabaseField(canBeNull = false)
	private String descripcion;

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
}
