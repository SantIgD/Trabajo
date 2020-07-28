package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;


@DatabaseTable(tableName = "apm_gestionAccion")
public final class AccionHC extends AbstractEntity {
	
	@DatabaseField(canBeNull = false)
	private int aplicacionID;

	@DatabaseField(canBeNull = false)
	private int aplicacionPerfilID;
	
	@DatabaseField(canBeNull = false)
	private int aplicacionPerfilSeccionCampoID;
	
	@DatabaseField
	private String opciones;
	
	@DatabaseField		
	private String acciones;
	
	public int getAplicacionID() {
		return aplicacionID;
	}

	public void setAplicacionID(int aplicacionID) {
		this.aplicacionID = aplicacionID;
	}

	public int getAplicacionPerfilID() {
		return aplicacionPerfilID;
	}

	public void setAplicacionPerfilID(int aplicacionPerfilID) {
		this.aplicacionPerfilID = aplicacionPerfilID;
	}

	public int getAplPerfilSeccionCampoID() {
		return aplicacionPerfilSeccionCampoID;
	}

	public void setAplPerfilSeccionCampoID(int aplicacionPerfilSeccionCampoID) {
		this.aplicacionPerfilSeccionCampoID = aplicacionPerfilSeccionCampoID;
	}
	
	public String getOpciones() {
		return opciones;
	}

	public void setOpciones(String opciones) {
		this.opciones = opciones;
	}
	
	public String getAcciones() {
		return acciones;
	}

	public void setAcciones(String acciones) {
		this.acciones = acciones;
	}
}