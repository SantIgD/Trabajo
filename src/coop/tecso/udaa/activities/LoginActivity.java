package coop.tecso.udaa.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import coop.tecso.udaa.R;
import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.common.GUIHelper;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.persistence.DatabaseHelper;
import coop.tecso.udaa.persistence.TxtExporter;
import coop.tecso.udaa.tasks.ChangePassAsyncTask;
import coop.tecso.udaa.tasks.LoginTask;
import coop.tecso.udaa.ui.utils.ButtonEnabler;
import coop.tecso.udaa.utils.Constants;
import coop.tecso.udaa.utils.ParamHelper;

/**
 * Login Activity.
 */

public final class LoginActivity extends Activity {

	private static final String LOG_TAG = LoginActivity.class.getSimpleName();

	private UdaaApplication appState;

	// Views
	private LinearLayout mLoginModeView;
	private EditText mUserIDTextView;
	private EditText mPasswordTextView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(LOG_TAG, "onCreate");
		super.onCreate(savedInstanceState);

		// Setting content View from XML
		setContentView(R.layout.login);

		// Load views from the XML definition
		mUserIDTextView = findViewById(R.id.id_edit);
		mPasswordTextView = findViewById(R.id.password_edit);
		Button mLoginButton = findViewById(R.id.login_button);
		mLoginModeView = findViewById(R.id.login_mode);

		// Set up the button enabler
		ButtonEnabler.register(mLoginButton, mUserIDTextView, mPasswordTextView);
	}

	@Override
	protected void onResume() {
		Log.i(LOG_TAG, "onResume");
		super.onResume();

		this.appState = (UdaaApplication) getApplication();
		setTitle(appState.getTitle());

		// Se verifica si corresponde un cambio de session
		if (appState.isChangeSession()) {
			mUserIDTextView.setText(appState.getUserNameForNewSession());
		}

		// Si se perdio la session se vuelve a validar el modo de login
		if (appState.getCurrentUser() == null) {
			UDAADao udaaDao = new UDAADao(this);
			List<UsuarioApm> listUsuarioApm = udaaDao.getListUsuarioApm();
			if (listUsuarioApm.isEmpty()) {
				// Realizar login remoto
				appState.setLocalLogin(false);
			} else {
				// Realizar login local
				appState.setLocalLogin(true);
			}
		}
		this.refreshLoginIndicator();

		if (appState.isWaitingInstallation()) {
			//Se cancelo la actualizacion 
			appState.setWaitingInstallation(false);

			if (appState.isUpgradeRequired()) {
				//Se canceló la actualización obligatoria
				String errorMsg = ParamHelper.getString("updateRequired", getString(R.string.update_required));
				GUIHelper.showError(this, errorMsg);
				finish();
			}
		}
	}

	// ------------------------------------------------------
	// Main menu 
	// ------------------------------------------------------

	// Menu builder
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.login_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	// Reaction to the menu selection
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.settings:
				doChangeSettings();
				break;
			case R.string.export:
				confirmExportFile();
				break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_UPGRADE_APP && resultCode != RESULT_OK) {
            LoginActivity.this.finish();
            System.runFinalization();
            System.exit(0);
        }
    }

    // MARK: - Interface

    public void refreshLoginIndicator() {
        if (appState.isLocalLogin()) {
            mLoginModeView.setBackgroundColor(this.getResources().getColor(R.color.local_login_color));
        } else {
            mLoginModeView.setBackgroundColor(this.getResources().getColor(R.color.remote_login_color));
        }
    }

	/**
	 * Construye un dialog para el seteo de password
	 */
	public Dialog buildDialogForSetPassword(String userName) {
		final Dialog dialog = new Dialog(LoginActivity.this);
		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dialog.setContentView(R.layout.set_password_dialog);
		dialog.setTitle(LoginActivity.this.getString(R.string.set_password_title));
		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_login_mode);
		dialog.setCancelable(true);

		// Nombre de Usuario
		final TextView userNameView = dialog.findViewById(R.id.username_box);
		userNameView.setText(userName);

		// Nuevo Password y su confirmacion
		final EditText passwordView = dialog.findViewById(R.id.password_box);
		final EditText confirmPasswordView = dialog.findViewById(R.id.confirm_password_box);

		// Cancel button
		Button buttonCancel = dialog.findViewById(R.id.set_password_button_cancel);
		buttonCancel.setOnClickListener(v -> dialog.dismiss());

		// Confirm button
		Button buttonConfirm = dialog.findViewById(R.id.set_password_button_confirm);
		buttonConfirm.setOnClickListener(v -> {
            String username = userNameView.getText().toString();
            String password = passwordView.getText().toString();
            String repasswd = confirmPasswordView.getText().toString();

            // envio de datos al srv via asynk task
            new ChangePassAsyncTask(this).execute(username, password, repasswd);
            dialog.dismiss();
        });
		ButtonEnabler.register(buttonConfirm, passwordView, confirmPasswordView);

		return dialog;
	}

    // MARK: - Internal

    // ------------------------------------------------------
    // Event handlers
    // ------------------------------------------------------

    private void confirmExportFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm_title);
        builder.setMessage(R.string.export_confirm_msg);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, id) -> doExportFile());
        builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    @SuppressLint("SimpleDateFormat")
    protected void doExportFile() {
        try {
            File root = Environment.getExternalStorageDirectory();

            // check sdcard permission
            if (root.canWrite()) {
                DatabaseHelper databaseHelper = new DatabaseHelper(this);
                Calendar calendar = Calendar.getInstance();
                TxtExporter txtExporter = new TxtExporter(databaseHelper.getWritableDatabase());
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                txtExporter.export("udaa.db", dateFormat.format(calendar.getTime()));

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.export);
                builder.setMessage(R.string.export_ok);
                builder.setCancelable(true);
                builder.setNegativeButton(R.string.ok, (dialog, id) -> dialog.cancel());

                AlertDialog alert = builder.create();
                alert.show();
            }
        } catch (IOException e) {
            Log.e("ERROR:---", "No se pudo escribir SDCard" + e.getMessage());

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.error);
            builder.setMessage(R.string.error_export);
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.ok, (dialog, id) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    public void doChangeSettings() {
        Log.d(LOG_TAG, "doChangeSettings init... ");
        Intent i = new Intent(this, SettingsActivity.class);
        i.putExtra("ISEDIT", true);
        startActivity(i);
    }

    public void DoLoginAction(View v) {
        Log.d(LOG_TAG, "DoLoginAction init... ");

        // Validate required fields
        String userID = mUserIDTextView.getText().toString();
        String password = mPasswordTextView.getText().toString();

        boolean hasErrors = false;
        if (TextUtils.isEmpty(userID)) {
            mUserIDTextView.setError("Debe ingresar su identificador de usuario");
            hasErrors = true;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordTextView.setError("Debe ingresar su contraseña");
            hasErrors = true;
        }

        // Realizo flujo de Login
        if (!hasErrors) {
            new LoginTask(this, userID, password).execute();
        }

        // Se limpia el campo de password
        mPasswordTextView.setText("");
    }

}