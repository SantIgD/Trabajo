package coop.tecso.udaa.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import coop.tecso.udaa.base.GpsListener;
import coop.tecso.udaa.base.SendLocationTask;
import coop.tecso.udaa.base.UDAAException;
import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.common.WebServiceDAO;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.trazabilidad.UbicacionGPS;

public class LocationReporter {

    private static final String LOG_TAG = LocationReporter.class.getSimpleName();

    // GPS variables
    private static final long TIMER_DESHABILITADO = 0L;
    private static final int TIPO_POS_GPS = 1;
    private static final int TIPO_POS_NET = 2;

    private UdaaApplication application;
    private UDAADao udaaDao;

    private LocationManager locationManager;
    private Timer timerGPS;
    private int umbralBateria;
    private Long periodo;
    private Integer tipoPos;
    private Integer numMaxPosTrans;

    private int lastBatteryLevel = 100;

    private GpsListener gpsListener;

    // MARK: - Init

    public LocationReporter(UdaaApplication application) {
        this.application = application;
        this.udaaDao = new UDAADao(application);
    }

    public void initialize() {
        periodo = ParamHelper.getLong(ParamHelper.COD_TIMER_PERIOD, 120001L);
        tipoPos = ParamHelper.getInteger(ParamHelper.COD_TIPO_POS, TIPO_POS_GPS);
        umbralBateria = ParamHelper.getInteger(ParamHelper.COD_UMBRAL_BAT, 30);
        numMaxPosTrans = ParamHelper.getInteger(ParamHelper.COD_NUM_MAX_POS_TRANS, 10);

        Log.i(LOG_TAG, String.format("Params: periodo: %d - tipoPos: %d - umbralBat: %d - numMaxPosTrans: %d", periodo, tipoPos, umbralBateria, numMaxPosTrans));
    }

    // MARK: - Interface

    public void startGPSLocationTimerIfNeeded(Activity activity) {
        if (gpsLocationShouldBeActive()) {
            this.stopGPSLocationTimerIfPossible();
            this.startGPSLocationTimerIfPossible(activity);
        }
    }

    /**
     * Finalize Location Timer
     */
    public void stopGPSLocationTimerIfPossible() {
        if (this.timerGPS == null) {
            return;
        }

        Log.d(LOG_TAG, "stopGPSLocationTimerIfPossible: enter");
        try {
            this.timerGPS.cancel();
            this.timerGPS.purge();

            locationManager.removeUpdates(gpsListener);
            locationManager = null;
        } catch (Exception ignore) {}

        this.timerGPS = null;
    }

    /**
     * Controla el estado del GPS en base al nivel de batería
     */
    public void onChangeBatteryLevel(int level) {
        this.lastBatteryLevel = level;

        if (level <= umbralBateria && this.timerGPS != null) {
            Log.d(LOG_TAG, "stopGPSLocationTimerIfPossible");
            this.stopGPSLocationTimerIfPossible();
        }
        if (level > umbralBateria && this.timerGPS == null && this.periodo != 0) {
            Log.d(LOG_TAG, "tryStartGPSLocationTimer");
            this.startGPSLocationTimerIfNeeded(null);
        }
    }

    public void getGPSLocation(boolean shouldSend) {
        if (application.getDispositivoMovil() == null) {
            return;
        }

        try {
            int dispositivoMovilID = application.getDispositivoMovil().getId();

            boolean isProviderActive = this.verifyActiveProviders();
            Log.i(LOG_TAG, "verifyActiveProviders: " + isProviderActive);

            if (isProviderActive && this.gpsLocationShouldBeActive()) {
                Location location = getLastKnownLocation();

                if (location != null) {
                    int registerType = getPosTypeFromLocation(location);
                    Log.i(LOG_TAG, "LocationGOT");
                    UbicacionGPS ubicacionGPS = new UbicacionGPS();
                    ubicacionGPS.setDispositivoMovilID(dispositivoMovilID);
                    ubicacionGPS.setFechaPosicion(formatLocTime(location.getTime()));
                    ubicacionGPS.setLatitud(location.getLatitude());
                    ubicacionGPS.setLongitud(location.getLongitude());
                    ubicacionGPS.setTipoPosicionamiento(registerType);
                    ubicacionGPS.setTipoRegistro(tipoPos);
                    ubicacionGPS.setFechaLectura(new Date());

                    Log.d(LOG_TAG, String.format("FechaPosicion %s", ubicacionGPS.getFechaPosicion()));
                    Log.d(LOG_TAG, String.format("Movil: ID: %d Location: LAT: %s - LONG: %s - TIME: %s", dispositivoMovilID, ubicacionGPS.getLatitud(), ubicacionGPS.getLongitud(), ubicacionGPS.getFechaPosicion()));

                    // Guardamos localmente
                    this.guardarUbicacionGPS(ubicacionGPS);
                    this.sendLocationsIfNeeded(shouldSend);
                } else {
                    Log.d(LOG_TAG, "Cannot determine Location");
                }
            }
        } catch (SecurityException e) {
            Log.d(LOG_TAG, "No suitable permission is present for the provider:", e);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "Provider is null or doesn't exist:", e);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error : ", e);
        }
    }

    // MARK: - Internal

    private boolean gpsLocationShouldBeActive() {
        boolean batteryMinimumLevel = this.lastBatteryLevel > umbralBateria;
        return batteryMinimumLevel && checkLocaitonPermission() && deviceShouldEnableLocation();
    }

    private boolean checkLocaitonPermission() {
        boolean fineLocationEabled = ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean coarseLocationEabled = ActivityCompat.checkSelfPermission(application, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        return fineLocationEabled || coarseLocationEabled;
    }

    private boolean deviceShouldEnableLocation() {
        DispositivoMovil dispositivoMovil = this.application.getDispositivoMovil();
        if (dispositivoMovil != null) {
            return dispositivoMovil.getActivarGPS();
        } else {
            return false;
        }
    }

    /**
     * Start Timer Location
     */
    private void startGPSLocationTimerIfPossible(Activity activity) {
        Log.d(LOG_TAG, "startGPSLocation: enter");

        // si periodo es cero no se lanza el timer
        if (periodo == TIMER_DESHABILITADO) {
            return;
        }

        this.checkLocationServiceAndStartGPSLocationTimer(activity);
    }

    private void checkLocationServiceAndStartGPSLocationTimer(Activity activity) {
        if (gpsLocationSystemIsEnabled()) {
            this.startGPSLocationTimer();
        } else {
            this.showLocationEnablingAlert(activity);
        }
    }

    private boolean gpsLocationSystemIsEnabled() {
        try {
            LocationManager locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);
            Log.d(LOG_TAG, "isasdasdasd "+ locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            return false;
        }
    }

    private void showLocationEnablingAlert(Activity activity) {
        if (activity == null) {
            return;
        }

        String message = "El servicio de localización está desactivado. Para poder usar la aplicación debe estar activado.\nSe abrirán las configuraciones de localización.";

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setMessage(message);
        alertBuilder.setPositiveButton("Entiendo, abrir ajustes", (dialog, id) -> {
            activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            activity.finish();
        });
        alertBuilder.create().show();
    }

    private void startGPSLocationTimer() {
        // inicializamos el locationManager
        this.locationManager = (LocationManager) application.getSystemService(Context.LOCATION_SERVICE);

        this.startLocationListenerIfPossible();

        this.getGPSLocation(true);

        this.timerGPS = new Timer();
        this.timerGPS.schedule(new SendLocationTask(application), 0, periodo);
    }

    @SuppressLint("MissingPermission")
    private void startLocationListenerIfPossible() {
        long timeInterval = periodo / 2;

        this.gpsListener = new GpsListener();
        this.locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, timeInterval, 0, gpsListener);
    }

    // MARK: Guardado y envío de ubicaciones

    private void guardarUbicacionGPS(UbicacionGPS ubicacionGPS) {
        Log.d(LOG_TAG, "guardarUbicacionGPS : enter");
        try {
            this.deleteLocationOverflow();
            udaaDao.createUbicacionGPS(ubicacionGPS);
        } catch (Exception e) {
            Log.d(LOG_TAG,"Error : ",e);
        }
    }

    private void deleteLocationOverflow() {
        long gpsLocationsCount = udaaDao.getCountGPSLocation();
        Log.d(LOG_TAG, "gpsLocationsCount in DB: " + gpsLocationsCount);

        long itemsToRemove = gpsLocationsCount - numMaxPosTrans + 1;
        while (itemsToRemove > 0) {
            long chunck = Math.min(itemsToRemove, 100);
            udaaDao.deleteLastGPSLocations(chunck);
            itemsToRemove -= chunck;
        }
    }

    private int getPosTypeFromLocation(Location location) {
        boolean isGPSProvider = location.getProvider().equals(LocationManager.GPS_PROVIDER);
        return isGPSProvider ? TIPO_POS_GPS : TIPO_POS_NET;
    }

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocation() {
        boolean noHasLocGPS = false;
        Location location = null;

        if (this.tipoPos == TIPO_POS_GPS) {
            Log.i(LOG_TAG, "Utilizando proveedor de GPS");
            String provider = LocationManager.GPS_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);
            noHasLocGPS = location == null;
            Log.d(LOG_TAG,"noHasLocGPS: " + noHasLocGPS);
        }
        // si tipoPos es por RED o por si fue por GPS y no obtuvimos Ubicacion
        if (this.tipoPos == TIPO_POS_NET || noHasLocGPS) {
            Log.i(LOG_TAG, "Utilizando proveedor de RED");
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return location;
    }

    private void sendLocationsIfNeeded(boolean shouldSend) throws UDAAException {
        if (!shouldSend) {
            return;
        }

        WebServiceDAO webServiceDAO = WebServiceDAO.getInstance(application);

        // intentamos enviar las pendientes
        List<UbicacionGPS> listUbicacionGPS = udaaDao.getGPSLocations();
        for (UbicacionGPS ubicacionGPS : listUbicacionGPS) {
            boolean response = webServiceDAO.sendLocationGPS(ubicacionGPS);
            Log.d(LOG_TAG, "Sending location " + ubicacionGPS.getId() + " - status " + response);
            if (response) {
                Log.d(LOG_TAG, "Deleting " + ubicacionGPS.getId());
                udaaDao.deleteLocationGPS(ubicacionGPS);
            }
        }
    }

    private boolean verifyActiveProviders() {
        if (tipoPos == TIPO_POS_GPS) {
            // verificamos si esta activo el GPS_provider
            return gpsProviderIsActive();
        }
        if (tipoPos == TIPO_POS_NET) {
            // verificamos si esta activo el GPS_provider
            return netProviderIsActve();
        }
        return false;
    }

    private boolean gpsProviderIsActive() {
        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error in: ", e);
            return false;
        }
    }

    private boolean netProviderIsActve() {
        try {
            return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error in: ", e);
            return false;
        }
    }

    private Date formatLocTime(long dateInMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateInMillis);
        return calendar.getTime();
    }

}
