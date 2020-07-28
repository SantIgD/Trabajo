package coop.tecso.hcd.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.ErrorAtencion;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ErrorConstants;

/**
 * 
 * @author tecso.coop
 *
 */
public final class SignActivity extends Activity {

	private static final String LOG_TAG = SignActivity.class.getSimpleName();
	
	private static final float LENGTH_THRESHOLD = 120.0f;
	private static final long  FADE_OFFSET = 10000L; //10 seg
	
	private GestureOverlayView overlay;
	private View mConfirmButton;
	private boolean hasInitGesture = false;
	private Gesture finalGesture;
	private String gestureID;
	private HCDigitalApplication appState;
	private boolean isSingAclaration = false;
	private boolean isValid = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		appState = (HCDigitalApplication) getApplicationContext();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.crear_firma);
        mConfirmButton = findViewById(R.id.confirm);

        overlay = findViewById(R.id.gestures_overlay);
        overlay.setFadeOffset(FADE_OFFSET);
        overlay.addOnGestureListener(new GesturesProcessor());

        isValid = getIntent().getBooleanExtra(Constants.GESTURE_VALID, true);
        //
        gestureID =	getIntent().getStringExtra(Constants.GESTURE_ID);

        List<Gesture> gestures = appState.getGestureStore().getGestures(gestureID);
        finalGesture = getFinalGesture(gestureID, appState.getGestureStore(), gestures);

        if(finalGesture != null){
            overlay.post(() -> overlay.setGesture(finalGesture));
            overlay.setEnabled(false);
            hasInitGesture = true;
            if (finalGesture.getBoundingBox().bottom >finalGesture.getBoundingBox().right) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            isSingAclaration = bundle.getBoolean(Constants.SIGN_ACLARATION);
        }

        // Title of activity
        String gestureTitle = getIntent().getStringExtra(Constants.GESTURE_TITLE);
        if (!TextUtils.isEmpty(gestureTitle)) {
            setTitle(gestureTitle);
        }
	}

	private Gesture getFinalGesture(String key, GestureLibrary gestureStore, List<Gesture> defaultGestures){
		String temporalKey = key + Constants.TEMPORAL_GESTURE_SUFFIX;
		ArrayList<Gesture> temporalGestureList = gestureStore.getGestures(temporalKey);
		return (temporalGestureList != null ? temporalGestureList.get(0) : (isValid && defaultGestures != null) ? defaultGestures.get(0) : null);
	}
	
	/**
	 * Add gesture to previous activity
	 */
	public void addGesture(View v) {
		Log.i(LOG_TAG, "addGesture : enter");

		String errorCode = ErrorConstants.ALERTA_SIN_FIRMA;
		if (overlay != null && overlay.getGesture() != null) {
			errorCode = ErrorConstants.ALERTA_NUEVA_FIRMA;
		}

		ErrorAtencion alerta = appState.getHCDigitalDAO().findErrorAtencionByCode(errorCode);
		int icono = 0;
		if(alerta != null && !TextUtils.isEmpty(alerta.getIconFileName()))
			icono = this.getResources().getIdentifier(alerta.getIconFileName(), "drawable", this.getPackageName());
		if(icono == 0) icono = R.drawable.ic_dialog_alert;
		
		int sound = 0;
		if(alerta != null && !TextUtils.isEmpty(alerta.getSoundFileName()))
			sound = this.getResources().getIdentifier(alerta.getSoundFileName(), "raw", this.getPackageName());
		if(sound == 0) sound = R.raw.heaven;
		
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		if (alerta != null) {
			alertDialog.setTitle(alerta.getDescripcionCorta());
			alertDialog.setMessage(alerta.getDescripcionLarga());
		} else {
			alertDialog.setTitle(errorCode);
			alertDialog.setMessage(errorCode);
		}
		alertDialog.setCancelable(false);
		alertDialog.setIcon(icono);
		alertDialog.setPositiveButton(R.string.yes, (dialog, id) -> {
            if (overlay != null && overlay.getGesture() != null) {
                String temporalGestureID = gestureID + Constants.TEMPORAL_GESTURE_SUFFIX;
                appState.getGestureStore().removeEntry(temporalGestureID);
                appState.getGestureStore().addGesture(temporalGestureID, overlay.getGesture());
                Intent resultIntent = new Intent();

                if(isSingAclaration)
                    resultIntent.putExtra(Constants.GESTURE_SIGNAC, overlay.getGesture());
                else
                    resultIntent.putExtra(Constants.GESTURE_SIGN, overlay.getGesture());

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
            else {
                String temporalGestureID = gestureID + Constants.TEMPORAL_GESTURE_SUFFIX;
                appState.getGestureStore().removeEntry(temporalGestureID);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("mock", 1);
                setResult(RESULT_OK, resultIntent);

                finish();
            }
        });
        alertDialog.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
		alertDialog.show();
		
		MediaPlayer.create(this, sound).start();
	}

	/**
	 * Cancel gesture and back to previous activity
	 */
	public void cancelGesture(View v) {
		setResult(RESULT_CANCELED);
		finish();
	}

	/**
	 * Clear current gesture
	 */
	public void clearGesture(View v) {
		hasInitGesture = false;
		
		overlay.clear(false);
		mConfirmButton.setEnabled(true);
        overlay.setEnabled(true);
	}

	
	/**
	 * 
	 * @author tecso.coop
	 *
	 */
	private class GesturesProcessor implements	GestureOverlayView.OnGestureListener {

		public void onGestureStarted(final GestureOverlayView overlay, MotionEvent event) {
			if(hasInitGesture){
	            overlay.post(() -> overlay.setGesture(finalGesture));
	            hasInitGesture = false;
			}
		}
		
		public void onGesture(GestureOverlayView overlay, MotionEvent event) {}

		public void onGestureEnded(GestureOverlayView overlay, MotionEvent event) {
			if (overlay.getGesture().getLength() < LENGTH_THRESHOLD) {
				overlay.clear(false);
			}
			mConfirmButton.setEnabled(true);
		}

		public void onGestureCancelled(GestureOverlayView overlay,	MotionEvent event) {}

	}
}
