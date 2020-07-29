package coop.tecso.hcd.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.gui.components.ElectroGUI;
import coop.tecso.hcd.utils.Constants;

public final class UDAAMessageReceiver extends BroadcastReceiver {

	private static final String TAG = UDAAMessageReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		String data = intent.getDataString();
		
		Log.i(TAG, String.format("onReceive: %s --> %s", action, data) );

		if (action == null) {
		    return;
        }

        switch (action) {
            case Constants.ACTION_NEW_NOTIFICATION: {
                int notificacionID = intent.getExtras().getInt("notificacionID", -1);
                Log.d(TAG, "Procesando evento desde UDAA. Nueva Notificacion. ID: " + notificacionID);
                if (notificacionID != -1) {
                    Log.d(TAG, "Ejecutando AsyncTask de Notificacion.");
                    HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
                    appState.getNotificationManager().procesarUDAANotification(notificacionID);
                } else {
                    Log.d(TAG, "Procesando evento desde UDAA. FAIL.");
                }
                break;
            }

            // End Session from UDAA
            case Constants.ACTION_LOSE_SESSION: {
                // Se elimina referencia a sesion
                HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
                appState.setCurrentUser(null);
                // BugFix Error 24 – No se limpian los registros
                // Se fuerza el sincronismo de HCDigital
                appState.hasSynchronized = false;
                break;
            }


            case Constants.ACTION_ELECTROCARDIOGRAMA: {
                HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();
                ElectroGUI electroGUI = (ElectroGUI) appState.getForm().getCampoGUISelected();
                electroGUI.setImageBitmap();
                break;
            }
        }
	}

    // MARK: - Static

    public static void createAndRegisterReceiver(HCDigitalApplication application) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("coop.tecso.udaa.custom.intent.action.NEW_NOTIFICATION" );
        intentFilter.addAction("coop.tecso.udaa.custom.intent.action.LOSE_SESSION" );
        intentFilter.addAction("coop.tecso.udaa.custom.intent.action.BLOCK_APPLICATION" );
        intentFilter.addAction("coop.tecso.udaa.custom.intent.action.UNBLOCK_APPLICATION" );
        intentFilter.addAction("coop.tecso.udaa.custom.intent.action.ACTION_ACRA_ERROR_SEND" );
        intentFilter.addAction("ecgUrg" );
        intentFilter.addAction("android.bluetooth.device.action.ACL_DISCONNECTED" );
        intentFilter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED" );

        UDAAMessageReceiver broadcastReceiver = new UDAAMessageReceiver();
        application.registerReceiver(broadcastReceiver, intentFilter);
    }

}
