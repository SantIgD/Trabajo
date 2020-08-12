package coop.tecso.hcd.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.graph.GraphAdapterBuilder;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;

import coop.tecso.IUDAAService;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.AtencionValor;
import coop.tecso.hcd.persistence.DatabaseHelper;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ErrorConstants;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion.App;
import coop.tecso.udaa.domain.aplicaciones.AplicacionBinarioVersion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfilSeccion;
import coop.tecso.udaa.domain.base.AbstractEntity;
import coop.tecso.udaa.domain.base.TablaVersion;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.domain.notificaciones.Notificacion;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.perfiles.CampoValor;
import coop.tecso.udaa.domain.perfiles.CampoValorOpcion;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

@SuppressWarnings("ResultOfMethodCallIgnored")
public final class UDAACoreServiceImpl implements UDAACoreService {

	private RuntimeExceptionDao<TablaVersion, Integer> tablaVersionDAO;

	private Context context;
	private Map<String,Integer> syncState;

	private static final String LOG_TAG = UDAACoreServiceImpl.class.getSimpleName();
	private String WS_URL;

	private DatabaseHelper database;
	private GsonBuilder builder;
	private IUDAAService rawService = null;

	@SuppressWarnings("FieldCanBeLocal")
	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder binder) {
			Log.d(LOG_TAG, "Bindeando servicio...");
			rawService = IUDAAService.Stub.asInterface(binder);
			try {
				WS_URL =  rawService.getServerURL() + "/WebServices/";
				Log.d(LOG_TAG, "Bindeando servicio... OK");
			} catch (Exception e) {
				WS_URL = "";
				e.printStackTrace();
				Log.d(LOG_TAG, "Bindeando servicio... FAIL");
			}
		}

		public void onServiceDisconnected(ComponentName className) {
			rawService = null;
		}
	};

	/**
	 * Constructor
	 */
	public UDAACoreServiceImpl(Context context) {
		this.context = context;
		this.database = OpenHelperManager.getHelper(context, DatabaseHelper.class);

		// Creates the JSON object which will manage the information received 
		this.builder = new GsonBuilder();

		// Register an adapter to manage the date types as long values 
		this.builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context1) -> new Date(json.getAsJsonPrimitive().getAsLong()));

		this.tablaVersionDAO = database.getRuntimeExceptionDao(TablaVersion.class);

		Log.d(LOG_TAG, "2 Bindeando servicio...");
		Intent intent = new Intent("coop.tecso.IUDAAService");
		intent = Helper.createExplicitFromImplicitIntent(context, intent);

		context.getApplicationContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		Log.d(LOG_TAG, "3 Bindeando servicio...");
	}

	@Override
	public UsuarioApm login(String username, String password) throws Exception  {
		String jo = rawService.login(username, password);		
		Log.d(LOG_TAG, "Usuario login: " + jo);
		Gson gson = builder.create();
		return gson.fromJson(jo, UsuarioApm.class);		
	}

	@Override
	public void changeSession(String username, String password) throws Exception  {
		rawService.changeSession(username, password);		
		Log.d(LOG_TAG, "Cambio de Usuario. Nuevo usuario: " + username);
	}

	@Override
	public String getServerURL() throws Exception {
		return rawService.getServerURL();		
	}

    @Override
    public boolean isTransTypePartial() throws Exception {
        return rawService.isTransTypePartial();
    }

	@Override
	public boolean hasAccess(int usuarioID, String codAplicacion) throws Exception {
		return rawService.hasAccess(usuarioID, codAplicacion);
	}

	public Notificacion getNotificacionById(int notificacionID) throws Exception {
		String jo = rawService.getNotificacionById(notificacionID);
		Log.d(LOG_TAG, "Notificacion: "+jo);
		return new Gson().fromJson(jo, Notificacion.class);		
	}

	@Override
	public AplicacionPerfil getAplicacionPerfilById(int aplicacionPerfilId) throws Exception {		
		String path = rawService.getAplicacionPerfilById(aplicacionPerfilId);
		Log.d(LOG_TAG, "HCD - PATH: " + path);

		int bufferSize = 8048; //2k  aprox
		File file = new File(path);
		FileReader fileReader = new FileReader(file);
		BufferedReader bufferedReader = new BufferedReader(fileReader, bufferSize);
		StringBuilder stringBuilder = new StringBuilder();
		try {
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
		}
		catch(Exception ex) {
			Log.i(LOG_TAG, "ERROR getAplicacionPerfilById: "+ ex.getStackTrace());
		}
		finally {
			fileReader.close();
			bufferedReader.close();
			file.delete();
		}

		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting(); 

		new GraphAdapterBuilder()
		.addType(AplicacionPerfil.class) 
		.addType(AplicacionPerfilSeccion.class)
		.addType(AplPerfilSeccionCampo.class)
		.addType(AplPerfilSeccionCampoValor.class) 
		.addType(AplPerfilSeccionCampoValorOpcion.class)
		.addType(Campo.class)
		.addType(CampoValor.class)
		.addType(CampoValorOpcion.class)
		.registerOn(gsonBuilder);

		Gson gson = gsonBuilder.create();

		return  gson.fromJson(stringBuilder.toString(), AplicacionPerfil.class);
	}

	@Override
	public Integer getIdAplicacionPerfilDefaultBy(String codAplicacion) throws Exception {
		String id = rawService.getIdAplicacionPerfilDefaultBy(codAplicacion);
		return Integer.valueOf(id);
	}

	@Override
	public Campo getCampoBy(int campoId, int aplicacionPerfilId) throws Exception {
		String jo = rawService.getCampoBy(campoId, aplicacionPerfilId);
		Log.d(LOG_TAG, "Campo: " + jo);

		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting(); 
		new GraphAdapterBuilder()
		.addType(Campo.class)
		.addType(CampoValor.class)
		.addType(CampoValorOpcion.class)
		.registerOn(gsonBuilder); 
		Gson gson = gsonBuilder.create();
		return gson.fromJson(jo, Campo.class);
	}

	@Override
	public UsuarioApm getCurrentUser() throws Exception  {
		String currentUser = rawService.getCurrentUser();
		Log.d(LOG_TAG, "Usuario actual: " + currentUser);
		Gson gson = builder.create();
		return gson.fromJson(currentUser, UsuarioApm.class);
	}

	@Override
	public UsuarioApm getUserById(int userID) throws Exception {
		String user = rawService.fetchUser(userID);
		Gson gson = builder.create();
		return gson.fromJson(user, UsuarioApm.class);
	}

	@Override
	public DispositivoMovil getDispositivoMovil() throws Exception {
		String dispositivoMovil = rawService.getDispositivoMovil();
		Log.d(LOG_TAG, "Dispositivo Movil: " + dispositivoMovil);

		Gson gson = builder.create();
		return gson.fromJson(dispositivoMovil, DispositivoMovil.class);
	}

	@Override
	public AplPerfilSeccionCampo getAplPerfilSeccionCampoById(int aplPerfilSeccionCampoId) throws Exception {
		String aplPerfilSeccionCampo = rawService.getAplPerfilSeccionCampoById(aplPerfilSeccionCampoId);
		Log.d(LOG_TAG, "AplPerfilSeccionCampo: " + aplPerfilSeccionCampo);

		GsonBuilder gsonBuilder = new GsonBuilder(); 
		new GraphAdapterBuilder()
		.addType(AplicacionPerfilSeccion.class)
		.addType(AplPerfilSeccionCampoValor.class)
		.registerOn(gsonBuilder); 
		Gson gson = gsonBuilder.create();
		return gson.fromJson(aplPerfilSeccionCampo, AplPerfilSeccionCampo.class);
	}

	@Override
	public void sendError(int tipoReg) throws Exception {
		Log.d(LOG_TAG, "sendError: enter");
		rawService.sendError(tipoReg);
	}

	@Override
	public String generateReport(Map<String, String> formData, Map<String, Boolean> mapSection, String template) throws Exception {
		Log.d(LOG_TAG, "generateReport: enter");

		Gson gson = new Gson();
		// serialize Form Data Map 
		String data = gson.toJson(formData);
		// serialize Form Section Map  
		String sections = gson.toJson(mapSection);

		return rawService.generateReport(data, sections, template);
	}

	public  <T extends AbstractEntity> void synchronize(Class<T> clazz, String tableName) throws Exception {
		Map<String, Object> params = new HashMap<>();
		String wsName = "EntityDelta";

		genericSync(clazz, tableName, params, wsName);
	}

	private <T extends AbstractEntity> void genericSync(Class<T> clazz, String tableName, Map<String, Object> params, String wsName) throws Exception {
		Log.i(LOG_TAG, "genericSync: enter");
		boolean permiteActParcial = false;
		
		try {
			// Verifico si debo sincronizar entidad
			if (isSynchronized(tableName)) {
				return;
			}

			HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();

			TablaVersion tv = appState.getHCDigitalDAO().getTablaVersionByTableName(tableName);
			permiteActParcial = appState.getHCDigitalDAO().getAplicacionTabla(tableName);
			
			int version = 0;
			int serverVersion = syncState.get(tableName.toLowerCase()) != null ? syncState.get(tableName.toLowerCase()) : 0;
			boolean forzarActualizar = appState.needsForceUpdate();

			if (!forzarActualizar) {
				version = tv.getLastVersion();
			}
			
			params.put("tableName", tableName);
			params.put("version", version);
			
			synchronize(clazz, params, wsName, version, serverVersion, tv, permiteActParcial, tableName);
		}
		catch (Exception ex) {
			if(!ex.getMessage().equals("ERROR_SINCRONIZACION")){
				String message = "Error sincronización de tabla: '"+ tableName +"'. ERROR: " + ex.getMessage();
				generateACRA_LOG(message, "SINCRONIZACION");
				if(!permiteActParcial){
					throw new Exception("ERROR_SINCRONIZACION");
				}
			}
			else {				
				throw ex;
			}
		}
	}

	private <T extends AbstractEntity> void synchronize(Class<T> clazz, Map<String, Object> params, String wsName, int version, int serverVersion, TablaVersion tv, boolean partial, String tableName) throws Exception {
		String jsonResponse = "";
		try {
			// Call the web service
			jsonResponse = secureWebServiceInvoke(wsName, params);			
			Log.d(LOG_TAG, jsonResponse);

			Gson gson = this.buildGson();

			List<T> itemsChanged = new ArrayList<>();
			JsonElement json = new JsonParser().parse(jsonResponse);
			for (JsonElement jsonElement: json.getAsJsonArray()) {
				itemsChanged.add(gson.fromJson(jsonElement, clazz));
			}
	
			processItemsChanged(clazz, itemsChanged, partial, tableName);
	
			Log.d(LOG_TAG, "Guardando version " + version);
			tv.setLastVersion(serverVersion);
			tablaVersionDAO.update(tv);
		}
		catch (Exception ex) {
			if(!ex.getMessage().equals("ERROR_SINCRONIZACION")){
				String message = "Error sincronización de tabla: '"+ tableName +"'; jsonResponse: " + jsonResponse + ". ERROR: " + ex.getMessage();
				generateACRA_LOG(message, "SINCRONIZACION");
				if(!partial){
					throw new Exception("ERROR_SINCRONIZACION");
				}
			}
			else {				
				throw ex;
			}
		}
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
			catch(Exception ex){
				String message = "Error sincronización de tabla: '"+ tableName +"'; ID_Registro: '" + entity.getId() + "'. ERROR: " + ex.getMessage();
				generateACRA_LOG(message, "SINCRONIZACION");
				if (!partial) {
					throw new Exception("ERROR_SINCRONIZACION");
				}
			}
		}
	}

	public Map<String, Integer> syncState() {
		RuntimeExceptionDao<TablaVersion, Integer> entityDAO = database.getRuntimeExceptionDao(TablaVersion.class);

		Map<String, Integer> tablesState = new HashMap<>();

		for (TablaVersion tv: entityDAO.queryForAll()) {				
			tablesState.put(tv.getTabla().toLowerCase(), tv.getLastVersion());
		}	

		//No debo tener en cuenta esta tabla
		tablesState.remove("hcd_atencion");

		Map<String, Object> params = new HashMap<>();
		params.put("codigoAplicacion", Constants.COD_HCDIGITAL);

		String response = secureWebServiceInvoke("GetTablesLastVersion", params);

		Log.d(LOG_TAG, response);

		if (response.contains("ERROR")) {
			throw new RuntimeException();
		}

		Map<String, Integer> serverState = new HashMap<>();
		JsonElement json = new JsonParser().parse(response);
		for (JsonElement jsonElement: json.getAsJsonArray()) {
			JsonObject jsonObject = jsonElement.getAsJsonObject();
			serverState.put(jsonObject.get("Key").getAsString().toLowerCase(), jsonObject.get("Value").getAsInt());
		}

		syncState = new HashMap<>();
		for (String key : tablesState.keySet()) {
			Integer serverVersion = serverState.get(key);
			Integer localVersion = tablesState.get(key);

			HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
			boolean forceUpdate = appState.needsForceUpdate();			
			if(serverVersion > localVersion || forceUpdate){
				syncState.put(key, serverVersion);
			}
		}

		return syncState;
	}

	/**
	 * Sincroniza las nuevas atenciones disponibles en el server.
	 * 
	 * . Sincroniza la tabla 'hcd_atencion' filtrando por dispositivo movil segun version
	 * . Por cada atencion obtenida:
	 * 		. Sincroniza la tabla 'hcd_atencionValor' para el idAtencion
	 * 		. Se pasa el id a valor negativo
	 * 		. Se guarda la atencion
	 * 		. Por cada atencionValor obtenido:
	 * 			. Se pasa el id a valor negativo
	 * 			. Se asocia a la atencion con id modificado
	 * 			. Se guarda la atencionValor
	 */
	public void synchronizeAtencion(Integer deviceID) throws Exception {
		// Si no se recibe el id de dispositivo no se sincronizan atenciones
		if (deviceID == null) {
			Log.d(LOG_TAG, "Synchronize 'hcd_atencion' without device id is not allowed.");
			return;
		}

		Gson gson = buildGson();

		TablaVersion atencionesTablaVersion = this.getAtencionesTablaVersion();
		int version = atencionesTablaVersion.getLastVersion();

		// Se accede al web service para traer Atenciones segun su version y filtrando por dispositivo movil
		String jsonResponse = this.fetchAtencionByDevice(deviceID, version);

		List<Atencion> itemsChanged = parseAtenciones(jsonResponse, gson);

		RuntimeExceptionDao<Atencion, Integer> atencionDAO = database.getRuntimeExceptionDao(Atencion.class);
		RuntimeExceptionDao<AtencionValor, Integer> atencionValorDAO = database.getRuntimeExceptionDao(AtencionValor.class);
		for (Atencion atencion: itemsChanged) {            	
			Log.d(LOG_TAG, "Procesando atencion " + atencion.getId());
			if (atencion.getVersion() > version) {
				version = atencion.getVersion();
			}

			// Hacemos el id negativo para distinguir la numeracion de la local. (En el DM id<0 => idRemoto, id>0 => idLocal)
			int idRemoto = atencion.getId();
			atencion.setId(-atencion.getId());
			atencion.setDispositivoMovilID(deviceID);
			boolean existsLocally = atencionDAO.queryForId(atencion.getId()) != null;

			if (existsLocally || !atencion.enPreparacion()) {
				Log.d(LOG_TAG, "El registro de atencion ya se encuentra en el DM");
				continue;
			}

			// Se accede al web service para traer los AtencionValor asociados
			String jsonResponseAV = this.fetchAtencionValor(idRemoto);

			JsonElement jsonAV = new JsonParser().parse(jsonResponseAV);

			// Se parsea la respuesta en un objeto AtencionValor
			List<AtencionValor> atencionValorList = new ArrayList<>();

			try {
				for (JsonElement jsonElement: jsonAV.getAsJsonArray()) {
					atencionValorList.add(gson.fromJson(jsonElement, AtencionValor.class));
				}

				Log.d(LOG_TAG, "Creando atencion ...");
				atencion.setFechaDescarga(new Date());
				atencion.setAtencionServerID(idRemoto);
				atencionDAO.create(atencion);
				Log.d(LOG_TAG, "Guardando version " + version);
				atencionesTablaVersion.setLastVersion(version);
				tablaVersionDAO.update(atencionesTablaVersion);
			} catch (Exception e) {
				e.printStackTrace();
				throw e;
			}

			for (AtencionValor atencionValor: atencionValorList) {
				Log.d(LOG_TAG, "Procesando atencionValorId " + atencionValor.getId());

				// Hacemos el id negativo para distinguir la numeracion de la local. (En el DM id<0 => idRemoto, id>0 => idLocal)
				atencionValor.setId(atencionValor.getId() * -1);
				// Se asocia el valor a la atencion de id modificado para el DM
				atencionValor.setAtencion(atencion);

				// Se verifica si existe un registro con el mismo id en la db local
				existsLocally = atencionDAO.queryForId(atencionValor.getId()) != null;
				if (existsLocally) {
					Log.d(LOG_TAG, "El registro de atencionValor ya se encuentra en el DM");
				} else {
					Log.d(LOG_TAG, "Creating atencionValor de id:" + atencionValor.getId() + "...idAtencion: " + atencionValor.getAtencion().getId());
					atencionValorDAO.create(atencionValor);
				}
			}
		}
	}

	private Gson buildGson() {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()));
		return builder.create();
	}

	private TablaVersion getAtencionesTablaVersion() throws SQLException {
		PreparedQuery<TablaVersion> preparedQuery = tablaVersionDAO.queryBuilder().where().eq("tabla", "hcd_atencion").prepare();
		return tablaVersionDAO.queryForFirst(preparedQuery);
	}

	private String fetchAtencionByDevice(int deviceID, int version) {
		// Load POST Parameters
		Map<String, Object> params = new HashMap<>();
		params.put("version", version);
		params.put("deviceID", deviceID);

		// Call the web service
		String jsonResponse = secureWebServiceInvoke("AtencionByDevice", params);
		if (jsonResponse != null){
			Log.d(LOG_TAG,"jsonResponse:" + jsonResponse);
			return jsonResponse;
		} else {
			throw new NullPointerException();
		}
	}

	private List<Atencion> parseAtenciones(String jsonResponse, Gson gson) {
		List<Atencion> itemsChanged = new ArrayList<>();
		JsonElement json = new JsonParser().parse(jsonResponse);
		try {
			for (JsonElement jsonElement: json.getAsJsonArray()) {
				itemsChanged.add(gson.fromJson(jsonElement, Atencion.class));
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

		return itemsChanged;
	}

	private String fetchAtencionValor(int idRemoto){
		// Se accede al web service para traer los AtencionValor asociados
		Map<String, Object> params = new HashMap<>();
		params.put("attentionID", idRemoto);

		// Call the web service
		String response = secureWebServiceInvoke("GetAtencionValorById", params);
		Log.d(LOG_TAG, response);
		return response;
	}

	public synchronized int syncAtencionToServer(Atencion atencion) {
		Log.i(LOG_TAG, "syncAtencionToServer: enter");

		WebService ws = null;
		try {
			// Parse Response into our object 
			Log.d(LOG_TAG, "Parseando Atencion");
			ws = new WebService(WS_URL); 

			HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
			atencion.setUserName(appState.getCurrentUser().getUsername());
			atencion.setPassWord(appState.getCurrentUser().getPassword());
			atencion.setFechaTrasmision(new Date());

			String response =  ws.webInvoke("SyncAtencion", atencion.toJSONWithoutFirmas().toString(), "application/json"); 
			Log.d(LOG_TAG, "Response: "+response);
			JSONArray responseJSON = new JSONArray(response);
			if (response != null && responseJSON.length() == 2 && responseJSON.getString(0).equals("OK") && responseJSON.getInt(1) > 0)
				return responseJSON.getInt(1);

			return 0;

		} catch (Exception e) {
			Log.e(LOG_TAG, "**ERROR**", e);
			return 0;		
		} finally {
			Log.i(LOG_TAG, "syncAtencionToServer: exit");
			if (ws != null) {
				ws.abort();
			}
		}
	}

	private String secureWebServiceInvoke(String method, Map<String, Object> params) {
		WebService ws = null;
		try {
			HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();

			params.put("username", appState.getCurrentUser().getUsername());
			params.put("password", appState.getCurrentUser().getPassword());
			ws = new WebService(WS_URL);

            return ws.webInvoke(method, params);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			if (ws != null) ws.abort();
		}
	}

	/**
	 * Sincronismo de tabla por aplicacion
	 */
	public  <T extends AbstractEntity> void synchronizeByAplicacion(Class<T> clazz, String tableName) throws Exception {
		Log.i(LOG_TAG, "synchronizeByAplicacion: enter");

		Map<String, Object> params = new HashMap<>();
		String wsName = "EntityDeltaByAplicacion"; 
		params.put("aplicacionID", Constants.COD_HCDIGITAL);		
		genericSync(clazz, tableName, params, wsName);
	}

	private boolean isSynchronized(String tableName){
		HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
		boolean forceUpdate = appState.needsForceUpdate();
		//
		if (forceUpdate) return false;
		// Mapa con tablas que deben actualizarse
		if(!syncState.isEmpty() && syncState.get(tableName.toLowerCase()) == null){
			Log.d(LOG_TAG, String.format("Sincronismo de '%s' omitido por estar en su versión más reciente.", tableName));
			return true;
		}

		return false;
	}

	@Override
	public String exportDataToFile(String jSonData) {
		return null;
	}

	@Override
	public AplicacionBinarioVersion getAplicacionBinarioVersion() {
		AplicacionBinarioVersion response = null;
		try {
			String json = rawService.getLastAplicacionBinarioVersionByCodigoAplicacion(Aplicacion.App.HCDigital.toString());
			if (json != null) {
				Gson gson = builder.create();
				response = gson.fromJson(json, AplicacionBinarioVersion.class);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return response;
	}

	public void generateACRA_LOG(String message, String operation) {
		HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
		
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
		reporteError.setDescripcion("HCDIGITAL_"+ operation +"|" + versionName);
		if (appState.getDispositivoMovil() != null)
			reporteError.setDispositivoMovil(appState.getDispositivoMovil().getId());

		msg.setAction(Constants.ACTION_ACRA_ERROR_SEND);

		DetalleReporteError detalleReporteError;
		

		detalleReporteError = new DetalleReporteError();
		detalleReporteError.setDescripcion(message);
		detalleReporteError.setReporteError(reporteError);
		detalleReporteError.setTipoDetalle("HCDIGITAL");
		
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

	@Override
	public DispositivoMovil confirmForceUpdate() {
		try {
			String response = rawService.confirmForceUpdate(App.HCDigital.toString(), true);
			if (response != null) {
				Gson gson = builder.create();
				return gson.fromJson(response, DispositivoMovil.class);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public synchronized boolean syncAtencionFirma(Atencion atencion) {
		Log.i(LOG_TAG, "syncAtencionFirmaToServer: enter");

		WebService ws = null;
		try {
			// Parse Response into our object 
			Log.d(LOG_TAG, "Parseando Atencion");
			ws = new WebService(WS_URL);
			
			HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
			atencion.setUserName(appState.getCurrentUser().getUsername());
			atencion.setPassWord(appState.getCurrentUser().getPassword());
			atencion.setFechaTrasmision(new Date());

			String response =  ws.webInvoke("SyncAtencionFirma", atencion.toJSON().toString(), "application/json");
			Log.d(LOG_TAG, "Response: "+response);

            return response != null && response.contains("OK");

        } catch (Exception e) {
			Log.i(LOG_TAG, "syncAtencionFirmaToServer: exit");
			return false;
		} finally {
			if (ws != null)
				ws.abort();
		}
	}

	public synchronized boolean syncAtencionValorToServer(AtencionValor atencionValor, Atencion atencion) {
		Log.i(LOG_TAG, "syncAtencionValorToServer: enter");
		
		WebService ws = null;
		try {
			// Parse Response into our object 
			Log.d(LOG_TAG, "Parseando AtencionValor");
			ws = new WebService(WS_URL);
			
			HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();

			JsonObject jSONAtencionValor = atencionValor.toJson().getAsJsonObject();
			jSONAtencionValor.addProperty("username", appState.getCurrentUser().getUsername());
			jSONAtencionValor.addProperty("password", appState.getCurrentUser().getPassword());			
			jSONAtencionValor.addProperty("AtencionID", atencion.getAtencionServerID());

			String response =  ws.webInvoke("SyncAtencionValorImage", jSONAtencionValor.toString(), "application/json");

			Log.d(LOG_TAG, "Response: "+response);

            return response != null && response.contains("OK");

        } catch (Exception e) {
			Log.e(LOG_TAG, "syncAtencionValorToServer: ERROR", e);
			return false;
		} finally {
			Log.i(LOG_TAG, "syncAtencionEstadoToServer: exit");
			if (ws != null)
				ws.abort();
		}
	}

	public synchronized boolean syncAtencionEstado(Atencion atencion) {
		Log.i(LOG_TAG, "syncAtencionEstadoToServer: enter");

		WebService ws = null;
		try {
			// Parse Response into our object 
			Log.d(LOG_TAG, "Parseando Atencion");
			ws = new WebService(WS_URL);

			HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
			atencion.setUserName(appState.getCurrentUser().getUsername());
			atencion.setPassWord(appState.getCurrentUser().getPassword());
			atencion.setFechaTrasmision(new Date());

			String response =  ws.webInvoke("SyncAtencionEstado", atencion.toJSONWithoutFirmas().toString(), "application/json");
			Log.d(LOG_TAG, "Response: "+response);

            return response != null && response.contains("OK");

        } catch (Exception e) {
			Log.e(LOG_TAG, "syncAtencionEstadoToServer: ERROR", e);
			return false;
		} finally {
			Log.i(LOG_TAG, "syncAtencionEstadoToServer: exit");
			if (ws != null)
				ws.abort();
		}
	}

	public void registerUsuarioApmVersion(String descripcion)  throws Exception {
		Log.d(LOG_TAG, "registerUsuarioApmVersion: enter");
		WebService ws = null;
		try {
			HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
			Map<String, Object> params = new HashMap<>();
			params.put("idUsuarioApm", appState.getCurrentUser().getId());
			params.put("idDispositivoMovil", appState.getDispositivoMovil().getId());
			params.put("idAplicacion", Constants.APLICACION_HCDIGITAL_ID);
			params.put("descripcion", descripcion);

			String response = secureWebServiceInvoke("RegisterUsuarioApmVersionAndAppID", params);

			Log.d(LOG_TAG, "Response: " + response);
			if (response.toUpperCase().contains("ERROR")) {
				throw new Exception(ErrorConstants.ERROR_WS );
			}

		} catch (Exception ignored) {
		} finally {
			if (ws != null) {
				ws.abort();
			}
		}
	}

	@Override
	public void updateApplicationSync() {
		try {
			rawService.updateApplicationSync(App.HCDigital.toString());
		} catch (RemoteException e) {
			Log.e(LOG_TAG, "updateApplicationSync: ERROR", e);
		}
	}

}