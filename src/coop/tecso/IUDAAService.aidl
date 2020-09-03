package coop.tecso;

interface IUDAAService {
	
	String getCurrentUser();
	
	String getServerURL();

	boolean isTransTypePartial();
	
	String getAplicacionPerfilById(int aplicacionPerfilId); 
	
	String sync(String clazz, String table, int version);
	
	String fetchUser(int userID);
	
	String login(String username, String password);
	
	String getDispositivoMovil();
	
	String getNotificacionById(int notificacionID);
	
	void changeSession(String username, String password);
	
	boolean hasAccess(int usuarioID, String codAplicacion);	
	
	String getIdAplicacionPerfilDefaultBy(String codAplicacion);
	
	String getCampoBy(int campoId, int aplicacionPerfilId);
	
	String getAplPerfilSeccionCampoById(int aplPerfilSeccionCampoId);
	
	void sendError(int tipoReg);
	
	String generateReport(String jsonData, String jsonSection, String jsonTemplate);
	
	String getListBinarioPathBy(String codAplicacion, String tipoBinario);
	
	String exportDataToFile(String jSonData);
	
	String getLastAplicacionBinarioVersionByCodigoAplicacion(String appCode); 
	
	String confirmForceUpdate(String appCode, boolean lastAppToUpdate);
		
	void updateApplicationSync(String appCode);
				
	String rawQueryList(String sql, String selectionArgs);
	
	String rawQueryCiudades(String sql, String selectionArgs);
	
	String query(String table, String columns, String selection, String selectionArgs, String groupBy, String having, String orderBy);
}