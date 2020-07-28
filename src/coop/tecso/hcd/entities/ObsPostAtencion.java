package coop.tecso.hcd.entities;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.udaa.domain.base.AbstractEntity;

/**
 * 
 * @author leonardo.fagnano
 *
 */
@DatabaseTable(tableName = "hcd_obsPostAtencion")
public final class ObsPostAtencion extends AbstractEntity {
	
	@DatabaseField(canBeNull = false, foreign = true)
	private Atencion atencion;

	@DatabaseField(canBeNull = false)
	private String idUsuarioCreador;
	
	@DatabaseField(canBeNull = false, dataType = DataType.DATE)
	private Date horaObservacion;
	
	@DatabaseField(canBeNull = false)
	private String observacion;

	public Atencion getAtencion() {
		return atencion;
	}

	public void setAtencion(Atencion atencion) {
		this.atencion = atencion;
	}

	public String getIdUsuarioCreador() {
		return idUsuarioCreador;
	}

	public void setIdUsuarioCreador(String idUsuarioCreador) {
		this.idUsuarioCreador = idUsuarioCreador;
	}

	public Date getHoraObservacion() {
		return horaObservacion;
	}

	public void setHoraObservacion(Date horaObservacion) {
		this.horaObservacion = horaObservacion;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
}