package coop.tecso.udaa.common;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;

import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionBinarioVersion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionParametro;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.aplicaciones.AplicacionSync;
import coop.tecso.udaa.domain.aplicaciones.AplicacionTabla;
import coop.tecso.udaa.domain.base.TablaVersion;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.domain.notificaciones.Notificacion;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.perfiles.PerfilAccesoAplicacion;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.domain.trazabilidad.UbicacionGPS;
import coop.tecso.udaa.persistence.DatabaseHelper;
import coop.tecso.udaa.utils.Constants;

public final class UDAADao {

	private static final String TAG = UDAADao.class.getSimpleName();

	private DatabaseHelper databaseHelper;
	private UdaaApplication appState; 

	public UDAADao(Context context) {
		this.databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		this.appState = (UdaaApplication) context.getApplicationContext();
	}

	/**
	 * Obtiene la Aplicacion para el id pasado como parametro.
	 */
	Aplicacion getAplicacionById(int id) {
		Log.i(TAG, "getAplicacionById: enter");

		// AplicacionDAO
		RuntimeExceptionDao<Aplicacion, Integer>  aplicacionDAO;
		aplicacionDAO = databaseHelper.getRuntimeExceptionDao(Aplicacion.class);

		Log.i(TAG, "getAplicacionById: exit");
		return aplicacionDAO.queryForId(id);
	}

	/**
	 * Obtiene la Aplicacion para el codigo pasado como parametro.
	 */
	Aplicacion getAplicacionByCodigo(String codigo) {
		Log.i(TAG, "getAplicacionByCodigo: enter");
		Aplicacion aplicacion = null;

		// AplicacionDAO
		RuntimeExceptionDao<Aplicacion, Integer>  aplicacionDAO;
		aplicacionDAO = databaseHelper.getRuntimeExceptionDao(Aplicacion.class);

		Map<String,Object> mFilter = new HashMap<>();
		mFilter.put("codigo", codigo);
		mFilter.put("deleted", false);
		
		// Aplicacion
		List<Aplicacion> result = aplicacionDAO.queryForFieldValuesArgs(mFilter);
		if(!result.isEmpty()) {
			aplicacion = result.get(0);
		}

		Log.i(TAG, "getAplicacionByCodigo: exit");
		return aplicacion;
	}
	
	/**
	 * Obtiene la Aplicacion para el codigo pasado como parametro.
	 */
    AplicacionBinarioVersion getLastAplicacionBinarioVersionByCodigoAplicacion(String codigo) throws SQLException {
		Log.i(TAG, "getAplicacionBinarioVersionByCodigo: enter");
		AplicacionBinarioVersion binary = null;
		
		Aplicacion app = getAplicacionByCodigo(codigo);

		if(app != null) {
			RuntimeExceptionDao<AplicacionBinarioVersion, Integer>  aplicacionBinaryDAO = databaseHelper.getRuntimeExceptionDao(AplicacionBinarioVersion.class);
			
			QueryBuilder<AplicacionBinarioVersion, Integer> query = aplicacionBinaryDAO.queryBuilder();
			query.where().eq("aplicacion_id", app.getId());
			query.orderBy("version", false);
			
			// Aplicacion
			List<AplicacionBinarioVersion> result = aplicacionBinaryDAO.query(query.prepare());
			if(!result.isEmpty()){
				binary = result.get(0);
				binary.setAplicacion(app);
			}
		}
		Log.i(TAG, "getAplicacionBinarioVersionByCodigo: exit");
		return binary;
	}

	/**
	 * Obtiene la Aplicacion para el codigo pasado como parametro.
	 */
	public AplicacionBinarioVersion getCoreAplicacionBinarioVersionByCodigoAplicacion(String codigo) throws SQLException {
		Log.i(TAG, "getAplicacionBinarioVersionByCodigo: enter");
		AplicacionBinarioVersion binary = null;
		
		Aplicacion app = getAplicacionByCodigo(codigo);

		if(app != null) {
			RuntimeExceptionDao<AplicacionBinarioVersion, Integer>  aplicacionBinaryDAO = databaseHelper.getRuntimeExceptionDao(AplicacionBinarioVersion.class);
			
			QueryBuilder<AplicacionBinarioVersion, Integer> query = aplicacionBinaryDAO.queryBuilder();
			query.where().eq("aplicacion_id", app.getId())
			 			 .and().eq("aplTipoBinario_id", 1)
						 .and().eq("deleted", false);
			query.orderBy("fecha", false);
			
			// Aplicacion
			List<AplicacionBinarioVersion> result = aplicacionBinaryDAO.query(query.prepare());
			if(!result.isEmpty()){
				binary = result.get(0);
				binary.setAplicacion(app);
			}
		}
		
		Log.i(TAG, "getAplicacionBinarioVersionByCodigo: exit");
		return binary;
	}

	/**
	 * Obtiene el Parametro de Aplicacion para el codigo pasado como parametro.
	 */
	@Deprecated
	public AplicacionParametro getAplicacionParametroByCodigo(String codParametro, String codAplicacion) {
		Log.i(TAG, "getAplicacionParametroByCodigo: enter");
		RuntimeExceptionDao<AplicacionParametro, Integer> aplicacionParametroDAO;
		aplicacionParametroDAO = databaseHelper.getRuntimeExceptionDao(AplicacionParametro.class);

		String strQuery="SELECT p.id FROM apm_aplicacionParametro p, apm_aplicacion ap ";
			   strQuery+=" WHERE p.codigo COLLATE NOCASE LIKE ? ";
			   strQuery+=" AND ap.codigo COLLATE NOCASE LIKE ? ";	
			   strQuery+=" AND p.aplicacion_id = ap.id ";	 
			   strQuery+=" AND p.deleted = 0";	   
			   
		Log.i(TAG, " query: " + strQuery);
		try {
			// Find AplicacionParametro
			GenericRawResults<String[]> rawResults;
			rawResults = aplicacionParametroDAO.queryRaw(strQuery, codParametro, codAplicacion);
			// There should be 1 result
			List<String[]> results = rawResults.getResults();
			if(results.isEmpty()){
				Log.d(TAG, "NO RESULTS!! codParametro = " + codParametro+", codAplicacion = "+codAplicacion);
				return null;
			}
			// The results array should have 1 value
			String[] resultArray = results.get(0);
			// Parse id to Integer
			Integer id = Integer.valueOf(resultArray[0]);
			Log.i(TAG, "getAplicacionParametroByCodigo: exit");
			return aplicacionParametroDAO.queryForId(id);
		} catch (Exception e) {
			Log.e(TAG, "getAplicacionParametroByCodigo: ERROR", e);
			return null;
		}
	}
	
	/**
	 * Obtiene la lista de AplicacionParametro activas
	 */
	public List<AplicacionParametro> getListAplicacionParametro() {
		Log.i(TAG, "getListAplicacionParametro: enter");

		// AplicacionParametroDAO
		RuntimeExceptionDao<AplicacionParametro, Integer>  aplicacionParametroDAO;
		aplicacionParametroDAO = databaseHelper.getRuntimeExceptionDao(AplicacionParametro.class);

		Map<String,Object> mFilter = new HashMap<>();
		mFilter.put("deleted", false);

		// AplicacionParametro
		List<AplicacionParametro> result = aplicacionParametroDAO.queryForFieldValuesArgs(mFilter);
		Log.i(TAG, "getListAplicacionParametro: exit");
		return result;
	}

	/**
	 * Obtiene el Perfil de id indicado. Si el id es menor a 0 devuelve el Perfil marcado por defecto.
	 */
    AplicacionPerfil getAplicacionPerfilById(int aplicacionPerfilId) throws RuntimeException {
		Log.i(TAG, "getAplicacionPerfilById: enter");
		// AplicacionPerfilDAO
		RuntimeExceptionDao<AplicacionPerfil, Integer> aplicacionPerfilDAO;
		aplicacionPerfilDAO = databaseHelper.getRuntimeExceptionDao(AplicacionPerfil.class);
		Log.i(TAG, "getAplicacionPerfilById: exit");
		return aplicacionPerfilDAO.queryForId(aplicacionPerfilId);
	}

	Integer getIdAplicacionPerfilDefaultBy(String codAplicacion){
		Log.i(TAG, "getIdAplicacionPerfilDefaultBy: enter");
		AplicacionPerfil aplicacionPerfil = null;
		// AplicacionPerfilDAO
		RuntimeExceptionDao<AplicacionPerfil, Integer>  aplicacionPerfilDAO;
		aplicacionPerfilDAO = databaseHelper.getRuntimeExceptionDao(AplicacionPerfil.class);
		// get our query builder from the DAO
		QueryBuilder<AplicacionPerfil, Integer> queryBuilder = aplicacionPerfilDAO.queryBuilder();
		try {
			// build query
			queryBuilder.where().eq("esPerfilPorDefecto", true)
						.and().eq("deleted", false)
						.and().eq("aplicacion_id", getAplicacionByCodigo(codAplicacion).getId());
			// prepare our sql statement
			PreparedQuery<AplicacionPerfil> preparedQuery = queryBuilder.prepare();
			// default profile
			aplicacionPerfil = aplicacionPerfilDAO.queryForFirst(preparedQuery);
		} catch (Exception e) {
			Log.e(TAG, "error al obtener perfil por defecto", e);
		}
		Log.i(TAG, "getIdAplicacionPerfilDefaultBy: exit");
		return aplicacionPerfil.getId();
	}

	public TablaVersion getTablaVersionByTableName(String tableName){
		Log.i(TAG, "getTablaVersionByTableName: enter");
		
		RuntimeExceptionDao<TablaVersion, Integer> tablaVersionDAO;
		tablaVersionDAO = databaseHelper.getRuntimeExceptionDao(TablaVersion.class);
		   
		TablaVersion tablaVersion = null;
		QueryBuilder<TablaVersion, Integer> queryBuilder = tablaVersionDAO.queryBuilder();
		try {
			queryBuilder.where().eq("deleted", false).and()
			.rawComparison("tabla", "COLLATE NOCASE LIKE", tableName);
			
			PreparedQuery<TablaVersion> pq = queryBuilder.prepare();
			
			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());
			// Return the first element
			tablaVersion = tablaVersionDAO.queryForFirst(pq);
		} catch (SQLException e) {
			Log.e(TAG, "getTablaVersionByTableName: ***ERROR***", e);
		}
		Log.i(TAG, "getTablaVersionByTableName: exit");
		return tablaVersion;
	}

	public List<TablaVersion> getListTablaVersion(){
		Log.i(TAG, "getListTablaVersion: enter");
		
		RuntimeExceptionDao<TablaVersion, Integer> tablaVersionDAO;
		tablaVersionDAO = databaseHelper.getRuntimeExceptionDao(TablaVersion.class);
		
		List<TablaVersion> result = tablaVersionDAO.queryForAll();
		Log.i(TAG, "getListTablaVersion: exit");
		return result;
	}

	/**
	 *  Crear Atencion (IM)
	 */
	public void updateTablaVersion(TablaVersion tablaVersion) throws RuntimeException {
		Log.i(TAG, "updateTablaVersion: enter");
		RuntimeExceptionDao<TablaVersion, Integer> tablaVersionDAO;
		tablaVersionDAO = databaseHelper.getRuntimeExceptionDao(TablaVersion.class);

		tablaVersion.setModificationTimeStamp(new Date());
		tablaVersion.setModificationUser("TECSO"); 

		tablaVersionDAO.update(tablaVersion);
		Log.i(TAG, "updateTablaVersion: enter");
	}



	public List<Notificacion> getListNotificacion(){
		RuntimeExceptionDao<Notificacion, Integer> notificacionesDAO = databaseHelper.getRuntimeExceptionDao(Notificacion.class);

		List<Notificacion> notificacionList = notificacionesDAO.queryForAll();
		
    	Collections.sort(notificacionList, (n1, n2) -> n1.getId() > n2.getId() ? -1 : 0);
    	
		return notificacionList;	
	}

	/**
	 *  Elimina todos los registros de Notificacion
	 */
	public void deleteAllNotificacion() throws SQLException {
		Log.i(TAG, "deleteAllNotificacion: enter");
		RuntimeExceptionDao<Notificacion, Integer>  notificacionDAO = databaseHelper.getRuntimeExceptionDao(Notificacion.class);

		notificacionDAO.delete(notificacionDAO.deleteBuilder().prepare());
		Log.i(TAG, "deleteAllNotificacion: exit");
	}

	/**
	 * Obtiene la Notificacion de id indicada.
	 */
	public Notificacion getNotificacionById(Integer notificacionId){
		RuntimeExceptionDao<Notificacion, Integer> notificacionDAO = databaseHelper.getRuntimeExceptionDao(Notificacion.class);
        return notificacionDAO.queryForId(notificacionId);
	}

	/**
	 * Obtiene la lista de UsuarioApm
	 */
	public List<UsuarioApm> getListUsuarioApm(){
		// Look up data
		RuntimeExceptionDao<UsuarioApm, Integer> usuarioApmDAO;
		usuarioApmDAO = databaseHelper.getRuntimeExceptionDao(UsuarioApm.class);
		// Prepare the listView
		return usuarioApmDAO.queryForAll();	
	}

	public void createOrUpdateUsuarioApm(UsuarioApm usuarioApm){
		// Look up data
		RuntimeExceptionDao<UsuarioApm, Integer> usuarioApmDAO;
		usuarioApmDAO = databaseHelper.getRuntimeExceptionDao(UsuarioApm.class);
		usuarioApmDAO.createOrUpdate(usuarioApm);
	}

	/**
	 * Obtiene el Usuario de nombre indicado
	 */
	public UsuarioApm getUsuarioApmByUserName(String username) {
		Log.i(TAG, "getUsuarioApmByUserName: enter");
		UsuarioApm usuarioApm = null;

		// UsuarioApmDAO
		RuntimeExceptionDao<UsuarioApm, Integer>  usuarioApmDAO;
		usuarioApmDAO = databaseHelper.getRuntimeExceptionDao(UsuarioApm.class);

		Map<String,Object> mFilter = new HashMap<>();
		mFilter.put("username", username);
		mFilter.put("deleted", false);

		// UsuarioApm
		List<UsuarioApm> result = usuarioApmDAO.queryForFieldValuesArgs(mFilter);
		if (!result.isEmpty()) {
            usuarioApm = result.get(0);
        }

		Log.i(TAG, "getUsuarioApmByUserName: exit");
		return usuarioApm;
	}	
	
	/**
	 * Obtiene el UsuarioAPM para el Id pasado como parametro
	 */
	public UsuarioApm getUsuarioApmById(int idUsuario) {
		Log.i(TAG, "getUsuarioApmById: enter");
		UsuarioApm usuarioApm;

		// UsuarioApmDAO
		RuntimeExceptionDao<UsuarioApm, Integer>  usuarioApmDAO;
		usuarioApmDAO = databaseHelper.getRuntimeExceptionDao(UsuarioApm.class);

		usuarioApm = usuarioApmDAO.queryForId(idUsuario);
		Log.i(TAG, "getUsuarioApmById: exit");
		return usuarioApm;
	}	

	/**
	 * Retorna una lista con las aplicaciones que puede acceder el usuario logueado.
	 */
	public List<Aplicacion> getListAplicacionByUsuario(UsuarioApm usuario) {
		Log.i(TAG, "getListAplicacionByUsuario: enter");

		// PerfilAccesoAplicacionDAO
		RuntimeExceptionDao<PerfilAccesoAplicacion, Integer>  perfilAccesoAplicacionDAO;
		perfilAccesoAplicacionDAO = databaseHelper.getRuntimeExceptionDao(PerfilAccesoAplicacion.class);

		// AplicacionDAO
		RuntimeExceptionDao<Aplicacion, Integer>  aplicacionDAO;
		aplicacionDAO = databaseHelper.getRuntimeExceptionDao(Aplicacion.class);

		String strQuery =" SELECT pap.aplicacion_id FROM apm_perfilAccesoAplicacion pap, ";
			   strQuery +=" apm_perfilAcceso pa, apm_perfilAccesoUsuario pau ";
			   strQuery +=" WHERE pap.perfilAcceso_id = pa.id AND pa.id = pau.perfilAcceso_id ";
			   strQuery +=" AND pau.usuarioAPM_id = "+usuario.getId();
			   strQuery +=" AND pap.deleted = 0";	   

		Log.d(TAG, "strQuery: " + strQuery);

		// Find PerfilAccesoAplicacion
		List<Aplicacion> result = new ArrayList<>();
		try {
			GenericRawResults<String[]> rawResults = perfilAccesoAplicacionDAO.queryRaw(strQuery);			
			
			for (String[] cols : rawResults.getResults()) {
				Integer id = Integer.valueOf(cols[0]);
				result.add(aplicacionDAO.queryForId(id));
			}
		} catch (Exception e) {
			Log.e(TAG, "getListAplicacionByUsuario: ERROR", e);
		}

		Log.i(TAG, "getListAplicacionByUsuario: exit");
		return result;
	}

	public void createOrUpdateDispositivoMovil(DispositivoMovil dispositivoMovil){
		Log.i(TAG, "createOrUpdateDispositivoMovil: enter");
		// Look up data
		RuntimeExceptionDao<DispositivoMovil, Integer> dispositivoMovilDAO;
		dispositivoMovilDAO = databaseHelper.getRuntimeExceptionDao(DispositivoMovil.class);
		// 
		dispositivoMovilDAO.createOrUpdate(dispositivoMovil);
		Log.i(TAG, "createOrUpdateDispositivoMovil: exit");
	}

	public DispositivoMovil getDispositivoMovil(String emailAddress){
		Log.i(TAG, "getDispositivoMovil: enter");

		DispositivoMovil dispositivoMovil = null;
		// Look up data
		RuntimeExceptionDao<DispositivoMovil, Integer> dispositivoMovilDAO;
		dispositivoMovilDAO = databaseHelper.getRuntimeExceptionDao(DispositivoMovil.class);

		// Get our query builder from the DAO
		QueryBuilder<DispositivoMovil, Integer> queryBuilder = dispositivoMovilDAO.queryBuilder();
		try {
			// build query
			queryBuilder.orderBy("id", false).where().eq("deleted", false).and().eq("emailAddress", emailAddress);
			// prepare our sql statement
			PreparedQuery<DispositivoMovil> preparedQuery = queryBuilder.prepare();
			// default profile
			dispositivoMovil = dispositivoMovilDAO.queryForFirst(preparedQuery);
		} catch (Exception e) {
			Log.e(TAG, "getDispositivoMovil: ERROR", e);
		}

		Log.i(TAG, "getDispositivoMovil: exit");
		return dispositivoMovil;
	}
	
	
	/**
	 * Determina si el usuario tiene acceso a la aplicacion pasada como parametro.
	 */
    boolean hasAccess(int usuarioID, String codAplicacion) {
		Log.i(TAG, "hasAccess: enter");
		if (Constants.COD_UDAA.equals(codAplicacion)) {
			return true;
		}

		boolean hasAccess = false;

		// PerfilAccesoAplicacionDAO
		RuntimeExceptionDao<PerfilAccesoAplicacion, Integer>  perfilAccesoAplicacionDAO;
		perfilAccesoAplicacionDAO = databaseHelper.getRuntimeExceptionDao(PerfilAccesoAplicacion.class);

		String strQuery =" SELECT ap.id FROM apm_aplicacion ap, apm_perfilAccesoAplicacion pap, ";
			   strQuery +=" apm_perfilAcceso pa, apm_perfilAccesoUsuario pau ";
			   strQuery +=" WHERE pap.perfilAcceso_id = pa.id AND pa.id = pau.perfilAcceso_id ";
			   strQuery +=" AND pap.aplicacion_id = ap.id ";
			   strQuery +=" AND pau.usuarioAPM_id = "+usuarioID;
			   strQuery +=" AND ap.codigo COLLATE NOCASE LIKE ? ";
			   strQuery +=" AND ap.deleted = 0";	   

		Log.d(TAG, "strQuery: " + strQuery);

		// Find PerfilAccesoAplicacion
		GenericRawResults<String[]> rawResults = perfilAccesoAplicacionDAO.queryRaw(strQuery, codAplicacion);
		try {
			hasAccess = !rawResults.getResults().isEmpty();
		} catch (Exception ignore) {}

		Log.i(TAG, "hasAccess: exit");
		return hasAccess;
	}

	Campo getCampoBy(int campoId, int aplicacionPerfilId) {
		Log.i(TAG, "getCampoBy: enter");
		Campo campo = null;

		// PerfilAccesoAplicacionDAO
		RuntimeExceptionDao<PerfilAccesoAplicacion, Integer>  perfilAccesoAplicacionDAO;
		perfilAccesoAplicacionDAO = databaseHelper.getRuntimeExceptionDao(PerfilAccesoAplicacion.class);
		
		String strQuery="SELECT psc.campo_id FROM apm_aplPerfilSeccionCampo psc, apm_aplicacionPerfilSeccion ps ";
			   strQuery +=" WHERE ps.aplicacionPerfil_id = "+aplicacionPerfilId;
			   strQuery +=" AND psc.aplPerfilSeccion_id = ps.id ";
			   strQuery +=" AND psc.campo_id = "+ campoId;
			   strQuery +=" AND psc.deleted = 0";	   

		Log.d(TAG, "strQuery: " + strQuery);

		// Find PerfilAccesoAplicacion
		GenericRawResults<String[]> rawResults = perfilAccesoAplicacionDAO.queryRaw(strQuery);
		try {
			campo = getCampoById(Integer.parseInt(rawResults.getResults().get(0)[0]));
		} catch (Exception ignore) {}

		Log.i(TAG, "getCampoBy: exit");
		return campo;
	}
	
	private Campo getCampoById(int campoId){
		// CampoDAO
		RuntimeExceptionDao<Campo, Integer> campoDAO = databaseHelper.getRuntimeExceptionDao(Campo.class);
		return campoDAO.queryForId(campoId);
	}

	AplPerfilSeccionCampo getAplPerfilSeccionCampoById(int id){
		// AplPerfilSeccionCampoDAO
		RuntimeExceptionDao<AplPerfilSeccionCampo, Integer> aplPerfilSeccionCampoDAO;
		aplPerfilSeccionCampoDAO = databaseHelper.getRuntimeExceptionDao(AplPerfilSeccionCampo.class);
		return aplPerfilSeccionCampoDAO.queryForId(id);
	}
	
	/**
	 *  Create UbicacionGPS
	 */
	public void createUbicacionGPS(UbicacionGPS ubicacionGPS) {
		Log.d(TAG,"createUbicacionGPS : enter");
		RuntimeExceptionDao<UbicacionGPS, Integer> ubicacionGPSDAO = databaseHelper.getRuntimeExceptionDao(UbicacionGPS.class);
		
		ubicacionGPS.setModificationTimeStamp(new Date());
		ubicacionGPS.setModificationUser(/*this.appState.getCurrentUsername()*/ "test");
		ubicacionGPS.setDeleted(false);
		ubicacionGPS.setVersion(1);
		
		ubicacionGPSDAO.create(ubicacionGPS);
	}
	
	public List<UbicacionGPS> getGPSLocations() {
		RuntimeExceptionDao<UbicacionGPS, Integer> ubicacionGPSDAO = databaseHelper.getRuntimeExceptionDao(UbicacionGPS.class);
		return ubicacionGPSDAO.queryForAll();
	}
	
	public void deleteLocationGPS(UbicacionGPS u) {
		RuntimeExceptionDao<UbicacionGPS, Integer> ubicacionGPSDAO = databaseHelper.getRuntimeExceptionDao(UbicacionGPS.class);
		ubicacionGPSDAO.delete(u);
	}
	
	public Long getCountGPSLocation() {
		Log.i(TAG, "getCountGPSLocation: enter");
		RuntimeExceptionDao<UbicacionGPS, Integer>  ubicacionGPSDAO = databaseHelper.getRuntimeExceptionDao(UbicacionGPS.class);
		return ubicacionGPSDAO.countOf();
	}

	public void deleteLastGPSLocations(long count) {
		Log.i(TAG, "deleteLastGPSLocations | count: " + count);
		RuntimeExceptionDao<UbicacionGPS, Integer> ubicacionGPSDAO = databaseHelper.getRuntimeExceptionDao(UbicacionGPS.class);

		QueryBuilder<UbicacionGPS, Integer> queryBuilder = ubicacionGPSDAO.queryBuilder();
		queryBuilder.orderBy("modificationTimeStamp", true);
		queryBuilder.limit(count);

		try {
			List<UbicacionGPS> locations = queryBuilder.query();
			ubicacionGPSDAO.delete(locations);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	List<String> getListBinarioPathBy(String codAplicacion, String tipoBinario){
		Log.i(TAG, "getListBinarioPathBy: enter");
		
		// AplicacionBinarioVersionDAO
		RuntimeExceptionDao<AplicacionBinarioVersion, Integer>  aplicacionBinarioVersionDAO;
		aplicacionBinarioVersionDAO = databaseHelper.getRuntimeExceptionDao(AplicacionBinarioVersion.class);

		String strQuery =" SELECT DISTINCT(bin.ubicacion) ";
			   strQuery +=" FROM apm_aplicacionbinarioversion bin, apm_aplicacion apm, apm_aplicacionTipoBinario tip ";
			   strQuery +=" WHERE apm.codigo COLLATE NOCASE LIKE ? ";
			   strQuery +=" AND bin.aplicacion_id = apm.id ";
			   strQuery +=" AND bin.aplTipoBinario_id = tip.id ";
			   strQuery +=" AND tip.descripcion COLLATE NOCASE LIKE ? ";
			   strQuery +=" AND bin.deleted = 0";	   

		Log.d(TAG, "strQuery: " + strQuery);

		// Find AplicacionBinarioVersion
		GenericRawResults<String[]> rawResults = aplicacionBinarioVersionDAO.queryRaw(strQuery, codAplicacion, tipoBinario);
		
		List<String> result = new ArrayList<>();
		try {
			for (String[] cols : rawResults.getResults()) {
				result.add(cols[0]);
			}
		} catch (Exception e) {
			Log.e(TAG, "getListBinarioPathBy: ERROR", e);
		}
		
		Log.i(TAG, "getListBinarioPathBy: exit");
		return result;
	}
	
	
	
	//---------------------------------------------------------------------------
	//  DealleReporteError
	//---------------------------------------------------------------------------
	// CREATE
	public void createDetalleReporteError (DetalleReporteError detalleReporteError) {
		Log.d(TAG,"createDetalleReporteError : enter");
		try {
			RuntimeExceptionDao<DetalleReporteError, Integer> detalleReporteErrorDao = databaseHelper.getRuntimeExceptionDao(DetalleReporteError.class);

			detalleReporteError.setModificationTimeStamp(new Date());
			detalleReporteError.setModificationUser(this.appState.getCurrentUsername());			
			detalleReporteError.setDeleted(false);
			detalleReporteError.setVersion(1);
			
			detalleReporteErrorDao.create(detalleReporteError);
		} catch(Exception e) {
			Log.d(TAG,"createDetalleReporteError : " + e );
			Log.d(TAG,"createDetalleReporteError : " + e.getMessage());
		}

	}
	
	//---------------------------------------------------------------------------
	//  ErrorReport
	//---------------------------------------------------------------------------
	// CREATE
	public void createReporteError (ReporteError reporteError) {
		Log.d(TAG,"createReporteError : enter");
		try{
			RuntimeExceptionDao<ReporteError, Integer> errorReporterDao = databaseHelper.getRuntimeExceptionDao(ReporteError.class);
			reporteError.setModificationTimeStamp(new Date());

			reporteError.setModificationUser(this.appState.getCurrentUsername());
			reporteError.setDeleted(false);
			reporteError.setVersion(1);

			errorReporterDao.create(reporteError);
		}catch(Exception e){
			Log.d(TAG,"createReporteError : " + e );
			Log.d(TAG,"createReporteError : " + e.getMessage());
		}

	}
	//DELETE
	public void deleteReporteError(ReporteError r) {
		RuntimeExceptionDao<ReporteError, Integer> reporteErrorDao = databaseHelper.getRuntimeExceptionDao(ReporteError.class);
		reporteErrorDao.delete(r);
	}

	public void deleteDetalleReporteError(DetalleReporteError r) {
		RuntimeExceptionDao<DetalleReporteError, Integer> reporteErrorDao = databaseHelper.getRuntimeExceptionDao(DetalleReporteError.class);
		reporteErrorDao.delete(r);
	}
	// GET
	public List<ReporteError> getListReporteError() {
		RuntimeExceptionDao<ReporteError, Integer> errorReporterDao = databaseHelper.getRuntimeExceptionDao(ReporteError.class);
		return errorReporterDao.queryForAll();
	}
	
	boolean getAplicacionTabla(String tableName){
		Log.i(TAG, "getAplicacionTabla: enter");
		
		// AplicacionTablaDAO
		RuntimeExceptionDao<AplicacionTabla, Integer>  aplicacionTablaDAO;
		aplicacionTablaDAO = databaseHelper.getRuntimeExceptionDao(AplicacionTabla.class);

		String strQuery =" SELECT permiteActParcial ";
			   strQuery +=" FROM apm_aplicacionTabla ";
			   strQuery +=" WHERE tabla COLLATE NOCASE LIKE ? ";
			   strQuery +=" AND deleted = 0";	   

		Log.d(TAG, "strQuery: " + strQuery);

		// Find AplicacionBinarioVersion
		GenericRawResults<String[]> rawResults = aplicacionTablaDAO.queryRaw(strQuery, tableName);
		
		try {
			for (String[] cols : rawResults.getResults()) {
				if(cols[0].equals("1"))
					return true;
			}			
		} catch (Exception e) {
			Log.e(TAG, "getAplicacionTabla: ERROR", e);
		}
		
		Log.i(TAG, "getAplicacionTabla: exit");
		return false;
	}
	
	
	public AplicacionSync getApplicationSync() {
		Log.i(TAG, "getApplicationSync: enter");
		AplicacionSync aplicacionSync;

		// UsuarioApmDAO
		RuntimeExceptionDao<AplicacionSync, Integer>  aplicacionSyncDAO;
		aplicacionSyncDAO = databaseHelper.getRuntimeExceptionDao(AplicacionSync.class);

		aplicacionSync = aplicacionSyncDAO.queryForId(1);
		Log.i(TAG, "getApplicationSync: exit");
		return aplicacionSync;
	}	

	public void updateApplicationSync(AplicacionSync aplicacionSync) throws RuntimeException {
		Log.i(TAG, "updateApplicationSync: enter");
		RuntimeExceptionDao<AplicacionSync, Integer> aplicacionSyncDAO;
		aplicacionSyncDAO = databaseHelper.getRuntimeExceptionDao(AplicacionSync.class);

		aplicacionSync.setModificationTimeStamp(new Date());
		aplicacionSync.setModificationUser("TECSO"); 

		aplicacionSyncDAO.update(aplicacionSync);
		Log.i(TAG, "updateApplicationSync: enter");
	}

}