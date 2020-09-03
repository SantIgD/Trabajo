package coop.tecso.udaa.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;

import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.utils.Constants;

public final class UDAAMessageReceiver extends BroadcastReceiver {

	private static final String TAG = UDAAMessageReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		UdaaApplication appState = (UdaaApplication) context.getApplicationContext();
		String action = intent.getAction();
		String data = intent.getDataString();

        Log.i(TAG, String.format("onReceive: %s --> %s", action, data) );

		if (!appState.isPermissionsGaranted() || action == null) {
			return;
		}

		// UnLock application
        switch (action) {
            // Change level of battery
            case Intent.ACTION_BATTERY_CHANGED:
                this.onActionBatteryChanged(context, intent);
                break;

            // registrar receiver
            case Constants.ACTION_ACRA_ERROR_SEND:
                this.onActionAcraErrorSend(context, intent);
                break;
        }
	}

    // MARK: - Static

    public static void createAndRegisterReceiver(UdaaApplication application) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("coop.tecso.udaa.custom.intent.action.UNBLOCK_APPLICATION" );
        intentFilter.addAction("android.intent.action.BATTERY_CHANGED" );
        intentFilter.addAction("coop.tecso.udaa.custom.intent.action.ACTION_ACRA_ERROR_SEND" );

        UDAAMessageReceiver broadcastReceiver = new UDAAMessageReceiver();

        application.registerReceiver(broadcastReceiver, intentFilter);
    }

	// MARK: - Internal
	
	private void onActionBatteryChanged(Context context, Intent intent) {
        UdaaApplication appState = (UdaaApplication) context.getApplicationContext();

        int rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        if (rawLevel >= 0 && scale > 0) {
            int level = (rawLevel * 100) / scale;
            Log.d(TAG, "Level of Battery: " + level);
            appState.onChangeBatteryLevel(level);
        }
    }

	private void onActionAcraErrorSend(Context context, Intent intent) {
        Bundle extras = intent.getExtras();

        GsonBuilder gsonBuilder = new GsonBuilder()
                .setPrettyPrinting();
        new GraphAdapterBuilder()
                .addType(ReporteError.class)
                .addType(DetalleReporteError.class)
                .registerOn(gsonBuilder);
        Gson gson = gsonBuilder.create();

        // ERROR
        String aux_jsone = extras.getString("REPORTE_ERROR");
        ReporteError reporteError = gson.fromJson(aux_jsone, ReporteError.class);

        UDAADao udaaDao = new UDAADao(context);
        udaaDao.createReporteError(reporteError);

        // DETALLES
        for (DetalleReporteError detalleError : reporteError.getDetalleReporteErrorList()) {
            udaaDao.createDetalleReporteError(detalleError);
        }
    }

}