package coop.tecso.hcd.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import coop.tecso.hcd.R;
import coop.tecso.hcd.dao.HCDigitalDAO;
import coop.tecso.hcd.entities.AccionHC;
import coop.tecso.hcd.entities.CondicionAlertaHC;
import coop.tecso.hcd.entities.ErrorAtencion;
import coop.tecso.hcd.helpers.IMNotification;
import coop.tecso.hcd.helpers.IMNotificationArea;
import coop.tecso.hcd.helpers.IMNotificationType;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ErrorConstants;
import coop.tecso.udaa.domain.notificaciones.Notificacion;
import coop.tecso.udaa.domain.notificaciones.TipoNotificacion;

/**
 * 
 * Manager para Notificaciones de Informes Medicos
 *
 * <p>Ejemplos para lanzar notificaciones: </p>
 * <p><i>IMNotificationManager.getInstance().addMedicalNotification(tabId, "A1");</i></p>
 * <p><i>IMNotificationManager.getInstance().addErrorNotification(tabId, "E1", false);</i></p>
 * <p><i>IMNotificationManager.getInstance().addGeneralNotification(tabId,notificacion);</i></p>
 * 
 * <p>Ejemplos para lanzar error sin area de notificacion: </p>
 * <p><i>IMNotificationManager.getInstance().viewError(this, ErrorConstants.ERROR_EXISTE_IM_SIN_CIERRE);</i></p>
 * 
 * @author tecso
 *
 */
@SuppressWarnings("WeakerAccess")
public final class IMNotificationManager {
	
	private static final String TAG = IMNotificationManager.class.getSimpleName();

	private HCDigitalDAO hcDigitalDAO;
	
	private Context context;
	
	// Cache de Notificaciones de tipo Alertas Medicas 
	private Map<String, IMNotification> alertasMedicasCache;
	// Cache de Notificaciones de tipo Errores
	private Map<String, IMNotification> errorCache;
	// Mapas de Areas de Notificacion por IM (Cada IM asociado a un Tab)
	private Map<String, IMNotificationArea> notificationAreaMap;

	public IMNotificationManager(Context context){
		this.context = context;
	}

	/**
	 * Inicializa el manager cargando el cache de notificaciones lanzables (todas menos las generales)
	 */
	public void initialize() {
		try {
			// Inicializa el DAO para acceder a la db
			this.hcDigitalDAO = new HCDigitalDAO(context);
			
			// Inicializar cache de alertas medicas desde la db 
			this.alertasMedicasCache = obtenerAlertasMedicas();
			// Inicializar cache de errores desde la db 
			this.errorCache = obtenerErrores();
			// Se inicializa el contenedor de Areas de Notificacion por IM 
			this.notificationAreaMap = new HashMap<>();
		} catch (Exception e) {
			Log.e(TAG, "Error al inicializar area de alertas", e);
		}
	}
	
	/**
	 * Obtiene las acciones en la db
	 */
	public List<AccionHC> obtenerAcciones(int idPerfil){
		return hcDigitalDAO.getAccionesByPerfil(idPerfil);
	}
	
	/**
	 * Obtiene las condiciones de alertas en la db
	 */
	public List<CondicionAlertaHC> obtenerCondicionesAlertas(int idPerfil){
		return hcDigitalDAO.getCondicionesAlertaByPerfil(idPerfil);
	}
	
	public List<CondicionAlertaHC> obtenerCondicionesAlertas(int idPerfil, String codAlerta){
		return hcDigitalDAO.getCondicionesAlertaByPerfilAndAlerta(idPerfil, codAlerta);
	}
	
	/**
	 * Obtiene las alertas medicas cargadas en la db y arma un mapa por codigo.
	 */
	private Map<String, IMNotification> obtenerAlertasMedicas(){
		Map<String, IMNotification> alertasMedicasCache = new HashMap<>();
		
		List<ErrorAtencion> listAlertasMedicas = hcDigitalDAO.getListErrorAtencionByTipo(Constants.ERROR_ATENCION_TIPO_ALERTA);
		for(ErrorAtencion alerta: listAlertasMedicas){
			
			int icono = 0;
			if (!TextUtils.isEmpty(alerta.getIconFileName())) {
				icono = context.getResources().getIdentifier(alerta.getIconFileName(), "drawable", context.getPackageName());
			}
			if (icono == 0) {
				icono = R.drawable.ic_emergency_default;
			}
			
			int sound = 0;
			if (!TextUtils.isEmpty(alerta.getSoundFileName())) {
				sound = context.getResources().getIdentifier(alerta.getSoundFileName(), "raw", context.getPackageName());
			}
			if (sound == 0) {
				sound = R.raw.heaven;
			}
			
			IMNotification notification = new IMNotification(alerta.getId(),
															 alerta.getCodigo(),
															 IMNotificationType.MEDICAS,
															 null,
															 null,
															 alerta.getDescripcionCorta(),
															 alerta.getDescripcionLarga(),
															 icono,
															 sound,
															 false,
															 alerta.isSiempreVisible(),
															 alerta.isForzarLectura());

			alertasMedicasCache.put(notification.getCode(), notification);
		}

		return alertasMedicasCache;
	}
	
	/**
	 * Obtiene las alertas medicas cargadas en la db y arma un mapa por codigo.
	 */
	private Map<String, IMNotification> obtenerErrores(){
		Map<String, IMNotification> errorCache = new HashMap<>();
		
		List<ErrorAtencion> listError = hcDigitalDAO.getListErrorAtencionByTipo(Constants.ERROR_ATENCION_TIPO_ERROR);
		for (ErrorAtencion error: listError) {
			
			int icono = 0;
			if (!TextUtils.isEmpty(error.getIconFileName())) {
				icono = context.getResources().getIdentifier(error.getIconFileName(), "drawable", context.getPackageName());
			}
			if (icono == 0) {
				icono = R.drawable.ic_emergency_default;
			}
			
			int sound = 0;
			if (!TextUtils.isEmpty(error.getSoundFileName())) {
				sound = context.getResources().getIdentifier(error.getSoundFileName(), "raw", context.getPackageName());
			}
			if (sound == 0) {
				sound = R.raw.heaven;
			}
			
			IMNotification notification = new IMNotification(error.getId(),
															 error.getCodigo(),
															 IMNotificationType.ERROR,
															 null,
															 null,
															 error.getDescripcionCorta(),
															 error.getDescripcionLarga(),
															 icono,
															 sound,
															 false,
															 error.isSiempreVisible(),
															 error.isForzarLectura());

			errorCache.put(notification.getCode(), notification);
		}
		
		return errorCache;
	}

	/**
	 *  Crea una nueva area de notificaciones asociada al tabId indicado
	 *  <p>Los IM se encuentran en tab, para notificar sobre un IM se debe indicar el tabId</p>
	 */
	public View buildNewArea(Activity context, String tabId){
		// Se crea un area de notificacion
		LayoutInflater inflater = context.getLayoutInflater();
		HorizontalScrollView areaView = (HorizontalScrollView) inflater.inflate(R.layout.im_notification_area, null);
		LinearLayout listNotificationView = areaView.findViewById(R.id.imNotificationArea);
		listNotificationView.setTag(tabId);

		// Se inicializa el mapa de notificaciones para el area
		Map<String,IMNotification> notificationsMap = new HashMap<>();

		IMNotificationArea notificationArea = new IMNotificationArea(context, notificationsMap, areaView);

		this.notificationAreaMap.put(tabId, notificationArea);

		return areaView;
	}
	
	/**
	 * Eliminar el area de notificaciones asociada al tabId indicado
	 */
	public void removeArea(String tabId){
		try {
			this.notificationAreaMap.remove(tabId);
		} catch (Exception ignore) {}
	}

	/**
	 * Agrega una notificacion de tipo 'Alerta Medica'
	 */
	public boolean addMedicalNotification(String tabId, String code){
		// Buscamos el area de notificacion para el tabId
		IMNotificationArea notificationArea = this.notificationAreaMap.get(tabId);
		if (notificationArea == null) {
			Log.d(TAG, "No se encontro el area de notificacion para el tabId: " + tabId);
			return false;
		}

		// Buscamos el tipo de notificacion en el cache correspondiente
		IMNotification medicalAlert = this.alertasMedicasCache.get(code);
		// Si no se encuentra en el cache se lanza error
		if (medicalAlert == null) {
			addErrorNotification(tabId, ErrorConstants.ERROR_MEDICAL_ALERT_UNKNONW, true);
			return false;
		}
		// Se busca el mapa de notificaciones asociado al tab indicado (Los IM se encuentran en tab, para notificar sobre un IM se debe indicar el tabId)
		Map<String,IMNotification> notificationsMap = notificationArea.getNotificationsMap();
		// Se valida si ya se lanzo la alerta. Si no se lanzo se carga.
		if (notificationsMap.get(code) == null) {
			// Se carga el error
			notificationArea.addNotification(medicalAlert);
			return true;
		}
		return false;
	}
	
	/**
	 * Busca un error por codigo en el cache. Si no encuentra el error genera uno generico sin descripcion.
	 */
	private IMNotification getError(String code){
		// Buscamos el tipo de notificacion en el cache correspondiente
		IMNotification error = this.errorCache.get(code);
		
		// Si no se encuentra en la lista de errores se arma notificacion con codigo y sin descripcion
		if (error == null) {
			error = new IMNotification(9999,code,IMNotificationType.ERROR,null,null,"","",R.drawable.ic_error_default,R.raw.heaven,false,false,true);			
		}
		return error;
	}
	
	/**
	 * Agrega una notificacion de tipo 'Error'
	 */
	public void addErrorNotification(String tabId, String code, boolean esGrave){
		// Buscamos el area de notificacion para el tabId
		IMNotificationArea notificationArea = this.notificationAreaMap.get(tabId);
		if (notificationArea == null) {
			Log.d(TAG, "No se encontro el area de notificacion para el tabId: "+tabId);
			return;
		}
		
		// Buscamos el tipo de notificacion en el cache correspondiente
		IMNotification error = this.getError(code);
		// Se carga el error
		notificationArea.addNotification(error);
	}

	/**
	 * Agrega una notificacion de tipo 'General' (Notificaciones informadas desde la UDAA)
	 */
	private void addGeneralNotification(String tabId, Notificacion notificacion){
		// Buscamos el area de notificacion para el tabId
		IMNotificationArea notificationArea = this.notificationAreaMap.get(tabId);
		if (notificationArea == null) {
			Log.d(TAG, "No se encontro el area de notificacion para el tabId: "+tabId);
			return;
		}

		TipoNotificacion tipoNotificacion = notificacion.getTipoNotificacion();

		int icon = 0;
		if (!TextUtils.isEmpty(tipoNotificacion.getUbicacionIcono())) {
			icon = context.getResources().getIdentifier(tipoNotificacion.getUbicacionIcono(), "drawable", context.getPackageName());
		}
		if (icon == 0) {
			icon = R.drawable.ic_emergency_default;
		}

		int sound = 0;
		if (!TextUtils.isEmpty(tipoNotificacion.getUbicacionSonido())) {
			sound = context.getResources().getIdentifier(tipoNotificacion.getUbicacionSonido(), "raw", context.getPackageName());
		}
		if (sound == 0) {
			sound = R.raw.moonbeam;
		}

		IMNotification notification = new IMNotification(notificacion.getId(), 
														 String.valueOf(notificacion.getId()),
														 IMNotificationType.GENERAL,
														 notificacion.getNumeroAplicacion(),
														 notificacion.getFecha(),
														 notificacion.getDescripcionReducida(),
														 notificacion.getDescripcionAmpliada(),
														 icon,
														 sound,
														 false,
														 false,
														 false);
		// Se carga la notificacion
		notificationArea.addNotification(notification);
	}
	
	/**
	 * Devuelve un string con la lista de codigos de alertas medicas lanzadas al area. Por cada una acompaña con un '#' y true o false indicando si fue leida o no.
	 * 
	 * <p> Ejemplo: codigosAlertas: 001#false,002#true,003#true </p>
	 * <p> , donde: false - No leido, true - Leido</p>
	 */
	public String saveNotificationCodes(String tabId){
		// Buscamos el area de notificacion para el tabId
		IMNotificationArea notificationArea = this.notificationAreaMap.get(tabId);
		if (notificationArea == null) {
			Log.d(TAG, "No se encontro el area de notificacion para el tabId: " + tabId);
			return null;
		}
		return notificationArea.saveNotificationCodes();
	}
	
	public void clearNotificationArea(String tabId) {
		IMNotificationArea notificationArea = this.notificationAreaMap.get(tabId);
		if (notificationArea != null) {
			notificationArea.clearNotificationArea();
		}
	}
	
	/** 
	 * Busca en el mensaje de error y lo muestra en un dialog sin cargar a un area de notificacion.
	 * <p> Esta funcion puede utilizarse para mostrar errores en activities sin area de notificaciones</p>
	 */
	public AlertDialog viewError(Activity context, String code){
		// Buscamos el tipo de notificacion en el cache correspondiente
		IMNotification error = this.getError(code);
		
		// Disparamos el dialog que muestra el detalle de la alerta
		String title = error.getIMNotificationType()+" ";
		if (error.getIMNotificationType().equals(IMNotificationType.ERROR) || error.getIMNotificationType().equals(IMNotificationType.ERRORGRAVE)) {
			title += "Nro: "+error.getCode();
		} else {
			title += error.getBriefDescription();
		}
		final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(error.getFullDescription());
		alertDialog.setCancelable(false);
		alertDialog.setIcon(error.getIcon());
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.accept), (dialog, id) -> alertDialog.dismiss());
		alertDialog.show();
		
		//Play alert sound
		MediaPlayer.create(context, error.getSound()).start();
		
		return alertDialog;
	}
	

	public void procesarUDAANotification(int notificacionID){
		new IMUDAANotificationTask(context).execute(notificacionID);
	}
	
	/**
	 *  
	 *  Cargar Notificacion General a IM enviada a traves de UDAA 
	 * 
	 * @author tecso
	 *
	 */
	@SuppressLint("StaticFieldLeak")
	private class IMUDAANotificationTask extends AsyncTask<Integer, CharSequence, Notificacion>{
		private Context context;
		private UDAACoreServiceImpl localService;
		
		public IMUDAANotificationTask(Context context) {
			this.context = context;
		}
		
		@Override
		protected void onPreExecute() {
			this.localService = new UDAACoreServiceImpl(context);
		}

		@Override
		protected Notificacion doInBackground(Integer... params) {
			try {
				int notificacionID = params[0];
				Thread.sleep(1000);
				Notificacion notificacion = localService.getNotificacionById(notificacionID);
				Log.d(TAG, "Procesando Notificacion enviada desde UDAA. OK.");
				return notificacion;
			} catch (Exception e) {
				e.printStackTrace();
				Log.d(TAG, "Procesando Notificacion enviada desde UDAA. ERROR: ",e);
				return null;
			}	    		
		}

		@Override
		protected void onProgressUpdate(CharSequence... values) { }

		@Override
		protected void onPostExecute(Notificacion notificacion) {
			if (notificacion != null && notificacion.getNumeroAplicacion() != 0) {
				//Tab donde mostrar la notificacion
				String tabId = String.valueOf(-notificacion.getNumeroAplicacion());
				
				addGeneralNotification(tabId, notificacion);
			}
		}
	}
}
