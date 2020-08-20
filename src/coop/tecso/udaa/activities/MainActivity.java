package coop.tecso.udaa.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import coop.tecso.udaa.base.UdaaApplication;

public class MainActivity extends Activity {

    private static final String[] permissions = new String[]{
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // MARK: - Life Cycle

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this, permissions, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        for (Integer status: grantResults) {
            if (status != PackageManager.PERMISSION_GRANTED) {
                finish();
                return;
            }
        }

        this.checkLocationServiceAndLaunchIfPossible();
    }

    // MARK: - Internal

    private void checkLocationServiceAndLaunchIfPossible() {
        if (gpsLocationProviderIsEnabled()) {
            this.goMainActivity();
            return;
        }

        String message = "El servicio de localización está desactivado. Para poder usar la aplicación debe estar activado.\nSe abrirán las configuraciones de localización.";

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton("Entiendo, abrir ajustes", (dialog, id) -> {
            this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            this.finish();
        });

        alertBuilder.create().show();
    }

    private boolean gpsLocationProviderIsEnabled() {
        /*
        try {
            LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            return false;
        }
        */
        return true;
    }

    private void goMainActivity() {
        UdaaApplication appState = (UdaaApplication) getApplicationContext();
        appState.setPermissionsGaranted(true);

        Intent intent = new Intent(this, NotificacionesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        this.finish();
    }

}
