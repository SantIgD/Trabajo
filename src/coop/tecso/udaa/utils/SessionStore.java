package coop.tecso.udaa.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.domain.util.DeviceContext;

public class SessionStore {

    private static final String LAST_USER = "UDAA_LAST_USER";

    /**
     * Vuelve a iniciar la sesión en caso de ser posible
     */
    public static void restoreSessionIfPossible(Context context) {
        UdaaApplication application = UdaaApplication.get(context);

        if (application.getCurrentUser() != null){
            return;
        }

        UsuarioApm user = getStoredSession(application);

        if (user == null) {
            return;
        }

        restoreSession(user,application);


    }

    private static void restoreSession(UsuarioApm user, UdaaApplication application){
        application.setCurrentUser(user);

        String deviceId = DeviceContext.getEmail(application);
        UDAADao udaaDao = new UDAADao(application);
        DispositivoMovil dispositivoMovil = udaaDao.getDispositivoMovil(deviceId);
        application.setDispositivoMovil(dispositivoMovil);
    }

    /**
     * Almacena la información persistente de la sesión
     * Usuario Actual
     */
    public static void storeSession(Context context) {
        UdaaApplication application = UdaaApplication.get(context);

        UsuarioApm currentUser = application.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences(application);
        sharedPref.edit().putString(LAST_USER, currentUser.getUsername()).apply();
    }

    /**
     * Borra la inforación persistente de la sesión
     * Usuario Actual
     */
    public static void unstoreSession(Context context) {
        UdaaApplication application = UdaaApplication.get(context);
        SharedPreferences sharedPref = getSharedPreferences(application);
        sharedPref.edit().remove(LAST_USER).apply();
    }

    // MARK: - Internal

    /**
     * Intenta obtener el usuario de la sesión almacenada si esta existe
     * En caso de no existir retorna null
     */
    private static UsuarioApm getStoredSession(Application application) {
        SharedPreferences sharedPref = getSharedPreferences(application);
        String lastUserName = sharedPref.getString("UDAA_LAST_USER", null);
        if (lastUserName == null) {
            return null;
        }

        UDAADao udaaDao = new UDAADao(application);
        return udaaDao.getUsuarioApmByUserName(lastUserName);
    }

    private static SharedPreferences getSharedPreferences(Application application) {
        return application.getSharedPreferences("UDAA_Login", Context.MODE_PRIVATE);
    }
}
