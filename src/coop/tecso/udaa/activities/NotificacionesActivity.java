package coop.tecso.udaa.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

import coop.tecso.udaa.R;
import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.common.GUIHelper;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionBinarioVersion;
import coop.tecso.udaa.domain.notificaciones.Notificacion;
import coop.tecso.udaa.persistence.DatabaseHelper;
import coop.tecso.udaa.tasks.InitializeAplTask;
import coop.tecso.udaa.utils.Constants;
import coop.tecso.udaa.ui.NotificacionArrayAdapter;
import coop.tecso.udaa.utils.ParamHelper;
import coop.tecso.udaa.utils.SessionStore;

/**
 * Listado de Notificaciones.
 * 
 *  @author Tecso Coop. Ltda.
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public final class NotificacionesActivity extends OrmLiteBaseListActivity<DatabaseHelper> {

	private static final String LOG_TAG = NotificacionesActivity.class.getSimpleName();

	public static final String KEY_UPDATE = "KEY_UPDATE"; 
	public static final String KEY_ERROR_SYNC = "KEY_ERROR_SYNC";
	
	private UdaaApplication appState;

	private NotificacionArrayAdapter adapter;

	// MARK: - Life Cycle

	@Override
	public void onCreate(Bundle icicle) {
		Log.d(LOG_TAG, "onCreate init...");		
		super.onCreate(icicle);

		this.appState = (UdaaApplication) getApplicationContext();



		SessionStore.restoreSessionIfPossible(this);

		Log.d(LOG_TAG, "Validating permissions...");
		if (appState.getCurrentUser() == null) {
			openLoginActivity();
			finish();
			return;
		}
		// Si se encuentra logeado se completa el area de links a aplicaciones
		setContentView(R.layout.notificaciones);
		Log.d(LOG_TAG, "Building Apl Links...");
		buildAplLinksArea();
		Bundle bundle = getIntent().getExtras();

		boolean updateMessage = false;
		boolean errorSyncMessage = false;
		if (bundle != null) {
			updateMessage = bundle.getBoolean(KEY_UPDATE);
			errorSyncMessage = bundle.getBoolean(KEY_ERROR_SYNC);
		}

		if (errorSyncMessage) {
			String text = ParamHelper.getString(ParamHelper.MENSAJE_ERROR_SYNC, getString(R.string.mensaje_error_sync));
			TextView view = this.findViewById(R.id.mensaje_error_sync);
			view.setVisibility(View.VISIBLE);
			view.setText(text);
		}
		if (updateMessage && appState.getUpgradeIntent() != null) {
			String text = ParamHelper.getString("updateAvailable", "");
            TextView view = this.findViewById(R.id.notificacion_app_update);
            view.setVisibility(View.VISIBLE);
			view.setText(text);
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
		// Fuerza limpieza de popup de loqueo
		onSearchRequested();

		Log.d(LOG_TAG, "Building ListView...");
		new NotificationTask().execute();	
	}

    // ------------------------------------------------------
    // Main menu 
    // ------------------------------------------------------

    // Menu builder
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.settings_menu, menu);
    	return true;
    }

    // Handle click on Notifiacion item.
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

    	// Show the detail
    	Notificacion notificacion = adapter.getItem(position);

    	final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(notificacion.getDescripcionReducida());
		alertDialog.setMessage(notificacion.getDescripcionAmpliada());
		alertDialog.setCancelable(false);
		alertDialog.setIcon(android.R.drawable.ic_menu_info_details);
		alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.accept), (dialog, id1) -> alertDialog.dismiss());
		alertDialog.show();
    	
    	super.onListItemClick(l, v, position, id);
    }
    
	// Reaction to the menu selection
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.exit:			
			confirmAndCloseApplication();
			break;
		case R.id.configuration:
			doChangeSettings();
			break;
		case R.id.sync:
			this.resyncData();
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}    
	
	// MARK: - Interface / Internal

	public void doChangeSettings() {
		Log.d(LOG_TAG, "doChangeSettings init... ");
		Intent intent = new Intent(this, SettingsActivity.class);
		intent.putExtra("ISEDIT", false);
		intent.putExtra("ISREADONLY", true);
		startActivity(intent);
	}

	private void confirmAndCloseApplication() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirm_title);
		builder.setMessage(R.string.exit_confirm_msg);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.yes, (dialog, id) -> doCloseSessionAction());
		builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());

		builder.create().show();
	}

	public void doCloseSessionAction() {
    	Log.d(LOG_TAG, "doCloseAction init... ");
		((UdaaApplication) getApplication()).reset();
		
		// Enviar mensaje de cierre del manager
		Intent msg = new Intent();
		msg.setAction(Constants.ACTION_LOSE_SESSION);
		this.sendBroadcast(msg);
		
		// Close current activity
		finish();
		
		// Stop GPS Location Timer
		appState.stopGPSLocationTimer();
    }

    private void resyncData() {
		InitializeAplTask task = new InitializeAplTask(this);
		task.ignoreAppUpdates = true;
		task.dialogTitle = "Sincronizando";
		task.execute();
	}

	private boolean canAccess() {
		Log.d(LOG_TAG, "Validate user");


		if (appState.getCurrentUser() != null) {
			return true;
		}



        return false;
	}

	private void openLoginActivity(){

		Log.d(LOG_TAG, "Ask for authentication...");
		// Start the Login Activity
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}
	
	/**
	 * Se cargan los links a aplicaciones habilitadas en el perfil del usuario
	 */
	private void buildAplLinksArea() {
		final Context context = this; 
		UDAADao udaaDao = new UDAADao(this);
		
		List<Aplicacion> listAplicacion = udaaDao.getListAplicacionByUsuario(appState.getCurrentUser());
		if (listAplicacion != null && listAplicacion.size() > 0) {
			for (Aplicacion aplicacion : listAplicacion) {
				if (aplicacion == null) {
				    continue;
                }
					
				// Para cada aplicacion se crea link de acceso
				LayoutInflater inflater = this.getLayoutInflater();
				View aplLinkView = inflater.inflate(R.layout.apl_link, null);
				TextView  aplCode  = aplLinkView.findViewById(R.id.apl_code);
				aplCode.setText(aplicacion.getCodigo());
				TextView  aplDescription  = aplLinkView.findViewById(R.id.apl_description);
				aplDescription.setText(aplicacion.getDescripcion());
				
				final String aplPkg =  aplicacion.getPkg()!=null?aplicacion.getPkg():"";  
				final String aplClass =  aplicacion.getClassName()!=null?aplicacion.getClassName():""; 
				final String codigoApp = aplicacion.getCodigo();
				aplLinkView.setOnClickListener(v -> {

                    // Custom Application Launcher
					Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setComponent(new ComponentName(aplPkg, aplClass));

                    if (GUIHelper.canHandleIntent(appState, intent)) {
                        appState.startActivity(intent);
                        return;
                    }

                    //Valido que la version instalada de la UDAA coincida con la ultima version registrada en la tabla
                    UDAADao udaaDao2  = new UDAADao(context);
                    AplicacionBinarioVersion aplBinVersion = null;
                    try {
                        aplBinVersion = udaaDao2.getCoreAplicacionBinarioVersionByCodigoAplicacion(codigoApp);
                    } catch (SQLException ignore) {}

                    if (aplBinVersion != null) {
                        File outputFile;
                        String fileName = aplBinVersion.getUbicacion().substring(aplBinVersion.getUbicacion().lastIndexOf(File.separator) + 1).trim();
                        String path = Environment.getExternalStorageDirectory() + "/download/";
                        File file = new File(path);
                        file.mkdirs();
                        outputFile = new File(path, fileName);
                        if (outputFile.exists()) {
                        	String authority = "com.fantommers.udaa.fileprovider";
                            Uri data = FileProvider.getUriForFile(NotificacionesActivity.this, authority, outputFile);

                            final Intent installIntent = new Intent(Intent.ACTION_VIEW);
                            installIntent.setDataAndType(data, "application/vnd.android.package-archive");
                            installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(context);
                            confirmDialogBuilder.setTitle(R.string.confirm_title);
                            confirmDialogBuilder.setMessage(ParamHelper.getString("InstallApplication", getString(R.string.install_application)));
                            confirmDialogBuilder.setCancelable(false);
                            confirmDialogBuilder.setPositiveButton(R.string.yes, (dialog, id) -> NotificacionesActivity.this.startActivityForResult(installIntent, Constants.REQUEST_UPGRADE_APP));
                            confirmDialogBuilder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
                            confirmDialogBuilder.create().show();
                        }
                    }
                    else {
                        // Acceso a Apl incorrecto
                        String err = "No se puede ejecutar la aplicación. \nPor favor, contactese con el administrador.";
                        final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle("Error al ejecutar Aplicación");
                        alertDialog.setMessage(err);
                        alertDialog.setCancelable(false);
                        alertDialog.setIcon(R.drawable.ic_error_default);
                        alertDialog.setButton(Dialog.BUTTON_POSITIVE, appState.getString(R.string.accept), (dialog, id) -> alertDialog.dismiss());
                        alertDialog.show();
                    }
				});
				LinearLayout listAplLinksView = this.findViewById(R.id.listAplLink);
				listAplLinksView.addView(aplLinkView);
				// Separador de links
				View line = new View(this);
				line.setBackgroundColor(this.getResources().getColor(R.color.apl_link_line_color));
				listAplLinksView.addView(line, new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.MATCH_PARENT));
			}
		} else {
			// Mensaje de lista de links vacia
			LinearLayout emptyLayout = new LinearLayout(this);
			emptyLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
			TextView msg = new TextView(this);
			msg.setTextColor(this.getResources().getColor(R.color.white));
			msg.setText(this.getString(R.string.apl_links_empty));
			msg.setTextSize(10);
			msg.setGravity(Gravity.CENTER);
			emptyLayout.addView(msg);
			LinearLayout listAplLinksView = this.findViewById(R.id.listAplLink);
			listAplLinksView.addView(emptyLayout);
		}
	}

    @SuppressLint("StaticFieldLeak")
	private class NotificationTask extends AsyncTask<Void, CharSequence, List<Notificacion>> {

        private ProgressDialog mDialog;
        private UDAADao udaaDao;

        @Override
        protected void onPreExecute() {
            udaaDao = new UDAADao(NotificacionesActivity.this);
            mDialog = GUIHelper.getIndetProgressDialog(NotificacionesActivity.this,
                    "",getString(R.string.loading_msg));
        }

        @Override
        protected List<Notificacion> doInBackground(Void... arg0) {
            try {
                // Prepare the listView
                return udaaDao.getListNotificacion();
            } catch(Exception e) {
                Log.e(LOG_TAG, "Error iniciando sesion", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Notificacion> result) {
            if (mDialog != null) {
                mDialog.dismiss();
            }

            UdaaApplication app = (UdaaApplication) getApplicationContext();

            if (result == null)  {
                GUIHelper.showError(NotificacionesActivity.this, "Error al obtener notificaciones");
                finish();
            } else {
                setTitle(app.getFormattedTitle());
                adapter = new NotificacionArrayAdapter(getApplicationContext(), R.layout.list_item_notificacion, result);
                setListAdapter(adapter);
            }
        }
    }

}