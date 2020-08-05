package coop.tecso.hcd.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.AccionHC;
import coop.tecso.hcd.entities.AplicacionTablaHC;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.AtencionCerrada;
import coop.tecso.hcd.entities.AtencionValor;
import coop.tecso.hcd.entities.CondicionAlertaHC;
import coop.tecso.hcd.entities.Despachador;
import coop.tecso.hcd.entities.EntidadBusqueda;
import coop.tecso.hcd.entities.ErrorAtencion;
import coop.tecso.hcd.entities.EstadoAtencion;
import coop.tecso.hcd.entities.MotivoCierreAtencion;
import coop.tecso.hcd.persistence.DatabaseHelper;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.aplicaciones.AplicacionParametro;
import coop.tecso.udaa.domain.base.TablaVersion;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;


public final class HCDigitalDAO {

	private static final String TAG = HCDigitalDAO.class.getSimpleName();

	private DatabaseHelper databaseHelper;
	private HCDigitalApplication appState; 

	public HCDigitalDAO(Context context) {
		this.databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
		this.appState = (HCDigitalApplication) context.getApplicationContext();
	}

	/**
	 * Obtiene la lista de Entidades de Busqueda
	 */
	public List<EntidadBusqueda> getListEntidadBusqueda() {
		Log.i(TAG, "getListEntidadBusqueda: enter");
		List<EntidadBusqueda> list;
		// EntidadBusqueda
		RuntimeExceptionDao<EntidadBusqueda, Integer> entidadBusquedaDAO = databaseHelper.getRuntimeExceptionDao(EntidadBusqueda.class);

		Map<String,Object> mFilter = new HashMap<>();
		mFilter.put("deleted", false);

		list = entidadBusquedaDAO.queryForFieldValues(mFilter);
		Log.i(TAG, "getListEntidadBusqueda: exit");
		return list;
	}

	/**
	 * Obtiene una lista de Atenciones Multiples asociadas a la Atencion Principal de id indicado. (tambien agrega a la lista la principal)
	 */
	public List<Atencion> getListAtencionByIdAtencionPrincipal(int atencionId) {
		Log.i(TAG, "getListAtencionByIdAtencionPrincipal: enter");
		List<Atencion> atencionList = new ArrayList<>();

		RuntimeExceptionDao<Atencion, Integer> atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);

		// Atencion Principal
		Atencion atencion = atencionDAO.queryForId(atencionId);
		int idAtencion;

		if (atencion != null) {
			idAtencion = atencion.getId();
			atencionList.add(atencion);
		} else {
			// No existe la atencion en la tabla, solo se busca la atencion principal
			idAtencion = atencionId;
		}

		Log.i(TAG, "getListAtencionByIdAtencionPrincipal: se agrega primero la principal con id="+idAtencion);

		Map<String,Object> mFilter = new HashMap<>();
		mFilter.put("atencionPrincipal_id", idAtencion);

		atencionList.addAll(atencionDAO.queryForFieldValuesArgs(mFilter));

		Log.i(TAG, "getListAtencionByIdAtencionPrincipal: exit , listSize="+atencionList.size());
		return atencionList;
	}

	/**
	 * Obtiene la Atencion de id indicada.
	 */
	public Atencion getAtencionById(Integer atencionId){
		RuntimeExceptionDao<Atencion, Integer> atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);
		RuntimeExceptionDao<AtencionValor, Integer>  atencionValorDAO = databaseHelper.getRuntimeExceptionDao(AtencionValor.class);		
		RuntimeExceptionDao<EstadoAtencion, Integer> estadoAtencionDAO = databaseHelper.getRuntimeExceptionDao(EstadoAtencion.class);
		RuntimeExceptionDao<MotivoCierreAtencion, Integer> motivoCierreAtencionDAO = databaseHelper.getRuntimeExceptionDao(MotivoCierreAtencion.class);
        RuntimeExceptionDao<Despachador, Integer> despachadorDAO = databaseHelper.getRuntimeExceptionDao(Despachador.class);
		Atencion atencion = atencionDAO.queryForId(atencionId);

		if(atencion != null){
			estadoAtencionDAO.refresh(atencion.getEstadoAtencion());
			motivoCierreAtencionDAO.refresh(atencion.getMotivoCierreAtencion());
            despachadorDAO.refresh(atencion.getDespachador());

			for (AtencionValor atencionValor : atencion.getAtencionValores()) {
				atencionValorDAO.refresh(atencionValor);
			}
		}

		return atencion;
	}

	/**
	 *  Obtiene el Estado Atencion para el id indicado
	 */
	public EstadoAtencion getEstadoAtencionById(int estadoAtencionId) {
		RuntimeExceptionDao<EstadoAtencion, Integer> estadoAtencionDAO = databaseHelper.getRuntimeExceptionDao(EstadoAtencion.class);

		return estadoAtencionDAO.queryForId(estadoAtencionId);
	}

	/**
	 *  Obtiene el Motivo de Cierre Atencion para el id indicado
	 */
	public MotivoCierreAtencion getMotivoCierreAtencionById(int motivoCierreAtencionId) {
		RuntimeExceptionDao<MotivoCierreAtencion, Integer> motivoCierreAtencionDAO = databaseHelper.getRuntimeExceptionDao(MotivoCierreAtencion.class);

		return motivoCierreAtencionDAO.queryForId(motivoCierreAtencionId);
	}

	/**
	 *  Crea una AtencionValor
	 */
	public AtencionValor createAtencionValor(AtencionValor atencionValor){
		// Logged User
		UsuarioApm currentUser = appState.getCurrentUser();
		RuntimeExceptionDao<AtencionValor, Integer>  atencionValorDAO = databaseHelper.getRuntimeExceptionDao(AtencionValor.class);

		atencionValor.setModificationTimeStamp(new Date());
		atencionValor.setModificationUser((currentUser!=null)?currentUser.getNombre():"desconocido");
		atencionValorDAO.create(atencionValor);

		return atencionValor;
	}

	/**
	 *  Elimina una AtencionValor
	 */
	public void deleteAtencionValor(AtencionValor atencionValor) {
		RuntimeExceptionDao<AtencionValor, Integer>  atencionValorDAO = databaseHelper.getRuntimeExceptionDao(AtencionValor.class);

		atencionValorDAO.delete(atencionValor);
	}

	/**
	 *  Crear Atencion (IM)
	 */
	public void createAtencion(Atencion atencion) throws SQLException {
		// Logged User
		UsuarioApm currentUser = appState.getCurrentUser();
		RuntimeExceptionDao<Atencion, Integer> atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);

		atencion.setAsignacionTimeStamp(new Date());
		atencion.setModificationTimeStamp(new Date());
		atencion.setModificationUser((currentUser!=null)?currentUser.getNombre():"desconocido"); 
		atencionDAO.create(atencion);
	}

	/**
	 *  Update Atencion (IM)
	 */
	public void updateAtencion(Atencion atencion) {
		// Logged User
		UsuarioApm currentUser = appState.getCurrentUser();
		RuntimeExceptionDao<Atencion, Integer> atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);

		atencion.setModificationTimeStamp(new Date());
		atencion.setModificationUser((currentUser!=null)?currentUser.getNombre():"desconocido");
		atencionDAO.update(atencion);
	}

	/**
	 *  Elimina todos los registros de Atencion
	 */
	public void deleteAllSynchronizedAtencion() throws SQLException {
		Log.i(TAG, "deleteAllSynchronizedAtencion: enter");

		RuntimeExceptionDao<Atencion, Integer>  atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);
		RuntimeExceptionDao<AtencionValor, Integer>  atencionValorDAO = databaseHelper.getRuntimeExceptionDao(AtencionValor.class);

		QueryBuilder<Atencion, Integer> queryBuilder = atencionDAO.queryBuilder();
		try {
			// Elimino Atenciones sincronizadas
			Double deleteHours = ParamHelper.getDouble(ParamHelper.COD_DELETE_IMD_TIME, 1D);
			// Paso horas a minutos
			int minutes = (int) (deleteHours * 60.0D);
			//Jira HCDDM-199: Borrado de IMD en tablet
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.MINUTE, -minutes);
			// Build query
			queryBuilder.where().eq("deleted", true).and().lt("modificationTimeStamp", calendar.getTime());
			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());

			//Execute query
			List<Atencion> atencionListToDelete = queryBuilder.query();

			// Recorremos los registros a eliminar. 
			Log.i(TAG, "deleteAllSynchronizedAtencion: cantidad atenciones:"+atencionListToDelete.size());
			for(Atencion atencion: atencionListToDelete){
				Log.i(TAG, "deleteAllSynchronizedAtencion: Eliminando Atencion de id:"+atencion.getId());
				// Eliminamos primero la lista de valores asociados
				DeleteBuilder<AtencionValor, Integer> db = atencionValorDAO.deleteBuilder();
				db.where().eq("atencion_id", atencion.getId());
				atencionValorDAO.delete(db.prepare());
				
				eliminarAtencionCerrada(atencion.getId());
				
				// Y finalmente la atencion
				atencionDAO.delete(atencion);
				Log.i(TAG, "deleteAllSynchronizedAtencion: Atencion eliminada.");
			}
		} catch (SQLException e) {
			Log.e(TAG, "ERROR: deleteAllSynchronizedAtencion",e);
		}

		Log.i(TAG, "deleteAllSynchronizedAtencion: exit");
	}

	/**
	 * Obtiene la lista de Motivo Cierre Atencion
	 */
	public List<MotivoCierreAtencion> getListMotivoCierreAtencion(Context context) {
		// MotivoCierreAtencion
		RuntimeExceptionDao<MotivoCierreAtencion, Integer> motivoCierreAtencionDAO = databaseHelper.getRuntimeExceptionDao(MotivoCierreAtencion.class);
		Map<String,Object> mFilter = new HashMap<>();
		mFilter.put("deleted", false);
		// List : MotivoCierreAtencion
		List<MotivoCierreAtencion> motivoCierreAtencionList = motivoCierreAtencionDAO.queryForFieldValuesArgs(mFilter);
		
	    Collections.sort(motivoCierreAtencionList, (p1, p2) -> p1.getOrden() > p2.getOrden() ? -1 : 0);
	    
		return motivoCierreAtencionList;
	}

	/**
	 * Obtiene la lista de Atenciones
	 */
	public List<Atencion> getListAtencion() {
		Log.i(TAG, "getListAtencion: enter");
		List<Atencion> atencionList = new ArrayList<>();

		// Atencion
		RuntimeExceptionDao<Atencion, Integer>  atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);
		// EstadoAtencion
		RuntimeExceptionDao<EstadoAtencion, Integer>  estadoAtencionDAO = databaseHelper.getRuntimeExceptionDao(EstadoAtencion.class);

		QueryBuilder<Atencion, Integer> queryBuilder = atencionDAO.queryBuilder();
		try {
			queryBuilder.orderBy("estadoAtencion_id", true)
			.orderBy("asignacionTimeStamp", false);

			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());
			//Execute query
			atencionList =queryBuilder.query();

			for (Atencion atencion : atencionList) {
				int idAP = 0;
				if(atencion.getAtencionPrincipal()!= null)
					idAP = atencion.getAtencionPrincipal().getId();
				Log.d(TAG, "atencionId: "+atencion.getId()+" , atencionPrincipalId "+idAP);
				estadoAtencionDAO.refresh(atencion.getEstadoAtencion());
				
				try {	
					if (atencion.enPreparacion()) {
						validarAtencionCerrada(atencion);
					}
				}
				catch (Exception e) {
					Log.e(TAG, "ERROR-INCONSISTENCIA:",e);
				}
			}
		} catch (SQLException e) {
			Log.e(TAG, "ERROR: getListAtencion",e);
		}

		Log.i(TAG, "getListAtencion: exit");
		return atencionList;
	}

	/**
	 * Verifica si existen IM en estado "En preparacion" o "Cierre Provisorio".
	 */
	public boolean existeIMAbierto() {
		Log.i(TAG, "existeIMAbierto: enter");
		List<Atencion> atencionList = new ArrayList<>();

		// Atencion
		RuntimeExceptionDao<Atencion, Integer>  atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);

		QueryBuilder<Atencion, Integer> queryBuilder = atencionDAO.queryBuilder();
		try {
			// Build query
			queryBuilder.where().eq("estadoAtencion_id", EstadoAtencion.ID_EN_PREPARACION)
			.or().eq("estadoAtencion_id", EstadoAtencion.ID_CERRADA_PROVISORIA);

			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());
			//Execute query
			atencionList = queryBuilder.query();
		} catch (SQLException e) {
			Log.e(TAG, "ERROR: getListAtencion",e);
		}

		Log.i(TAG, "existeIMAbierto: exit");
		return !CollectionUtils.isEmpty(atencionList);
	}

	/**
	 * Obtiene la primer Atencion disponible en estado en preparacion
	 */
	public Atencion getAtencionEnPreparacion(List<Integer> skipAtencionIdList) {
		Log.i(TAG, "getAtencionEnPreparacion: enter , skipAtencionIdList="+(skipAtencionIdList!=null?skipAtencionIdList.size():"null"));
		List<Atencion> atencionList = new ArrayList<>();

		// Atencion
		RuntimeExceptionDao<Atencion, Integer>  atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);

		QueryBuilder<Atencion, Integer> queryBuilder = atencionDAO.queryBuilder();
		try {
			// Build query
			queryBuilder.orderBy("asignacionTimeStamp", true)
			.where().isNull("atencionPrincipal_id")
			.or().eq("atencionPrincipal_id", 0)
			.and().eq("estadoAtencion_id", EstadoAtencion.ID_EN_PREPARACION);

			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());
			//Execute query
			atencionList = queryBuilder.query();

		} catch (SQLException e) {
			Log.e(TAG, "ERROR: getListAtencion",e);
		}

		for (Atencion atencion: atencionList) {
			boolean shouldSkip = CollectionUtils.matchElement(skipAtencionIdList, (id) -> atencion.getId() == id);
			if (shouldSkip) {
				continue;
			}

			Log.i(TAG, "getAtencionEnPreparacion: exit , atencionId "+atencion.getId());
			return atencion;
		}
		Log.i(TAG, "getAtencionEnPreparacion: exit without result");
		return null;
	}

	/**
	 * Obtiene la lista de Atenciones pendiente de envio al servidor
	 */
	public List<Atencion> getListAtencionToSync() {
		Log.i(TAG, "getListAtencionToSync: enter");
		List<Atencion> atencionList = new ArrayList<>();

		// Atencion
		RuntimeExceptionDao<Atencion, Integer>  atencionDAO = databaseHelper.getRuntimeExceptionDao(Atencion.class);

		QueryBuilder<Atencion, Integer> queryBuilder = atencionDAO.queryBuilder();
		try {
			// Build query
			queryBuilder.orderBy("asignacionTimeStamp", true)
			.where().eq("estadoAtencion_id", EstadoAtencion.ID_CERRADA_DEFINITIVA)
			.or().eq("estadoAtencion_id", EstadoAtencion.ID_ANULADA)
			.and().eq("deleted", false);

			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());
			//Execute query
			atencionList = queryBuilder.query();

		} catch (SQLException e) {
			Log.e(TAG, "ERROR: getListAtencion",e);
		}

		Log.i(TAG, "getListAtencionToSync: exit");
		return atencionList;
	}

	// ErrorAtencion -->>

	/**
	 * Obtiene lista de registros de ErrorAtencion activos del "tipo" pasado como parametro.
	 */
	public List<ErrorAtencion> getListErrorAtencionByTipo(String tipo) {
		Log.i(TAG, "getListErrorAtencion: enter");

		// AplicacionParametroDAO
		RuntimeExceptionDao<ErrorAtencion, Integer>  errorAtencionDAO;
		errorAtencionDAO = databaseHelper.getRuntimeExceptionDao(ErrorAtencion.class);

		// Filters
		Map<String,Object> mFilter = new HashMap<>();
		mFilter.put("deleted", false);
		mFilter.put("tipo", tipo);

		Log.i(TAG, "getListErrorAtencion: exit");
		return errorAtencionDAO.queryForAll();
	}
	// <<-- ErrorAtencion
	
	/**
	 * Obtiene la lista de acciones del perfil
	 */
	public List<AccionHC> getAccionesByPerfil(int idPerfil) {
		Log.i(TAG, "getAccionesByPerfil: enter");
		List<AccionHC> accionList = new ArrayList<>();

		// AccionHC
		RuntimeExceptionDao<AccionHC, Integer> accionDAO = databaseHelper.getRuntimeExceptionDao(AccionHC.class);

		QueryBuilder<AccionHC, Integer> queryBuilder = accionDAO.queryBuilder();
		try {
			queryBuilder
			.where().eq("aplicacionPerfilID", idPerfil)
			.and().eq("deleted", false);

			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());
			
			//Execute query
			accionList = queryBuilder.query();
		} catch (SQLException e) {
			Log.e(TAG, "ERROR: getAccionesByPerfil",e);
		}

		Log.i(TAG, "getAccionesByPerfil: exit");
		return accionList;
	}	

	/**
	 * Obtiene la lista de Condiciones de alerta del perfil
	 */
	public List<CondicionAlertaHC> getCondicionesAlertaByPerfil(int idPerfil) {
		Log.i(TAG, "getCondicionesAlertaByPerfil: enter");
		List<CondicionAlertaHC> condicionList = new ArrayList<>();

		// CondicionAlerta
		RuntimeExceptionDao<CondicionAlertaHC, Integer> condicionAlertaDAO = databaseHelper.getRuntimeExceptionDao(CondicionAlertaHC.class);
		// ErrorAtencion
		RuntimeExceptionDao<ErrorAtencion, Integer>  errorAtencionDAO = databaseHelper.getRuntimeExceptionDao(ErrorAtencion.class);

		QueryBuilder<CondicionAlertaHC, Integer> queryBuilder = condicionAlertaDAO.queryBuilder();
		try {
			queryBuilder
			.where().eq("aplicacionPerfilID", idPerfil)
			.and().eq("deleted", false);

			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());
			
			//Execute query
			condicionList = queryBuilder.query();

			for (CondicionAlertaHC condicionAlerta : condicionList) {
				Log.d(TAG, "condicionAlertaHCId: "+condicionAlerta.getId()+" , alertaHCDId "+condicionAlerta.getAlertaHcd().getId());
				errorAtencionDAO.refresh(condicionAlerta.getAlertaHcd());
			}
		} catch (SQLException e) {
			Log.e(TAG, "ERROR: getCondicionesAlertaByPerfil",e);
		}

		Log.i(TAG, "getCondicionesAlertaByPerfil: exit");
		return condicionList;
	}	
	
	public List<CondicionAlertaHC> getCondicionesAlertaByPerfilAndAlerta(int idPerfil, String codAlerta) {
		Log.i(TAG, "getCondicionesAlertaByPerfil: enter");
		List<CondicionAlertaHC> condicionList = new ArrayList<>();
		// CondicionAlerta
		RuntimeExceptionDao<CondicionAlertaHC, Integer> condicionAlertaDAO = databaseHelper.getRuntimeExceptionDao(CondicionAlertaHC.class);
		// ErrorAtencion
		RuntimeExceptionDao<ErrorAtencion, Integer>  errorAtencionDAO = databaseHelper.getRuntimeExceptionDao(ErrorAtencion.class);

		try {		
			ErrorAtencion alerta = findErrorAtencionByCode(codAlerta);
			QueryBuilder<CondicionAlertaHC, Integer> queryBuilder = condicionAlertaDAO.queryBuilder();
			queryBuilder
			.where().eq("aplicacionPerfilID", idPerfil)
			.and().eq("alertaHcd_id", alerta.getId())
			.and().eq("deleted", false);

			Log.d(TAG, "query: "+queryBuilder.prepareStatementString());
			
			//Execute query
			condicionList = queryBuilder.query();

			for (CondicionAlertaHC condicionAlerta : condicionList) {
				Log.d(TAG, "condicionAlertaHCId: "+condicionAlerta.getId()+" , alertaHCDId "+condicionAlerta.getAlertaHcd().getId());
				errorAtencionDAO.refresh(condicionAlerta.getAlertaHcd());
			}
		} catch (SQLException e) {
			Log.e(TAG, "ERROR: getCondicionesAlertaByPerfil",e);
		}

		Log.i(TAG, "getCondicionesAlertaByPerfil: exit");
		return condicionList;
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
     * Obtiene el Despachador de id indicado.
     */
    public Despachador getDespachadorById(Integer idDespachador) {
        RuntimeExceptionDao<Despachador, Integer> despachadorDAO = databaseHelper.getRuntimeExceptionDao(Despachador.class);
		return despachadorDAO.queryForId(idDespachador);
    }

	public boolean getAplicacionTabla (String tableName){
		Log.i(TAG, "getAplicacionTabla: enter");

		// AplicacionTablaDAO
		RuntimeExceptionDao<AplicacionTablaHC, Integer>  aplicacionTablaDAO;
		aplicacionTablaDAO = databaseHelper.getRuntimeExceptionDao(AplicacionTablaHC.class);

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
	
	public ErrorAtencion findErrorAtencionByCode(String codigo) {
		Log.i(TAG, "findErrorAtencionByCode: enter");

		// Filters
		Map<String,Object> mFilter = new HashMap<>();
		mFilter.put("deleted", false);
		mFilter.put("codigo", codigo);

		RuntimeExceptionDao<ErrorAtencion, Integer> atencionDAO = databaseHelper.getRuntimeExceptionDao(ErrorAtencion.class);
		List<ErrorAtencion> erroresAtencion = atencionDAO.queryForFieldValuesArgs(mFilter);
		Log.i(TAG, "findErrorAtencionByCode: exit");
		if(erroresAtencion.size() == 0)
			return null;
		
		return erroresAtencion.get(0);
	}

	public void updateAtencionValor(AtencionValor atencionValor) {
		// Logged User
		UsuarioApm currentUser = appState.getCurrentUser();
		RuntimeExceptionDao<AtencionValor, Integer> atencionDAO = databaseHelper.getRuntimeExceptionDao(AtencionValor.class);

		atencionValor.setModificationTimeStamp(new Date());
		atencionValor.setModificationUser((currentUser!=null)?currentUser.getNombre():"desconocido");
		atencionDAO.update(atencionValor);
	}

	public List<AtencionValor> findAllToSync(Atencion atencion) {
		Log.i(TAG, "findAllToSync: enter");

		List<AtencionValor> resultList = new ArrayList<>();
		RuntimeExceptionDao<AtencionValor, Integer>  atencionValorDAO = databaseHelper.getRuntimeExceptionDao(AtencionValor.class);
		QueryBuilder<AtencionValor, Integer> queryBuilder = atencionValorDAO.queryBuilder();
		try {
			queryBuilder.orderBy("modificationTimeStamp", true)
			.where().eq("syncImagen", false)
			.and().isNotNull("imagen")
			.and().eq("atencion_id", atencion.getId());

			//Execute query
			Log.d(TAG, " query: "+ queryBuilder.prepareStatementString());
			resultList = queryBuilder.query();
		} catch (SQLException e) {
			Log.e(TAG, "findAllToSync: ***ERROR***", e);
		}

		Log.i(TAG, "findAllToSync: exit");
		return resultList;
	}

	private List<AtencionCerrada> getAtencionCerrada(int idAtencion) {
		try {
			// Build query
			RuntimeExceptionDao<AtencionCerrada, Integer> atencionCerradaDAO = databaseHelper.getRuntimeExceptionDao(AtencionCerrada.class);
			QueryBuilder<AtencionCerrada, Integer> queryBuilder = atencionCerradaDAO.queryBuilder();
			queryBuilder.where().eq("idAtencion", idAtencion);
			return queryBuilder.query();
		} catch (SQLException e) {
			Log.e(TAG, "getAtencionCerrada: ***ERROR***", e);
			return null;
		}		
	}
	
	public void createAtencionCerrada(AtencionCerrada atCerrada) {
		try {
			List<AtencionCerrada> atencionCerradasList = getAtencionCerrada(atCerrada.getIdAtencion());
			if(atencionCerradasList != null && atencionCerradasList.size() == 0) {
				RuntimeExceptionDao<AtencionCerrada, Integer> atencionCerradaDAO = databaseHelper.getRuntimeExceptionDao(AtencionCerrada.class);
				atencionCerradaDAO.create(atCerrada);
			}
		} catch (Exception ignore) {}
	}

	public void updateAtencionCerrada(int idAtencion, int idEstado, boolean isSyncHeader, boolean isSyncComplete) {
		try {
			List<AtencionCerrada> atencionCerradasList = getAtencionCerrada(idAtencion);
			if (!CollectionUtils.isEmpty(atencionCerradasList)) {
				RuntimeExceptionDao<AtencionCerrada, Integer> atencionCerradaDAO = databaseHelper.getRuntimeExceptionDao(AtencionCerrada.class);				
				for (AtencionCerrada atCerrada : atencionCerradasList) {	
					atCerrada.setIdEstadoAtencion(idEstado);			
					atCerrada.setIsSyncHeader(isSyncHeader);
					atencionCerradaDAO.update(atCerrada);
				}
			}
			else {
				AtencionCerrada atCerrada = new AtencionCerrada();
				atCerrada.setIdAtencion(idAtencion);
				atCerrada.setIdEstadoAtencion(idEstado);
				if(idEstado == EstadoAtencion.ID_ANULADA)
					atCerrada.setFechaAnulacion(new Date());	
				if(idEstado == EstadoAtencion.ID_CERRADA_DEFINITIVA)
					atCerrada.setFechaCierreDefinitivo(new Date());
				this.createAtencionCerrada(atCerrada);
			}
		} catch (Exception ignore) {}
	}
	
	public void validarAtencionCerrada(int idAtencion) {
		Atencion atencion = getAtencionById(idAtencion);
		validarAtencionCerrada(atencion);
	}
	
	private void validarAtencionCerrada(Atencion atencion) {
		try {
			if (atencion == null || !atencion.enPreparacion()) {
				return;
			}

			List<AtencionCerrada> atencionCerradasList = getAtencionCerrada(atencion.getId());
			// Build query
			for (AtencionCerrada atCerrada : atencionCerradasList) {
				if (!atCerrada.enPreparacion() && atCerrada.getIdEstadoAtencion() > 0) {
					generateACRA_LOG("jsonPreGuardado: " + atCerrada.getJsonPreGuardado());
					generateACRA_LOG("jsonPostGuardado: " + atCerrada.getJsonPostGuardado());
					generateACRA_LOG("jsonAtencion: " + atencion.toJSONWithoutFirmas());
				}
				return;
			}
		} catch (Exception ignore) {}
	}
	
	private void eliminarAtencionCerrada(int idAtencion) {
		try {
			List<AtencionCerrada> atencionCerradasList = getAtencionCerrada(idAtencion);
			RuntimeExceptionDao<AtencionCerrada, Integer> atencionCerradaDAO = databaseHelper.getRuntimeExceptionDao(AtencionCerrada.class);
			for (AtencionCerrada atCerrada : atencionCerradasList) {
				atencionCerradaDAO.delete(atCerrada);
			}
		} catch (Exception ignore) {}
	}	

	private void generateACRA_LOG(String message) {
		// Application version
		Intent msg = new Intent();

		ReporteError reporteError = new ReporteError();
		reporteError.setFechaCaptura(new Date());
		reporteError.setDescripcion(Constants.COD_HCDIGITAL + "_ErrorAtencionReabierta");
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

		this.appState.sendBroadcast(msg);
	}
}