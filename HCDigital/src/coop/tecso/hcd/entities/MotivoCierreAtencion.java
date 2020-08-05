package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;


@DatabaseTable(tableName = "hcd_motivoCierreAtencion")
public final class MotivoCierreAtencion extends AbstractEntity { 
	
	public final static int ID_CON_ACTO_MEDICO = 1;
	public final static int ID_SIN_ACTO_MEDICO = 2;
	public final static int ID_SE_NIEGA_A_FIRMAR = 3;
	
	@DatabaseField(canBeNull = false)
	private String descripcion;

	@DatabaseField
	private int orden;

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}
}
