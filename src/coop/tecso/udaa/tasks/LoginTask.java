package coop.tecso.udaa.tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import coop.tecso.udaa.R;
import coop.tecso.udaa.activities.LoginActivity;
import coop.tecso.udaa.base.UDAAException;
import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.common.GUIHelper;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.common.WebServiceDAO;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.domain.util.DeviceContext;
import coop.tecso.udaa.utils.Constants;
import coop.tecso.udaa.utils.ErrorConstants;
import coop.tecso.udaa.utils.LogUtils;
import coop.tecso.udaa.utils.ParamHelper;
import coop.tecso.udaa.utils.RemoteLoginPerformer;

/**
 * Realiza el Login (Local o Remoto)
 */
@SuppressLint("StaticFieldLeak")
public class LoginTask extends AsyncTask<Void, CharSequence, String> {

    // MARK: - Data

    private static final String LOG_TAG = LoginTask.class.getSimpleName();

    private ProgressDialog mDialog;
    private String user;
    private String password;

    private LoginActivity loginActivity;
    private UDAADao udaaDao;
    private UdaaApplication udaaApplication;

    private LogUtils logUtils;

    // MARK: - Init

    public LoginTask(LoginActivity loginActivity, String userID, String password) {
        this.loginActivity = loginActivity;
        this.user = userID;
        this.password = password;
        this.logUtils = new LogUtils(loginActivity);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        udaaDao = new UDAADao(loginActivity);

        logUtils.generateLoginLog("Login -> Start");
        logUtils.generateLoginLog("Login -> PreExecute Start");

        udaaApplication = (UdaaApplication) loginActivity.getApplication();
        WebServiceDAO.getInstance(loginActivity);
        // Show status message
        mDialog = GUIHelper.getIndetProgressDialog(loginActivity, "Iniciando sesión", "Por favor, aguarde unos segundos…");
        if (mDialog != null) {
            mDialog.setCancelable(false);
        }

        logUtils.generateLoginLog("Login -> PreExecute End");
    }

    @Override
    protected String doInBackground(Void... arg0) {
        try {
            // Se valida el tipo de login (local o remoto)
            // Login Local
            if (false) { //udaaApplication.isLocalLogin()
                Log.d(LOG_TAG, " Comenzando Login Local...");
                this.publishProgress(" Iniciando sesión local, \n Por favor, aguarde unos segundos...");

                // Se verifica si existe el usuario en la db local
                logUtils.generateLoginLog("Login -> UDAA getUsuarioApmByUserName Start");
                UsuarioApm usuario = udaaDao.getUsuarioApmByUserName(user);
                logUtils.generateLoginLog("Login -> UDAA getUsuarioApmByUserName End");
                if (usuario != null) {
                    Log.d(LOG_TAG, "Usuario '" + user + "' encontrado");
                    // Si existe valido la contraseña
                    if (password.equals(usuario.getPassword())) {
                        Log.d(LOG_TAG, "Password correcto");
                        // Si valida seteo usuario y finaliza login
                        udaaApplication.setCurrentUser(usuario);

                        logUtils.generateLoginLog("Login -> UDAA getDispositivoMovil Start");
                        // Verifico si se bloqueo el dispositivo movil
                        String deviceId = DeviceContext.getEmail(loginActivity);
                        Log.i(LOG_TAG, "Default Google mail account: " + deviceId);
                        DispositivoMovil dispositivoMovil = udaaDao.getDispositivoMovil(deviceId);
                        udaaApplication.setDispositivoMovil(dispositivoMovil);
                        logUtils.generateLoginLog("Login -> UDAA getDispositivoMovil End");

                        return "OK";
                    } else {
                        // Si no pasa validación:
                        // Se aumenta contador de login local erroneo y se valida la cantidad de intentos
                        udaaApplication.addLocalLoginCount();
                        Log.d(LOG_TAG, "Password incorrecto. Cantidad de intentos: " + udaaApplication.getLocalLoginCount());
                        int loginLocalMaxCount;
                        try {
                            loginLocalMaxCount = ParamHelper.getInteger(ParamHelper.COD_LOCAL_LOGIN_MAX_COUNT, 1);
                        } catch (Exception e) {
                            Log.d(LOG_TAG, "Error en valor de parametro '" + ParamHelper.COD_LOCAL_LOGIN_MAX_COUNT);
                            loginLocalMaxCount = 1;
                        }
                        // Si la cantidad de intentos de login local supera el valor seteado se pasa al login remoto
                        if (udaaApplication.getLocalLoginCount() >= loginLocalMaxCount) {
                            Log.d(LOG_TAG, "Cantidad de intentos supera el valor permitido. Se fuerza el login remoto.");
                            // Si no existe el usuario en la db local se continua con un login remoto
                            udaaApplication.setLocalLogin(false);
                        } else {
                            Log.d(LOG_TAG, "Login Local FAIL!!");
                            // Se termina con error
                            return ErrorConstants.ERROR_107;
                        }
                    }
                } else {
                    Log.d(LOG_TAG, "No existe Usuario en db local.. se fuerza el login remoto. ");
                    udaaApplication.setLocalLogin(false);
                }
            }

            // Login Remoto
            if (true) { //!udaaApplication.isLocalLogin()
                logUtils.generateLoginLog("Login -> WebService Remote Login Start");

                Log.d(LOG_TAG, " Comenzando Login Remoto...");
                this.publishProgress(" Iniciando sesión remota, \n Por favor, aguarde unos segundos...");
                // Identifico dispositivo movil
                String defaultGmailAccount = DeviceContext.getEmail(loginActivity);
                Log.i(LOG_TAG, "Default Google mail account: " + defaultGmailAccount);
                logUtils.generateLoginLog("Login -> WebService identifyDM GmailAccount Start");
                DispositivoMovil dispositivoMovil = WebServiceDAO.identifyDM(defaultGmailAccount);
                logUtils.generateLoginLog("Login -> WebService identifyDM GmailAccount End");
                Log.i(LOG_TAG, "Device ID from server: " + dispositivoMovil.getId());

                // Se llama al servicio de login remoto
                logUtils.generateLoginLog("Login -> WebService login Start");
                RemoteLoginPerformer remoteLoginPerformer = new RemoteLoginPerformer(user,password,dispositivoMovil);
                UsuarioApm usuario = remoteLoginPerformer.performLogin();
                logUtils.generateLoginLog("Login -> WebService login End");
                if (usuario == null) {
                    return ErrorConstants.ERROR_103;
                }
                logUtils.generateLoginLog("Login -> UDAA UpdateUsuarioApm DispositivoMovil Start");

                udaaDao.createOrUpdateUsuarioApm(usuario);
                udaaDao.createOrUpdateDispositivoMovil(dispositivoMovil);

                logUtils.generateLoginLog("Login -> UDAA UpdateUsuarioApm DispositivoMovil End");

                //subo propiedades al contexto
                udaaApplication.setCurrentUser(usuario);
                udaaApplication.setDispositivoMovil(dispositivoMovil);

            }

            return "OK";
        } catch (UDAAException error) {
            Log.e(LOG_TAG, "Error iniciando sesion: " + error.getError());
            return error.getError();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error iniciando sesion", e);
            e.printStackTrace();
            return ErrorConstants.ERROR_100;
        }
    }

    @Override
    protected void onProgressUpdate(CharSequence... values) {
        if (mDialog != null)
            mDialog.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if (mDialog != null) {
            mDialog.dismiss();
        }

        // Refresca indicador de tipo de login
        loginActivity.refreshLoginIndicator();
        logUtils.generateLoginLog("Login -> End");

        if (!"OK".equals(result)) {
            GUIHelper.showError(loginActivity, result);
            if (ErrorConstants.ERROR_108.equals(result)) {
                // Dialogo de Cierre IM
                loginActivity.buildDialogForSetPassword(user).show();
            } else if (ErrorConstants.ERROR_103.equals(result)) {
                this.showError103Dialog();
            } else if (ErrorConstants.ERROR_114.equals(result) || ErrorConstants.ERROR_115.equals(result)) {
                GUIHelper.showRecoverableError(loginActivity,result);
            }
        } else {
            // Si el login termino exitosamente se inicia la aplicacion

            // Para que HC actualice el usuario
            Intent msg = new Intent();
            msg.setAction(Constants.ACTION_LOSE_SESSION);
            udaaApplication.sendBroadcast(msg);


            if (udaaApplication.getCurrentUser() != null) {
                // Send UnBlock Application Message
                Intent unlockIntent = new Intent();
                unlockIntent.setAction(Constants.ACTION_UNBLOCK_APPLICATION);
                udaaApplication.sendBroadcast(unlockIntent);

                new InitializeAplTask(loginActivity).execute();
            }
        }
    }

    // MARK: - Internal

    private void showError103Dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(loginActivity);
        builder.setTitle("Error");
        builder.setIcon(R.drawable.ic_error_default);
        builder.setMessage(ErrorConstants.ERROR_103);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, null);
        builder.create().show();
    }

}