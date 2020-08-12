package coop.tecso.hcd.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;

public class PrePrintUtil {

    private HCDigitalApplication application;

    private WifiManager wifiManager;

    // MARK: - Init

    public PrePrintUtil(Context context) {
        this.application = (HCDigitalApplication) context.getApplicationContext();
        this.wifiManager = (WifiManager) application.getSystemService(Context.WIFI_SERVICE);
    }

    // MARK: - Internal

    public void printIfPossible(Context context, @NonNull PrintPerformer printPerformer) {
        if (!isCurrentAccessPointPrinter()) {
            printPerformer.performPrint();
            return;
        }

        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Actualmente está conectado a una impresora como punto de acceso WiFi");
        messageBuilder.append("\n\n");
        messageBuilder.append("Se recomienda conectarse a otra red para poder imprimir");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Arvertencia");
        builder.setMessage(messageBuilder.toString());
        builder.setPositiveButton(R.string.continue_anyway, (dialog, id) -> {
            printPerformer.performPrint();
        });
        builder.setNegativeButton(R.string.disconnect_wifi, (dialog, id) -> {
            this.openWifiSettings();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // MARK: - Interface

    private boolean isCurrentAccessPointPrinter() {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID();
        return this.ssidIsAccessPoint(ssid);
    }

    private void openWifiSettings() {
        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        application.startActivity(intent);
    }

    private boolean ssidIsAccessPoint(String ssid) {
        return ssid != null && ssid.toLowerCase().contains("direct");
    }

    // MARK: -

    public interface PrintPerformer {

        void performPrint();

    }

}
