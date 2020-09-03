package coop.tecso.udaa.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.graph.GraphAdapterBuilder;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import coop.tecso.udaa.tasks.InitializeAplTask;
import coop.tecso.udaa.activities.NotificacionesActivity;
import coop.tecso.udaa.base.UDAAException;
import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionBinarioVersion;
import coop.tecso.udaa.domain.base.AbstractEntity;
import coop.tecso.udaa.domain.base.TablaVersion;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.domain.trazabilidad.UbicacionGPS;
import coop.tecso.udaa.domain.util.DeviceContext;
import coop.tecso.udaa.persistence.DatabaseHelper;
import coop.tecso.udaa.utils.Constants;
import coop.tecso.udaa.utils.ErrorConstants;

@SuppressWarnings({"ResultOfMethodCallIgnored", "TrustAllX509TrustManager"})
public final class WebServiceDAO  {

	/**
	 * Singleton instance.
	 */
	@SuppressLint("StaticFieldLeak")
	private static WebServiceDAO INSTANCE;

	public static synchronized WebServiceDAO getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new WebServiceDAO(context);
		}
		else {
			INSTANCE.refreshSettingsWebServiceDAO(context);
		}
		return INSTANCE;
	}

	public static UsuarioApm login(String username, String password, Integer deviceID) throws Exception{
		WebService ws = null;
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("username", username);
			params.put("password", password);
			params.put("dispositivoMovilID", deviceID);

			ws = new WebService(WS_URL);		

			String response = ws.webInvoke("Login", params);

			Log.d(LOG_TAG, response);

			if(response==null){
				return null;
			}


			if(response.contains("Error-103")){
				throw new UDAAException(ErrorConstants.ERROR_103);
			}else if(response.contains("Error-104")){
				throw new UDAAException(ErrorConstants.ERROR_104);
			}else if(response.contains("Error-105")){
				throw new UDAAException(ErrorConstants.ERROR_105);
			}else if(response.contains("Error-106")){
				throw new UDAAException(ErrorConstants.ERROR_106);
			}else if(response.contains("Error-107")){
				throw new UDAAException(ErrorConstants.ERROR_107);
			}else if(response.contains("Error-108")){
				throw new UDAAException(ErrorConstants.ERROR_108);
			}else if(response.toUpperCase().contains("ERROR")){
				throw new UDAAException(ErrorConstants.ERROR_103);
			}

			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()));

			return builder.create().fromJson(response, UsuarioApm.class);

		}catch (Exception e) {
			Log.d(LOG_TAG, "**ERROR**", e);
			throw e;
		} finally {
			if (ws != null) ws.abort();
		}
	}

	public Map<String, Integer> syncState(int dispositivoMovilID) throws RuntimeException {
		RuntimeExceptionDao<TablaVersion, Integer> entityDAO = database.getRuntimeExceptionDao(TablaVersion.class);

		Map<String, Integer> localState = new HashMap<>();

		for (TablaVersion tablaVersion: entityDAO.queryForAll()) {
		    String tableName = tablaVersion.getTabla().toLowerCase();
			localState.put(tableName, tablaVersion.getLastVersion());
		}		

		Map<String, Object> params = new HashMap<>();
		params.put("codigoAplicacion", Constants.COD_UDAA);
		params.put("dispositivoMovilID", dispositivoMovilID);

		String response = secureWebServiceInvoke("GetTablesLastVersionByDevice", params);
		Log.d(LOG_TAG, response);

		if (response.contains("ERROR")) {
			throw new RuntimeException();
		}

        this.syncState = new HashMap<>();

		Map<String, Integer> serverState = new HashMap<>();
		JsonElement json = new JsonParser().parse(response);
		for (JsonElement jsonElement: json.getAsJsonArray()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			String key = jsonObject.get("Key").getAsString().toLowerCase();
			Integer value = jsonObject.get("Value").getAsInt();
			serverState.put(key, value);
		}

        UdaaApplication appState = (UdaaApplication) context.getApplicationContext();

		for (String key : localState.keySet()) {
			Log.d(LOG_TAG, String.format("Analizando tabla '%s'", key));

			Integer serverVersion = serverState.get(key);
			if (key.contains("#")) {
				String rKey = key.split("#")[0];
				serverVersion =  serverState.get(rKey);
			}
			
			Integer localVersion = localState.get(key);
			boolean forceUpdate = appState.needsForceUpdate();
			boolean shouldUpdate = serverVersion > localVersion;

			if (forceUpdate || shouldUpdate){
				syncState.put(key, serverVersion);
			}
		}

		return syncState;
	}

	public static DispositivoMovil identifyDM(String deviceID) throws UDAAException {
		WebService ws = null;
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("identification", deviceID);

			ws = new WebService(WS_URL);			
			String jsonResponse = ws.webInvoke("IdentifyDispositivoMovil", params);
			Log.d(LOG_TAG, ""+jsonResponse);

			if (jsonResponse == null) {
				throw new UDAAException(ErrorConstants.ERROR_113);
			} else if (jsonResponse.contains("Error-101")) {
				throw new UDAAException(ErrorConstants.ERROR_101);
			} else if (jsonResponse.contains("Error-112")) {
				throw new UDAAException(ErrorConstants.ERROR_112);
			} else if (jsonResponse.contains("Error")) {
				if (jsonResponse.contains("404")) {
                    throw new UDAAException(ErrorConstants.ERROR_404);
                }
				else {
                    throw new UDAAException(ErrorConstants.ERROR_102);
                }
			}

			GsonBuilder builder = new GsonBuilder();
			builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()));

			// Parse Response into our object            
			Gson gson = builder.create();								

			DispositivoMovil dispositivoMovil = gson.fromJson(jsonResponse, DispositivoMovil.class);
			
			Log.d(LOG_TAG, dispositivoMovil.toString());

			return dispositivoMovil;
		} catch (UDAAException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new UDAAException(ErrorConstants.ERROR_102);
		} finally {
			if (ws != null) ws.abort();
		}
	}

	public static String setPassword(String username, String password) throws UDAAException {
		WebService ws = null;
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("username", username);
			params.put("password", password);

			ws = new WebService(WS_URL);			
			String response = ws.webInvoke("ChangePassword", params);

			Log.d(LOG_TAG, response);
			if (response.toUpperCase().contains("ERROR")) {
				throw new UDAAException(ErrorConstants.ERROR_110);
			}
			Log.d(LOG_TAG, response);

			return response.replaceAll("\"", "");
		} catch (UDAAException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new UDAAException(ErrorConstants.ERROR_110);
		} finally {
			if (ws != null) {
			    ws.abort();
            }
		}
	}

	public  <T extends AbstractEntity> void synchronize(Class<T> clazz, String tableName) throws Exception {
        UdaaApplication app = (UdaaApplication) context.getApplicationContext();
        synchronize(clazz, tableName, null, app.needsForceUpdate());
	}

	public  <T extends AbstractEntity> void synchronize(Class<T> clazz, String tableName, Integer deviceID, boolean forzarActualizar) throws Exception {
		Map<String, Object> params = new HashMap<>();
		
		String wsName = "EntityDelta"; 
		// Apply device filter?
		if (deviceID != null) {
			params.put("deviceID", deviceID);
			wsName = "EntityDeltaByDevice";
		}
		
		genericSync(clazz, tableName, params, wsName, forzarActualizar, false);
	}
	
	public  <T extends AbstractEntity> void synchronizeByDispositivo(Class<T> clazz, String tableName, Integer deviceID) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("deviceID", deviceID);	
		
		genericSync(clazz, tableName, params, "EntityDeltaByDispositivo", true, false);
	}
	
	public  <T extends AbstractEntity> void synchronizeByPerfil(Class<T> clazz, String tableName) throws Exception {
		Map<String, Object> params = new HashMap<>();
		genericSync(clazz, tableName, params, "EntityDeltaByPerfil", true, !isSynchronized("apm_perfilAccesoUsuario"));
	}
	
	public  <T extends AbstractEntity> void synchronizeByAplicacion(Class<T> clazz, String tableName, String aplicacionID) throws Exception {
		Log.i(LOG_TAG, "synchronizeByAplicacion: enter");

		Map<String, Object> params = new HashMap<>();
		params.put("aplicacionID", aplicacionID);
		genericSync(clazz, tableName, params, "EntityDeltaByAplicacion", true, false);
	}

	public  <T extends AbstractEntity> void synchronizeByAplicacion(Class<T> clazz, String tableName, String aplicacion, Integer aplicacionID) throws Exception {
		Log.i(LOG_TAG, "synchronizeByAplicacion: enter");

		Map<String, Object> params = new HashMap<>();
		params.put("aplicacionID", aplicacion);
		String localTableName = tableName+"#"+aplicacionID;
		genericSync(clazz, tableName, localTableName, params, "EntityDeltaByAplicacion", true, false);
	}
	
	public  <T extends AbstractEntity> void synchronizeByUsuarioID(Class<T> clazz, String tableName, Integer usuarioID) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("usuarioApmID", usuarioID);
		genericSync(clazz, tableName, params, "EntityDeltaByUsuarioApm", true, false);
	}

	public  <T extends AbstractEntity> void synchronizeByAplicacionID(Class<T> clazz, String tableName, Integer aplicacionID) throws Exception {
		Map<String, Object> params = new HashMap<>();
		params.put("aplicacionID", aplicacionID);
		String localTableName = tableName + "#" + aplicacionID;
		genericSync(clazz, tableName, localTableName, params, "ProfileDeltaByAplicacion", true, false);
	}

	public  <T extends AbstractEntity> void synchronizeBatch(Class<T> clazz, String tableName, int batchNumber) throws Exception {
		Log.i(LOG_TAG, "genericSync: enter");

		if (isSynchronized(tableName)) {
			return;			
		}
		
		boolean permiteActParcial = false;
		
		try {
			// Verifico si debo sincronizar entidad	
			TablaVersion tablaVersion = udaaDao.getTablaVersionByTableName(tableName);
			permiteActParcial = udaaDao.getAplicacionTabla(tableName);

			Integer serverVersion = syncState.get(tableName.toLowerCase());
			if (serverVersion == null) {
			    serverVersion = 0;
            }

            int version = tablaVersion.getLastVersion();

			Map<String, Object> params = new HashMap<>();
			
			boolean moreRecords = true;
			while(moreRecords){
				String wsName = "EntityDeltaByBatch";
				params.put("batchNumber", batchNumber);
				params.put("tableName", tableName);
				params.put("version", version);
				moreRecords = synchronize(clazz, params, wsName, version, serverVersion, tablaVersion, permiteActParcial, tableName);
				batchNumber++;
			}
		}
		catch (Exception exception) {
			if (!exception.getMessage().equals("ERROR_SINCRONIZACION")) {
				String message = "Error sincronización de tabla: '" + tableName + "'. ERROR: " + exception.getMessage();
				generateACRA_LOG(message, "SINCRONIZACION");
				if (!permiteActParcial) {
					throw new Exception("ERROR_SINCRONIZACION");
				}
			}
			else {				
				throw exception;
			}
		}
	}
	
	private <T extends AbstractEntity> void genericSync(Class<T> clazz, String tableName, Map<String, Object> params, String wsName, boolean valForzarAct, boolean forzarActByPerfil) throws Exception {
		genericSync(clazz, tableName, tableName, params, wsName, valForzarAct, forzarActByPerfil);
	}
	
	private <T extends AbstractEntity> void genericSync(Class<T> clazz, String tableName, String localTableName, Map<String, Object> params, String wsName, boolean valForzarAct, boolean forzarActByPerfil) throws Exception {
		Log.i(LOG_TAG, "genericSync: enter");
		boolean permiteActParcial = false;

		try {
			// Verifico si debo sincronizar entidad
			if (!forzarActByPerfil && isSynchronized(localTableName)) {
				return;			
			}
	
			TablaVersion tablaVersion = udaaDao.getTablaVersionByTableName(localTableName);
			permiteActParcial = udaaDao.getAplicacionTabla(tableName);

			Integer serverVersion = syncState.get(localTableName.toLowerCase());
			if (serverVersion == null) {
			    serverVersion = 0;
            }

			boolean forzarActualizar = false;
            int version = 0;
			
			if (valForzarAct) {
				UdaaApplication appState = (UdaaApplication) context.getApplicationContext();
				forzarActualizar = appState.needsForceUpdate();			
			}
			
			if (!forzarActualizar && !forzarActByPerfil) {
			    version = tablaVersion.getLastVersion();
            }
			
			params.put("tableName", tableName);
			params.put("version", version);

			synchronize(clazz, params, wsName, version, serverVersion, tablaVersion, permiteActParcial, tableName);
		}
		catch (Exception exception) {
		    if (!exception.getMessage().equals("ERROR_SINCRONIZACION")) {
				String message = "Error sincronización de tabla: '" + tableName + "'. ERROR: " + exception.getMessage();
				generateACRA_LOG(message, "SINCRONIZACION");
				if (!permiteActParcial) {
					throw new Exception("ERROR_SINCRONIZACION");
				}
			}
			else {				
				throw exception;
			}
		}
	}
	
	private <T extends AbstractEntity> boolean synchronize(Class<T> clazz, Map<String, Object> params, String wsName, int version, int serverVersion, TablaVersion tablaVersion, boolean partial, String tableName) throws Exception {
		boolean moreRecords = false;
		String jsonResponse = "";
		try {
			// Call the web service
			jsonResponse = secureWebServiceInvoke(wsName, params);			
			Log.d(LOG_TAG, jsonResponse);
			
			// Creates the JSON object which will manage the information received 
			GsonBuilder builder = new GsonBuilder(); 
	
			// Register an adapter to manage the date types as long values 
			builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()));
	
			// Parse Response into our object            
			Gson gson = builder.create();								
			List<T> itemsChanged = new ArrayList<>();
			JsonElement json = new JsonParser().parse(jsonResponse);
			for (JsonElement jsonElement: json.getAsJsonArray()) {
				itemsChanged.add(gson.fromJson(jsonElement, clazz));
			}

			processItemsChanged(clazz, itemsChanged, partial, tableName);

			if (tablaVersion != null) {
                Log.d(LOG_TAG, "Guardando version " + version);
                tablaVersion.setLastVersion(serverVersion);
                udaaDao.updateTablaVersion(tablaVersion);
            }
			
			moreRecords = itemsChanged.size() > 0 && itemsChanged.get(0).getMoreRecords();
		}
		catch (Exception exception) {
			if (!exception.getMessage().equals("ERROR_SINCRONIZACION")) {
				String message = "Error sincronización de tabla: '"+ tableName +"'; jsonResponse: " + jsonResponse + ". ERROR: " + exception.getMessage();
				generateACRA_LOG(message, "SINCRONIZACION");
				if (!partial) {
					throw new Exception("ERROR_SINCRONIZACION");
				}
			}
			else {				
				throw exception;
			}
		}
		return moreRecords;
	}

	private <T extends AbstractEntity> void processItemsChanged(Class<T> clazz, List<T> itemsChanged, boolean partial, String tableName) throws Exception {
		RuntimeExceptionDao<T, Integer> entityDAO = database.getRuntimeExceptionDao(clazz);
		int version = 0;
		for (T entity: itemsChanged) {      
			try {
				Log.d(LOG_TAG, "Procesando entidad " + entity.getId());
				if (entity.getVersion() > version)  version = entity.getVersion();
				boolean existsLocally = entityDAO.queryForId(entity.getId()) != null;
				if (!existsLocally && !entity.isDeleted()) {
					Log.d(LOG_TAG, "Creating entity ...");
					entityDAO.create(entity);
				} else if (entity.isDeleted()) {
					Log.d(LOG_TAG, "Deleting entity ...");
					entityDAO.delete(entity);
				} else {
					Log.d(LOG_TAG, "Updating entity ...");
					entityDAO.update(entity);
				}
			}
			catch (Exception exception) {
				String message = "Error sincronización de tabla: '" + tableName + "'; ID_Registro: '" + entity.getId() + "'. ERROR: " + exception.getMessage();
				generateACRA_LOG(message, "SINCRONIZACION");
				if (!partial) {
					throw new Exception("ERROR_SINCRONIZACION");
				}
			}
		}
    }

	public void synchronizeBinary(String tableName, Integer usuarioID, InitializeAplTask initAplTask, Integer deviceID) throws Exception {
		Map<String, AplicacionBinarioVersion> mToInstall = new HashMap<>();
		
		RuntimeExceptionDao<AplicacionBinarioVersion, Integer> aplicacionBinarioVersionDAO = 
				database.getRuntimeExceptionDao(AplicacionBinarioVersion.class);
				
		UdaaApplication app = (UdaaApplication) context.getApplicationContext();
		int version = 0;
		TablaVersion tv = udaaDao.getTablaVersionByTableName(tableName);
		if (!app.needsForceUpdate()) {
			version = tv.getLastVersion();
		}

		if (!isSynchronized(tableName)) {
			// Load POST Parameters
			Map<String, Object> params = new HashMap<>();
			params.put("tableName", tableName);
			params.put("version", version);
			params.put("deviceID", deviceID);
	 
			// Call the web service EntityDeltaByDispositivo(string tableName, int version, int deviceID)
			String jsonResponse = secureWebServiceInvoke("EntityDeltaByDispositivo", params);
			Log.d(LOG_TAG, jsonResponse);
	
			// Creates the JSON object which will manage the information received 
			GsonBuilder builder = new GsonBuilder(); 
	
			// Register an adapter to manage the date types as long values  
			builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()));
	
			// Parse Response into our object            
			Gson gson = builder.create();								
			List<AplicacionBinarioVersion> itemsChanged = new ArrayList<>();
			JsonElement json = new JsonParser().parse(jsonResponse);
			for (JsonElement je: json.getAsJsonArray()) {            					
				itemsChanged.add(gson.fromJson(je, AplicacionBinarioVersion.class));
			}

			//La aplicacion UDAA esta disponible para todos los usuaios y no es enviada desde el server, la insertamos
			Aplicacion aplicacion = udaaDao.getAplicacionByCodigo(Aplicacion.App.UDAA.toString());			
			if (aplicacion == null) {
				RuntimeExceptionDao<Aplicacion, Integer> appDao = database.getRuntimeExceptionDao(Aplicacion.class);
				
				aplicacion = new Aplicacion();
				aplicacion.setPkg(context.getPackageName());
				aplicacion.setCodigo(Aplicacion.App.UDAA.toString());
				aplicacion.setClassName(NotificacionesActivity.class.getCanonicalName());
				aplicacion.setDescripcion(Aplicacion.App.UDAA.toString());
				aplicacion.setModificationTimeStamp(new Date());
				aplicacion.setModificationUser("user");
				aplicacion.setId(Aplicacion.App.UDAA.getId());
				appDao.createIfNotExists(aplicacion);
			}
			
			for (AplicacionBinarioVersion entity: itemsChanged) {  
				Aplicacion apl = udaaDao.getAplicacionById(entity.getAplicacion().getId()); 
				if(Aplicacion.App.UDAA.getId() != entity.getAplicacion().getId()){
					if(apl == null || !udaaDao.hasAccess(usuarioID, apl.getCodigo())){
						continue;
					}
				} 

				if (apl != null) {
					entity.setAplicacion(apl);
				}
	
				Log.d(LOG_TAG, "Procesando entidad " + entity.getId());
				if (entity.getVersion() > version)  version = entity.getVersion();
				boolean existsLocally = aplicacionBinarioVersionDAO.queryForId(entity.getId()) != null;
				if (!existsLocally && !entity.isDeleted()) {
					Log.d(LOG_TAG, "Creating entity ...");
					aplicacionBinarioVersionDAO.create(entity);
				} else if (entity.isDeleted()) {
					Log.d(LOG_TAG, "Deleting entity ...");            		
					aplicacionBinarioVersionDAO.delete(entity);
				} else {
					Log.d(LOG_TAG, "Updating entity ...");
					aplicacionBinarioVersionDAO.update(entity);
				}
	
				if(!entity.isDeleted()){
					String key = entity.getAplicacion().getCodigo()+"-"+entity.getAplTipoBinario().getId();
					// Template de Impresion
					if(entity.getAplTipoBinario().getId() == 3
							|| entity.getAplTipoBinario().getId() == 4)	key += "-"+entity.getId();
	
					AplicacionBinarioVersion aplVersion = mToInstall.get(key);
					
					if (entity.getAplicacion() != null && entity.getAplicacion().getCodigo() != null && Aplicacion.App.UDAA.toString().equals(entity.getAplicacion().getCodigo())) {
						AplicacionBinarioVersion aplBinVersion = udaaDao.getLastAplicacionBinarioVersionByCodigoAplicacion(Aplicacion.App.UDAA.toString());
						PackageInfo pInfo = DeviceContext.getPackageInfoFromInstalledApp(aplBinVersion.getAplicacion(), context);
						if(pInfo != null && !pInfo.versionName.equals(aplBinVersion.getNombreVersion())){
							if(null == aplVersion || aplVersion.getVersion() < entity.getVersion()){
								mToInstall.put(key, entity);
							}
						}
					} else {
						if (aplVersion == null || aplVersion.getVersion() < entity.getVersion()) {
							mToInstall.put(key, entity);
						}
						
					}
						
				}
			}
		}
		
		//Valido que la version instalada de la UDAA coincida con la ultima version registrada en la tabla
		AplicacionBinarioVersion aplBinVersion = udaaDao.getCoreAplicacionBinarioVersionByCodigoAplicacion(Aplicacion.App.UDAA.toString());
		if (aplBinVersion != null && !mToInstall.containsKey(aplBinVersion.getAplicacion().getCodigo()+"-"+aplBinVersion.getAplTipoBinario().getId())){
			//Agrego la UDAA para que se valide
			PackageInfo pInfo = DeviceContext.getPackageInfoFromInstalledApp(aplBinVersion.getAplicacion(), context);
			if(pInfo != null && (!pInfo.versionName.equals(aplBinVersion.getNombreVersion()) || app.needsForceUpdate())){
				mToInstall.put(Integer.toString(aplBinVersion.getId()), aplBinVersion);
			}
		}

		//Valido que la version instalada de HC coincida con la ultima version registrada en la tabla
		AplicacionBinarioVersion aplBinVersionHC = udaaDao.getCoreAplicacionBinarioVersionByCodigoAplicacion(Aplicacion.App.HCDigital.toString());
		if(aplBinVersionHC != null && !mToInstall.containsKey(aplBinVersionHC.getAplicacion().getCodigo()+"-"+aplBinVersionHC.getAplTipoBinario().getId())){
			//Agrego la HC para que se valide
			PackageInfo pInfo = DeviceContext.getPackageInfoFromInstalledApp(aplBinVersionHC.getAplicacion(), context);
			if(pInfo != null && (!pInfo.versionName.equals(aplBinVersionHC.getNombreVersion()) || app.needsForceUpdateHC())){
				mToInstall.put(Integer.toString(aplBinVersionHC.getId()), aplBinVersionHC);
			}
		}

		//Valido que la version instalada de SA coincida con la ultima version registrada en la tabla
		AplicacionBinarioVersion aplBinVersionSA = udaaDao.getCoreAplicacionBinarioVersionByCodigoAplicacion(Aplicacion.App.SADigital.toString());
		if(aplBinVersionSA != null && !mToInstall.containsKey(aplBinVersionSA.getAplicacion().getCodigo()+"-"+aplBinVersionSA.getAplTipoBinario().getId())){
			//Agrego la SA para que se valide
			PackageInfo pInfo = DeviceContext.getPackageInfoFromInstalledApp(aplBinVersionSA.getAplicacion(), context);
			if(pInfo != null && (!pInfo.versionName.equals(aplBinVersionSA.getNombreVersion()) || app.needsForceUpdateSA())){
				mToInstall.put(Integer.toString(aplBinVersionSA.getId()), aplBinVersionSA);
			}
		}

		//Valido que la version instalada de CTO coincida con la ultima version registrada en la tabla
		AplicacionBinarioVersion aplBinVersionCTO = udaaDao.getCoreAplicacionBinarioVersionByCodigoAplicacion(Aplicacion.App.CTODigital.toString());
		if(aplBinVersionCTO != null && !mToInstall.containsKey(aplBinVersionCTO.getAplicacion().getCodigo()+"-"+aplBinVersionCTO.getAplTipoBinario().getId())){
			//Agrego la CTO para que se valide
			PackageInfo pInfo = DeviceContext.getPackageInfoFromInstalledApp(aplBinVersionCTO.getAplicacion(), context);
			if(pInfo != null && (!pInfo.versionName.equals(aplBinVersionCTO.getNombreVersion()) || app.needsForceUpdateCTO())){
				mToInstall.put(Integer.toString(aplBinVersionCTO.getId()), aplBinVersionCTO);
			}
		}

		//Valido que la version instalada de MSJ coincida con la ultima version registrada en la tabla
		AplicacionBinarioVersion aplBinVersionMSJ = udaaDao.getCoreAplicacionBinarioVersionByCodigoAplicacion(Aplicacion.App.MSJDigital.toString());
		if(aplBinVersionMSJ != null && !mToInstall.containsKey(aplBinVersionMSJ.getAplicacion().getCodigo()+"-"+aplBinVersionMSJ.getAplTipoBinario().getId())){
			//Agrego la MSJ para que se valide
			PackageInfo pInfo = DeviceContext.getPackageInfoFromInstalledApp(aplBinVersionMSJ.getAplicacion(), context);
			if(pInfo != null && (!pInfo.versionName.equals(aplBinVersionMSJ.getNombreVersion()))){
				mToInstall.put(Integer.toString(aplBinVersionMSJ.getId()), aplBinVersionMSJ);
			}
		}

		//
		for (String key : mToInstall.keySet()) {
			boolean needsForceUpdate;
			if (key.startsWith(Aplicacion.App.SADigital.toString())) {
				needsForceUpdate = app.needsForceUpdateSA();
			} else if (key.startsWith(Aplicacion.App.CTODigital.toString())) {
				needsForceUpdate = app.needsForceUpdateCTO();
			} else if (key.startsWith(Aplicacion.App.HCDigital.toString())) {
				needsForceUpdate = app.needsForceUpdateHC();
			} else {
				needsForceUpdate = app.needsForceUpdate();
			}				
			Install(mToInstall.get(key), initAplTask, needsForceUpdate);
		}
		
		if(!isSynchronized(tableName)){
			Log.d(LOG_TAG, "Guardando version " + version);
			tv.setLastVersion(version);
			udaaDao.updateTablaVersion(tv);
		}

	}

	private boolean isSynchronized(String tableName){
		UdaaApplication appState = (UdaaApplication) context.getApplicationContext();
		boolean forceUpdate = appState.needsForceUpdate();

		if (forceUpdate) {
		    return false;
        }
		// Mapa con tablas que deben actualizarse
		if (!syncState.isEmpty() && syncState.get(tableName.toLowerCase()) == null){
			Log.d(LOG_TAG, String.format("Sincronismo de '%s' omitido por estar en su versión más reciente.", tableName));
			return true;
		}
		return false;
	}
	
	/**
	 * Download and install a binary file from URL via Http/Https GET.
	 * Note: installation execute only .apk's files.
	 */
	private void Install(AplicacionBinarioVersion binary, InitializeAplTask initAplTask, boolean forceUpdate){
		try {
			Aplicacion aplicacion = udaaDao.getAplicacionById(binary.getAplicacion().getId());

			StringBuilder fileUrl = new StringBuilder(URL + "/" + binary.getUbicacion().trim());
			
			Map<String, String> params = new HashMap<>();
			UdaaApplication appState =  (UdaaApplication) context.getApplicationContext();
			params.put("username", appState.getCurrentUser().getUsername());
			params.put("appID", aplicacion.getCodigo());
			
			String fileName = binary.getUbicacion().substring(
					binary.getUbicacion().lastIndexOf(File.separator) + 1).trim();

			File outputFile = null;
			boolean needToDownload = true;

			switch (binary.getAplTipoBinario().getId()) {
			case 1: // Core
				String path = Environment.getExternalStorageDirectory() + "/download/";
				File file = new File(path);
				file.mkdirs();
				outputFile = new File(path, fileName);
				
				if(outputFile.exists()){
					//A file has been downloaded before, we proceed to check the version of the apk
					PackageInfo pInfo = DeviceContext.getPackageInfoFromAPK(outputFile, context);
					if(pInfo != null){
						needToDownload = !binary.getNombreVersion().equals(pInfo.versionName);
					}

					// El archivo descargado tiene diferente tamaño del archivo a descargar
					if (binary.getLongitud()!= 0 && outputFile.length() != binary.getLongitud()) {
						needToDownload = true;
					}
					
					if(forceUpdate){
						needToDownload = true;
					}
					
					if(needToDownload){
						outputFile.delete();
					}
				}
				break;
			case 2: // Library: download file into internal storage
				outputFile = new File(context.getDir("dex", Context.MODE_PRIVATE), fileName);
				break;
			case 3: // Template: download file into internal template storage
				outputFile = new File(context.getDir("tpl", Context.MODE_PRIVATE), fileName);
				break;
			case 4: // DataBase: download database file into internal db storage
				outputFile = new File(context.getDir("db", Context.MODE_PRIVATE), fileName);
				break;
			}
			
			boolean downloadOK;
			
			if (needToDownload) {
				// Build URL
				int i = 0;
				for (Map.Entry<String, String> param : params.entrySet()) {
					if (i++ == 0) {
						fileUrl.append("?");
					} else {
						fileUrl.append("&");
					}
					try {
						fileUrl.append(param.getKey());
                        fileUrl.append("=");
                        fileUrl.append(URLEncoder.encode(param.getValue(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						Log.e(LOG_TAG, "**ERROR**", e);
					} 
				}
				
				Log.d(LOG_TAG, "Descargando… " + fileUrl);
				// Create a trust manager that does not validate certificate chains
				TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {}
				}
				};
				// Install the all-trusting trust manager
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	
				// Create all-trusting host name verifier
				HostnameVerifier allHostsValid = (hostname, session) -> true;
				// Install the all-trusting host verifier
				HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	
				URL url = new URL(fileUrl.toString());
	
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();

	            int fileLength = connection.getContentLength();
	            long total = 0;
	            	
				FileOutputStream fos = new FileOutputStream(outputFile);
				InputStream inputStream = connection.getInputStream();
	
				byte[] buffer = new byte[1024];
				int len1;
				while ((len1 = inputStream.read(buffer)) != -1) {
	                total += len1;
	                if (fileLength > 0) {
	                	initAplTask.doProgress(binary.getDescripcion(), (int) (total * 100 / fileLength));
	                }
					fos.write(buffer, 0, len1);
				}
				fos.close();
				inputStream.close();
				
				// El archivo descargado tiene diferente tamaño del archivo a descargar
				if (binary.getLongitud()!= 0 && total != binary.getLongitud()) {
					Log.d(LOG_TAG, "Descargando WRONG");
					downloadOK = false;
				} else {
					downloadOK = true;
					Log.d(LOG_TAG, "Descargando OK");
				}
			} else {
				downloadOK = true;
				Log.d(LOG_TAG, "No se necesita descargar el binario");
			}

			if(downloadOK && context.getPackageName().equals(binary.getAplicacion().getPkg()) && binary.getAplTipoBinario().getId() == 1){
				if (binary.getAplicacion() != null && binary.getAplicacion().getCodigo() != null && Aplicacion.App.UDAA.toString().equals(binary.getAplicacion().getCodigo())) {
					String authority = "com.fantommers.udaa.fileprovider";
					Uri data = FileProvider.getUriForFile(context, authority, outputFile);
					// Intent to install apk
					final Intent installIntent = new Intent(Intent.ACTION_VIEW);
					installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
					installIntent.setDataAndType(data, "application/vnd.android.package-archive");
					
					if (android.os.Build.VERSION.SDK_INT >= 23) {
                        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
					
					appState.setLastVersionName(binary.getNombreVersion());
					
					PackageInfo packageInfo = DeviceContext.getPackageInfoFromInstalledApp(binary.getAplicacion(), context);
					if (packageInfo != null && !packageInfo.versionName.equals(binary.getNombreVersion())) {
						appState.setWaitingInstallation(true);	
					}			
					appState.setUpgradeIntent(installIntent);		
					appState.setUpgradeRequired(binary.isObligatorio());
					appState.setLastVersionName(binary.getNombreVersion());
				}
			}
		} catch (Exception e) {	  
			Log.e(LOG_TAG, "Install : ERROR ", e);
		}
	}      		

	private String secureWebServiceInvoke(String method, Map<String, Object> params) {
		WebService ws = null;
		try {
			UdaaApplication appState =  (UdaaApplication) context.getApplicationContext();

			params.put("username", appState.getCurrentUser().getUsername());
			params.put("password", appState.getCurrentUser().getPassword());
			ws = new WebService(WS_URL);
			
			String response = ws.webInvoke(method, params);
			Log.d(LOG_TAG, ""+response);

			if (response != null && response.contains("ERROR-427")) {
				throw new UDAAException(ErrorConstants.ERROR_427);
			}

			return response;
		} catch (Exception e) {
			Log.d(LOG_TAG, "ERROR : secureWebServiceInvoke : ", e);
			return "";
		} finally {
			if (ws != null) {
			    ws.abort();
            }
		}
	}

    public boolean sendLocationGPS(UbicacionGPS uGPS) throws UDAAException {
		try {
			Map<String, Object> params = new HashMap<>();
			params.put("ubicacionGPS", uGPS);

			String response = secureWebServiceInvoke("SaveGPSLocation", params);

			Log.d(LOG_TAG, "Response: " + response);
			if (response == null) { response = ""; }
			if (response.toUpperCase().contains("ERROR")) {
				throw new UDAAException(ErrorConstants.ERROR_110);
			}

            return response.contains("OK");
		} catch (UDAAException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new UDAAException(ErrorConstants.ERROR_110);
		}
    }

	public void registerUsuarioApmVersion(String descripcion) throws UDAAException {
		Log.d(LOG_TAG, "registerUsuarioApmVersion: enter");
		try {
			UdaaApplication appState =  (UdaaApplication) context.getApplicationContext();
			Map<String, Object> params = new HashMap<>();
			params.put("idUsuarioApm", appState.getCurrentUser().getId());
			params.put("idDispositivoMovil", appState.getDispositivoMovil().getId());
			params.put("idAplicacion", Constants.APLICACION_UDAA_ID);
			params.put("descripcion", descripcion);

			String response = secureWebServiceInvoke("RegisterUsuarioApmVersionAndAppID", params);
			
			if (response.contains("Error-108")) {
				throw new UDAAException(ErrorConstants.ERROR_108);
			}
			
			Log.d(LOG_TAG, "Response: " + response);
			if (response.toUpperCase().contains("ERROR")) {
				throw new UDAAException(ErrorConstants.ERROR_WS);
			}

            response.contains("OK");

        } catch (UDAAException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new UDAAException(ErrorConstants.ERROR_WS);
		}
	}

	// Implementation helpers
	private static final String LOG_TAG = WebServiceDAO.class.getSimpleName();

	private static String WS_URL;
	private static String URL;
	private UDAADao udaaDao;
	private Context context;
	private DatabaseHelper database;
	private Map<String,Integer> syncState = new HashMap<>();

	private WebServiceDAO(Context context) {
		database = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		udaaDao  = new UDAADao(context);

		SharedPreferences myPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		URL = myPrefs.getString("URL", "");
		WS_URL = myPrefs.getString("URL", "") + "/WebServices/";
		this.context = context;
	}
	
	private void refreshSettingsWebServiceDAO(Context context) {
		SharedPreferences myPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
		URL = myPrefs.getString("URL", "");
		WS_URL = myPrefs.getString("URL", "") + "/WebServices/";
		this.context = context;
	}
		
	public boolean SendReporteError(ReporteError reporteError) {
		WebService ws = null;
		try {
			// Parse Response into our object 
			Log.d(LOG_TAG, "Parseando saveReporteError");
			ws = new WebService(WS_URL);	

			String response,data;
			try {
				data = reporteError.toJSON().toString();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}

			response =  ws.webInvoke("saveReporteError",data , "application/json");
			Log.d(LOG_TAG, "RESPONSE!!! "+response);

            return response != null && response.contains("OK");
        } catch (Exception e) {
			Log.e(LOG_TAG, "syncAtencionToServer: ERROR", e);
			return false;
		} finally {
			if (ws != null) {
                ws.abort();
            }
		}
	}
		
	private void generateACRA_LOG(String message, String operation) {
		try {
			UdaaApplication appState = (UdaaApplication)context.getApplicationContext();
			
			// Application version
			Intent msg = new Intent();
			String versionName = "";
			try {
				versionName = "v"
						+ context.getPackageManager().getPackageInfo(
								context.getPackageName(), 0).versionName;
			} catch (Exception ignore) {}
	
			ReporteError reporteError = new ReporteError();
			reporteError.setFechaCaptura(new Date());
			reporteError.setDescripcion("UDAA_"+ operation +"|" + versionName);
			if (appState.getDispositivoMovil() != null) {
                reporteError.setDispositivoMovil(appState.getDispositivoMovil().getId());
            }
	
			msg.setAction(Constants.ACTION_ACRA_ERROR_SEND);
	
			DetalleReporteError detalleReporteError = new DetalleReporteError();
			detalleReporteError.setDescripcion(message);
			detalleReporteError.setReporteError(reporteError);
			detalleReporteError.setTipoDetalle("UDAA");
			
			List<DetalleReporteError> detalleReporteErrorList = new ArrayList<>();
			detalleReporteErrorList.add(detalleReporteError);
	
			reporteError.setDetalleReporteErrorList(detalleReporteErrorList);
	
			// --
			GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
			
			new GraphAdapterBuilder()
			.addType(ReporteError.class)
			.addType(DetalleReporteError.class)
			.registerOn(gsonBuilder);
			Gson gson = gsonBuilder.create();
			// --
	
			msg.putExtra("REPORTE_ERROR", gson.toJson(reporteError));
	
			appState.sendBroadcast(msg);
		}
		catch(Exception ex) {
			Log.d(LOG_TAG, "Error al generar reporte en ACRA. Exception: " + ex.getMessage());
		}
	}
	
	public void confirmForceUpdate(int app) {
		UdaaApplication appState = (UdaaApplication) context.getApplicationContext();
		DispositivoMovil movil = appState.getDispositivoMovil();

        Map<String, Object> params = new HashMap<>();
		params.put("deviceID",  movil.getId());
		params.put("aplicacion",  app);
        secureWebServiceInvoke("UpdateForzarActAplicacion", params);
        udaaDao.createOrUpdateDispositivoMovil(movil);
	}
	
	String getPDFTemplate(int perfilID, int dispositivoID, Map<String, String> atencion, Map<String, Boolean> secciones) {
		WebService ws = null;
		try {
			UdaaApplication appState =  (UdaaApplication) context.getApplicationContext();
			ws = new WebService(WS_URL);
			String response = URL + ws.webInvoke("GetPDFTemplate", pdfToJSON(perfilID, dispositivoID, appState.getCurrentUser().getUsername(), appState.getCurrentUser().getPassword(), atencion, secciones).toString(), "application/json");
			Log.d(LOG_TAG, ""+response);

			if (response.contains("ERROR-427")) {
				throw new UDAAException(ErrorConstants.ERROR_427);
			}

			return response;
		} catch (Exception e) {
			Log.d(LOG_TAG, "ERROR : secureWebServiceInvoke : ", e);
			return "";
		} finally {
			if (ws != null) {
			    ws.abort();
            }
		}
	}
	
	private JsonObject pdfToJSON(int perfilID, int dispositivoID, String username, String password, Map<String, String> atencion, Map<String, Boolean> secciones) {
		JsonObject joPDF = new JsonObject();
		joPDF.addProperty("perfilID", perfilID);
		joPDF.addProperty("dispositivoID", dispositivoID);
		joPDF.addProperty("username", username);
		joPDF.addProperty("password", password);
		
		JsonArray joAtencion = new JsonArray();
		for (Map.Entry<String, String> entry : atencion.entrySet()) {
			JsonObject jo = new JsonObject();
			jo.addProperty("Key", entry.getKey());			
			jo.addProperty("Value", entry.getValue());			
			
			joAtencion.add(jo);
		}
		joPDF.add("atencion", joAtencion);

		JsonArray joSecciones = new JsonArray();
		for (Map.Entry<String, Boolean> entry : secciones.entrySet()) {
			JsonObject jo = new JsonObject();
			jo.addProperty("ID", entry.getKey());			
			jo.addProperty("Visible", entry.getValue());			
			
			joSecciones.add(jo);
		}		
		joPDF.add("PDFSections", joSecciones);
		
		return joPDF;
	}

}