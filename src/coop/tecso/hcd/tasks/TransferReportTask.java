package coop.tecso.hcd.tasks;

import android.annotation.SuppressLint;
import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.TabHost;

import java.io.File;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.RegisterActivity;
import coop.tecso.hcd.activities.TabHostActivity;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.services.HCDigitalManager;
import coop.tecso.hcd.utils.CollectionUtils;

public class TransferReportTask extends AsyncTask<String, Float, String> {

    private static final String TAG = TransferReportTask.class.getSimpleName();

    // MARK: - Data

    @SuppressLint("StaticFieldLeak")
    private TabHostActivity tabHostActivity;

    private ProgressDialog dialog;

    private HCDigitalManager service;

    // MARK: - Init

    public TransferReportTask(TabHostActivity tabHostActivity) {
        this.tabHostActivity = tabHostActivity;
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        String message = tabHostActivity.getString(R.string.bt_transferring_msg);
        this.dialog = ProgressDialog.show(tabHostActivity, "", message, true);
        this.service = new HCDigitalManager(tabHostActivity);
    }

    @Override
    protected String doInBackground(String... params) {
        String result;
        try {
            TabHost tabHost = tabHostActivity.getTabHost();
            String currentTabTag = tabHost.getCurrentTabTag();

            // CurrentActivity
            LocalActivityManager localActivityManager = tabHostActivity.getLocalActivityManager();
            RegisterActivity currentActivity = (RegisterActivity) localActivityManager.getActivity(currentTabTag);

            // IM Form
            PerfilGUI form = currentActivity.getForm();
            Thread.sleep( 1000 );

            // true => pdf template parametrizado
            // false => pdf template del perfil
            result = service.generateReport(form, true);
        } catch (Exception e) {
            Log.e(TAG, "error: transfer report", e);
            result = "Error al generar reporte a transferir, contactese con el administrador";
        }

        return result;
    }

    @Override
    protected void onPostExecute(String result) {
        if (tabHostActivity.isFinishing()) {
            return;
        }

        GUIHelper.dismissDialog(dialog);

        boolean hasError = result.toUpperCase().contains("ERROR");
        if (hasError){
            GUIHelper.showMessage(tabHostActivity, result);
            return;
        }

        String authority = "com.fantommers.hc.fileprovider";
        Uri uri = FileProvider.getUriForFile(tabHostActivity, authority, new File(result));

        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/octet-stream");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        PackageManager pm = tabHostActivity.getPackageManager();
        List<ResolveInfo> appsList = pm != null ? pm.queryIntentActivities(intent, 0) : null;

        // Busco el bluetooth en el listado de posibles senders
        if(CollectionUtils.isEmpty(appsList)) {
            return;
        }

        String packageName = null;
        String className = null;
        boolean found = false;

        for(ResolveInfo info: appsList) {
            packageName = info.activityInfo.packageName;
            if(packageName.equals("com.android.bluetooth")) {
                className = info.activityInfo.name;
                found = true;
                break;
            }
        }

        if(!found) {
            GUIHelper.showMessage(tabHostActivity, "El Bluetooth no está disponible para transferir, intente nuevamente");
            return;
        }

        // lanzo el envío por bluetooth y aparece el listado de los dispositivos cercanos con bluetooth...
        intent.setClassName(packageName, className);
        tabHostActivity.startActivity(intent);
    }

}