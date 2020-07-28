package coop.tecso.hcd.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import java.io.File;

import coop.tecso.hcd.helpers.GUIHelper;

public class UDAAUpdater {

    private static int[] minimumUpdatedVersion = { 5, 7, 9 };

    // MARK: - Data

    private Activity activity;

    // MARK: - Init

    public UDAAUpdater(Activity activity) {
        this.activity = activity;
    }

    // MARK: - Interface

    public boolean shouldInstallUpdate() {
        /*
        int[] currentUDAAVersion = getUDDAVersion();
        if (currentUDAAVersion == null) {
            return false;
        }

        for (int i = 0; i < 3; i++) {
            if (currentUDAAVersion[i] < minimumUpdatedVersion[i]) {
                return true;
            }
        }
        */
        return false;
    }

    public void updateUDAA() {
        String fileName = "URGAdmin.apk";
        String path = Environment.getExternalStorageDirectory() + "/download/";

        // Intent to install apk
        File udaaAPKFile = new File(path, fileName);

        if (!udaaAPKFile.exists()) {
            String message = "Debe actualizar UDAA pero no se encuentra el archivo. Vuelva a iniciar sesión.";
            activity.runOnUiThread(() -> GUIHelper.showError(activity, message));
            return;
        }

        this.installUDAA(udaaAPKFile);
    }

    // MARK: - Internal

    private int[] getUDDAVersion() {
        String udaaPackageName = "coop.tecso.udaa";
        PackageManager packageManager = activity.getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(udaaPackageName, PackageManager.GET_META_DATA);
            String versionName = packageInfo.versionName;
            if (versionName == null) {
                return null;
            }

            String[] versionSplit = versionName.split(" ");
            if (versionSplit.length < 2) {
                return null;
            }

            String[] versionNumbers = versionSplit[0].split("\\.");
            if (versionNumbers.length < 3) {
                return null;
            }

            int[] result = new int[3];

            for (int i = 0; i < 3; i++) {
                result[i] = Integer.parseInt(versionNumbers[i]);
            }

            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private void installUDAA(File udaaAPKFile) {
        String authority = "com.fantommers.hc.fileprovider";
        Uri data = FileProvider.getUriForFile(activity, authority, udaaAPKFile);

        Intent udaaUpgradelIntent = new Intent(Intent.ACTION_VIEW);
        udaaUpgradelIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        udaaUpgradelIntent.setDataAndType(data, "application/vnd.android.package-archive");

        activity.startActivityForResult(udaaUpgradelIntent, Constants.REQUEST_UPGRADE_APP);
    }

}
