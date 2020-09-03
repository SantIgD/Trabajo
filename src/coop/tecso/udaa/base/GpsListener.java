package coop.tecso.udaa.base;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public final class GpsListener implements LocationListener {
	
	private static final String LOG_TAG = GpsListener.class.getSimpleName();
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(LOG_TAG, "onStatusChanged: ");
	}
	@Override
	public void onProviderEnabled(String provider) {
		Log.d(LOG_TAG, "onProviderEnabled: ");
	}
	@Override
	public void onProviderDisabled(String provider) {
		Log.d(LOG_TAG, "onProviderDisabled: ");
	}
	@Override
	public void onLocationChanged(Location location) {
		Log.d(LOG_TAG, "onLocationChanged: " + location.getLatitude() + " - " + location.getLongitude());
	}
}
