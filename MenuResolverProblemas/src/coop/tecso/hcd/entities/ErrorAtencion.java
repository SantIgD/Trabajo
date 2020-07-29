package coop.tecso.hcd.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;


@DatabaseTable(tableName = "hcd_errorAtencion")
public final class ErrorAtencion extends AbstractEntity {

	@DatabaseField(canBeNull = false)
	private String tipo;
	
	@DatabaseField(canBeNull = false)
	private String codigo;

	@DatabaseField		
	private String descripcionCorta;
	
	@DatabaseField		
	private String descripcionLarga;
	
	@DatabaseField(canBeNull = false)
	private boolean siempreVisible;
	
	@DatabaseField(canBeNull = false)
	private boolean forzarLectura;
	
	@DatabaseField		
	private String soundFileName;
	
	@DatabaseField		
	private String iconFileName;

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcionCorta() {
		return descripcionCorta;
	}

	public void setDescripcionCorta(String descripcionCorta) {
		this.descripcionCorta = descripcionCorta;
	}

	public String getDescripcionLarga() {
		return descripcionLarga;
	}

	public void setDescripcionLarga(String descripcionLarga) {
		this.descripcionLarga = descripcionLarga;
	}

	public boolean isSiempreVisible() {
		return siempreVisible;
	}

	public void setSiempreVisible(boolean siempreVisible) {
		this.siempreVisible = siempreVisible;
	}

	public boolean isForzarLectura() {
		return forzarLectura;
	}

	public void setForzarLectura(boolean forzarLectura) {
		this.forzarLectura = forzarLectura;
	}
	
	public String getSoundFileName() {
		return soundFileName;
	}

	public void setSoundFileName(String soundFileName) {
		this.soundFileName = soundFileName;
	}

	public String getIconFileName() {
		return iconFileName;
	}

	public void setIconFileName(String iconFileName) {
		this.iconFileName = iconFileName;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(String.format("Entidad --> {%s} ", getClass().getSimpleName()));
		buffer.append(String.format("Tipo: {%s}, ", tipo));
		buffer.append(String.format("Codigo: {%s}, ", codigo));
		buffer.append(String.format("Descripcion corta: {%s}, ", descripcionCorta));
		buffer.append(String.format("Descripcion larga: {%s}, ", descripcionLarga));
		buffer.append(String.format("Siempre visible: {%s}, ", siempreVisible));
		buffer.append(String.format("Forzar lectura: {%s}, ", forzarLectura));
		buffer.append(String.format("Icono: {%s}, ", iconFileName));
		buffer.append(String.format("Sonido: {%s}, ", soundFileName));
		buffer.append("\n");
		return buffer.toString();
	}
}