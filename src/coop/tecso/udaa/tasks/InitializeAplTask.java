package coop.tecso.udaa.tasks;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import coop.tecso.udaa.R;
import coop.tecso.udaa.activities.LoginActivity;
import coop.tecso.udaa.activities.NotificacionesActivity;
import coop.tecso.udaa.base.UDAAException;
import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.common.GUIHelper;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.common.WebServiceDAO;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionParametro;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfilSeccion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionSync;
import coop.tecso.udaa.domain.aplicaciones.AplicacionTabla;
import coop.tecso.udaa.domain.aplicaciones.AplicacionTipoBinario;
import coop.tecso.udaa.domain.base.TablaVersion;
import coop.tecso.udaa.domain.domicilio.Barrio;
import coop.tecso.udaa.domain.domicilio.Calle;
import coop.tecso.udaa.domain.domicilio.Ciudad;
import coop.tecso.udaa.domain.domicilio.Provincia;
import coop.tecso.udaa.domain.domicilio.SucursalDomicilio;
import coop.tecso.udaa.domain.domicilio.Zona;
import coop.tecso.udaa.domain.notificaciones.EstadoNotificacion;
import coop.tecso.udaa.domain.notificaciones.Notificacion;
import coop.tecso.udaa.domain.notificaciones.TipoNotificacion;
import coop.tecso.udaa.domain.padron.TipoDocumento;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.perfiles.CampoValor;
import coop.tecso.udaa.domain.perfiles.CampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.PerfilAcceso;
import coop.tecso.udaa.domain.perfiles.PerfilAccesoAplicacion;
import coop.tecso.udaa.domain.perfiles.PerfilAccesoUsuario;
import coop.tecso.udaa.domain.perfiles.Seccion;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.Sucursal;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.utils.Constants;
import coop.tecso.udaa.utils.ErrorConstants;
import coop.tecso.udaa.utils.LogUtils;
import coop.tecso.udaa.utils.ParamHelper;
import coop.tecso.udaa.utils.SessionStore;

/**
 * Inicializa la aplicacion (unicamente si el login fue exitoso)
 */
@SuppressLint({"SimpleDateFormat", "StaticFieldLeak", "HardwareIds"})
public final class InitializeAplTask extends AsyncTask<Void, CharSequence, Integer> {

    // MARK: - Data

    private static final String LOG_TAG = InitializeAplTask.class.getSimpleName();

    private Activity activity;

    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    
    private UDAADao udaaDao;
    private UdaaApplication udaaApplication;
    private WebServiceDAO localService;

    private LogUtils logUtils;

    public boolean ignoreAppUpdates = false;
    public String dialogTitle = "Iniciando Sesión";

    // MARK: - Init

    public InitializeAplTask(Activity activity) {
        this.activity = activity;
        this.logUtils = new LogUtils(activity);
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        logUtils.generateLoginLog("Initialize -> Start");

        logUtils.generateLoginLog("Initialize -> PreExecute Start");
        udaaDao = new UDAADao(activity);
        udaaApplication = (UdaaApplication) activity.getApplication();
        localService = WebServiceDAO.getInstance(activity);

        // Show status message
        progressDialog = GUIHelper.getIndetProgressDialog(activity, dialogTitle, "Por favor, aguarde unos segundos…");
        if (progressDialog != null) {
            progressDialog.setCancelable(false);
        }

        logUtils.generateLoginLog("Initialize -> PreExecute End");
    }

    @Override
    protected Integer doInBackground(Void... arg0) {
        try {
            try {
                logUtils.generateLoginLog("Initialize UDAA -> localService registerUsuarioApmVersion Start");
                String description = getVersionDescription();
                localService.registerUsuarioApmVersion(description);
                logUtils.generateLoginLog("Initialize UDAA -> localService registerUsuarioApmVersion End");
            } catch (UDAAException error) {
                if (ErrorConstants.ERROR_108.equals(error.getError())) {
                    return -150;
                }
            }

            // Se toma el id de dispositivo movil
            DispositivoMovil dispositivo = udaaApplication.getDispositivoMovil();
            Integer deviceID = dispositivo.getId();

            // Se sincroniza y carga versiones de tablas
            Map<String, Integer> map = localService.syncState(deviceID);
            // Build remote status
            Log.d(LOG_TAG, "Remote status: ");
            for (String key : map.keySet()) {
                Log.d(LOG_TAG, String.format("## '%s' --> %s", key, map.get(key)));
            }

            // Build local status
            Log.d(LOG_TAG, "Local status: ");

            logUtils.generateLoginLog("Initialize -> UDAA getListTablaVersion Start");
            List<TablaVersion> listTablaVersion = udaaDao.getListTablaVersion();
            logUtils.generateLoginLog("Initialize -> UDAA getListTablaVersion End");

            for (TablaVersion tablaVersion : listTablaVersion) {
                Log.d(LOG_TAG, String.format("## '%s' --> %s", tablaVersion.getTabla(), tablaVersion.getLastVersion()));
            }

            Integer usuarioID = udaaApplication.getCurrentUser().getId();

            // Dispositivo

            Integer dispositivoMovilString = map.get("apm_dispositivomovil");
            if (dispositivoMovilString != null && dispositivoMovilString > dispositivo.getVersion()) {
                publishProgress("Sincronizando dispositivo…");
                logUtils.generateLoginLog("Login -> WebService identifyDM Start");
                DispositivoMovil dispositivoMovil = WebServiceDAO.identifyDM(dispositivo.getEmailAddress());
                logUtils.generateLoginLog("Login -> WebService identifyDM End");
                udaaDao.createOrUpdateDispositivoMovil(dispositivoMovil);
                udaaApplication.setDispositivoMovil(dispositivoMovil);
            }

            // Parametros de Aplicacion
            publishProgress("Sincronizando parámetros de aplicación…");
            // [JG-08.10.2013] Se sincroniza por aplicación
            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionParametro Start");
            localService.synchronizeByAplicacion(AplicacionParametro.class, "apm_aplicacionParametro", Constants.COD_UDAA);
            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionParametro End");
            ParamHelper.initialize(activity);

            // Sincroniza tablas de Aplicaciones
            publishProgress("Sincronizando aplicaciones…");

            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacion Start");
            localService.synchronizeByPerfil(Aplicacion.class, "apm_aplicacion");
            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacion End");

            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionTipoBinario Start");
            localService.synchronize(AplicacionTipoBinario.class, "apm_aplicacionTipoBinario");
            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionTipoBinario End");

            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionTabla Start");
            localService.synchronizeByAplicacion(AplicacionTabla.class, "apm_aplicacionTabla", Constants.COD_UDAA, Constants.APLICACION_UDAA_ID);
            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionTabla End");

            // Sincronizando perfiles de acceso:
            publishProgress("Sincronizando perfiles de acceso…");
            logUtils.generateLoginLog("Initialize -> synchronize apm_perfilAcceso Start");
            localService.synchronizeByPerfil(PerfilAcceso.class, "apm_perfilAcceso");
            logUtils.generateLoginLog("Initialize -> synchronize apm_perfilAcceso End");

            logUtils.generateLoginLog("Initialize -> synchronize apm_perfilAccesoUsuario Start");
            localService.synchronizeByDispositivo(PerfilAccesoUsuario.class, "apm_perfilAccesoUsuario", deviceID);
            logUtils.generateLoginLog("Initialize -> synchronize apm_perfilAccesoUsuario End");

            logUtils.generateLoginLog("Initialize -> synchronize apm_perfilAccesoAplicacion Start");
            localService.synchronizeByPerfil(PerfilAccesoAplicacion.class, "apm_perfilAccesoAplicacion");
            logUtils.generateLoginLog("Initialize -> synchronize apm_perfilAccesoAplicacion End");

            logUtils.generateLoginLog("Initialize -> synchronize apm_usuarioAPM Start");
            localService.synchronizeByUsuarioID(UsuarioApm.class, "apm_usuarioAPM", usuarioID);
            logUtils.generateLoginLog("Initialize -> synchronize apm_usuarioAPM End");

            publishProgress("Descarga de actualizaciones…");

            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionBinarioVersion Start");
            localService.synchronizeBinary("apm_aplicacionBinarioVersion", usuarioID, this, udaaApplication.getDispositivoMovil().getId());
            logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionBinarioVersion End");

            if (ignoreAppUpdates) {
                udaaApplication.setWaitingInstallation(false);
            }

            while (udaaApplication.isWaitingInstallation()) {
                Thread.sleep(1000);
                if (alertDialog == null && !udaaApplication.isUpgradeRequired()) {
                    activity.runOnUiThread(() -> {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(R.string.confirm_title);
                        builder.setMessage(ParamHelper.getString("installUpdate", activity.getString(R.string.update_install)));
                        builder.setCancelable(false);
                        builder.setPositiveButton(R.string.yes, (dialog, id) -> activity.startActivityForResult(udaaApplication.getUpgradeIntent(), Constants.REQUEST_UPGRADE_APP));
                        builder.setNegativeButton(R.string.no, (dialog, id) -> {
                            dialog.cancel();
                            udaaApplication.setWaitingInstallation(false);
                        });

                        InitializeAplTask.this.alertDialog = builder.create();
                        InitializeAplTask.this.alertDialog.show();
                    });
                } else if (udaaApplication.isUpgradeRequired()) {
                    activity.startActivityForResult(udaaApplication.getUpgradeIntent(), Constants.REQUEST_UPGRADE_APP);
                    //Se desloguea al usuario ya que la instalación es obligatoria
                    udaaApplication.setCurrentUser(null);
                    return 1;
                }
            }

            logUtils.generateLoginLog("Initialize -> synchronize apm_sucursal Start");
            localService.synchronize(Sucursal.class, "apm_sucursal");
            logUtils.generateLoginLog("Initialize -> synchronize apm_sucursal End");

            boolean esUsuarioHC = false;

            // Sincroniza Perfiles por aplicacion
            List<Aplicacion> listAplicacion = udaaDao.getListAplicacionByUsuario(udaaApplication.getCurrentUser());
            for (Aplicacion aplicacion : listAplicacion) {
                int aplicacionID = aplicacion.getId();

                esUsuarioHC |= aplicacionID == Aplicacion.App.HCDigital.getId();

                publishProgress(activity.getString(R.string.synchronizing_item_msg, "perfiles " + aplicacion.getDescripcion()));

                publishProgress("Sincronizando campos…");
                logUtils.generateLoginLog("Initialize -> synchronize apm_campo Start");
                localService.synchronizeByAplicacionID(Campo.class, "apm_campo", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_campo End");

                logUtils.generateLoginLog("Initialize -> synchronize apm_campoValor Start");
                localService.synchronizeByAplicacionID(CampoValor.class, "apm_campoValor", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_campoValor End");

                logUtils.generateLoginLog("Initialize -> synchronize apm_campoValorOpcion Start");
                localService.synchronizeByAplicacionID(CampoValorOpcion.class, "apm_campoValorOpcion", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_campoValorOpcion End");

                publishProgress("Sincronizando secciones…");
                logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionPerfil Start");
                localService.synchronizeByAplicacionID(AplicacionPerfil.class, "apm_aplicacionPerfil", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionPerfil End");

                logUtils.generateLoginLog("Initialize -> synchronize apm_seccion Start");
                localService.synchronizeByAplicacionID(Seccion.class, "apm_seccion", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_seccion End");

                logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionTabla Start");
                localService.synchronizeByAplicacionID(AplicacionTabla.class, "apm_aplicacionTabla", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionTabla End");

                logUtils.generateLoginLog("Initialize -> synchronize apm_aplPerfilSeccionCampo Start");
                localService.synchronizeByAplicacionID(AplPerfilSeccionCampo.class, "apm_aplPerfilSeccionCampo", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_aplPerfilSeccionCampo End");

                logUtils.generateLoginLog("Initialize -> synchronize apm_aplPerfilSeccionCampoValor Start");
                localService.synchronizeByAplicacionID(AplPerfilSeccionCampoValor.class, "apm_aplPerfilSeccionCampoValor", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_aplPerfilSeccionCampoValor End");

                logUtils.generateLoginLog("Initialize -> synchronize apm_aplPerfilSeccionCampoValorOpcion Start");
                localService.synchronizeByAplicacionID(AplPerfilSeccionCampoValorOpcion.class, "apm_aplPerfilSeccionCampoValorOpcion", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_aplPerfilSeccionCampoValorOpcion End");

                logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionPerfilSeccion Start");
                localService.synchronizeByAplicacionID(AplicacionPerfilSeccion.class, "apm_aplicacionPerfilSeccion", aplicacionID);
                logUtils.generateLoginLog("Initialize -> synchronize apm_aplicacionPerfilSeccion End");
            }

            if (esUsuarioHC) {
                SharedPreferences myPrefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE);
                SharedPreferences.Editor pref = myPrefs.edit();
                pref.putString("initDBs", Constants.NOT_INIT_DBS);
                pref.apply();
            } else {
                publishProgress("Sincronizando Zonas…");
                logUtils.generateLoginLog("Initialize -> synchronize afi_zona Start");
                localService.synchronizeBatch(Zona.class, "afi_zona", 1);
                logUtils.generateLoginLog("Initialize -> synchronize afi_zona End");

                publishProgress("Sincronizando Sucursales…");
                logUtils.generateLoginLog("Initialize -> synchronize afi_sucursal Start");
                localService.synchronizeBatch(SucursalDomicilio.class, "afi_sucursal", 1);
                logUtils.generateLoginLog("Initialize -> synchronize afi_sucursal End");

                publishProgress("Sincronizando Provincias…");
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_provincia Start");
                localService.synchronizeBatch(Provincia.class, "afi_provincia", 1);
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_provincia End");

                publishProgress("Sincronizando Ciudades…");
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_ciudad Start");
                localService.synchronizeBatch(Ciudad.class, "afi_ciudad", 1);
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_ciudad End");

                publishProgress("Sincronizando Barrios…");
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_barrio Start");
                localService.synchronizeBatch(Barrio.class, "afi_barrio", 1);
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_barrio End");

                publishProgress("Sincronizando Calles…");
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_calle Start");
                localService.synchronizeBatch(Calle.class, "afi_calle", 1);
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_calle End");

                publishProgress("Tipos Documentos…");
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_tipodocumento Start");
                localService.synchronizeBatch(TipoDocumento.class, "afi_tipodocumento", 1);
                logUtils.generateLoginLog("Initialize SADigital -> synchronize afi_tipodocumento End");
            }

            // Delete All previous Notifications
            logUtils.generateLoginLog("Initialize -> UDAA deleteAllNotificacion Start");
            udaaDao.deleteAllNotificacion();
            logUtils.generateLoginLog("Initialize -> UDAA deleteAllNotificacion End");

            logUtils.generateLoginLog("Initialize -> UDAA synchronize TablaVersion Start");

            // Sincronizando Notificaciones
            TablaVersion tablaVersion = udaaDao.getTablaVersionByTableName("not_notificacion");
            if (tablaVersion.getLastVersion() == 0) {
                tablaVersion.setLastVersion(map.get("not_notificacion"));
                udaaDao.updateTablaVersion(tablaVersion);
            }
            logUtils.generateLoginLog("Initialize -> UDAA synchronize TablaVersion End");

            publishProgress("Sincronizando notificaciones…");

            logUtils.generateLoginLog("Initialize -> synchronize not_estadoNotificacion Start");
            localService.synchronize(EstadoNotificacion.class, "not_estadoNotificacion");
            logUtils.generateLoginLog("Initialize -> synchronize not_estadoNotificacion End");

            logUtils.generateLoginLog("Initialize -> synchronize not_tipoNotificacion Start");
            localService.synchronize(TipoNotificacion.class, "not_tipoNotificacion");
            logUtils.generateLoginLog("Initialize -> synchronize not_tipoNotificacion End");

            logUtils.generateLoginLog("Initialize -> synchronize not_notificacion Start");
            localService.synchronize(Notificacion.class, "not_notificacion", deviceID, false);
            logUtils.generateLoginLog("Initialize -> synchronize not_notificacion End");

            logUtils.generateLoginLog("Initialize -> UDAA setAppSettings Start");
            udaaApplication.setAppSettings();
            logUtils.generateLoginLog("Initialize -> UDAA setAppSettings End");

            logUtils.generateLoginLog("Initialize -> UDAA createOrUpdateDispositivoMovil Start");
            DispositivoMovil movil = udaaApplication.getDispositivoMovil();

            AplicacionSync aplicacionSync = udaaDao.getApplicationSync();
            aplicacionSync.setSyncUDDATimeStamp(new Date());
            udaaDao.updateApplicationSync(aplicacionSync);

            try {
                String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                localService.registerUsuarioApmVersion("Sincronización " + Aplicacion.App.UDAA.toString() + ": " + dateString);
            } catch (Exception ignore) {}

            if (udaaApplication.needsForceUpdate()) {
                localService.confirmForceUpdate(Aplicacion.App.UDAA.getId());
                movil.setForzarActualizacion(false);
                udaaApplication.setDispositivoMovil(movil);
                udaaDao.createOrUpdateDispositivoMovil(movil);
            }
            logUtils.generateLoginLog("Initialize -> UDAA createOrUpdateDispositivoMovil End");

            return 0;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error iniciando sesion", e);
            if (!e.getMessage().equals("ERROR_SINCRONIZACION")) {
                String message = "Error en sincronización. ERROR: " + e.getMessage();
                logUtils.generateACRA_LOG(message, "SINCRONIZACION");
            }
            return -1;
        }
    }

    @Override
    protected void onProgressUpdate(CharSequence... values) {
        if (progressDialog != null) {
            progressDialog.setMessage(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        this.updateURLServer();

        logUtils.generateLoginLog("Initialize -> End");

        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        if (result == -150) {
            if (activity instanceof LoginActivity) {
                LoginActivity loginActivity = (LoginActivity) activity;
                String username = udaaApplication.getCurrentUser().getUsername();
                loginActivity.buildDialogForSetPassword(username).show();
            }
            return;
        }

        boolean notificationErrorSync = false;

        if (result < 0) {
            notificationErrorSync = true;
        }

        if (result == 0){
            SessionStore.storeSession(activity);
        }

        logUtils.generateLoginLog("Initialize -> Post tasks Start");

        // Se inicia el canal de contingencias
        udaaApplication.startEmergencyChannel();

        activity.setTitle(udaaApplication.getFormattedTitle());

        logUtils.generateLoginLog("Initialize -> Post tasks End");

        //	Si la actualizacion es obligatoria no ejecutamos el proximo activity
        if (!udaaApplication.isUpgradeRequired() && !ignoreAppUpdates) {
            Intent intent = new Intent(activity, NotificacionesActivity.class);
            if (udaaApplication.isWaitingInstallation()) {
                intent.putExtra(NotificacionesActivity.KEY_UPDATE, true);
            }

            if (notificationErrorSync) {
                intent.putExtra(NotificacionesActivity.KEY_ERROR_SYNC, true);
            }

            activity.startActivity(intent);
            activity.finish();
        }
    }

    public void doProgress(String description, int value) {
        publishProgress("Descarga de actualizaciones\n" + description + " (" + value + "%)…");
    }

    // MARK: - Internal

    private String getVersionDescription() {
        Log.i(LOG_TAG, "getVersionDescription: enter");

        StringBuilder versionStr = new StringBuilder();

        // Recuperar las versiones instaladas de UDAA y HCDigital
        List<Aplicacion> listAplicacion = udaaDao.getListAplicacionByUsuario(udaaApplication.getCurrentUser());

        //UDAA Application
        Aplicacion udaaApp = new Aplicacion();
        udaaApp.setCodigo(Constants.COD_UDAA);
        udaaApp.setPkg(activity.getPackageName());

        listAplicacion.add(0, udaaApp);
        for (Aplicacion aplicacion : listAplicacion) {
            try {
                versionStr.append(aplicacion.getCodigo());
                versionStr.append("v");
                versionStr.append(activity.getPackageManager().getPackageInfo(aplicacion.getPkg(), 0).versionName);
                versionStr.append("|");
            } catch (Exception e) {
                Log.e(LOG_TAG, "**ERROR: Determinando Versiones", e);
            }
        }

        int readPhonePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE);
        if (readPhonePermission == PackageManager.PERMISSION_GRANTED) {
            TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
            String sSIM = telephonyManager.getSimSerialNumber();
            versionStr.append("|SIM:");
            versionStr.append(sSIM);
            String sIMEI = telephonyManager.getDeviceId();
            versionStr.append("|IMEI:");
            versionStr.append(sIMEI);
        }

        String sOS = android.os.Build.VERSION.RELEASE;
        versionStr.append("|OS:");
        versionStr.append(sOS);

        String sModel = Build.MODEL;
        versionStr.append("|MOD:");
        versionStr.append(sModel);

        Log.i(LOG_TAG, "getVersionDescription: exit");

        return versionStr.toString();
    }

    private void updateURLServer() {
        String urlServer = ParamHelper.getString(ParamHelper.SERVER_URL_CONFIGURATION, null);
        SharedPreferences sharedPreferences = activity.getSharedPreferences("settings", Activity.MODE_PRIVATE);

        String actualValue = sharedPreferences.getString("URL", null);
        if (urlServer != null && actualValue != null && !actualValue.equals(urlServer)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("URL", "https://" + urlServer.replace("https://", ""));
            editor.apply();
        }
    }

}