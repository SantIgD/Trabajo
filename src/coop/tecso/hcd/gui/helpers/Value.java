package coop.tecso.hcd.gui.helpers;

import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

public final class Value {

	private AplPerfilSeccionCampo campo = null;
	private AplPerfilSeccionCampoValor campoValor = null;
	private AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;
	private String valor = null;
	private String codigoEntidadBusqueda = null; 
	private  byte[] data;
	private byte[] thumbnail;
	private byte[] extraFile;
	
	public Value() {
		super();
	}
	public Value(AplPerfilSeccionCampo campo, AplPerfilSeccionCampoValor campoValor, AplPerfilSeccionCampoValorOpcion campoValorOpcion, String valor, String codigoEntidadBusqueda) {
		super();
		this.campo = campo;
		this.campoValor = campoValor;
		this.campoValorOpcion = campoValorOpcion;
		this.valor = valor;
		this.codigoEntidadBusqueda = codigoEntidadBusqueda;
		
	}
	public Value(AplPerfilSeccionCampo campo, AplPerfilSeccionCampoValor campoValor, AplPerfilSeccionCampoValorOpcion campoValorOpcion, String valor, String codigoEntidadBusqueda, byte[] data) {
		super();
		this.campo = campo;
		this.campoValor = campoValor;
		this.campoValorOpcion = campoValorOpcion;
		this.valor = valor;
		this.codigoEntidadBusqueda = codigoEntidadBusqueda;
		this.data = data;
	}
	public Value(AplPerfilSeccionCampo campo, AplPerfilSeccionCampoValor campoValor, AplPerfilSeccionCampoValorOpcion campoValorOpcion, String valor, String codigoEntidadBusqueda, byte[] data, byte[] thumbnail) {
		super();
		this.campo = campo;
		this.campoValor = campoValor;
		this.campoValorOpcion = campoValorOpcion;
		this.valor = valor;
		this.codigoEntidadBusqueda = codigoEntidadBusqueda;
		this.data = data;
		this.thumbnail = thumbnail;
	}

	public byte[] getImagen() {
		return data;
	}
	public void setImagen(byte[] imagen) {
		this.data = imagen;
	}	

	public AplPerfilSeccionCampo getCampo() {
		return campo;
	}
	public void setCampo(AplPerfilSeccionCampo campo) {
		this.campo = campo;
	}
	public AplPerfilSeccionCampoValor getCampoValor() {
		return campoValor;
	}
	public void setCampoValor(AplPerfilSeccionCampoValor campoValor) {
		this.campoValor = campoValor;
	}
	public AplPerfilSeccionCampoValorOpcion getCampoValorOpcion() {
		return campoValorOpcion;
	}
	public void setCampoValorOpcion(
			AplPerfilSeccionCampoValorOpcion campoValorOpcion) {
		this.campoValorOpcion = campoValorOpcion;
	}
	public String getValor() {
		return valor;
	}
	public void setValor(String valor) {
		this.valor = valor;
	}
	public String getCodigoEntidadBusqueda() {
		return codigoEntidadBusqueda;
	}
	public void setCodigoEntidadBusqueda(String codigoEntidadBusqueda) {
		this.codigoEntidadBusqueda = codigoEntidadBusqueda;
	}
	public String getTratamiento(){

		if(this.campo.getCampo().getTratamiento() != null && Tratamiento.getByCod(this.campo.getCampo().getTratamiento()).equals(Tratamiento.DESCONOCIDO) && this.campo.getCampo().getTratamientoDefault() != null){
			return this.campo.getCampo().getTratamientoDefault();
		}

		return this.campo.getCampo().getTratamiento();
	}

	public byte[] getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		this.thumbnail = thumbnail;
	}

	public byte[] getExtraFile() {
		return extraFile;
	}

	public void setExtraFile(byte[] extraFile) {
		this.extraFile = extraFile;
	}
}
