package coop.tecso.hcd.entities;

import com.google.gson.JsonObject;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.udaa.domain.base.AbstractEntity;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;


@DatabaseTable(tableName = "hcd_atencionValor")
public final class AtencionValor extends AbstractEntity {

	@DatabaseField(canBeNull = false, foreign = true)
	private Atencion atencion;
	
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = false)
	private AplPerfilSeccionCampo aplPerfilSeccionCampo;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false)
	private AplPerfilSeccionCampoValor aplPerfilSeccionCampoValor;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = false)
	private AplPerfilSeccionCampoValorOpcion aplPerfilSeccionCampoValorOpcion;
	
	@DatabaseField
	private String valor;
	
	@DatabaseField
	private String codigoEntidadBusqueda;
	
	@DatabaseField()
	private boolean syncronize;
	
	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] imagen;

	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] thumbnail;

	@DatabaseField(dataType = DataType.BYTE_ARRAY)
	private byte[] extraFile;

	@DatabaseField()
	private boolean syncImagen;

	public Atencion getAtencion() {
		return atencion;
	}

	public void setAtencion(Atencion atencion) {
		this.atencion = atencion;
	}

	public AplPerfilSeccionCampo getAplPerfilSeccionCampo() {
		return aplPerfilSeccionCampo;
	}

	public void setAplPerfilSeccionCampo(AplPerfilSeccionCampo aplPerfilSeccionCampo) {
		this.aplPerfilSeccionCampo = aplPerfilSeccionCampo;
	}

	public AplPerfilSeccionCampoValor getAplPerfilSeccionCampoValor() {
		return aplPerfilSeccionCampoValor;
	}

	public void setAplPerfilSeccionCampoValor(
			AplPerfilSeccionCampoValor aplPerfilSeccionCampoValor) {
		this.aplPerfilSeccionCampoValor = aplPerfilSeccionCampoValor;
	}

	public AplPerfilSeccionCampoValorOpcion getAplPerfilSeccionCampoValorOpcion() {
		return aplPerfilSeccionCampoValorOpcion;
	}

	public void setAplPerfilSeccionCampoValorOpcion(
			AplPerfilSeccionCampoValorOpcion aplPerfilSeccionCampoValorOpcion) {
		this.aplPerfilSeccionCampoValorOpcion = aplPerfilSeccionCampoValorOpcion;
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

	public boolean isSyncronize() {
		return syncronize;
	}

	public void setSyncronize(boolean syncronize) {
		this.syncronize = syncronize;
	}

	public byte[] getImagen() {
		return imagen;
	}

	public void setImagen(byte[] imagen) {
		this.imagen = imagen;
	}
	
	public JsonObject toJson() {
		JsonObject jo = toJsonWithoutImage().getAsJsonObject();
		if(imagen != null)	{
			System.gc();
			jo.addProperty("Imagen", Utils.encodeToBase64(imagen));
		}
		if(thumbnail != null) {
			System.gc();
			jo.addProperty("ExtraFile", Utils.encodeToBase64(thumbnail));
		}
		System.gc();

		return jo;
	}

	public byte[] getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		this.thumbnail = thumbnail;
	}

	public boolean isSyncImagen() {
		return syncImagen;
	}

	public void setSyncImagen(boolean syncImagen) {
		this.syncImagen = syncImagen;
	}

	public byte[] getExtraFile() {
		return extraFile;
	}

	public void setExtraFile(byte[] extraFile) {
		this.extraFile = extraFile;
	}

	public JsonObject toJsonWithoutImage() {
		JsonObject jo = new JsonObject();
		jo.addProperty("BussID", getId());
		
		if(getAplPerfilSeccionCampo() != null)
			jo.addProperty("AplPerfilSeccionCampoID", getAplPerfilSeccionCampo().getId());
		if(getAplPerfilSeccionCampoValor() != null)
			jo.addProperty("AplPerfilSeccionCampoValorID", getAplPerfilSeccionCampoValor().getId());
		if(getAplPerfilSeccionCampoValorOpcion() != null)
			jo.addProperty("AplPerfilSeccionCampoValorOpcionID", getAplPerfilSeccionCampoValorOpcion().getId());		
		jo.addProperty("Valor", getValor());
		jo.addProperty("CodigoEntidadBusqueda", getCodigoEntidadBusqueda());
		jo.addProperty("ModificationTimeStamp", Utils.formatDateToJSON(getModificationTimeStamp()));
		jo.addProperty("ModificationUser", getModificationUser());
		jo.addProperty("Deleted", false);
		jo.addProperty("Version", getVersion());
		return jo;
	}

}