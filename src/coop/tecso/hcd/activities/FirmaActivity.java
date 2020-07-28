package coop.tecso.hcd.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.utils.Constants;

/**
 * 
 * @author tecso.coop
 *
 */
public final class FirmaActivity extends Activity {

	private static final String LOG_TAG = FirmaActivity.class.getSimpleName();
	
	private static final float LENGTH_THRESHOLD = 120.0f;
	private static final long  FADE_OFFSET = 10000L; //10 seg
	
	private GestureOverlayView overlay;
	private View mConfirmButton;
	private boolean hasInitGesture = false;
	private Gesture gesture;
	
	private HCDigitalApplication appState;
	private String tabId;

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

        tabId =	getIntent().getStringExtra(Constants.TAB_TAG);
        String codigoAtencion = getIntent().getStringExtra(Constants.CODIGO_ATENCION);

        hasInitGesture = false;
        overlay.clear(false);
        //
        List<Gesture> gestures = appState.getGestureStore().getGestures(tabId);
        if(null != gestures){
            gesture = gestures.get(0);
            overlay.post(() -> overlay.setGesture(gesture));
            hasInitGesture = true;
            overlay.setEnabled(false);
        }

        if (gesture != null) {
            if (gesture.getBoundingBox().bottom > gesture.getBoundingBox().right) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }

        this.setTitle(codigoAtencion);
	}

	/**
	 * Add gesture to previous activity
	 */
	public void addGesture(View v) {
		Log.i(LOG_TAG, "addGesture : enter");
		
		appState.getGestureStore().removeEntry(tabId);

        if (overlay != null && overlay.getGesture() != null) {
            appState.getGestureStore().addGesture(tabId, overlay.getGesture());
            Intent resultIntent = new Intent();
            resultIntent.putExtra("sign", overlay.getGesture());
            setResult(Activity.RESULT_OK, resultIntent);
        } else {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("mock", 1);
            setResult(RESULT_OK, resultIntent);
        }

        finish();
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
	            overlay.post(() -> overlay.setGesture(gesture));
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
