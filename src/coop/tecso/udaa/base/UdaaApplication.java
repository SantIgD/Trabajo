package coop.tecso.udaa.base;

import java.util.Timer;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.ReportSender;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import coop.tecso.udaa.R;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion.App;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.receivers.UDAAMessageReceiver;
import coop.tecso.udaa.utils.Constants;
import coop.tecso.udaa.utils.LocationReporter;
import coop.tecso.udaa.utils.ParamHelper;
import coop.tecso.udaa.utils.SessionStore;

@ReportsCrashes(formKey = "", // will not be used
mode = ReportingInteractionMode.SILENT)
public final class UdaaApplication extends Application {
	
	private static final String LOG_TAG = UdaaApplication.class.getSimpleName();
	
	private boolean localLogin = false;
	private UsuarioApm currentUser;
	private DispositivoMovil dispositivoMovil;
	private int localLoginCount = 0; 
	
	// Cambio de usuario
	private boolean changeSession = false;
	private String userNameForNewSession = "";
	private Timer timer;

	private LocationReporter locationReporter;

	//APK version
	private String lastVersionName;
	private boolean waitingInstallation = false;
	private boolean updateRequired = false;
	private Intent upgradeIntent;

	private boolean permissionsGaranted = false;
	  
	@Override
	public void onCreate() {
		super.onCreate();
	    
		ParamHelper.initialize(this);

		this.locationReporter = new LocationReporter(this);

		this.setAppSettings();

		String acraParams = "APP_VERSION_NAME|AVAILABLE_MEM_SIZE|STACK_TRACE";

		// Initialize ACRA Service
		ACRA.init(this);

		// Setting my custom report sender
		ReportSender reportSender = new UDAAReportSender(this, acraParams);
		ACRA.getErrorReporter().setReportSender(reportSender);

		UDAAMessageReceiver.createAndRegisterReceiver(this);
	}
	
	public void setAppSettings(){
		// Session Init
		Log.i(LOG_TAG, "Cargando parametros de session...");
		try {
			SharedPreferences myPrefs = this.getSharedPreferences("settings", Context.MODE_PRIVATE);
			if(myPrefs.getString("URL", "").equals(""))  {
	            SharedPreferences.Editor e = myPrefs.edit();
	            e.putString("URL", "https://190.210.56.178:8443/Capacitacion");
	            e.apply();
			}

			this.locationReporter.initialize();

			// Registramos el BroadcastReceiver
			registerReceiver(new UDAAMessageReceiver(), new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		} catch (Exception e) {
			Log.i(LOG_TAG, "onCreate : error : ", e);
		}
	}

	public String getCurrentUsername() {
		try {
			if(this.getCurrentUser() != null && this.getCurrentUser().getUsername() != null) {
				return this.getCurrentUser().getUsername();	
			}
			else {
				return "-";
			}
		} catch (Exception e) {
			return "-";
		}
	}
	
	// Getters & Setters
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

	public String getImagesDir() {
		return "res/images/";
	}

	public boolean isLocalLogin() {
		return localLogin;
	}

	public void setLocalLogin(boolean localLogin) {
		this.localLogin = localLogin;
	}

	public int getLocalLoginCount() {
		return localLoginCount;
	}

	public void resetLocalLoginCount() {
		this.localLoginCount = 0;
	}
	
	public void addLocalLoginCount() {
		this.localLoginCount++;
	}
	
	public boolean isChangeSession() {
		return changeSession;
	}

	public void setChangeSession(boolean changeSession) {
		this.changeSession = changeSession;
	}

	public String getUserNameForNewSession() {
		return userNameForNewSession;
	}

	public void setUserNameForNewSession(String userNameForNewSession) {
		this.userNameForNewSession = userNameForNewSession;
	}

	public boolean isPermissionsGaranted() {
		return permissionsGaranted;
	}

	public void setPermissionsGaranted(boolean permissionsGaranted) {
		this.permissionsGaranted = permissionsGaranted;
	}

	public void reset() {
		// Clean up the logged user.
		setCurrentUser(null);
		SessionStore.unstoreSession(this);
		stopEmergencyChannel();
	}
	
	public void changeSession(String userName) {
		// Clean up the logged user.
		setCurrentUser(null);
		SessionStore.unstoreSession(this);

		changeSession = true;
		userNameForNewSession = userName;
		
		// Enviar mensaje de cierre del manager
		Intent msg = new Intent();
		msg.setAction(Constants.ACTION_LOSE_SESSION);
		this.sendBroadcast(msg);
	}
	
	/**
	 * Start Emergency Channel
	 */
	public void startEmergencyChannel(){
		//If exist..
		if (this.timer != null){
			// Stop previous channel
			stopEmergencyChannel();
		}
		
		Long period = ParamHelper.getLong(ParamHelper.EMERGENCY_CHANNEL_PERIOD, 300000L);
		this.timer = new Timer();
		this.timer.schedule(new ErrorReportCheckTask(this), 0, period);
	}
	
	/**
	 * Finalize Emergency Channel
	 */
	public void stopEmergencyChannel(){
		try {
			this.timer.cancel();
			this.timer.purge();
		} catch (Exception ignore) {};
		
		this.timer = null;
	}

    // ----------------------------

	public void onChangeBatteryLevel(int level) {
		locationReporter.onChangeBatteryLevel(level);
	}

	public void reportGPSLocation(boolean shouldSend) {
		locationReporter.getGPSLocation(shouldSend);
	}

	public void startGPSLocationTimerAsPossible() {
		if (locationReporter.gpsLocationShouldBeActive()) {
			locationReporter.startGPSLocationTimerAsPossible(); // Se inicia el timer GPS
		}
	}

	public void stopGPSLocationTimer() {
		locationReporter.stopGPSLocationTimerIfPossible();
	}

    // ----------------------------

	public String getFormattedTitle(){
		// Application version
		String versionName = "";
		try {
			versionName = "v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (Exception ignore) {}
		// Logged User
		String userName = "";
		if(getCurrentUser() != null){
			userName = getCurrentUser().getNombre();
		}
		return getString(R.string.app_header_tittle, versionName, userName);
	}

	public String getTitle(){
		// Application version
		String versionName = "";
		try {
			versionName = "v" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (Exception ignore) {}
		return getString(R.string.app_name) +" "+ versionName;
	}

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

	public boolean needsForceUpdate(){
		return dispositivoMovil.isForzarActualizacion() && (dispositivoMovil.getUpdatedApps() == null 
				|| !dispositivoMovil.getUpdatedApps().contains(App.UDAA.toString()));
	}
	
	public boolean needsForceUpdateHC(){
		return dispositivoMovil.isForzarHC() && (dispositivoMovil.getUpdatedApps() == null 
				|| !dispositivoMovil.getUpdatedApps().contains(App.HCDigital.toString()));
	}

	public boolean needsForceUpdateCTO(){
		return dispositivoMovil.isForzarCTO() && (dispositivoMovil.getUpdatedApps() == null 
				|| !dispositivoMovil.getUpdatedApps().contains(App.CTODigital.toString()));
	}

	public boolean needsForceUpdateSA(){
		return dispositivoMovil.isForzarSA() && (dispositivoMovil.getUpdatedApps() == null 
				|| !dispositivoMovil.getUpdatedApps().contains(App.SADigital.toString()));
	}

	public static UdaaApplication get(Context context) {
		if (context instanceof UdaaApplication) {
			return (UdaaApplication) context;
		} else {
			return (UdaaApplication) context.getApplicationContext();
		}
	}
}