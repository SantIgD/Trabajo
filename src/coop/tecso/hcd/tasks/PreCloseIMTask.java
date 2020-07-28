package coop.tecso.hcd.tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.RegisterActivity;
import coop.tecso.hcd.activities.TabHostActivity;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.EstadoAtencion;
import coop.tecso.hcd.gui.components.GenericScoreGUI;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.services.HCDigitalManager;
import coop.tecso.hcd.utils.HotspotDialog;
import coop.tecso.hcd.utils.HotspotUtils;

public class PreCloseIMTask extends AsyncTask<Void, String, Boolean> {

    private static final String TAG = PreCloseIMTask.class.getSimpleName();

    // MARK: - Data

    @SuppressLint("StaticFieldLeak")
    private TabHostActivity tabHostActivity;

    private ProgressDialog dialogProgress;

    private HCDigitalManager service;

    private HCDigitalApplication appState;

    // MARK: - Init

    public PreCloseIMTask(TabHostActivity tabHostActivity) {
        this.tabHostActivity = tabHostActivity;
        this.appState = (HCDigitalApplication) tabHostActivity.getApplicationContext();
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        String message = tabHostActivity.getString(R.string.loading_msg);
        this.tabHostActivity.setOnPreCloseIMTask(true);
        this.dialogProgress = ProgressDialog.show(tabHostActivity, "", message, true);
        this.service = new HCDigitalManager(tabHostActivity);
    }

    @Override
    protected Boolean doInBackground(Void... params) {return true;}

    @Override
    protected void onPostExecute(Boolean errorProcesamiento) {
        if (tabHostActivity.isFinishing()) {
            return;
        }

        Log.i(TAG, "DoCloseIM: enter");
        // CurrentTabTag
        String currentTabTag = tabHostActivity.getTabHost().getCurrentTabTag();

        // CurrentActivity
        RegisterActivity currentActivity = (RegisterActivity) tabHostActivity.getLocalActivityManager().getActivity(currentTabTag);

        // IM Form
        PerfilGUI form = currentActivity.getForm();
        // Atencion actual
        Atencion atencion = (Atencion) form.getBussEntity();
        if (atencion.getEstadoAtencion().getId() != EstadoAtencion.ID_CERRADA_PROVISORIA && !form.validate()) {
            GUIHelper.dismissDialog(dialogProgress);

            Toast.makeText(tabHostActivity,
                    tabHostActivity.getString(R.string.close_IM_validate_error),
                    Toast.LENGTH_SHORT).show();
            this.tabHostActivity.setOnPreCloseIMTask(false);
            return;
        }

        // Comparo el TAG_ID del IM principal con el del actual
        if (!TextUtils.equals(currentTabTag, tabHostActivity.MAIN_TAB_TAG)) {
            // Caso IMM
            RegisterActivity mainActivity = (RegisterActivity) tabHostActivity.getLocalActivityManager().getActivity(tabHostActivity.MAIN_TAB_TAG);
            Atencion atencionPrincipal = (Atencion) mainActivity.getForm().getBussEntity();

            // IM Principal sin guardar
            if (atencionPrincipal.getId() == 0) {
                dialogProgress.dismiss();

                this.showCloseMainIMAlert();
                this.tabHostActivity.setOnPreCloseIMTask(false);
                return;
            }
            atencion.setAtencionPrincipal(atencionPrincipal);
        }

        dialogProgress.dismiss();
        if (!form.runEvalAllCierre()) {
            // Dialogo de Cierre IM
            try {
                String tabTag = tabHostActivity.getTabHost().getCurrentTabTag();
                tabHostActivity.dialog = service.buildDialogForCierreIM(tabTag, atencion, false, form);
                if (Build.VERSION.SDK_INT < 21 && tabHostActivity.dialog.getWindow() != null) {
                    tabHostActivity.dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG);
                }
                appState.getAlertChain().runAtEnd(() -> tabHostActivity.dialog.show());
                //tabHostActivity.dialog.show();
            } catch (Exception e) {
                Toast.makeText(tabHostActivity, "Error generando Dialog de cierre IM", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }

            this.disableWifiHotspot();
        }
        this.tabHostActivity.setOnPreCloseIMTask(false);
    }

    private void disableWifiHotspot() {
        if (HotspotUtils.isWifiHotspotEnabled(appState)) {
            showHotspotDidableAlert();
        }
    }

    private void showHotspotDidableAlert() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(tabHostActivity);
        alertBuilder.setTitle(R.string.inactive_anclaje_title);
        alertBuilder.setMessage(R.string.inactive_anclaje_msg);
        alertBuilder.setCancelable(true);
        alertBuilder.setPositiveButton(R.string.accept, (dialog, id) -> {
            if (!HotspotUtils.disableWifiHotspotAndCheck(appState)) {
                HotspotDialog.showDialog(tabHostActivity, false);
            }
        });
        alertBuilder.setNegativeButton(R.string.cancel, null);
        alertBuilder.create().show();
    }

    private void showCloseMainIMAlert() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(tabHostActivity);
        alertBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertBuilder.setTitle(tabHostActivity.getString(R.string.close_IMM_error_tittle));
        alertBuilder.setMessage(tabHostActivity.getString(R.string.close_IMM_error_msg));
        alertBuilder.setPositiveButton(tabHostActivity.getString(R.string.accept), (dialog, id) -> dialog.dismiss());
        alertBuilder.create().show();
    }

}