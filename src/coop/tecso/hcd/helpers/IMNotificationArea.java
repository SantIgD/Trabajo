package coop.tecso.hcd.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;

/**
 *
 * @author tecso
 *
 */
@SuppressWarnings("unused")
public final class IMNotificationArea {

	private static final String TAG = IMNotificationArea.class.getSimpleName();
	
	private Activity context;
	
	// Mapa de Notificaciones para el Area 
	private Map<String,IMNotification> notificationsMap;
	// Areas de Notificacion para el IM (tab) 
	private HorizontalScrollView areaView;
	
	
	public IMNotificationArea(Activity context, Map<String, IMNotification> notificationsMap, HorizontalScrollView areaView) {
		super();
		this.context = context;
		this.notificationsMap = notificationsMap;
		this.areaView = areaView;
	}
	
	// Getters And Setters
	public Map<String, IMNotification> getNotificationsMap() {
		return notificationsMap;
	}
	public void setNotificationsMap(Map<String, IMNotification> notificationsMap) {
		this.notificationsMap = notificationsMap;
	}
	public HorizontalScrollView getAreaView() {
		return areaView;
	}
	public void setAreaView(HorizontalScrollView areaView) {
		this.areaView = areaView;
	}
	
	
	/**
	 * Agrega una notificacion al area de notificacion
	 */
	public void addNotification(IMNotification notification){
		// Se carga la notificacion en el mapa
		this.notificationsMap.put(notification.getCode(), notification);
		
		// Se crea el componente grafico y se agrega al area de notificacion
		LayoutInflater inflater = this.context.getLayoutInflater();
		View notificationView = inflater.inflate(R.layout.im_notification, null);
		ImageView notificationIcon = notificationView.findViewById(R.id.notificacion_icon);
		TextView  notificationBriefDescription  = notificationView.findViewById(R.id.notificacion_briefDescription);
		notificationIcon.setImageResource(notification.getIcon());
		notificationBriefDescription.setText(notification.getBriefDescription());
		notificationView.setOnClickListener(this::viewNotification);
		// Se setea el codigo de la notificacion en la vista para asociar a la notificacion del mapa
		notificationView.setTag(notification.getCode());
		
		LinearLayout listNotificationView = this.areaView.findViewById(R.id.imNotificationArea);
		listNotificationView.addView(notificationView);
		
		// Si la notificacion esta marcada como autodesplegable (forzar visualizacion) se visualiza
		if (notification.isReadForced()) {
			viewNotification(notificationView);
		}
	}
	
	/**
	 * Elimina un item de la lista
	 */
	private void viewNotification(View notificationView){
		// Buscamos el codigo de la notificacion y el tabId almacenado en la vista
		String code = (String) notificationView.getTag();
		
		// Buscamos la notificacion en el area
		IMNotification notification = this.notificationsMap.get(code);
		
		// Disparamos el dialog que muestra el detalle de la alerta
		String title = notification.getIMNotificationType() + " ";
		if (notification.getIMNotificationType().equals(IMNotificationType.ERROR) || notification.getIMNotificationType().equals(IMNotificationType.ERRORGRAVE)) {
			title += "Nro: "+notification.getCode();
		} else {
			title += notification.getBriefDescription();
		}

		final AlertDialog alertDialog = new AlertDialog.Builder(this.context).create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(notification.getFullDescription());
		alertDialog.setCancelable(false);
		alertDialog.setIcon(notification.getIcon());
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.accept), (dialog, id) -> HCDigitalApplication.getApplication(context).getAlertChain().onEndAlert());
		alertDialog.show();

		HCDigitalApplication.getApplication(context).getAlertChain().onShowAlert();
		
		// Marcar Notificacion como leida
		if (!notification.isReaded()) {
			notification.setReaded(true);

			//Play alert sound
			MediaPlayer.create(context, notification.getSound()).start();
		}
		
		// Elimina la notificacion de la vista (salvo que este marcada como siempre visible)
		if (!notification.isAlwaysVisible()) {
			LinearLayout listNotificationView = this.areaView.findViewById(R.id.imNotificationArea);
			listNotificationView.removeView(notificationView);
		}
	}
	
	public void clearNotificationArea() {
		LinearLayout listNotificationView = this.areaView.findViewById(R.id.imNotificationArea);
		listNotificationView.removeAllViews();
		notificationsMap.clear(); 
	}
	
	/**
	 * Devuelve un string con la lista de codigos de alertas medicas lanzadas al area. Por cada una acompaña con un '#' y true o false indicando si fue leida o no.
	 * 
	 * <p> Ejemplo: codigosAlertas: 001#false,002#true,003#true </p>
	 * <p> , donde: false - No leido, true - Leido</p>
	 */
	public String saveNotificationCodes(){
		String codigosAlertas = null;
		boolean first = true;

		for(IMNotification notification: this.notificationsMap.values()){
			if (notification.getIMNotificationType().equals(IMNotificationType.MEDICAS)) {
				String code = notification.getCode() + "#" + notification.isReaded();
			   if (!first) {
				   codigosAlertas += ",";
			   }
			   else {
				   first = false;
			   }
			   codigosAlertas += code;
			}
		}
		
		return codigosAlertas;
	}
}
