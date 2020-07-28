package coop.tecso.hcd.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.EstadoAtencion;
import coop.tecso.hcd.gui.components.AttacherGUI;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.components.SignGUI;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.persistence.DatabaseHelper;
import coop.tecso.hcd.services.HCDigitalManager;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ParamHelper;

/**
 * 
 * @author tecso.coop
 *
 */
public final class RegisterActivity extends OrmLiteBaseActivity<DatabaseHelper> {
	//IMD
	private PerfilGUI form;
	private View seccionObsPostAtencion;
	private final static String LOG_TAG = RegisterActivity.class.getSimpleName();
	private Dialog dialog;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		HCDigitalApplication appState = (HCDigitalApplication) getApplicationContext();
		
		Set<String> keySet = appState.getGestureStore().getGestureEntries();

		for (int i = keySet.size()-1; i>=0 ; i--) {
			if (keySet.toArray()[i].toString().endsWith("|0")) {
				appState.getGestureStore().removeEntry(keySet.toArray()[i].toString());
			}
		}

		for (int i = keySet.size()-1; i>=0 ; i--) {
			if (keySet.toArray()[i].toString().endsWith(Constants.TEMPORAL_GESTURE_SUFFIX)) {
				appState.getGestureStore().removeEntry(keySet.toArray()[i].toString());
			}
		}


		Bundle bundle = getIntent().getExtras();
		String[] params = new String[3];
		//Accion
		params[0] = bundle.getString(Constants.ACTION);
		//ID IMD
		params[1] = String.valueOf(bundle.getInt(Constants.ENTITY_ID));
		//Dimiclio IMD - Solo se usa en IMM offLine
		params[2] = bundle.getString(Constants.DOMICILIO_IM);

		new ObtenerAtencionTask().execute(params);

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent result) {
		Log.i(LOG_TAG, "onActivityResult: enter");

		if (resultCode != RESULT_OK) {
			Log.d(LOG_TAG, " RESULT_CODE: "+resultCode);
			Log.i(LOG_TAG, "onActivityResult: exit");
			return;
		}

		int inset;
		int size;
		Resources resources = getResources();

		Bundle extras = null;
		if (result != null) {
			extras = result.getExtras();
		}
		
		String closeSignRes = ParamHelper.getString("closeSignResolution", null);

		switch (requestCode) {
		case Constants.REQUEST_NEW_PHOTO:
			AttacherGUI attacher = (AttacherGUI) form.getCampoGUISelected();
			attacher.loadCapturedPhoto();
			break;
		case Constants.REQUEST_NEW_GESTURE:
			Gesture gesture = (Gesture) extras.get(Constants.GESTURE_SIGN);
			SignGUI sign = (SignGUI) form.getCampoGUISelected();

			if (gesture != null) {
				sign.setImages(gesture);
			}
			else {
				sign.invalidate();
			}
			break;
		case Constants.REQUEST_NEW_GESTURE_DIALOG:
			Gesture signal = (Gesture) extras.get(Constants.GESTURE_SIGN) ; 
			inset = (int) resources.getDimension(R.dimen.gesture_thumbnail_inset);
			size = (int) resources.getDimension(R.dimen.gesture_thumbnail_size);

			ImageView imageSign = dialog.findViewById(R.id.imageViewSign);

			if (closeSignRes != null) {
				String[] res = closeSignRes.split("x");
				if (res.length == 2 && signal != null) {
					//Guardamos la imagen a enviar en el tag
					imageSign.setTag(signal.toBitmap(Integer.parseInt(res[0]), Integer.parseInt(res[1]), inset, Color.BLACK));
				}
			}
			
			if (signal != null) {
				imageSign.setImageBitmap(signal.toBitmap(size, size, inset, Color.BLACK));
			}
			else {
				imageSign.setImageDrawable(null);
			}
			break;

			/*agregado  datos firmante */	
		case Constants.REQUEST_NEW_GESTURE_DIALOG_ACLARATION:
			Gesture signalAc = (Gesture) extras.get(Constants.GESTURE_SIGNAC) ; 
			inset = (int) resources.getDimension(R.dimen.gesture_thumbnail_inset);
			size = (int) resources.getDimension(R.dimen.gesture_thumbnail_size);

			ImageView imageSignAc = dialog.findViewById(R.id.imageViewSignAc);
			
			if (closeSignRes != null) {
				String[] res = closeSignRes.split("x");
				if (res.length == 2 && signalAc != null) {
					//Si se especifico una resolucion, la guardamos en el tag para luego ser enviada
					imageSignAc.setTag(signalAc.toBitmap(Integer.parseInt(res[0]), Integer.parseInt(res[1]), inset, Color.BLACK));
				}
			}
			
			if (signalAc != null) {
				imageSignAc.setImageBitmap(signalAc.toBitmap(size, size, inset, Color.BLACK));
			}
			else {
				imageSignAc.setImageDrawable(null);
			}
			break;			
		}
		Log.i(LOG_TAG, "onActivityResult: exit");
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			// Application State
			HCDigitalApplication appState = (HCDigitalApplication) getApplicationContext();
			if (appState == null || appState.getNotificationManager() == null) {
				return;
			}

			// Current TabID in parent
			TabHostActivity tabHostActivity = (TabHostActivity) getParent();
			String tabId = "";
			if (tabHostActivity != null && tabHostActivity.getTabHost() != null) {
				tabId = tabHostActivity.getTabHost().getCurrentTabTag();
			}

			// Remove area from activity
			if (!TextUtils.isEmpty(tabId)) {
				appState.getNotificationManager().removeArea(tabId);
			}

			if (appState.getGestureStore() != null) {
				appState.getGestureStore().removeEntry(form.getBussEntity().getId()+"");
				Set<String> keySet = appState.getGestureStore().getGestureEntries();
				for (String key : keySet) {
					if (key.endsWith("|0")) {
						appState.getGestureStore().removeEntry(key);
					}
				}
				for (int i = keySet.size()-1; i>=0 ; i--) {
					if (keySet.toArray()[i].toString().endsWith(Constants.TEMPORAL_GESTURE_SUFFIX)) {
						appState.getGestureStore().removeEntry(keySet.toArray()[i].toString());
					}
				}
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, " |onDestroy()| error : " + Arrays.toString(e.getStackTrace()));
		}
	}

	@Override
	public void onBackPressed() {
		getParent().onBackPressed();
	}

	public PerfilGUI getForm() {
		return form;
	}

	public void disable() {
		View dForm = this.form.disable();

		Atencion atencion = (Atencion) this.form.getBussEntity();

		EstadoAtencion estadoAtencion = atencion.getEstadoAtencion();

		if (EstadoAtencion.ID_CERRADA_DEFINITIVA == estadoAtencion.getId()
				|| EstadoAtencion.ID_CERRADA_PROVISORIA == estadoAtencion.getId()) {
			HCDigitalManager manager = new HCDigitalManager(this);
			// Seccion con Observaciones Post Atencion
			LinearLayout parent = (LinearLayout) dForm.getParent();

			if (seccionObsPostAtencion != null) {
				parent.removeView(seccionObsPostAtencion);
			}

			seccionObsPostAtencion = manager.buildSeccionPostAtencion(atencion);
			parent.addView(seccionObsPostAtencion);
		}

		getIntent().putExtra(Constants.ESTADO_IM, estadoAtencion.getId());
	}

	@SuppressLint("StaticFieldLeak")
	private class ObtenerAtencionTask extends AsyncTask<String, String, Atencion> {

		private HCDigitalManager manager;
		private HCDigitalApplication appState;
		private RegisterActivity context;

		@Override
		protected void onPreExecute() {
			this.context = RegisterActivity.this;
			progressDialog = ProgressDialog.show(context, "", getString(R.string.loading_msg), true);
			this.manager = new HCDigitalManager(context);
			this.appState = (HCDigitalApplication) getApplicationContext();
		}

		@Override
		@SuppressLint("UseSparseArrays")
		protected Atencion doInBackground(String... params) {
			String action = params[0];
			Integer atencionId = Integer.valueOf(params[1]);
			Log.d("INTENT HCDigital:", "parametros recibido: atencionID="+atencionId);
			// Atencion - IMD
			try {
				if (!appState.canAccess()) {
					throw new Exception("Session closed!");
				}

				Thread.sleep(1000);
				Atencion atencion;
				if (action.equals(Constants.ACTION_CREATE_IMM)) {
					//- JIRA HCDDM-156: Modificación a IMD múltiples
					// Valores a copiar desde IM principal, por ahora solo se copia domicilio
					String domicilio = params[2];
					Map<Integer,String> mValues = new HashMap<>();
					
					String paramDomicilios = ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_DOMICILIO);
					String[] paramDomiciliosArray = paramDomicilios.split("&");
					if (!TextUtils.isEmpty(domicilio) && paramDomiciliosArray.length > 0) {
						for (String paramDomicilio : paramDomiciliosArray) {
							try {
								int paramDomicilioCampo = Integer.parseInt(paramDomicilio);
								mValues.put(paramDomicilioCampo, domicilio);
							} catch (Exception e) {
								// error de conversion;
							}
							
						}
					}
					
					atencion = manager.getAtencionForRegistrarIM(atencionId, mValues);
				} else {
					// IM Simple
					atencion = manager.getAtencionForRegistrarIM(atencionId);
					Log.i("RegisterActivity", "atencion.getNumeroAtencion():" + atencion.getNumeroAtencion());
				}

				return atencion;
			} catch (Exception e) {
				Log.e("Register", "Error doInBackground ", e);
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onPostExecute(Atencion atencion) {
			try {
				progressDialog.cancel();	
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (atencion != null && atencion.getAplicacionPerfil() != null) {
				form = manager.buildIM(atencion);
				appState.setForm(form);
				// ScrollView with disable focus when move
				ScrollView scrollView = new ScrollView(context) {
					@Override
					protected boolean onRequestFocusInDescendants(int direction,Rect previouslyFocusedRect) {
						return true;
					}
				};
				// Container
				LinearLayout container = new LinearLayout(context);
				container.setOrientation(LinearLayout.VERTICAL);
				// Body
				LinearLayout appBody = new LinearLayout(context);
				appBody.setOrientation(LinearLayout.VERTICAL);	
				appBody.addView(form.getView());

				EstadoAtencion estadoAtencion = atencion.getEstadoAtencion();
				if (EstadoAtencion.ID_EN_PREPARACION == estadoAtencion.getId()) {
					String tabId;
					if (atencion.getId() != 0) {
						tabId = String.valueOf(atencion.getId());
					} else {
						// Parent: TabHostActivity
						TabHostActivity tabHostActivity = (TabHostActivity) getParent();
						tabId = tabHostActivity.getTabHost().getCurrentTabTag();
					}
					View notificationArea = appState.getNotificationManager().buildNewArea(context, tabId);
					container.addView(notificationArea);
					form.runEvalAll();
				}else if (EstadoAtencion.ID_CERRADA_DEFINITIVA == estadoAtencion.getId()
						|| EstadoAtencion.ID_CERRADA_PROVISORIA == estadoAtencion.getId()) {
					// Seccion con Observaciones Post Atencion
					seccionObsPostAtencion = manager.buildSeccionPostAtencion(atencion);
					appBody.addView(seccionObsPostAtencion);
				}

				try {
					scrollView.addView(appBody);
					container.addView(scrollView);
					setContentView(container);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				GUIHelper.showError(RegisterActivity.this, "Se perdió la sesión.");
				RegisterActivity.this.finish();
			}
		}
	}

}