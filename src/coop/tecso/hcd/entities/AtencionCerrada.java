package coop.tecso.hcd.entities;

import java.util.Date;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "hcd_atencionCerrada")
public class AtencionCerrada {

	@DatabaseField(generatedId = true, id = false, allowGeneratedIdInsert = false)
	private int id;
	public int getId() {
		return id;
	}	
	public void setId(int id) {
		this.id = id;
	}
	
	@DatabaseField		
	private int idAtencion;
	public int getIdAtencion() {
		return idAtencion;
	}	
	public void setIdAtencion(int idAtencion) {
		this.idAtencion = idAtencion;
	}
		
	@DatabaseField(dataType = DataType.DATE)
	private Date fechaCierreDefinitivo;
	public Date getFechaCierreDefinitivo() {
		return fechaCierreDefinitivo;
	}
	public void setFechaCierreDefinitivo(Date fechaCierreDefinitivo) {
		this.fechaCierreDefinitivo = fechaCierreDefinitivo;
	}	
	
	@DatabaseField(dataType = DataType.DATE)
	private Date fechaAnulacion;
	public Date getFechaAnulacion() {
		return fechaAnulacion;
	}
	public void setFechaAnulacion(Date fechaAnulacion) {
		this.fechaAnulacion = fechaAnulacion;
	}
	
	@DatabaseField		
	private int idEstadoAtencion;
	public int getIdEstadoAtencion() {
		return idEstadoAtencion;
	}
	public void setIdEstadoAtencion(int idEstadoAtencion) {
		this.idEstadoAtencion = idEstadoAtencion;
	}	
	
	@DatabaseField		
	private boolean isSyncHeader;
	public boolean getIsSyncHeader() {
		return isSyncHeader;
	}
	public void setIsSyncHeader(boolean isSyncHeader) {
		this.isSyncHeader = isSyncHeader;
	}
	
	@DatabaseField		
	private String jsonPreGuardado;
	public String getJsonPreGuardado() {
		return jsonPreGuardado;
	}
	public void setJsonPreGuardado(String jsonPreGuardado) {
		this.jsonPreGuardado = jsonPreGuardado;
	}
	
	@DatabaseField		
	private String jsonPostGuardado;
	public String getJsonPostGuardado() {
		return jsonPostGuardado;
	}
	public void setJsonPostGuardado(String jsonPostGuardado) {
		this.jsonPostGuardado = jsonPostGuardado;
	}

	public boolean enPreparacion() {
		return this.getIdEstadoAtencion() == EstadoAtencion.ID_EN_PREPARACION;
	}

}
