package coop.tecso.hcd.application;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.net.wifi.WifiManager;
import android.os.Environment;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.ReportSender;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.dao.HCDigitalDAO;
import coop.tecso.hcd.entities.EntidadBusqueda;
import coop.tecso.hcd.gui.components.AlertDispatcher;
import coop.tecso.hcd.gui.components.AlertDispatcherImpl;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.receivers.ReportSenderImpl;
import coop.tecso.hcd.receivers.UDAAMessageReceiver;
import coop.tecso.hcd.services.IMNotificationManager;
import coop.tecso.hcd.utils.AlertChainUtil;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion.App;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

/**
 * Singleton manejado por Android que extiende de clase Application.
 * Contiene instancias comunes a toda la apliacion.
 * 
 * @author tecso.coop
 *
 */
@ReportsCrashes(formKey = "", // will not be used
mode = ReportingInteractionMode.TOAST,
resToastText = R.string.app_crash)
public final class HCDigitalApplication extends Application {

	private GestureLibrary gestureStore;
	private IMNotificationManager iMNotificationManager;
	private List<EntidadBusqueda> listEntidadBusqueda;
	private UsuarioApm currentUser;
	private DispositivoMovil dispositivoMovil;
	private HCDigitalDAO hcDigitalDAO;

	public boolean hasSynchronized;

	private AlertDispatcher alertDispatcher;
	
	public static BroadcastReceiver broadcastReceiver;
	
	//APK version
	private String lastVersionName;
	private boolean waitingInstallation = false;
	private boolean updateRequired = false;
	private Intent upgradeIntent;
	
	private final File gestureStoreFile = new File(Environment.getExternalStorageDirectory(), "gestures");

	private PerfilGUI form;

	private AlertChainUtil alertChain = new AlertChainUtil();

	@Override
	public void onCreate() {
		super.onCreate();
		// 
		ParamHelper.initialize(this);
		
		this.hcDigitalDAO = new HCDigitalDAO(this);
		this.hasSynchronized = false;

		// Archivo de firmas
		gestureStore = GestureLibraries.fromFile(gestureStoreFile);
		gestureStore.load();
		
		// Jira HCDDM-213
		alertDispatcher = new AlertDispatcherImpl();

		UDAAMessageReceiver.createAndRegisterReceiver(this);
		
		// Initialize ACRA Service
		ACRA.init(this);
		// Setting my custom report sender
		ReportSender reportSender = new ReportSenderImpl(this);
		ACRA.getErrorReporter().setReportSender(reportSender);
	}

	public List<EntidadBusqueda> getListEntidadBusqueda() {
		return listEntidadBusqueda;
	}

	public void setListEntidadBusqueda(List<EntidadBusqueda> listEntidadBusqueda) {
		this.listEntidadBusqueda = listEntidadBusqueda;
	}

	public UsuarioApm getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(UsuarioApm currentUser) {
		this.currentUser = currentUser;
	}

	public DispositivoMovil getDispositivoMovil() {
		return dispositivoMovil;
	}

	public void setDispositivoMovil(DispositivoMovil dispositivoMovil) {
		this.dispositivoMovil = dispositivoMovil;
	}

	/**
	 * Manager de Notificaciones
	 */
	public IMNotificationManager getNotificationManager() {
		return iMNotificationManager;
	}

	public void setNotificationManager(IMNotificationManager iMNotificationManager) {
		this.iMNotificationManager = iMNotificationManager;
	}

	public HCDigitalDAO getHCDigitalDAO() {
		return hcDigitalDAO;
	}

	public AlertDispatcher getAlertDispatcher() {
		return alertDispatcher;
	}

	public GestureLibrary getGestureStore() {
		return gestureStore;
	}

	public boolean canAccess() {
		return getCurrentUser() != null;
	}

    // ----------------------------

	public void setLastVersionName(String lastVersionName) {
		this.lastVersionName = lastVersionName;
	}

	public boolean isWaitingInstallation() {
		return waitingInstallation;
	}

	public synchronized void setWaitingInstallation(boolean waitingInstallation) {
		this.waitingInstallation = waitingInstallation;
	}

	public Intent getUpgradeIntent() {
		return upgradeIntent;
	}

	public void setUpgradeIntent(Intent upgradeIntent) {
		this.upgradeIntent = upgradeIntent;
	}

	public boolean isUpgradeRequired() {
		return updateRequired;
	}

	public void setUpgradeRequired(boolean updateRequired) {
		this.updateRequired = updateRequired;
	}

	public String getLastVersionName() {
		return lastVersionName;
	}

	public void saveGestures(){
		gestureStore.save();
	}

	public synchronized void removeGestures(List<String> afiliacionIds){
		try {
			for(Iterator<String> it = gestureStore.getGestureEntries().iterator(); it.hasNext();){
				String sufix = it.next().split("\\|")[1];
				if(afiliacionIds.contains(sufix)){
					it.remove();
				}
			}
		} catch (Exception ignore) {}
	}
	
	public boolean needsForceUpdate(){
		return dispositivoMovil.isForzarHC() && (dispositivoMovil.getUpdatedApps() == null 
				|| !dispositivoMovil.getUpdatedApps().contains(App.HCDigital.toString()));
	}

	public PerfilGUI getForm() {
		return form;
	}

	public void setForm(PerfilGUI form) {
		this.form = form;
	}

	public AlertChainUtil getAlertChain() {
		return alertChain;
	}

	// ----------------------------

	public boolean isWifiEnabled() {
	    try {
	    	WifiManager wifimanager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	        Method method = wifimanager.getClass().getDeclaredMethod("isWifiEnabled");
	        method.setAccessible(true);
	        return (Boolean) method.invoke(wifimanager);
	    }
	    catch (Throwable e) {
	    	e.printStackTrace();
	    	return false;
	    }
	}

    public void setWifiEnabled(boolean enabled){
        WifiManager wifimanager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifimanager.setWifiEnabled(enabled);
    }

	// ----------------------------

	public static HCDigitalApplication getApplication(Context context) {
		if (context instanceof HCDigitalApplication) {
			return (HCDigitalApplication) context;
		}

		return (HCDigitalApplication) context.getApplicationContext();
	}

}