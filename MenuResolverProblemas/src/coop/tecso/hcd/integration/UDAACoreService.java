package coop.tecso.hcd.integration;

import java.util.Map;

import coop.tecso.udaa.domain.aplicaciones.AplicacionBinarioVersion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.base.AbstractEntity;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

public interface UDAACoreService {

	public UsuarioApm login(String username, String password) throws Exception;
	
	public String getServerURL() throws Exception;

    public boolean isTransTypePartial() throws Exception;
	
	public AplicacionPerfil getAplicacionPerfilById(int aplicacionPerfilId) throws Exception;

	public UsuarioApm getCurrentUser() throws Exception;
	
	public UsuarioApm getUserById(int userID) throws Exception;

	public  <T extends AbstractEntity> void synchronize(Class<T> clazz, String tableName) throws Exception;
	
	public DispositivoMovil getDispositivoMovil() throws Exception;
	
	public void changeSession(String username, String password) throws Exception; 
	
	public boolean hasAccess(int usuarioID, String codAplicacion) throws Exception; 
	
	public Integer getIdAplicacionPerfilDefaultBy(String codAplicacion) throws Exception;
	
	public Campo getCampoBy(int campoId, int aplicacionPerfilId) throws Exception;
	
	public AplPerfilSeccionCampo getAplPerfilSeccionCampoById(int aplPerfilSeccionCampoId) throws Exception;

	public void sendError (int tipoReg) throws Exception;
	
	public String generateReport(Map<String,String> formData, Map<String, Boolean> mapSeccion, String template) throws Exception;
	
	public String exportDataToFile(String jSonData) throws Exception;
	
	public AplicacionBinarioVersion getAplicacionBinarioVersion();
	
	public DispositivoMovil confirmForceUpdate();
	
	public void generateACRA_LOG(String message, String operation);

	void updateApplicationSync();
}
