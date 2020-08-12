package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;


@DatabaseTable(tableName = "apm_aplicacionTabla")
public final class AplicacionTablaHC extends AbstractEntity  {
	
//	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
//	private Aplicacion aplicacion;
//	
//	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
//	private TablaVersion tablaVersion;

	@DatabaseField(canBeNull = false)
	private int orden;

	@DatabaseField(canBeNull = false)
	private String tabla;
	
	@DatabaseField(canBeNull = false)
	private boolean permiteActParcial;

//	public Aplicacion getAplicacion() {
//		return aplicacion;
//	}

	public int getOrden() {
		return orden;
	}

	public boolean getPermiteActParcial() {
		return permiteActParcial;
	}

//	public TablaVersion getTablaVersion() {
//		return tablaVersion;
//	}
//
//	public void setAplicacion(Aplicacion aplicacion) {
//		this.aplicacion = aplicacion;
//	}

	public void setOrden(int orden) {
		this.orden = orden;
	}

	public void setPermiteActParcial(boolean permiteActParcial) {
		this.permiteActParcial = permiteActParcial;
	}
	
	public void setTabla(String tabla) {
		this.tabla = tabla;
	}
	
	@Override
	public String toString() {
		String buffer = ""; 
		buffer += String.format("Entidad --> {0} ", getClass().getSimpleName());
//		buffer += String.format("Aplicacion: {0}, ", aplicacion.getDescripcion());
//		buffer += String.format("Version de Tabla: {0}, ", tablaVersion.getVersion());
		buffer += String.format("Permite Actualización Parcial: {0}, ", getPermiteActParcial());
		buffer += String.format("Orden: {0}, ", orden);
		buffer += "\n";
		return buffer;
	}
}

