package coop.tecso.udaa.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import coop.tecso.udaa.R;
import coop.tecso.udaa.common.GUIHelper;
import coop.tecso.udaa.common.WebServiceDAO;
import coop.tecso.udaa.utils.ErrorConstants;

/**
 * Tarea cambio de contraseña
 */
@SuppressLint("StaticFieldLeak")
public class ChangePassAsyncTask extends AsyncTask<String, String, String> {

    // MARK: - Data

    public static final String LOG_TAG = ChangePassAsyncTask.class.getSimpleName();

    private Context context;

    private ProgressDialog dialog;

    // MARK: - Init

    public ChangePassAsyncTask(Context context) {
        this.context = context;
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        String title = context.getString(R.string.actualizando_datos);
        String message = context.getString(R.string.por_favor_aguarde_unos_segundos);
        dialog = GUIHelper.getIndetProgressDialog(context, title, message);
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String userName = params[0];
            String passWord = params[1];
            String passWord2 = params[2];

            return setearPassword(userName, passWord, passWord2);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error cambiando pass", e);
            return "";
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (dialog != null)
            dialog.dismiss();
        if (!result.equals("OK")) {
            GUIHelper.showRecoverableError(context, result);
        } else {
            String message = context.getString(R.string.seteo_de_contraseña_exitoso);
            GUIHelper.showRecoverableError(context, message);
        }
    }

    // MARK: - Internal

    /**
     * Valida y setea el nuevo password accediendo al WS para su seteo remoto.
     */
    private String setearPassword(String userName, String password, String confirmPassword) {
        Log.d(LOG_TAG, "Seteo de Password. Usuario: " + userName + " Password: " + password + " Confirmación: " + confirmPassword);

        // Se valida que la confirmacion del password sea correcta.
        if (!confirmPassword.equals(password)) {
            return ErrorConstants.ERROR_111;
        }

        try {
            WebServiceDAO.getInstance(context);

            // Se llama al servicio de seteo de contraseña
            return WebServiceDAO.setPassword(userName, password);
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error durante seteo de password: ", e);
            return ErrorConstants.ERROR_110;
        }
    }

}
