package coop.tecso.hcd.tasks;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.MainHCActivity;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.AccionHC;
import coop.tecso.hcd.entities.AplicacionTablaHC;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.BloqueoRamaElectro;
import coop.tecso.hcd.entities.CondicionAlertaHC;
import coop.tecso.hcd.entities.DerivacionesOndaTElectro;
import coop.tecso.hcd.entities.DerivacionesSegmElectro;
import coop.tecso.hcd.entities.Despachador;
import coop.tecso.hcd.entities.EntidadBusqueda;
import coop.tecso.hcd.entities.ErrorAtencion;
import coop.tecso.hcd.entities.EstadoAtencion;
import coop.tecso.hcd.entities.MotivoCierreAtencion;
import coop.tecso.hcd.entities.OndaTElectro;
import coop.tecso.hcd.entities.Regla;
import coop.tecso.hcd.entities.ReglaCondicion;
import coop.tecso.hcd.entities.RitmoElectro;
import coop.tecso.hcd.entities.Score;
import coop.tecso.hcd.entities.SegmentoElectro;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.helpers.SearchPageAdapter;
import coop.tecso.hcd.helpers.SyncInfoHelper;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;
import coop.tecso.hcd.services.IMNotificationManager;
import coop.tecso.hcd.services.ServiceStarter;
import coop.tecso.hcd.services.SyncAtencionService;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.hcd.utils.UDAAUpdater;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionBinarioVersion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionParametro;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.domain.util.DeviceContext;


@SuppressWarnings("unchecked")
@SuppressLint({"StaticFieldLeak", "SimpleDateFormat"})
public class BuildSearchPageTask extends AsyncTask<Void, CharSequence, List<Atencion>> {

    private static String LOG_TAG = BuildSearchPageTask.class.getSimpleName();

    public boolean forceSyncrhonization = false;

    private ProgressDialog dialog;
    private MainHCActivity mainHCActivity;
    private HCDigitalApplication appState;
    private UDAACoreServiceImpl localService;
    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    private UDAACoreServiceImpl udaaService = null;

    private AlertDialog alertDialog;

    private boolean errorSync = false;
    private boolean permissionLocked = false;
    private SyncInfoHelper syncInfoHelper = new SyncInfoHelper();

    // MARK: - Init

    public BuildSearchPageTask(MainHCActivity mainHCActivity) {
        this.mainHCActivity = mainHCActivity;
        this.appState = HCDigitalApplication.getApplication(mainHCActivity);
        this.localService = new UDAACoreServiceImpl(mainHCActivity);
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        this.dialog = ProgressDialog.show(mainHCActivity, "", mainHCActivity.getString(R.string.loading_msg), true);
    }

    @Override
    protected List<Atencion> doInBackground(Void... params) {
        try {
            Thread.sleep(2000);
            // Validaciones Iniciales
            UsuarioApm currentUser = getCurrentUser();

            checkUserAccess(currentUser);

            appState.setCurrentUser(currentUser);

            Intent intentTitle = new Intent(Constants.ACTION_TITLE);
            mainHCActivity.sendBroadcast(intentTitle);

            if (upgradeUDAAIfNeed()){
                return  null;
            }

            this.checkNewVersionAvailable();

            if (!this.initializeHC()) {
                return null;
            }

            Intent intent = new Intent(mainHCActivity, SyncAtencionService.class);
            ServiceStarter.startService(mainHCActivity, intent);

            if (appState.needsForceUpdate()) {
                DispositivoMovil updatedMovil = localService.confirmForceUpdate();
                appState.setDispositivoMovil(updatedMovil);
            }

            // Atenciones pendientes
            return appState.getHCDigitalDAO().getListAtencion();
        } catch (Exception e) {
            Log.i(LOG_TAG, "BuildSearchPageTask: ERROR", e);
            return null;
        }
    }

    private void checkUserAccess(UsuarioApm currentUser) throws Exception{

        if (!localService.hasAccess(currentUser.getId(), Constants.COD_HCDIGITAL)) {
            permissionLocked = true;
            throw new Exception(mainHCActivity.getString(R.string.login_user_permission_error));
        }
    }

    private boolean upgradeUDAAIfNeed(){
        UDAAUpdater udaaUpdater = new UDAAUpdater(mainHCActivity);
        if (udaaUpdater.shouldInstallUpdate()) {
            udaaUpdater.updateUDAA();
            mainHCActivity.finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onProgressUpdate(CharSequence... values) {
        dialog.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(List<Atencion> atencionList) {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (IllegalArgumentException ignore) {}

        if (errorSync) {
            this.showSyncErrorView();
            this.showSyncErrorsAlert();
            return;
        }

        this.hideSyncErrorView();

        if (permissionLocked) {
            this.showPermissionDeniedAlert();
        } else if (atencionList != null) {
            SearchPageAdapter adapter = new SearchPageAdapter(mainHCActivity, atencionList);
            mainHCActivity.setListAdapter(adapter);
            mainHCActivity.setTitle(Utils.getFormattedTitle(mainHCActivity));
            mainHCActivity.getActionBar().setSubtitle(Utils.getFormattedSubtitled(mainHCActivity));
        }
    }

    // MARK: - Internal

    private UsuarioApm getCurrentUser() throws Exception {
        UsuarioApm currentUser;

        if (!appState.canAccess()) {
            currentUser = localService.getCurrentUser();
            if (currentUser == null) {
                mainHCActivity.runOnUiThread(this::noSessionFromUdaa);
                throw  new  Exception("No se inició sesión en UDAA o está cerrada la aplicación");
            }
        } else {
            currentUser = appState.getCurrentUser();
        }

        return currentUser;
    }

    private boolean initializeHC() throws Exception {
        generateLoginLog("Initialize HCDigital -> Start");

        if (!appState.hasSynchronized || errorSync || forceSyncrhonization) {
            Log.i(LOG_TAG, "Cargando parametros de session...");

            // Sincronizo Datos
            // ================
            DispositivoMovil dispositivoMovil = localService.getDispositivoMovil();
            appState.setDispositivoMovil(dispositivoMovil);

            // si retorna false es porque se estaba sincronizando y ocurrió una excepcion
            if (!performSynchronizationProccess()){
                return false;
            }

            // Elimino atenciones sincronizadas
            appState.getHCDigitalDAO().deleteAllSynchronizedAtencion();

            // Inicializo caches
            // ==================

            // * Entidades de Busqueda
            publishProgress(mainHCActivity.getString(R.string.initializing_item_msg, "cache"));
            appState.setListEntidadBusqueda(appState.getHCDigitalDAO().getListEntidadBusqueda());

            this.initializeNotificationManagerIfNeeded();

            this.initializeFirebaseIfNeeded(dispositivoMovil);

            appState.hasSynchronized = true;
            localService.updateApplicationSync();
        }

        if (mainHCActivity.createLogVersion) {
            mainHCActivity.createLogVersion = false;
            generateLoginLog("Initialize HCDigital -> localService registerUsuarioApmVersion Start");
            String description = getVersionDescription();
            localService.registerUsuarioApmVersion(description);
            generateLoginLog("Initialize HCDigital -> localService registerUsuarioApmVersion End");
        }

        if (mainHCActivity.createLogSyncVersion) {
            mainHCActivity.createLogSyncVersion = false;
            try {
                String dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                localService.registerUsuarioApmVersion("Sincronización "+ Aplicacion.App.HCDigital.toString()+": " + dateString);
            } catch (Exception ignore) {}
        }

        generateLoginLog("Initialize HCDigital -> End");
        return true;
    }

    private boolean performSynchronizationProccess(){
        if (hasToSynchronize() || errorSync || forceSyncrhonization) {
            try {
                publishProgress(mainHCActivity.getString(R.string.synchronizing_msg));

                localService.updateTablesToSync();
                syncTables();
                this.errorSync = false;

            } catch (Exception e) {
                this.errorSync = true;
                Log.d(LOG_TAG, "***NO SYNC***", e);
                if (e.getMessage() != null && e.getMessage().equals("ERROR_SINCRONIZACION")){
                    return false;
                }
            }
        }
        else {
            mainHCActivity.createLogSyncVersion = false;
        }
        return true;
    }

    private void syncTables() throws Exception {
        syncElement("apm_aplicacionTabla", AplicacionTablaHC.class, true);

        // Parametros
        publishProgress(mainHCActivity.getString(R.string.synchronizing_item_msg, "parámetros"));

        syncElement("apm_aplicacionParametro", AplicacionParametro.class, true);

        ParamHelper.initialize(mainHCActivity);

        // Maestros
        publishProgress(mainHCActivity.getString(R.string.synchronizing_item_msg,"datos maestros"));

        syncElement("hcd_estadoAtencion", EstadoAtencion.class, false);
        syncElement("hcd_motivoCierreAtencion", MotivoCierreAtencion.class, false);
        syncElement("hcd_entidadBusqueda", EntidadBusqueda.class, false);
        syncElement("hcd_errorAtencion", ErrorAtencion.class, false);
        syncElement("hcd_despachador", Despachador.class, false);

        // Tratamiento ELECTRO
        syncElement("hcd_ritmoElectro", RitmoElectro.class, false);
        syncElement("hcd_segmentoElectro", SegmentoElectro.class, false);
        syncElement("hcd_derivacionesSegmentoElectro", DerivacionesSegmElectro.class, false);
        syncElement("hcd_ondaTElectro", OndaTElectro.class, false);
        syncElement("hcd_derivacionesOndaTElectro", DerivacionesOndaTElectro.class, false);
        syncElement("hcd_bloqueoRamaElectro", BloqueoRamaElectro.class, false);

        //Tratamiento SCORE
        syncElement("hcd_score", Score.class, false);

        //Condiciones ALERTAS
        syncElement("apm_condicionAlerta", CondicionAlertaHC.class, true);

        //Acciones
        syncElement("apm_gestionAccion", AccionHC.class, true);

        // Cepo regla-reglaCondicion

        syncElement("hcd_regla", Regla.class, false);
        syncElement("hcd_reglaCondicion", ReglaCondicion.class, false);
    }

    private void initializeNotificationManagerIfNeeded() {
        if (appState.getNotificationManager() != null) {
            return;
        }

        IMNotificationManager notifManager = new IMNotificationManager(mainHCActivity);
        notifManager.initialize();
        appState.setNotificationManager(notifManager);
    }

    private void initializeFirebaseIfNeeded(DispositivoMovil dispositivoMovil) throws IOException {
        if (appState.hasSynchronized) {
            return;
        }

        String dispositivoMovilID = Integer.toString(dispositivoMovil.getId());
        FirebaseInstanceId.getInstance().deleteInstanceId();
        FirebaseMessaging.getInstance().subscribeToTopic(dispositivoMovilID);
    }

    private void syncElement(String table, Class clazz, boolean byAplicacion) throws Exception {
        try {
            generateLoginLog("Initialize HCDigital -> synchronize " + table + " Start");
            if (byAplicacion) {
                localService.synchronizeByAplicacion(clazz, table);
            } else {
                localService.synchronize(clazz, table);
            }
            generateLoginLog("Initialize HCDigital -> synchronize " + table + " End");
        } catch (Exception e) {
            syncInfoHelper.addElement("Error de sincronización en la tabla: " + clazz.getSimpleName());
            throw e;
        }
    }

    private void showSyncErrorView() {
        TextView view = mainHCActivity.findViewById(R.id.mensaje_error_sync);
        view.setVisibility(View.VISIBLE);
        try {
            String defaultMessageError = mainHCActivity.getString(R.string.mensaje_error_sync);
            String text = ParamHelper.getString(ParamHelper.MENSAJE_ERROR_SYNC, defaultMessageError);
            view.setText(text);
        } catch (Exception ignore) {}
    }

    private void hideSyncErrorView() {
        TextView view = mainHCActivity.findViewById(R.id.mensaje_error_sync);
        view.setVisibility(View.GONE);
    }

    private void showSyncErrorsAlert() {
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mainHCActivity, android.R.layout.simple_dropdown_item_1line, syncInfoHelper.getListOfElements());

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mainHCActivity);
        alertDialogBuilder.setTitle(R.string.mensaje_error_sync);
        alertDialogBuilder.setAdapter(arrayAdapter, null);
        alertDialogBuilder.setIcon(R.drawable.ic_error_default);
        alertDialogBuilder.setPositiveButton(R.string.accept, (dialog, id) -> dialog.dismiss());

        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void showPermissionDeniedAlert() {
        GUIHelper.showErrorDialog(mainHCActivity, mainHCActivity.getString(R.string.login_user_permission_error));
    }

    private void checkNewVersionAvailable() throws Exception {
        //Valido version
        AplicacionBinarioVersion binary = localService.getAplicacionBinarioVersion();

        if (!existsUpgrade (binary)){
            return;
        }

        String fileName = binary.getUbicacion().substring(
                binary.getUbicacion().lastIndexOf(File.separator) + 1).trim();
        String path = Environment.getExternalStorageDirectory() + "/download/";

        // Intent to install apk
        File file = new File(path, fileName);

        if (file.exists() && (binary.getLongitud() == 0 || binary.getLongitud() == file.length())){
            Uri data = FileProvider.getUriForFile(mainHCActivity,
                    "com.fantommers.hc.fileprovider",
                    file);

            final Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            installIntent.setDataAndType(data, "application/vnd.android.package-archive");

            appState = setAppStateforInstallation(binary,installIntent);

            while (appState.isWaitingInstallation()) {
                Thread.sleep(1000);

                if (alertDialog == null && !appState.isUpgradeRequired()) {
                    mainHCActivity.runOnUiThread(this::confirmAndUpgrade);
                } else if (appState.isUpgradeRequired()) {
                    mainHCActivity.startActivityForResult(appState.getUpgradeIntent(), Constants.REQUEST_UPGRADE_APP);
                    throw new Exception("Instalacion obligatoria");
                }
            }

        } else {
            // no existe el apk
            Thread.sleep(1000);
            if (!binary.isObligatorio()) {
                return;
            }
            mainHCActivity.runOnUiThread(() -> {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(mainHCActivity);
                alertBuilder.setTitle(R.string.disable_section_title);
                alertBuilder.setMessage(mainHCActivity.getString(R.string.update_blocked));
                alertBuilder.setCancelable(false);
                alertBuilder.setPositiveButton(R.string.yes, (dialog, id) -> System.exit(0));
                alertBuilder.create().show();
            });
        }
}

    private void generateLoginLog(String message) {
        int loginTimeReport =  ParamHelper.getInteger(ParamHelper.LOGIN_TIME_REPORT, 1);

        if (loginTimeReport == 1) {
            if (udaaService == null) {
                udaaService = new UDAACoreServiceImpl(mainHCActivity);
            }
            udaaService.generateACRA_LOG(dateformat.format(new Date()) + " - "+ message, "LOGIN_TIME_REPORT");
        }
    }

    private String getVersionDescription() {
        Log.i(LOG_TAG, "getVersionDescription: enter");

        StringBuilder versionStr = new StringBuilder();

        try {
            versionStr.append(Constants.COD_HCDIGITAL);
            versionStr.append("v");
            versionStr.append(mainHCActivity.getPackageManager().getPackageInfo(mainHCActivity.getPackageName(), 0).versionName);
            versionStr.append("|");
        } catch (Exception e) {
            Log.e(LOG_TAG, "**ERROR: Determinando Versiones", e);
        }

        Log.i(LOG_TAG, "getVersionDescription: exit");

        return versionStr.toString();
    }

    private boolean hasToSynchronize() {
        SharedPreferences sharedPreferences = mainHCActivity.getSharedPreferences(appState.getClass().getName(), Activity.MODE_PRIVATE);
        String lastExitTimestamp = sharedPreferences.getString(Constants.LAST_EXIT_TIMESTAMP, Constants.NO_TIMESTAMP);

        Time lastExitTime = new Time();
        Time now = new Time();
        now.setToNow();
        long sinceLastExitMillis;

        if (!lastExitTimestamp.equals(Constants.NO_TIMESTAMP)) {
            lastExitTime.parse3339(lastExitTimestamp);
            sinceLastExitMillis = now.toMillis(true) - lastExitTime.toMillis(true);
        } else {
            sinceLastExitMillis = Constants.ONE_MINUTE_IN_MILLIS + 1;
        }

        return sinceLastExitMillis > Constants.ONE_MINUTE_IN_MILLIS;
    }

    private void noSessionFromUdaa() {
        AlertDialog.Builder alertBuilder =  new AlertDialog.Builder(mainHCActivity);
        alertBuilder.setTitle(R.string.confirm_title);
        alertBuilder.setMessage("No puede avanzar, debe iniciar sesión en URGAdmin");
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(R.string.close_application, (dialog, id) -> mainHCActivity.finish());
        alertBuilder.setNegativeButton(R.string.openudaa, (dialog, id) -> {
            dialog.cancel();

            //Custom Application Launcher
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName("coop.tecso.udaa","coop.tecso.udaa.activities.MainActivity"));
            appState.startActivity(intent);
        });

        this.alertDialog = alertBuilder.create();
        this.alertDialog.show();
    }

    private void confirmAndUpgrade() {
        int title = R.string.confirm_title;
        //ParamHelper.getString("install update",mainHCActivity.getString(R.string.update_install));
        int mensaje = R.string.update_install;
        this.alertDialog = GUIHelper.createAlertDialog(mainHCActivity,title,mensaje, this::launchInstallation, this::launchCancelInstallation);
        this.alertDialog.show();
    }

    private void launchInstallation(){
        mainHCActivity.startActivityForResult(appState.getUpgradeIntent(), Constants.REQUEST_UPGRADE_APP);
    }

    private void launchCancelInstallation(){
        dialog.cancel();
        appState.setWaitingInstallation(false);
        mainHCActivity.checkCurrentVersion();
    }

    private boolean existsUpgrade(AplicacionBinarioVersion binary) {
        PackageInfo packageInfo = DeviceContext.getPackageInfoFromInstalledApp(mainHCActivity.getPackageName(), mainHCActivity);
        String currentVersion = packageInfo != null ? packageInfo.versionName : "";
        return (
                binary != null &&
                mainHCActivity.getPackageName().equals(binary.getAplicacion().getPkg()) &&
                binary.getAplTipoBinario().getId() == 1 &&
                !currentVersion.equals(binary.getNombreVersion())
        );
    }

    private HCDigitalApplication setAppStateforInstallation (AplicacionBinarioVersion binary, Intent installIntent){
        appState.setLastVersionName(binary.getNombreVersion());
        appState.setWaitingInstallation(true);
        appState.setUpgradeIntent(installIntent);
        appState.setUpgradeRequired(binary.isObligatorio());
        appState.setLastVersionName(binary.getNombreVersion());

        return appState;
    }

}
