package coop.tecso.hcd.entities;

import java.util.Collection;
import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.base.AbstractEntity;


@DatabaseTable(tableName = "hcd_atencion")
public final class Atencion extends AbstractEntity {
	
	@DatabaseField(foreign = true)
	private Atencion atencionPrincipal;
	
	@DatabaseField(canBeNull = false, foreign = true)
	private EstadoAtencion estadoAtencion;
	
	@DatabaseField(canBeNull = false, foreign = true)
	private AplicacionPerfil aplicacionPerfil;

	@DatabaseField(canBeNull = false)
	private int dispositivoMovilID;

	@DatabaseField		
	private int atencionDMID;
	
	@DatabaseField
	private int idUsuarioCierreProvisorio;
	
	@DatabaseField
	private int idUsuarioCierreDefinitivo;
	
	@DatabaseField(foreign = true)
	private MotivoCierreAtencion motivoCierreAtencion;
	
	@DatabaseField
	private int idUsuarioApmAsignado;
	
	@DatabaseField(dataType = DataType.DATE)
	private Date asignacionTimeStamp;
	
	@DatabaseField
	private String observacionCierreDefinitivo;
	
	@DatabaseField
	private String observacionPostAtencion;
	
	@DatabaseField
	private String listaCodigoAlertaMedica;
	
	@DatabaseField
	private String numeroAtencion;
	
	@DatabaseField
	private String domicilioAtencion;
	
	@DatabaseField
	private String codigoAtencion;
	
	@DatabaseField(dataType = DataType.DATE)
	private Date fechaAtencion;
	
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] firmaDigital;
	
	@ForeignCollectionField(eager = false)
	private Collection<AtencionValor> atencionValores;
	
	@DatabaseField(dataType = DataType.DATE)
	private Date fechaDescarga;
	
	@DatabaseField(dataType = DataType.DATE)
	private Date fechaCierreProvisorio;
	
	@DatabaseField(dataType = DataType.DATE)
	private Date fechaCierreDefinitivo;
	
	@DatabaseField(dataType = DataType.DATE)
	private Date fechaAnulacion;
	
	@DatabaseField(dataType = DataType.DATE)
	private Date fechaTrasmision;

	@DatabaseField(foreign = true)
    private Despachador despachador;
	
	@DatabaseField		
	private int atencionServerID;

	@DatabaseField()
	private boolean syncfirmaDigital;

	@DatabaseField
	private boolean syncHeader; 

	@DatabaseField		
	private int idEstado;
	
	//No mapear!
	private String userName;
	
	//No mapear!
	private String passWord;
	
	public EstadoAtencion getEstadoAtencion() {
		return estadoAtencion;
	}

	public void setEstadoAtencion(EstadoAtencion estadoAtencion) {
		this.estadoAtencion = estadoAtencion;
	}

	public Atencion getAtencionPrincipal() {
		return atencionPrincipal;
	}

	public void setAtencionPrincipal(Atencion atencionPrincipal) {
		this.atencionPrincipal = atencionPrincipal;
	}

	public MotivoCierreAtencion getMotivoCierreAtencion() {
		return motivoCierreAtencion;
	}

	public void setMotivoCierreAtencion(MotivoCierreAtencion motivoCierreAtencion) {
		this.motivoCierreAtencion = motivoCierreAtencion;
	}

	public AplicacionPerfil getAplicacionPerfil() {
		return aplicacionPerfil;
	}

	public void setAplicacionPerfil(AplicacionPerfil aplicacionPerfil) {
		this.aplicacionPerfil = aplicacionPerfil;
	}

	public int getIdUsuarioCierreProvisorio() {
		return idUsuarioCierreProvisorio;
	}

	public void setIdUsuarioCierreProvisorio(int idUsuarioCierreProvisorio) {
		this.idUsuarioCierreProvisorio = idUsuarioCierreProvisorio;
	}

	public int getIdUsuarioCierreDefinitivo() {
		return idUsuarioCierreDefinitivo;
	}

	public void setIdUsuarioCierreDefinitivo(int idUsuarioCierreDefinitivo) {
		this.idUsuarioCierreDefinitivo = idUsuarioCierreDefinitivo;
	}

	public int getIdUsuarioApmAsignado() {
		return idUsuarioApmAsignado;
	}

	public void setIdUsuarioApmAsignado(int idUsuarioApmAsignado) {
		this.idUsuarioApmAsignado = idUsuarioApmAsignado;
	}

	public Date getAsignacionTimeStamp() {
		return asignacionTimeStamp;
	}

	public void setAsignacionTimeStamp(Date asignacionTimeStamp) {
		this.asignacionTimeStamp = asignacionTimeStamp;
	}

	public int getDispositivoMovilID() {
		return dispositivoMovilID;
	}

	public void setDispositivoMovilID(int dispositivoMovilID) {
		this.dispositivoMovilID = dispositivoMovilID;
	}

	public Collection<AtencionValor> getAtencionValores() {
		return atencionValores;
	}

	public void setAtencionValores(Collection<AtencionValor> atencionValores) {
		this.atencionValores = atencionValores;
	}

	public String getObservacionCierreDefinitivo() {
		return observacionCierreDefinitivo;
	}

	public void setObservacionCierreDefinitivo(String observacionCierreDefinitivo) {
		this.observacionCierreDefinitivo = observacionCierreDefinitivo;
	}

	public String getObservacionPostAtencion() {
		return observacionPostAtencion;
	}

	public void setObservacionPostAtencion(String observacionPostAtencion) {
		this.observacionPostAtencion = observacionPostAtencion;
	}

	public String getListaCodigoAlertaMedica() {
		return listaCodigoAlertaMedica;
	}

	public void setListaCodigoAlertaMedica(String listaCodigoAlertaMedica) {
		this.listaCodigoAlertaMedica = listaCodigoAlertaMedica;
	}

	public int getAtencionDMID() {
		return atencionDMID;
	}

	public void setAtencionDMID(int atencionDMID) {
		this.atencionDMID = atencionDMID;
	}

	public String getNumeroAtencion() {
		return numeroAtencion;
	}

	public void setNumeroAtencion(String numeroAtencion) {
		this.numeroAtencion = numeroAtencion;
	}

	public String getDomicilioAtencion() {
		return domicilioAtencion;
	}

	public void setDomicilioAtencion(String domicilioAtencion) {
		this.domicilioAtencion = domicilioAtencion;
	}

	public Date getFechaAtencion() {
		return fechaAtencion;
	}

	public void setFechaAtencion(Date fechaAtencion) {
		this.fechaAtencion = fechaAtencion;
	}

	public byte[] getFirmaDigital() {
		return firmaDigital;
	}

	public void setFirmaDigital(byte[] firmaDigital) {
		this.firmaDigital = firmaDigital;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getCodigoAtencion() {
		return codigoAtencion;
	}

	public void setCodigoAtencion(String codigoAtencion) {
		this.codigoAtencion = codigoAtencion;
	}

	public Date getFechaDescarga() {
		return fechaDescarga;
	}

	public void setFechaDescarga(Date fechaDescarga) {
		this.fechaDescarga = fechaDescarga;
	}

	public Date getFechaCierreProvisorio() {
		return fechaCierreProvisorio;
	}

	public void setFechaCierreProvisorio(Date fechaCierreProvisorio) {
		this.fechaCierreProvisorio = fechaCierreProvisorio;
	}

	public Date getFechaCierreDefinitivo() {
		return fechaCierreDefinitivo;
	}

	public void setFechaCierreDefinitivo(Date fechaCierreDefinitivo) {
		this.fechaCierreDefinitivo = fechaCierreDefinitivo;
	}

	public Date getFechaAnulacion() {
		return fechaAnulacion;
	}

	public void setFechaAnulacion(Date fechaAnulacion) {
		this.fechaAnulacion = fechaAnulacion;
	}

	public Date getFechaTrasmision() {
		return fechaTrasmision;
	}

	public void setFechaTrasmision(Date fechaTrasmision) {
		this.fechaTrasmision = fechaTrasmision;
	}

    public Despachador getDespachador() {
        return despachador;
    }

    public void setDespachador(Despachador despachador) { 
    	this.despachador = despachador; 
    }

	public int getAtencionServerID() {
		return atencionServerID;
	}

	public void setAtencionServerID(int atencionServerID) {
		this.atencionServerID = atencionServerID;
	}

	public boolean isSyncfirmaDigital() {
		return syncfirmaDigital;
	}

	public void setSyncfirmaDigital(boolean syncfirmaDigital) {
		this.syncfirmaDigital = syncfirmaDigital;
	}

	public boolean isSyncHeader() {
		return syncHeader;
	}

	public void setSyncHeader(boolean syncHeader) {
		this.syncHeader = syncHeader;
	}

	public int getIdEstado() {
		return idEstado;
	}

	public void setIdEstado(int idEstado) {
		this.idEstado = idEstado;
	}

	public boolean enPreparacion() {
		return this.getEstadoAtencion().getId() == EstadoAtencion.ID_EN_PREPARACION;
	}

	public boolean cerradaDefinitiva() {
		return this.getEstadoAtencion().getId() == EstadoAtencion.ID_CERRADA_DEFINITIVA;
	}

	public boolean cerradaProvisoria() {
		return this.getEstadoAtencion().getId() == EstadoAtencion.ID_CERRADA_PROVISORIA;
	}

	public JsonObject toJSON() {
		Atencion atencion = this;
		JsonObject joAtencion = toJSONWithoutFirmas();
		if(atencion.getFirmaDigital() != null)
			joAtencion.addProperty("FirmaDigital", Utils.encodeToBase64(atencion.getFirmaDigital()));
		return joAtencion;
	}
	
	
	public JsonObject toJSONWithoutFirmas() {
		Atencion atencion = this;
		JsonObject joAtencion = new JsonObject();
		joAtencion.addProperty("BussID", atencion.getAtencionServerID());
		joAtencion.addProperty("AtencionDMID", atencion.getAtencionDMID());
		if(atencion.getAtencionPrincipal() != null)
			joAtencion.addProperty("AtencionPrincipalID", atencion.getAtencionPrincipal().getId());
		if(atencion.getAplicacionPerfil() != null)
			joAtencion.addProperty("AplicacionPerfilID", atencion.getAplicacionPerfil().getId());
		if(atencion.getEstadoAtencion() != null)
			joAtencion.addProperty("EstadoAtencionID", atencion.getEstadoAtencion().getId());
		joAtencion.addProperty("DispositivoMovilID", atencion.getDispositivoMovilID());
		joAtencion.addProperty("AsignacionTimeStamp", Utils.formatDateToJSON(atencion.getAsignacionTimeStamp()));
		joAtencion.addProperty("ObservacionPostAtencion", atencion.getObservacionPostAtencion());
		joAtencion.addProperty("ObservacionCierreDefinitivo", atencion.getObservacionCierreDefinitivo());
		joAtencion.addProperty("ListaCodigoAlertaMedica", atencion.getListaCodigoAlertaMedica());
		if(atencion.getMotivoCierreAtencion() != null)
			joAtencion.addProperty("MotivoCierreAtencionID", atencion.getMotivoCierreAtencion().getId());
		if(atencion.getIdUsuarioApmAsignado() == 0 ){
			joAtencion.addProperty("UsuarioApmAsignadoID", atencion.getIdUsuarioCierreDefinitivo());
		}
		else {
			joAtencion.addProperty("UsuarioApmAsignadoID", atencion.getIdUsuarioApmAsignado());
		}
		joAtencion.addProperty("UsuarioApmProvisorioID", atencion.getIdUsuarioCierreProvisorio());
		joAtencion.addProperty("UsuarioApmDefinitivoID", atencion.getIdUsuarioCierreDefinitivo());
		joAtencion.addProperty("NumeroAtencion", atencion.getNumeroAtencion());
		joAtencion.addProperty("DomicilioAtencion", atencion.getDomicilioAtencion());
		joAtencion.addProperty("CodigoAtencion", atencion.getCodigoAtencion());
		joAtencion.addProperty("FechaAtencion", Utils.formatDateToJSON(atencion.getFechaAtencion()));
		joAtencion.addProperty("FechaDescarga", Utils.formatDateToJSON(atencion.getFechaDescarga()));
		joAtencion.addProperty("FechaCierreProvisorio", Utils.formatDateToJSON(atencion.getFechaCierreProvisorio()));
		joAtencion.addProperty("FechaCierreDefinitivo", Utils.formatDateToJSON(atencion.getFechaCierreDefinitivo()));
		joAtencion.addProperty("FechaAnulacion", Utils.formatDateToJSON(atencion.getFechaAnulacion()));
		joAtencion.addProperty("FechaTrasmision", Utils.formatDateToJSON(atencion.getFechaTrasmision()));
		if(atencion.getDespachador() != null)
			joAtencion.addProperty("DespachadorID", atencion.getDespachador().getId());
		joAtencion.addProperty("ModificationTimeStamp", Utils.formatDateToJSON(atencion.getModificationTimeStamp()));
		joAtencion.addProperty("ModificationUser", atencion.getModificationUser());
		joAtencion.addProperty("Deleted", false);
		joAtencion.addProperty("Version", atencion.getVersion());
		joAtencion.addProperty("username", userName);
		joAtencion.addProperty("password", passWord);

		JsonArray joAtencionValores = new JsonArray();
		for (AtencionValor av: atencion.getAtencionValores()) {
			joAtencionValores.add(av.toJsonWithoutImage());
		}
		joAtencion.add("AtencionValores", joAtencionValores);
		return joAtencion;
	}

	public boolean allImagesSincronized() {
		if (getFirmaDigital() != null && !isSyncfirmaDigital()) {
			return false;
		}

		for (AtencionValor atencionValor: atencionValores) {
			if (atencionValor.getImagen() != null && !atencionValor.isSyncImagen())	{
				return false;
			}
		}

		return true;
	}

}