package coop.tecso.hcd.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

/**
 * 
 * @author leonardo.fagnano
 *
 */
public final class LoginUserAsyncTask extends AsyncTask<String, CharSequence, String>{
	
	private static final String TAG = LoginUserAsyncTask.class.getSimpleName();
	
	private HCDigitalApplication appState;

	private ProgressDialog progressDialog;

	private UDAACoreServiceImpl udaaService;

	@SuppressLint("StaticFieldLeak")
	private Activity activity;
	
	public LoginUserAsyncTask(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		this.appState = (HCDigitalApplication) activity.getApplicationContext();
		this.progressDialog = ProgressDialog.show(activity, "", activity.getString(R.string.saving_msg), true);
		this.udaaService = new UDAACoreServiceImpl(activity);
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			publishProgress("Validando Usuario…");
			Thread.sleep(1000);
			String userName = params[0];
			String passWord = params[1];

			// User and Password validation
			UsuarioApm userToValidate = udaaService.login(userName, passWord);
			
			// Invalid data
			if(null == userToValidate){
				return appState.getString(R.string.login_user_or_pass_error);
			}
			// Same User in Session
			if(userToValidate.getId() == appState.getCurrentUser().getId()){
				return "";
			}
			if(!udaaService.hasAccess(userToValidate.getId(), Constants.COD_HCDIGITAL)){
				// Validate User Permission
				return appState.getString(R.string.login_user_permission_error);
			}
			// Notify Change Session to UDAA
			udaaService.changeSession(userName, passWord);
			
			return "";
		} catch (Exception e) {
			Log.e(TAG, "doInBackground: ERROR", e);
			return "Error Inesperado al intentar login";
		}
	}

	@Override
	protected void onProgressUpdate(CharSequence... values) {
		progressDialog.setMessage(values[0]);
	}

	@Override
	protected void onPostExecute(String error) {
		progressDialog.dismiss();
		if (TextUtils.isEmpty(error)) {
			// Send UnBlock Application Message
			Intent msg = new Intent();
			msg.setAction(Constants.ACTION_UNBLOCK_APPLICATION);
			appState.sendBroadcast(msg);
		} else {
			GUIHelper.showError(activity, error);
		}
	}
}
