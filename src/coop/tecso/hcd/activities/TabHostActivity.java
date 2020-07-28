package coop.tecso.hcd.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LocalActivityManager;
import android.app.TabActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.EstadoAtencion;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.components.SoftKeyboardHandledLinearLayout;
import coop.tecso.hcd.gui.components.SoftKeyboardHandledLinearLayout.SoftKeyboardVisibilityChangeListener;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.services.HCDigitalManager;
import coop.tecso.hcd.tasks.CloseIMTask;
import coop.tecso.hcd.tasks.PreCloseIMTask;
import coop.tecso.hcd.tasks.PrintReportTask;
import coop.tecso.hcd.tasks.RegisterIMMTask;
import coop.tecso.hcd.tasks.TransferReportTask;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.hcd.utils.HotspotDialog;
import coop.tecso.hcd.utils.HotspotUtils;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.hcd.utils.PrePrintUtil;

/**
 * 
 * @author tecso.coop
 * 
 */
public final class TabHostActivity extends TabActivity {

	private static final String TAG = TabHostActivity.class.getSimpleName();

	public final Object oReference = new Object();

	public Dialog dialog;

	private Button closeTabButton;
	private Button addTabButton;
	private Button closeIMButton;

	private HCDigitalApplication appState;

	public String MAIN_TAB_TAG;
	public List<Integer> listIdAtencionInTabHost;
	public Map<Integer, String> mTabTag;

	private Timer timerSaveIM;
    private LinearLayout llb;
	private LinearLayout llcb;
	private boolean onPreCloseIMTask = false;

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.appState = (HCDigitalApplication) getApplicationContext();

	
        //+**************************************************
        // Botonera
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TabHost tabHost = (TabHost) inflater.inflate(R.layout.tabhost_layout, null , false);
        SoftKeyboardHandledLinearLayout container = new SoftKeyboardHandledLinearLayout(this);

        (container).setOnSoftKeyboardVisibilityChangeListener(
            new SoftKeyboardVisibilityChangeListener() {
            @Override
            public void onSoftKeyboardShow() {
                TabHostActivity.this.botonesVisibles(false);
            }
            @Override
            public void onSoftKeyboardHide() {
                TabHostActivity.this.botonesVisibles(true);
            }
            @Override
            public void softKeyboardHide() {
                TabHostActivity.this.hideSoftKeyboard();
            }
        });

        RelativeLayout.LayoutParams lpLabel = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lpLabel.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1) ;
        container.addView(tabHost, lpLabel);


        setContentView(container);
        llb = findViewById(R.id.linearLayout_botonera);
        llcb = findViewById(R.id.linearLayout_contenedorbotonera);
        //+**************************************************

        this.setTitle(Utils.getFormattedTitle(this));

        this.closeIMButton = findViewById(R.id.closeIMButton);
        this.addTabButton = findViewById(R.id.addTabButton);
        this.closeTabButton = findViewById(R.id.closeTabButton);

        Bundle bundle = getIntent().getExtras();
        Log.d(TAG, "Parametro recibido en INTENT: atencionID="
                        + bundle.getInt(Constants.ENTITY_ID));

        // Verifica perdida de session
        if (!appState.canAccess()) {
            GUIHelper.showError(TabHostActivity.this, "Se perdió la sesión.");
            this.finish();
        }

        this.mTabTag = new HashMap<>();

        // Validar estado atencion
        appState.getHCDigitalDAO().validarAtencionCerrada(bundle.getInt(Constants.ENTITY_ID));

        // Build Tabs
        this.buildTabHost(bundle.getString(Constants.ACTION), bundle.getInt(Constants.ENTITY_ID));

        getTabHost().setOnTabChangedListener(this::drawKeypad);

        long periodSaveIM = ParamHelper.getLong(ParamHelper.SAVE_IM_TIME, 300000L);
        Log.i(TAG, "periodSaveIM: "+ periodSaveIM);

        TimerTask timerSaveIMTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    Log.i(TAG, "Timer auto guardado: enter");
                    synchronized(oReference) {
                        doGuardarIMs(false);
                    }
                });
            }
        };
        timerSaveIM = new Timer();
        timerSaveIM.schedule(timerSaveIMTask, periodSaveIM, periodSaveIM);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.im_option_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.anularIM:
			DoAnularIM();
			break;
		case R.id.guardarIM:
			doGuardarIM();
			break;
		case R.id.cerrarSinActoIM:
			doCerrarSinActoIM();
			break;
        case R.id.transferIM:
            DoTransferIM();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		int dirtyIM = 0;
		TabWidget tabWidget = getTabHost().getTabWidget();
		Map<Integer, String> mTag = getTagMap();

		for (int i = 0; i < (tabWidget != null ? tabWidget.getChildCount() : 0); i++) {
			View v = tabWidget.getChildAt(i);
			if (v == null || View.GONE == v.getVisibility()) {
                continue;
            }

			// Generate tab tag
			RegisterActivity activity = (RegisterActivity) getLocalActivityManager().getActivity(mTag.get(i));

			if(activity == null) { continue; }
            // IM Form
            PerfilGUI form = activity.getForm();
            // Atencion actual
            Atencion atencion = (Atencion) form.getBussEntity();
            if (atencion.getEstadoAtencion().getId() != EstadoAtencion.ID_CERRADA_DEFINITIVA
                && atencion.getEstadoAtencion().getId() != EstadoAtencion.ID_ANULADA
                && activity.getForm().isDirty()) {
                    dirtyIM++;
            }
		}

		if (dirtyIM > 0) {
			doGuardarIMs();
		}
		finish();
	}
	
	/**
	 * Registrar Atencion Multiple
	 */
	public void DoAddTab(View view) {
		new RegisterIMMTask(this).execute();
	}
	/**
	 * Cerrar Atencion Multiple
	 */
	public void DoCloseTab(View view) {
		final TabHost tabHost = getTabHost();

		TextView titleTextView = tabHost.getCurrentTabView().findViewById(android.R.id.title);
		String label = titleTextView.getText().toString();

		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.confirm_title);
        alertBuilder.setMessage(getResources().getString(R.string.delete_tab_confirm_msg, label));
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton(R.string.yes, (dialog, id) -> {
            tabHost.getCurrentTabView().setVisibility( View.GONE);
            tabHost.setCurrentTabByTag(MAIN_TAB_TAG);
            addTabButton.setVisibility(View.VISIBLE);
            closeTabButton.setVisibility(View.GONE);
        });
        alertBuilder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());

		AlertDialog alert = alertBuilder.create();
		alert.show();
	}

	public Map<Integer, String> getTagMap() {
		return mTabTag;
	}

	/**
	 * Cerrar Informe Medico
	 */
	public void DoCloseIM(View view) {

		if (!isOnPreCloseIMTask()) {
			new PreCloseIMTask(this).execute();
		}
		Log.i(TAG, "DoCloseIM: exit");
	}

	public void excuteCloseIM() {
		// UserName
		EditText userNameTextEdit = dialog.findViewById(R.id.editTextIdMedico);
        String userName = userNameTextEdit.getText().toString();

		// Password
		EditText passwordTextEdit = dialog.findViewById(R.id.editTextPassword);
        String password = passwordTextEdit.getText().toString();

		new CloseIMTask(this).execute(userName, password);
	}

	/**
	 * Anular Informe Medico
	 */
	public void DoAnularIM() {

		Log.i(TAG, "DoAnularIM: enter");
		// CurrentTabTag
		String currentTabTag = getTabHost().getCurrentTabTag();

		// CurrentActivity
		RegisterActivity currentActivity;
		currentActivity = (RegisterActivity) getLocalActivityManager()
				.getActivity(currentTabTag);

		// IM Form
		PerfilGUI form = currentActivity.getForm();
		// Atencion actual
		Atencion atencion = (Atencion) form.getBussEntity();

		// Verificar que el estado de atencion sea en preparacion
		if (atencion != null
				&& atencion.getEstadoAtencion() != null
				&& atencion.getEstadoAtencion().getId() != EstadoAtencion.ID_EN_PREPARACION) {
			GUIHelper.showError(TabHostActivity.this,
					"No se puede anular el IM");
			return;
		}

		// Comparo el TAG_ID del IM principal con el del actual
		if (!currentTabTag.equals(MAIN_TAB_TAG)) {
			// Caso IMM
			RegisterActivity mainActivity;
			mainActivity = (RegisterActivity) getLocalActivityManager()
					.getActivity(MAIN_TAB_TAG);
			Atencion atencionPrincipal = (Atencion) mainActivity.getForm()
					.getBussEntity();

			// IM Principal sin guardar
			if (atencionPrincipal.getId() == 0) {
				final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle(getString(R.string.close_IMM_error_tittle));
				alertDialog.setMessage(getString(R.string.close_IMM_error_msg));
				alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.accept), (dialog, id) -> {
                    alertDialog.dismiss();
                });
				alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
				alertDialog.show();
				return;
			}
			atencion.setAtencionPrincipal(atencionPrincipal);
		}

		HCDigitalManager service = new HCDigitalManager(this);
		// Dialogo de Cierre IM
		dialog = service.buildDialogForCierreIM(currentTabTag, atencion, true, form);
		dialog.show();
		Log.i(TAG, "DoAnularIM: exit");
	}

	/**
	 * Guardar Informes Medicos
	 *
	 */
	public void doGuardarIMs() {
		doGuardarIMs(true);
	}

	public int doGuardarIMs(boolean showMsg) {
		TabWidget tabWidget = getTabHost().getTabWidget();

		Map<Integer, String> mTag = getTagMap();
		
		Atencion atencionPrincipal = null;
		
		int atenGuardadas = 0;
		
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			View v = tabWidget.getChildAt(i);
			if (View.GONE == v.getVisibility()) {
                continue;
            }

			// Generate tab tag
			String tag = mTag.get(i);
			RegisterActivity activity;
			activity = (RegisterActivity) getLocalActivityManager().getActivity(tag);

			// El tab no ha llamado al activity
			if (null == activity || activity.getForm() == null) {
			    continue;
            }
			
			Atencion atencion = (Atencion) activity.getForm().getBussEntity();

			if(i == 0) {
				atencionPrincipal = atencion;
			}
			else {
				atencion.setAtencionPrincipal(atencionPrincipal);
			}

			// Form has unsaved values

			if(activity.getForm().isDirty() && this.doGuardarIM(tag)) {
                if(i == 0) {
                    atencionPrincipal = atencion;
                }
                atenGuardadas++;
			}
		}
		
		if(showMsg && atenGuardadas > 0) {
			if(atenGuardadas > 1) {
				Toast.makeText(getApplicationContext(),
						"Los Informes Médicos han sido guardados con éxito",
						Toast.LENGTH_LONG).show();
			}
			else {
				Toast.makeText(getApplicationContext(),
						"El Informe Médico ha sido guardado con éxito",
						Toast.LENGTH_LONG).show();
			}
		}
		return atenGuardadas;
	}

	public void doCerrarSinActoIM() {
		String currentTabTag = getTabHost().getCurrentTabTag();

		try {
			Log.i(TAG, "doCerrarSinActoIM: enter");
			
			// CurrentActivity
			RegisterActivity currentActivity = (RegisterActivity) getLocalActivityManager().getActivity(currentTabTag);
	
			PerfilGUI form = currentActivity.getForm();
			
			// Atencion actual
			Atencion atencion = (Atencion) form.getBussEntity();
			// Verificar que el estado de atencion sea en preparacion
			if (atencion != null
					&& atencion.getEstadoAtencion() != null
					&& atencion.getEstadoAtencion().getId() != EstadoAtencion.ID_EN_PREPARACION) {
				GUIHelper.showError(TabHostActivity.this,
						"La operación no es válida");
				return;
			}
			// Comparo el TAG_ID del IM principal con el del actual
			if (!currentTabTag.equals(MAIN_TAB_TAG)) {
				// Caso IMM
				RegisterActivity mainActivity = (RegisterActivity) getLocalActivityManager().getActivity(MAIN_TAB_TAG); // Constants.MAIN_TAB_TAG);
				Atencion atencionPrincipal = (Atencion) mainActivity.getForm().getBussEntity();

				// IM Principal sin guardar
				if (atencionPrincipal.getId() == 0) {
					final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setTitle(getString(R.string.close_IMM_error_tittle));
					alertDialog.setMessage(getString(R.string.close_IMM_error_msg));
					alertDialog.setButton(Dialog.BUTTON_POSITIVE, getString(R.string.accept), (dialog, id) -> {
                        alertDialog.dismiss();
					});
					alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
					alertDialog.show();
					return;
				}
				atencion.setAtencionPrincipal(atencionPrincipal);
			}

			HCDigitalManager service = new HCDigitalManager(this);
			dialog = service.buildDialogForCierreIM(currentTabTag, atencion, false, true, form);
			dialog.show();
		}
		catch(Exception ex){
			Toast.makeText(getApplicationContext(),
					"Ha ocurrido un error al guardar el Informe Médico",
					Toast.LENGTH_LONG).show();
		}
	}

	public void doGuardarIM() {
		String currentTabTag = getTabHost().getCurrentTabTag();
		if(this.doGuardarIM(currentTabTag)) {
			Toast.makeText(getApplicationContext(),
					"El Informe Médico ha sido guardado con éxito",
					Toast.LENGTH_LONG).show();			

			// CurrentActivity
			RegisterActivity currentActivity = (RegisterActivity) getLocalActivityManager().getActivity(currentTabTag);	
			PerfilGUI form = currentActivity.getForm();
			form.runEvalAllCierre();
		}
		else {
			GUIHelper.showError(TabHostActivity.this, "No se puede guardar el IM");
		}
	}

	public boolean doGuardarIM(String tabTag) {

		try {
			Log.i(TAG, "DoGuardarIM: enter");
			
			// CurrentActivity
			RegisterActivity currentActivity = (RegisterActivity) getLocalActivityManager().getActivity(tabTag);
	
			PerfilGUI form = currentActivity.getForm();

			HCDigitalManager service = new HCDigitalManager(this);

			// Atencion actual
			Atencion atencion = (Atencion) form.getBussEntity();
			// Verificar que el estado de atencion sea en preparacion
			if (atencion != null
					&& atencion.getEstadoAtencion() != null
					&& atencion.getEstadoAtencion().getId() != EstadoAtencion.ID_EN_PREPARACION
					&& atencion.getEstadoAtencion().getId() != EstadoAtencion.ID_CERRADA_PROVISORIA) {
				return false;
			}
			service.saveIM(currentActivity.getForm(), tabTag);
			return true;		
		}
		catch(Exception ex){
			Toast.makeText(getApplicationContext(),
					"Ha ocurrido un error al guardar el Informe Médico",
					Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public void DoPrintIM(View view) {

        this.enableWifiIfNeeded();
		this.executePrintIMIfPossible();
	}

	private void enableWifiIfNeeded() {
		if(appState.isWifiEnabled()) {
			return;
		}

		if(HotspotUtils.isWifiHotspotEnabled(this)){
			if (!HotspotUtils.disableWifiHotspotAndCheck(appState)) {
				HotspotDialog.showDialog(this, false);
			}
			Helper.sleep(3000);
		}

		appState.setWifiEnabled(true);
		Helper.sleep(3000);
	}

	private void executePrintIMIfPossible() {
        PrePrintUtil prePrintUtil = new PrePrintUtil(this);
		prePrintUtil.printIfPossible(this, () -> {
			new PrintReportTask(this).execute();
		});
    }

    public void DoTransferIM() {
        Log.i(TAG, "DoTransferIM: enter");
        // CurrentTabTag
        String currentTabTag = getTabHost().getCurrentTabTag();

        // CurrentActivity
		LocalActivityManager localActivityManager = getLocalActivityManager();
        RegisterActivity currentActivity = (RegisterActivity) localActivityManager.getActivity(currentTabTag);

        // IM Form
        PerfilGUI form = currentActivity.getForm();
        // Atencion actual
        Atencion atencion = (Atencion) form.getBussEntity();

        // Verificar que el estado de atencion sea en preparacion
        if (atencion != null && atencion.getEstadoAtencion() != null
                && !(atencion.cerradaDefinitiva() || atencion.cerradaProvisoria()))
        {
            GUIHelper.showError(TabHostActivity.this, "No se puede Transferir el IM. Debería cerrarlo antes.");
            return;
        }

        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null) {
            GUIHelper.showMessage( TabHostActivity.this , getString( R.string.bt_notfound ) );
            return;
        }

		new TransferReportTask(this).execute(); // Transfer Report
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode) {
            case Constants.REQUEST_NEW_GESTURE:
                if(resultCode == RESULT_OK && data != null) {
                    loadSign(data);
                }
                break;

            case Constants.REQUEST_BLUETOOTH_DISCOVERY:
                if(resultCode == RESULT_OK || resultCode == Constants.BLUETOOTH_DISCOVER_DURATION) {
                    new TransferReportTask(this).execute(); // Transfer Report
                }
              break;
        }
	}

	private void loadSign(Intent data) {
		if (dialog == null) {
            return;
        }

        Bundle extras = data.getExtras();

        // confirm button
        ImageView imageSign = dialog.findViewById(R.id.imageViewSign);

        if (extras != null) {
            Gesture sign = (Gesture) extras.get("sign");
            final Resources resources = getResources();

            if (resources == null) {
				return;
			}
			int inset = (int) resources.getDimension(R.dimen.gesture_thumbnail_inset);
			int size = (int) resources.getDimension(R.dimen.gesture_thumbnail_size);

			if (sign != null) {
				imageSign.setImageBitmap(sign.toBitmap(size, size, inset, Color.BLACK));
				return;
			}
        }

        imageSign.setImageDrawable(null);
	}

	private void buildTabHost(String action, Integer entityId) {
		Log.i(TAG, "buildTabHost: enter");

		List<Atencion> atencionList = new ArrayList<Atencion>();
		if (Constants.ACTION_CREATE.equals(action)) {
			// Register IM
			Atencion atencion = new Atencion();
			atencion.setId(0);
			atencionList.add(atencion);
		} else {
			// Update IM
			atencionList = appState.getHCDigitalDAO().getListAtencionByIdAtencionPrincipal(entityId);
		}

		listIdAtencionInTabHost = new ArrayList<>();

		TabHost tabHost = getTabHost();
		int i = 0;
		for (Atencion atencion : atencionList) {
			//
			Intent intent = new Intent(this, RegisterActivity.class);

			Bundle bundle = new Bundle();
			bundle.putString(Constants.ACTION, action);
			bundle.putInt(Constants.ENTITY_ID, atencion.getId());
			if (Constants.ACTION_CREATE.equals(action)) {
				bundle.putInt(Constants.ESTADO_IM, EstadoAtencion.ID_EN_PREPARACION);
			} else {
				bundle.putInt(Constants.ESTADO_IM, atencion.getEstadoAtencion().getId());
			}
			intent.putExtras(bundle);

			String tag = Constants.TAB_TAG + i;
			if (atencion.getId() != 0) {
				tag = String.valueOf(atencion.getId());
				listIdAtencionInTabHost.add(atencion.getId());
			}
			TabSpec tabSpec = tabHost.newTabSpec(tag);
			mTabTag.put(i, tag);

			String indicator = "Nuevo IM";
			if (atencion.getId() != 0) {
				indicator = "Nro: "+ String.valueOf(atencion.getNumeroAtencion());
			}
			tabSpec.setIndicator(indicator,
					getResources().getDrawable(R.drawable.ic_menu_compose))
					.setContent(intent);
			i++;
			if (atencion.getAtencionPrincipal() == null
					|| atencion.getAtencionPrincipal().getId() == 0)
				MAIN_TAB_TAG = tag;
			Log.i(TAG, "buildTabHost: addingTab , tabTag=" + tag
					+ " , indicator=" + indicator);
			tabHost.addTab(tabSpec);

		}
		if (MAIN_TAB_TAG == null) {
			Log.i(TAG, "MAIN_TAB_TAG IS NULL");
			finish();
		} else {
			this.drawKeypad(MAIN_TAB_TAG);
			Log.i(TAG, "buildTabHost: exit");
		}
	}

	/**
	 * Dibuja la botonera de acciones teniendo en cuenta permisos
	 */
    public void drawKeypad(String tabTag) {
		Log.i(TAG, "drawKeypad: enter , tabTag: " + tabTag);

		Activity activity = getLocalActivityManager().getActivity(tabTag);
		Bundle bundle = activity.getIntent().getExtras();

		// ID IAM
		int idIM = bundle.getInt(Constants.ENTITY_ID);
		// Estado IAM
		int estadoIM = bundle.getInt(Constants.ESTADO_IM);

		if (EstadoAtencion.ID_EN_PREPARACION == estadoIM) {
			if (tabTag.equals(MAIN_TAB_TAG)) {
				// IM Principal
				addTabButton.setVisibility(View.VISIBLE);
				closeTabButton.setVisibility(View.GONE);
			} else {
				// IMM Asociado
				addTabButton.setVisibility(View.GONE);
				if (idIM < 1) {
					closeTabButton.setVisibility(View.VISIBLE);
				} else {
					closeTabButton.setVisibility(View.GONE);
				}
			}
			closeIMButton.setVisibility(View.VISIBLE);
		} else {
			// Estado: Anulada o Cerrada Definitiva
			closeTabButton.setVisibility(View.GONE);
			addTabButton.setVisibility(View.GONE);
			// Estado: Cerrada Provisoria
			if (EstadoAtencion.ID_CERRADA_PROVISORIA == estadoIM) {
				closeIMButton.setVisibility(View.VISIBLE);
			} else {
				closeIMButton.setVisibility(View.GONE);
			}
		}
		Log.i(TAG, "drawKeypad: exit");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Cata code
		try {
			unbindDrawables(getTabHost());
			if(this.timerSaveIM != null) {
				this.timerSaveIM.cancel();
				this.timerSaveIM.purge();
			}
		} catch (Exception e) {
			Log.d(TAG, " |onDestroy()| error : " + Arrays.toString(e.getStackTrace()));
		}
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

	public void botonesVisibles(Boolean visible) {
        if (llcb == null || llb == null) {
            return;
        }

        if (visible) {
            llcb.addView(llb);
        }
        else {
            llcb.removeView(llb);
        }
	}

	/**
	 * Hides the soft keyboard
	 */
	public void hideSoftKeyboard() {
	    if(getCurrentFocus() != null) {
	        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	    }
	}

	public boolean isOnPreCloseIMTask() {
		return onPreCloseIMTask;
	}

	public void setOnPreCloseIMTask(boolean onPreCloseIMTask) {
		this.onPreCloseIMTask = onPreCloseIMTask;
	}

}
