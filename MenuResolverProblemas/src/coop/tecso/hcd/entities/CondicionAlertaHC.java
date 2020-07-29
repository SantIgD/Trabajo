package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;


@DatabaseTable(tableName = "apm_condicionAlerta")
public final class CondicionAlertaHC extends AbstractEntity {

	@DatabaseField(canBeNull = false)
	private String nombre;
	
	@DatabaseField(canBeNull = false)
	private String condicion;

	@DatabaseField(canBeNull = false)
	private int aplicacionPerfilID;
	
	@DatabaseField		
	private int campoFoco;
	
	@DatabaseField(canBeNull = false, foreign = true)	
	private ErrorAtencion alertaHcd;
	
	@DatabaseField(canBeNull = false)
	private boolean bloqueante;
	
	@DatabaseField		
	private String camposMarcados;

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getCondicion() {
		return condicion;
	}

	public void setCondicion(String condicion) {
		this.condicion = condicion;
	}

	public int getAplicacionPerfilID() {
		return aplicacionPerfilID;
	}

	public void setAplicacionPerfilID(int aplicacionPerfilID) {
		this.aplicacionPerfilID = aplicacionPerfilID;
	}

	public int getCampoFoco() {
		return campoFoco;
	}

	public void setCampoFoco(int campoFoco) {
		this.campoFoco = campoFoco;
	}

	public ErrorAtencion getAlertaHcd() {
		return alertaHcd;
	}

	public void setAlertaHcd(ErrorAtencion alertaHcd) {
		this.alertaHcd = alertaHcd;
	}
	
	public boolean getBloqueante() {
		return bloqueante;
	}

	public void setBloqueante(boolean bloqueante) {
		this.bloqueante = bloqueante;
	}
	
	public String getCamposMarcados() {
		return camposMarcados;
	}

	public void setCamposMarcados(String camposMarcados) {
		this.camposMarcados = camposMarcados;
	}

//	@Override
//	public String toString() {
//		StringBuffer buffer = new StringBuffer();
//		buffer.append(String.format("Entidad --> {%s} ", getClass().getSimpleName()));
//		buffer.append(String.format("Tipo: {%s}, ", tipo));
//		buffer.append(String.format("Codigo: {%s}, ", codigo));
//		buffer.append(String.format("Descripcion corta: {%s}, ", descripcionCorta));
//		buffer.append(String.format("Descripcion larga: {%s}, ", descripcionLarga));
//		buffer.append(String.format("Siempre visible: {%s}, ", siempreVisible));
//		buffer.append(String.format("Forzar lectura: {%s}, ", forzarLectura));
//		buffer.append(String.format("Icono: {%s}, ", iconFileName));
//		buffer.append(String.format("Sonido: {%s}, ", soundFileName));
//		buffer.append("\n");
//		return buffer.toString();
//	}
}