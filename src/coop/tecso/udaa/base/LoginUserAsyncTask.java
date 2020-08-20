package coop.tecso.udaa.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import coop.tecso.udaa.R;
import coop.tecso.udaa.common.GUIHelper;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.utils.Constants;

/**
 * 
 * @author leonardo.fagnano
 *
 */
public final class LoginUserAsyncTask extends AsyncTask<String, CharSequence, String>{
	
	private static final String TAG = LoginUserAsyncTask.class.getSimpleName();
	
	private UdaaApplication appState;
	private ProgressDialog progressDialog;
	private Activity activity;
	private UDAAManager manager;
	
	public LoginUserAsyncTask(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected void onPreExecute() {
		this.appState = (UdaaApplication) activity.getApplicationContext();
		this.progressDialog = ProgressDialog.show(activity, "", "Validando", true);
		this.manager = new UDAAManager(activity);
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			publishProgress("Validando Usuario…");
			Thread.sleep(1000);
			String userName = params[0];
			String passWord = params[1];
			
			UsuarioApm userToValidate = manager.getUsuarioApm(userName, passWord);
			
			// Invalid data
			if(null == userToValidate){
				return appState.getString(R.string.login_user_or_pass_error);
			}
			// Same User in Session
			if(userToValidate.getId() == appState.getCurrentUser().getId()){
				return "";
			}
			
			appState.changeSession(userName);
			return appState.getString(R.string.login_lost_session);
			
		} catch (Exception e) {
			Log.e(TAG, "doInBackground: ERROR", e);
			return "Error Inesperado al realizar login";
		}
	}

	@Override
	protected void onProgressUpdate(CharSequence... values) {
		progressDialog.setMessage(values[0]);
	}

	@Override
	protected void onPostExecute(String error) {
		progressDialog.dismiss();
		if(TextUtils.isEmpty(error)){
			// Send UnBlock Application Message
			Intent msg = new Intent();
			msg.setAction(Constants.ACTION_UNBLOCK_APPLICATION);
			appState.sendBroadcast(msg);
		} else{
			GUIHelper.showError(activity, error);
			
			if(appState.getCurrentUser() == null){
				activity.finish();
			}
		}
	}
}
