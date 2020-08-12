package coop.tecso.hcd.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.gui.utils.Utils;

public class HotspotUtils {

    public static boolean enableWifiHotspotAndCheck(Context context) {
        if (isWifiHotspotEnabled(context)) {
            return true;
        }

        HCDigitalApplication application = HCDigitalApplication.getApplication(context);
        WifiConfiguration wificonfiguration = new WifiConfiguration();

        String username = null;
        String ssidName = ParamHelper.getString(ParamHelper.SSID_NAME,"");
        if(ssidName == null || ssidName.isEmpty()){
            username = getCurrentUserName(context);
            ssidName = username;
        }

        String ssidPasw = ParamHelper.getString(ParamHelper.SSID_PASW,"");
        if(ssidPasw == null || ssidPasw.isEmpty()){
            if(username == null){
                username = getCurrentUserName(context);
            }
            ssidPasw = Utils.completarCaracterDer(username, 8,'0');
        }

        wificonfiguration.SSID =  ssidName;
        wificonfiguration.preSharedKey = ssidPasw;
        wificonfiguration.hiddenSSID = false;
        wificonfiguration.status = WifiConfiguration.Status.ENABLED;
        wificonfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wificonfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wificonfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wificonfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wificonfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wificonfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        wificonfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

        //apago la wifi
        if(application.isWifiEnabled()) {
            application.setWifiEnabled(false);
        }

        try {
            WifiManager wifimanager = getWifiManager(context);
            Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
            if(isWifiHotspotEnabled(context)){
                //apago el anclaje de red
                method.invoke(wifimanager, null, false);
            }
            //enciendo el nuevo anclaje de red
            method.invoke(wifimanager, wificonfiguration, true);

            return isWifiHotspotEnabled(context);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean disableWifiHotspotAndCheck(Context context) {
        try {
            if(isWifiHotspotEnabled(context)){
                WifiManager wifimanager = getWifiManager(context);
                Method method = wifimanager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
                //apago el anclaje de red
                method.invoke(wifimanager, null, false);
            }
            return !isWifiHotspotEnabled(context);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @SuppressLint("PrivateApi")
    public static boolean isWifiHotspotEnabled(Context context){
        try {
            WifiManager wifimanager = getWifiManager(context);
            Method method = wifimanager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifimanager);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void toggleHotspotStatus(Context context) {
        if (HotspotUtils.isWifiHotspotEnabled(context)) {
            if (!disableWifiHotspotAndCheck(context)) {
                HotspotDialog.showDialog(context, true);
            }
        } else {
            if (!enableWifiHotspotAndCheck(context)) {
                HotspotDialog.showDialog(context, false);
            }
        }
    }

    // MARK: - Internal

    private static WifiManager getWifiManager(Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }

    private static String getCurrentUserName(Context context) {
        return HCDigitalApplication.getApplication(context).getCurrentUser().getUsername();
    }

}
