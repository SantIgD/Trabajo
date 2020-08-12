package coop.tecso.hcd.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TabHost;
import android.widget.Toast;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.RegisterActivity;
import coop.tecso.hcd.activities.TabHostActivity;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;
import coop.tecso.hcd.services.HCDigitalManager;
import coop.tecso.hcd.services.IMNotificationManager;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ErrorConstants;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

/**
 * Cerrar Informe Medico
 */
@SuppressLint("StaticFieldLeak")
public class CloseIMTask extends AsyncTask<String, CharSequence, String> {

    private static final String TAG = CloseIMTask.class.getSimpleName();

    private TabHostActivity tabHostActivity;

    private RegisterActivity currentActivity;

    private ProgressDialog progressDialog;

    private HCDigitalManager service;

    private UDAACoreServiceImpl udaaService;

    private HCDigitalApplication appState;

    // MARK: - Init

    public CloseIMTask(TabHostActivity tabHostActivity) {
        this.tabHostActivity = tabHostActivity;
        this.appState = (HCDigitalApplication) tabHostActivity.getApplicationContext();
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        String message = tabHostActivity.getString(R.string.saving_msg);
        this.progressDialog = ProgressDialog.show(tabHostActivity, "", message, true);
        this.service = new HCDigitalManager(tabHostActivity);
        this.udaaService = new UDAACoreServiceImpl(tabHostActivity);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            publishProgress("Validando Usuario");
            Thread.sleep(1000);
            String userName = params[0];
            String passWord = params[1];

            // Se valida la firma digital
            UsuarioApm userToValidate = udaaService.login(userName, passWord);
            // Los datos ingresados no son validos
            if (userToValidate == null) {
                return ErrorConstants.ERROR_LOGIN;
            }

            if (appState.getCurrentUser() != null) {
                // Se verifica si existe cambio de usuario
                if (userToValidate.getId() != appState.getCurrentUser().getId()) {
                    if (!udaaService.hasAccess(userToValidate.getId(), Constants.COD_HCDIGITAL)) {
                        // Usuario sin permisos para acceder a la aplicación
                        return ErrorConstants.ERROR_PERMISSION_APPLICATION;
                    }
                    // Notificar al UDAA cambio de usuario
                    udaaService.changeSession(userName, passWord);
                }
            }

            publishProgress("Cerrando IMD");
            synchronized(tabHostActivity.oReference) {
                // Current Tab
                String tag = tabHostActivity.getTabHost().getCurrentTabTag();
                currentActivity = (RegisterActivity) tabHostActivity.getLocalActivityManager().getActivity(tag);

                Atencion atencion = service.closeIM(currentActivity.getForm(), tabHostActivity.dialog, userToValidate, tag);
                if (atencion == null) {
                    return ErrorConstants.ERROR_CLOSE_IAM;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "CloseIMTask: doInBackground", e);

            udaaService.generateACRA_LOG(ErrorConstants.ERROR_FATAL_CLOSE_IAM, e.getMessage());

            return ErrorConstants.ERROR_FATAL_CLOSE_IAM;
        }
        return "";
    }

    @Override
    protected void onProgressUpdate(CharSequence... values) {
        progressDialog.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(String errorCode) {
        if (tabHostActivity.isFinishing()) {
            return;
        }

        TabHost tabHost = tabHostActivity.getTabHost();

        GUIHelper.dismissDialog(progressDialog);

        if (TextUtils.isEmpty(errorCode)) {
            GUIHelper.dismissDialog(tabHostActivity.dialog);

            Toast.makeText(tabHostActivity,
                    "El Informe Médico se cerró satisfactoriamente",
                    Toast.LENGTH_SHORT).show();

            if (tabHost.getTabWidget().getChildCount() == 1) {
                tabHostActivity.finish();
            } else {
                currentActivity.disable();
                // Dibujo la botonera otra vez para el nuevo estado
                tabHostActivity.drawKeypad(tabHost.getCurrentTabTag());
            }
        } else {
            IMNotificationManager notificationManager = appState.getNotificationManager();
            notificationManager.viewError(tabHostActivity, errorCode);
        }
    }

}