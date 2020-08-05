package coop.tecso.hcd.tasks;

import android.annotation.SuppressLint;
import android.app.LocalActivityManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.TabHost;

import java.io.File;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.RegisterActivity;
import coop.tecso.hcd.activities.TabHostActivity;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.services.HCDigitalManager;

public class PrintReportTask extends AsyncTask<String, Float, String> {

    private static final String TAG = PrintReportTask.class.getSimpleName();

    private static final String PRINT_SHARE_PACKAGE = "com.dynamixsoftware.printershare";

    // MARK: - Data

    @SuppressLint("StaticFieldLeak")
    private TabHostActivity tabHostActivity;

    private ProgressDialog dialog;

    private HCDigitalManager service;

    // MARK: - Init

    public PrintReportTask(TabHostActivity tabHostActivity) {
        this.tabHostActivity = tabHostActivity;
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        String printingMessage = tabHostActivity.getString(R.string.printing_msg);
        this.dialog = ProgressDialog.show(tabHostActivity, "", printingMessage, true);
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
            Thread.sleep(1000);

            // false => pdf template del perfil
            // true => pdf template de parametro
            result = service.generateReport(form, false);
        } catch (Exception e) {
            Log.e(TAG, "error: print report", e);
            result = "Error al generar reporte PDF, contactese con el administrador";
        }

        return result;
    }

    @Override
    protected void onPostExecute(final String result) {
        if (tabHostActivity.isFinishing()) {
            return;
        }

        GUIHelper.dismissDialog(dialog);

        if (resultHasError(result)) {
            GUIHelper.showMessage(tabHostActivity, result);
            return;
        }

        this.print(result);
    }

    // MARK: - Internal

    private boolean resultHasError(String result) {
        return result.toUpperCase().contains("ERROR");
    }

    private void print(String result) {
        String authority = "com.fantommers.hc.fileprovider";
        Uri dataUri = FileProvider.getUriForFile(tabHostActivity, authority, new File(result));

        if (isPrintShareInstalled()) {
            this.printWithPrintShare(dataUri);
        } else {
            this.printWithSystem(dataUri);
        }
    }

    private boolean isPrintShareInstalled() {
        return Utils.isAppInstalled(tabHostActivity, PRINT_SHARE_PACKAGE);
    }

    private void printWithPrintShare(Uri dataUri) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(dataUri, "application/pdf");
        intent.setPackage(PRINT_SHARE_PACKAGE);

        tabHostActivity.startActivity(intent);
    }

    private void printWithSystem(Uri dataUri) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, dataUri);

        tabHostActivity.startActivity(intent);
    }

}