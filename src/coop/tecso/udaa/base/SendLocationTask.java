package coop.tecso.udaa.base;

import java.util.TimerTask;

import android.content.Context;
import android.util.Log;

/**
 *
 * @author tecso.coop
 *
 */
public final class SendLocationTask extends TimerTask {

    private static final String LOG_TAG = SendLocationTask.class.getSimpleName();

    // MARK: - Data

    private UdaaApplication appState;

	public SendLocationTask(Context context) {
        this.appState = (UdaaApplication) context.getApplicationContext();
	}

	@Override
	public void run() {
		try {
			Log.i(LOG_TAG, "SendLocationTask : run");
			appState.reportGPSLocation(true);
			
		} catch (Exception e) {
			Log.i(LOG_TAG, "Failure to execute web service process:", e);
		}
	}

}
