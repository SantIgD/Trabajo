package coop.tecso.hcd.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.Despachador;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.helpers.SearchPageAdapter;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;
import coop.tecso.hcd.persistence.DatabaseHelper;
import coop.tecso.hcd.persistence.TxtExporter;
import coop.tecso.hcd.services.HCDigitalManager;
import coop.tecso.hcd.services.SyncAtencionService;
import coop.tecso.hcd.tasks.BuildSearchPageTask;
import coop.tecso.hcd.tasks.CargarListView;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ErrorConstants;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.domain.util.DeviceContext;

@SuppressLint("SimpleDateFormat")
public final class MainHCActivity extends ListActivity {

	private static final String LOG_TAG = MainHCActivity.class.getSimpleName();

	private HCDigitalApplication appState;

	private BroadcastReceiver refreshReceiver;
	private BroadcastReceiver titleReceiver;
	private BroadcastReceiver mMessageReceiver;
	
	private static final int TIPO_REG_ERR_DES = 2;

	public boolean createLogVersion = false;
	public boolean createLogSyncVersion = false;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		this.setContentView(R.layout.main);
		this.appState = HCDigitalApplication.getApplication(this);

		createLogVersion = true;
		createLogSyncVersion = true;
				
		/*
		 *  ---------------------------------------------------------------------------------------------------------------------------
		 *   RECEPCION DE MENSAJES - 
		 *  ---------------------------------------------------------------------------------------------------------------------------
		 */	
		HCDigitalApplication.broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Bundle bundle = intent.getExtras();
				if (bundle == null) {
					return;
				}

				try {
					//----| GET LISTA |---------------------------------------------------------------------
					if (intent.getStringExtra(Constants.REFRESH) == null) {
						return;
					}
					List<Atencion> atencionList = appState.getHCDigitalDAO().getListAtencion();
					if (atencionList != null) {
						SearchPageAdapter adapter = new SearchPageAdapter(MainHCActivity.this, atencionList);
						setListAdapter(adapter);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};		
		//  ---------------------------------------------------------------------------------------------------------------------------			
		try {
			registerReceiver(HCDigitalApplication.broadcastReceiver, new IntentFilter(Constants.ACTION_REFRESH));	
		} catch (Exception e) {
			e.printStackTrace();
		}	

		titleReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				MainHCActivity.this.setTitle(Utils.getFormattedTitle(context));
				MainHCActivity.this.getActionBar().setSubtitle(Utils.getFormattedSubtitled(context));
			}
		};

		this.registerReceiver(titleReceiver, new IntentFilter(Constants.ACTION_TITLE));
		
		refreshReceiver = new BroadcastReceiver() {
			 
			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					new CargarListView(MainHCActivity.this).execute();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};

		this.registerReceiver(refreshReceiver, new IntentFilter(Constants.ACTION_REFRESH_LIST));
		
		mMessageReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
				if (!appState.getHCDigitalDAO().existeIMAbierto()) {
					registerIM(MainHCActivity.this);
				}
	        }
	    };
	    
	    this.registerReceiver(mMessageReceiver, new IntentFilter(Constants.NEW_ATTENTION));

		Button btnMensajeria = findViewById(R.id.btnMsjDigital);
		btnMensajeria.setOnClickListener(v -> startAppMsjDigital());
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.checkCurrentVersion();
		
		if (appState.isWaitingInstallation()) {
			//Se cancelo la actualizacion 
			appState.setWaitingInstallation(false);
			
			if (appState.isUpgradeRequired()) {
				//Se canceló la actualización obligatoria
				String errorMsg = ParamHelper.getString("updateRequired", getString(R.string.update_required));
				GUIHelper.showError(this, errorMsg);
				finish();
			}
		} else {
			onSearchRequested();
			new BuildSearchPageTask(this).execute();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.getExtras() == null) {
			return;
		}
		//init message
		String type = intent.getExtras().getString("type");

		if (type != null && type.equals(Constants.NEW_ATTENTION) && !appState.getHCDigitalDAO().existeIMAbierto()) {
			registerIM(MainHCActivity.this);
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.closeApplication:
			confirmAndCloseApplication();
			break;
		case R.id.registrarIM:
			// Verifica perdida de session
			if (!appState.canAccess()) {
				GUIHelper.showErrorLossSession(this);
				break;
			}
			new RegisterIMTask().execute();
			break;
		case R.string.export:
			confirmAndExportFile();
			break;
		case R.string.refresh_list:
			Intent intentRefreshList = new Intent(Constants.ACTION_REFRESH_LIST);
			sendBroadcast(intentRefreshList);
			break;
		case R.string.synchronize:
			BuildSearchPageTask task = new BuildSearchPageTask(this);
			task.forceSyncrhonization = true;
			task.execute();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onListItemClick(ListView l, final View v, final int position, long id) {
		final Activity currentAct = this;
		openItem(currentAct, v, position);
	}
	
	protected void openItem(Activity currentAct, View v, int position) {
		// Verifica perdida de session
		if (!appState.canAccess()) {
			GUIHelper.showErrorLossSession(currentAct);
			return;
		}

		Atencion atencion = (Atencion) getListAdapter().getItem(position);

		Bundle bundle = new Bundle();
		bundle.putString(Constants.ACTION, Constants.ACTION_UPDATE);
		if (atencion.getAtencionPrincipal().getId() != 0) {
			bundle.putInt(Constants.ENTITY_ID, atencion.getAtencionPrincipal().getId());

		} else {
			bundle.putInt(Constants.ENTITY_ID, atencion.getId());
		}
		Intent intent = new Intent(v.getContext(), TabHostActivity.class);
		intent.putExtras(bundle);

		startActivity(intent);
	}

	/**
	 * Registrar nuevo IM.
	 */
	public void DoRegisterIM(View view) {
		registerIM(this);
	}

	protected void registerIM(Activity currentAct) {
		// Verifica perdida de session
		if (!appState.canAccess()) {
			GUIHelper.showErrorLossSession(currentAct);
			return;
		}

		new RegisterIMTask().execute();		
	}

	/**
     * Abrir detalles del despachador.
     */
    public void DoDispatcherDetail(View view) {
        // Verifica perdida de session
        if (!appState.canAccess()) {
            GUIHelper.showErrorLossSession(this);
            return;
        }

        Dialog dialog = new Dialog(this);

        dialog.setContentView(R.layout.dispatcher_detail);
        dialog.setTitle("Despachador");
        dialog.setCancelable(true);
        dialog.onAttachedToWindow();

        final ImageView imageView = dialog.findViewById(R.id.imageView);
        final TextView textView = dialog.findViewById(R.id.textView);

        Despachador despachador = appState.getHCDigitalDAO().getDespachadorById(view.getId());

        if (despachador == null) {
			return;
		}
		// obtengo la imagen
		byte[] byteArray = despachador.getFoto();
		if (byteArray != null) {
			Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
			imageView.setImageBitmap(bmp);
		}

		textView.setText(despachador.getInformacionPersonal());

		dialog.show();
    }

	@SuppressLint("CommitPrefEdits")
	private void confirmAndCloseApplication() {
		// Verifica perdida de session
		if (!appState.canAccess()) {
			GUIHelper.showErrorLossSession(this);
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.confirm_title);
		builder.setMessage(R.string.exit_confirm_msg);
		builder.setCancelable(false);
		builder.setPositiveButton(R.string.yes, (dialog, id) ->{
            // BugFix Error 24 – No se limpian los registros
            // Se fuerza el sincronismo de HCDigital
            appState.hasSynchronized = false;
            SharedPreferences.Editor prefEditor = getSharedPreferences(appState.getClass().getName(), MODE_PRIVATE).edit();
            setExitTimestamp(prefEditor);
			MainHCActivity.this.finish();
		});
		builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());

		AlertDialog alert = builder.create();
		alert.show();
	}
	
	/**
	 * Registrar nuevo Informe de Atencion Medica
	 */
	@SuppressLint("StaticFieldLeak")
	@SuppressWarnings("StringBufferReplaceableByString")
	private class RegisterIMTask extends AsyncTask<Void, CharSequence, Atencion> {

		private ProgressDialog dialog;
		private MainHCActivity context = MainHCActivity.this;
		private UDAACoreServiceImpl localService;
		private HCDigitalManager manager;

		boolean existeIMAbierto = false;
		private String errorParams;

		@Override
		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(context, "", context.getString(R.string.loading_msg), true);
			this.localService = new UDAACoreServiceImpl(context);
			this.manager = new HCDigitalManager(context);
		}

		@Override
		protected Atencion doInBackground(Void... params) {
			// Antes de abrir un nuevo informe se debe verificar que no existan
			// otros IM en abiertos (Estados: "En Preparacion" o "Cierre Provisorio").
			if (appState.getHCDigitalDAO().existeIMAbierto()) {
				existeIMAbierto = true;
				return null;
			}

			try {
				Thread.sleep(1000);
				// Obtiene nuevas atenciones asignadas desde el servidor
				// (sincroniza atencion y su lista de atencionValor)
				Integer dispositivoMovilID = appState.getDispositivoMovil().getId();
				localService.synchronizeAtencion(dispositivoMovilID);
			} catch (Exception e) {
				Log.d(LOG_TAG, " synchronizeAtencion: **ERROR** " + e.getMessage());

				try { // Informar a la Udaa Error al descargar IMD
					localService.sendError(TIPO_REG_ERR_DES);
				} catch (Exception err) {
					Log.d(LOG_TAG, "Error in sendError service : " + err.getMessage());
				}

				StringBuilder messageBuilder = new StringBuilder();
				messageBuilder.append(LOG_TAG);
				messageBuilder.append("|synchronizeAtencion: **ERROR** |");
				messageBuilder.append(e.getMessage());
				messageBuilder.append("|");

				generateACRA_LOG(messageBuilder.toString());

				return null;
			}

			try {
				Thread.sleep(1000);
				// Si se obtuvo una nueva atencion se obtiene para preparar el IM
				Atencion atencion = appState.getHCDigitalDAO().getAtencionEnPreparacion(null);

				// Validaciones de parametros por perfil
				int idAplicacionPerfil;
				if (atencion == null) {
					idAplicacionPerfil = localService.getIdAplicacionPerfilDefaultBy(Constants.COD_HCDIGITAL);
				} else {
					idAplicacionPerfil = atencion.getAplicacionPerfil().getId();
				}
				errorParams = this.manager.validateParams(idAplicacionPerfil);
				if (!TextUtils.isEmpty(errorParams)) {
                    return null;
                }

				return atencion;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		// ------------------------------------------------------------------
		// ACRA LOG SYNCHRONIZE ERROR //CATA CODE
		// ------------------------------------------------------------------
		private void generateACRA_LOG(String message) {
			String versionName = "";
			try {
				String packageName = context.getPackageName();
				versionName = "v" + context.getPackageManager().getPackageInfo(packageName,0).versionName;
			} catch (Exception ignore) {}
			
			GenerateACRA_LOG(Constants.COD_HCDIGITAL + "|" + versionName, message);
		}				
		// -------------------------------------------------------------------
		// -------------------------------------------------------------------

		@Override
		protected void onProgressUpdate(CharSequence... values) {
			dialog.setMessage(values[0]);
		}

		@Override
		protected void onPostExecute(Atencion atencion) {
			dialog.dismiss();

			// Si existe IM abierto
			if (existeIMAbierto) {
				appState.getNotificationManager().viewError(context, ErrorConstants.ERROR_EXISTE_IM_SIN_CIERRE);
				return;
			}

			// Si se puede proseguir se abre IM para atencion encontrada o se
			// genera una nueva generica
			if (atencion != null) {
				Intent intent = new Intent(context, TabHostActivity.class);

				Bundle bundle = new Bundle();
				bundle.putString(Constants.ACTION, Constants.ACTION_UPDATE);
				bundle.putInt(Constants.ENTITY_ID, atencion.getId());
				intent.putExtras(bundle);
				startActivity(intent);
			} else {
				this.confirmAndCreateDefaultIM();
			}
		}

		// MARK: - Internal

		private void confirmAndCreateDefaultIM() {
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle(R.string.register_IM_default_title);
			builder.setMessage(R.string.register_IM_default_warning);
			builder.setCancelable(false);
			builder.setPositiveButton(R.string.yes, (dialog, id) -> {
				if (TextUtils.isEmpty(errorParams)) {
					Intent intent = new Intent(context, TabHostActivity.class);

					Bundle bundle = new Bundle();
					bundle.putString(Constants.ACTION, Constants.ACTION_CREATE);
					bundle.putInt(Constants.ENTITY_ID, 0);
					intent.putExtras(bundle);
					startActivity(intent);
				} else {
					Toast.makeText(context,	errorParams, Toast.LENGTH_LONG).show();
				}
			});
			builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
			builder.create().show();
		}

	}

	@Override
	@SuppressLint("CommitPrefEdits")
	protected void onDestroy() {
		if (isFinishing()) {
			SharedPreferences.Editor prefEditor = getSharedPreferences(appState.getClass().getName(), MODE_PRIVATE).edit();
			setExitTimestamp(prefEditor);
			// Destroy services
			Intent intent = new Intent(this, SyncAtencionService.class);
			stopService(intent);

			// Cata code
			try {
				unbindDrawables(getListView());
			} catch (Exception e) {
				Log.d("MainHCActivity"," |onDestroy()| error : " + Arrays.toString(e.getStackTrace()));
			}

			try {
				if (HCDigitalApplication.broadcastReceiver != null) {
					unregisterReceiver(HCDigitalApplication.broadcastReceiver);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			unregisterReceiver(titleReceiver);
			unregisterReceiver(refreshReceiver);
			unregisterReceiver(mMessageReceiver);
		}

		super.onDestroy();
	}

	private void unbindDrawables(View view) {
		if (view.getBackground() != null) {
			view.getBackground().setCallback(null);
		}

		if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				unbindDrawables(((ViewGroup) view).getChildAt(i));
			}
			try {
				((ViewGroup) view).removeAllViews();
			} catch (UnsupportedOperationException ignore) {}
		}
	}
	
	public void checkCurrentVersion() {
		//Show update available message
		String currentVersion = DeviceContext.getPackageInfoFromInstalledApp(getPackageName(), this).versionName;
		if (appState.getUpgradeIntent() != null && !currentVersion.equals(appState.getLastVersionName())) {
			String text = ParamHelper.getString("updateAvailable", "");
			TextView view = findViewById(R.id.notificacion_app_update);
			view.setVisibility(View.VISIBLE);
			view.setText(text);
		}
	}
	
	private void confirmAndExportFile() {
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.confirm_title);
        alertBuilder.setMessage(R.string.export_confirm_msg);
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(R.string.yes, (dialog, id) -> doExportFile());
        alertBuilder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());
		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	protected void doExportFile() {
		try {
			File root = Environment.getExternalStorageDirectory();

			// check sdcard permission
			if (root.canWrite()) {
				DatabaseHelper databaseHelper = new DatabaseHelper(this);
				Calendar calendar = Calendar.getInstance();
				TxtExporter txtExporter = new TxtExporter(databaseHelper.getWritableDatabase());
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
				txtExporter.export("hcd.db", dateFormat.format(calendar.getTime()));
				
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

	public void setExitTimestamp(SharedPreferences.Editor prefEditor) {
		Time now = new Time();
		now.setToNow();
		prefEditor.putString(Constants.LAST_EXIT_TIMESTAMP, now.format3339(false));
        prefEditor.commit();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == Constants.REQUEST_UPGRADE_APP && resultCode != RESULT_OK) {
			MainHCActivity.this.finish();
            System.runFinalization();
            System.exit(0);
        }
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Back?
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // Back
            moveTaskToBack(true);
            return true;
        }
        else {
            // Return
            return super.onKeyDown(keyCode, event);
        }
    }

	private void GenerateACRA_LOG(String descError, String message) {
		// Application version
		Intent msg = new Intent();
		
		ReporteError reporteError = new ReporteError();
		reporteError.setFechaCaptura(new Date());
		reporteError.setDescripcion(descError);
		if (appState.getDispositivoMovil() != null) {
			reporteError.setDispositivoMovil(appState.getDispositivoMovil().getId());
		}

		msg.setAction(Constants.ACTION_ACRA_ERROR_SEND);

		DetalleReporteError detalleReporteError;

		detalleReporteError = new DetalleReporteError();
		detalleReporteError.setDescripcion(message);
		detalleReporteError.setReporteError(reporteError);
		detalleReporteError.setTipoDetalle("HCDIGITAL");
		
		List<DetalleReporteError> detalleReporteErrorList = new ArrayList<DetalleReporteError>();
		detalleReporteErrorList.add(detalleReporteError);

		reporteError.setDetalleReporteErrorList(detalleReporteErrorList);

		// --
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		
		new GraphAdapterBuilder()
		.addType(ReporteError.class)
		.addType(DetalleReporteError.class)
		.registerOn(gsonBuilder);
		Gson gson = gsonBuilder.create();
		// --

		msg.putExtra("REPORTE_ERROR", gson.toJson(reporteError));

		sendBroadcast(msg);
	}

	private void startAppMsjDigital() {
		Intent intent;
		// Custom Application Launcher
		intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(new ComponentName("coop.tecso.msj", "coop.tecso.msj.activity.MainActivity"));

		if (GUIHelper.canHandleIntent(appState, intent)) {
			appState.startActivity(intent);
			return;
		}

		AlertDialog.Builder confirmDialogBuilder = new AlertDialog.Builder(MainHCActivity.this, 0);
		confirmDialogBuilder.setTitle("Aplicacion no instalada");
		confirmDialogBuilder.setMessage("La aplicacion MSJDigital no está instalada");
		confirmDialogBuilder.setCancelable(false);
		confirmDialogBuilder.setPositiveButton(R.string.accept, (dialog, id) -> {
			dialog.cancel();
		});
		confirmDialogBuilder.create();
		confirmDialogBuilder.show();
	}

}
