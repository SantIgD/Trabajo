package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.SQLHelper;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.HotspotDialog;
import coop.tecso.hcd.utils.HotspotUtils;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;

@SuppressLint("ClickableViewAccessibility") 
public class ElectroGUI extends CampoGUI {
	private static final String LOG_TAG = ElectroGUI.class.getSimpleName();
	private final static int MAX_LENGHT_CONCLUSION = 5;
	
	public enum TipoTeclado {
		ALFANUMERICO, NUMERICO, DECIMAL
	};
	
	private class SpinnerDTO {
		private int id;
		private String description;
		private String shortDesc;
	}
	
	public final class ElectroDTO {
		byte[] electro;
		byte[] electroCompleto;
		int idRitmo;
		String otroRitmo = "";
		String fc = "";
		String ondaP = "";
		String pr = "";
		int idBloqueoRama;
		int idSegmento;
		String segmento = "";
		int[] derivacionesSegm;
		int idOndaT;
		int[] derivacionesOndaT;
		String conclusiones = "";
	}

	private boolean newElectro = true;
	
	private ElectroDTO electroDTO;
	
	private boolean isAddAction = true;
	private TableLayout electroLayout;
	private LinearLayout imgLayout;

	private ImageView imageView;

	private Spinner spinnerRitmo;
	private EditText editRitmoLpm;

	private EditText editOndaP;
	private EditText editOtroRitmo;
	private EditText editPR;
	
	private Spinner spinnerBloqueoRama;
	
	private Spinner spinnerSegmST;
	private EditText editSegmST;
	
	private TextView selectedSegmDer;
	private ImageButton btnSegmDer;

	private CharSequence[] optionsSegmDer;
	private boolean[] checkedSegmDer;
	private Map<Integer, Integer> mapOptionsSegDer;

	private CharSequence[] optionsOndaDer;
	private boolean[] checkedOndaDer;
	private Map<Integer, Integer> mapOptionsOndaDer;
	
	private Spinner spinnerOndaT;	
	private TextView selectedOndaTDer;
	private ImageButton btnOndaDer;
	
	private EditText editConclusiones;

	private Button btnWifi;

	private static final int DIR_UP = 0;
	private static final int DIR_RIGHT = 1;
	private static final int DIR_DOWN = 2;
	private static final int DIR_LEFT = 3;

	private static final int ZOOM_IN = 0;
	private static final int ZOOM_OUT = 1;
	
	private static int labelWidth = 130;
	
	/**
	 * These matrices will be used to scale points of the image
	 */
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	/**
	 * The 3 states (events) which the user is trying to perform
	 */
	private static final int NONE = 0;
	private static final int DRAG = 1;
	private static final int ZOOM = 2;

	private int mode = NONE;
	
	/**
	 * These PointF objects are used to record the point(s) the user is touching
	 */
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private float oldDist = 1f;

    public ElectroGUI(Context context, boolean enabled) {
		super(context, enabled);
	}

	@Override
	public View build() {
    	List<Value> initialValues = this.getInitialValues();
		if (!CollectionUtils.isEmpty(initialValues)) {
			Value initialValue = initialValues.get(0);
			String strElectro = initialValue.getCodigoEntidadBusqueda();
			
			electroDTO = parseElectro(strElectro);
			electroDTO.electro = initialValue.getImagen();

			newElectro = false;
		} else {
			newElectro = true;
			electroDTO = parseElectro(this.getValorDefault());
		}
		
		TableLayout mainLayout = new TableLayout(context);	
		mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOrientation(LinearLayout.HORIZONTAL);
		titleLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		titleLayout.setOrientation(LinearLayout.HORIZONTAL);
		titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// 	 Etiqueta
		this.label = new TextView(context);
		this.label.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(this.getEtiqueta());
		this.label.setGravity(Gravity.CENTER_VERTICAL);
		this.label.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

		electroLayout = new TableLayout(context);
		electroLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		// Imagen
		this.builAreaImage(electroLayout);		
		// Ritmo
		this.buildAreaRitmo(electroLayout);		
		// Onda P
		this.buildAreaOndaP(electroLayout);		
		// Bloqueo Rama
		this.buildAreaBloqueoRama(electroLayout);
		// Segmento ST
		this.preBuildAreaSegm();
		this.buildAreaSegm(electroLayout);
		// Derivaciones Segmento ST
		this.buildAreaSegmDer(electroLayout);
		// OndaT
		this.preBuildAreaOndaT();
		this.buildAreaOndaT(electroLayout);		
		// Derivaciones OndaT
		this.buildAreaOndaTDer(electroLayout);
		// Derivaciones Conclusiones
		this.buildAreaConclusiones(electroLayout);

		// Wifi
		this.buildAreaWifi(electroLayout);

		mainLayout.addView(electroLayout);

		if (this.electroDTO.electro != null && this.electroDTO.electro.length > 0) {
			Options options = new BitmapFactory.Options();
		    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		    options.inDither = false;

	    	Bitmap bitmap = BitmapFactory.decodeByteArray(this.electroDTO.electro, 0, this.electroDTO.electro.length, options);
	    	imageView.setImageBitmap(bitmap);
		    
			this.setImageBehavior(true);			
		}
		else if(electroDTO.idRitmo != 0) {
			this.setImageBehavior(false);	
		}

		RelativeLayout tempLayout = new RelativeLayout(context);
		tempLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		tempLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		tempLayout.addView(mainLayout);
		
		this.view = tempLayout;

		BroadcastReceiver electroGUIReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				boolean isHotSpotEnabled = HotspotUtils.isWifiHotspotEnabled(context);
				if (isHotSpotEnabled) {
					btnWifi.setText("Desconectar Anclaje");
				} else {
					btnWifi.setText("Conectar Anclaje");
				}
			}
		};

		context.registerReceiver(electroGUIReceiver, new IntentFilter(Constants.WIFI_AP_STATE_CHANGED));

		return this.view;
	}	

	@Override
	public View redraw() {
		this.editRitmoLpm.setEnabled(this.enabled);
		this.editRitmoLpm.setFocusable(this.enabled);	
		this.editRitmoLpm.setFocusableInTouchMode(enabled);
		this.editOndaP.setEnabled(this.enabled);
		this.editOndaP.setFocusable(this.enabled);	
		this.editOndaP.setFocusableInTouchMode(enabled);
		this.editPR.setEnabled(this.enabled);
		this.editPR.setFocusable(this.enabled);
		this.editPR.setFocusableInTouchMode(enabled);
		this.spinnerSegmST.setEnabled(this.enabled);
		this.spinnerSegmST.setFocusable(this.enabled);
		this.editSegmST.setEnabled(this.enabled);
		this.editSegmST.setFocusable(this.enabled);
		this.editSegmST.setFocusableInTouchMode(enabled);
		this.btnSegmDer.setEnabled(this.enabled);
		this.btnSegmDer.setFocusable(this.enabled);
		this.spinnerOndaT.setEnabled(this.enabled);
		this.spinnerOndaT.setFocusable(this.enabled);
		this.btnOndaDer.setEnabled(this.enabled);
		this.btnOndaDer.setFocusable(this.enabled);
		this.editConclusiones.setEnabled(this.enabled);
		this.editConclusiones.setFocusable(this.enabled);
		this.editConclusiones.setFocusableInTouchMode(enabled);
		
		return this.view;
	}

	private ElectroDTO parseElectro(String strToParse) {
		Log.i(LOG_TAG, "parseElectro: enter");
		ElectroDTO electroDTO = new ElectroDTO();
		electroDTO.electro = new byte[]{};; 
		electroDTO.idRitmo = 0;
		electroDTO.otroRitmo = "";
		electroDTO.fc = "";
		electroDTO.ondaP = "";
		electroDTO.pr = "";
		electroDTO.idBloqueoRama = 0;
		electroDTO.idSegmento = 0;
		electroDTO.segmento = "";
		electroDTO.derivacionesSegm = new int[] {};
		electroDTO.idOndaT = 0;
		electroDTO.derivacionesOndaT = new int[] {};
		electroDTO.conclusiones = "";
		try {
			if (!TextUtils.isEmpty(strToParse)) {
				String[] electroInfo = strToParse.split("\\|");
				int i = 0;
				electroDTO.idRitmo = Integer.valueOf(electroInfo[i++]);
				electroDTO.otroRitmo = electroInfo[i++];
				electroDTO.fc = electroInfo[i++];
				electroDTO.ondaP = electroInfo[i++];
				electroDTO.pr = electroInfo[i++];
				electroDTO.idBloqueoRama = Integer.valueOf(electroInfo[i++]);
				electroDTO.idSegmento = Integer.valueOf(electroInfo[i++]);
				electroDTO.segmento = electroInfo[i++];
				electroDTO.derivacionesSegm = this.parseDerivaciones(electroInfo[i++]);
				electroDTO.idOndaT = Integer.valueOf(electroInfo[i++]);
				electroDTO.derivacionesOndaT = this.parseDerivaciones(electroInfo[i++]);
				if(electroInfo.length > 11)
					electroDTO.conclusiones = electroInfo[i++];
			}
		} catch (Exception e) {
			Log.w(LOG_TAG, "parseDomicilio: **ERROR**", e);
		}
		Log.i(LOG_TAG, "parseElectro: exit");
		return electroDTO;
	}
	
	private int[] parseDerivaciones(String derivaciones) {
		if (TextUtils.isEmpty(derivaciones)) {
			return new int[0];
		}
		String[] der = derivaciones.split(",");
		int[] selectedDerv = new int[der.length];
		for (int i = 0; i < der.length; i++) {
			selectedDerv[i] = Integer.valueOf(der[i]);
		}
		return selectedDerv;
	}
	
	public String getValorView() {
		if(newElectro)
			return "";

		StringBuilder builder = new StringBuilder();
		builder.append(((SpinnerDTO)spinnerRitmo.getSelectedItem()).id);
		builder.append("|");
		builder.append(editOtroRitmo.getText());
		builder.append("|");
		builder.append(editRitmoLpm.getText());
		builder.append("|");
		builder.append(editOndaP.getText());
		builder.append("|");
		builder.append(editPR.getText());
		builder.append("|");
		builder.append(((SpinnerDTO)spinnerBloqueoRama.getSelectedItem()).id);
		builder.append("|");
		builder.append(((SpinnerDTO)spinnerSegmST.getSelectedItem()).id);
		builder.append("|");
		builder.append(editSegmST.getText());
		builder.append("|");
		builder.append(getSelectedValues(checkedSegmDer, mapOptionsSegDer));
		builder.append("|");
		builder.append(((SpinnerDTO)spinnerOndaT.getSelectedItem()).id);
		builder.append("|");
		builder.append(getSelectedValues(checkedOndaDer, mapOptionsOndaDer));
		builder.append("|");
		builder.append(editConclusiones.getText());
		return builder.toString();
	}


	private String getValorViewDetail() {
		StringBuilder builder = new StringBuilder();
		builder.append("RITMO=");
		builder.append(((SpinnerDTO)spinnerRitmo.getSelectedItem()).description);
		builder.append("|");
		builder.append("OTRO=");
		builder.append(editOtroRitmo.getText());
		builder.append("|");
		builder.append("FC[lpm]=");
		builder.append(editRitmoLpm.getText());
		builder.append("|");
		builder.append("ONDA-P=");
		builder.append(editOndaP.getText());
		builder.append("|");
		builder.append("PR=");
		builder.append(editPR.getText());
		builder.append("|");
		builder.append("BLOQUEO-RAMA=");
		builder.append(((SpinnerDTO)spinnerBloqueoRama.getSelectedItem()).description);
		builder.append("|");
		builder.append("SEGMENTO-ST=");
		builder.append(((SpinnerDTO)spinnerSegmST.getSelectedItem()).description);
		builder.append("|");
		builder.append("ST[mm]=");
		builder.append(editSegmST.getText());
		builder.append("|");
		builder.append("DERIVACIONES-ST=");
		builder.append(getSelectedValuesDetail(checkedSegmDer, optionsSegmDer));
		builder.append("|");
		builder.append("ONDA-T=");
		builder.append(((SpinnerDTO)spinnerOndaT.getSelectedItem()).description);
		builder.append("|");
		builder.append("DERIVACIONES-ONDA=");
		builder.append(getSelectedValuesDetail(checkedOndaDer, optionsOndaDer));
		builder.append("|");
		builder.append("CONCLUSIONES=");
		builder.append(editConclusiones.getText());
		return builder.toString();
	}


	private String getSelectedValues(boolean[] checked, Map<Integer, Integer> mapOptions) {
		String selectedOptions = "";
		for (int i = 0; i < checked.length; i++) {
			if(checked[i])
				selectedOptions += mapOptions.get(i) + ",";
		}
		if(selectedOptions.endsWith(",")) {
			selectedOptions = selectedOptions.substring(0, selectedOptions.length() - 1 );
		}
		return selectedOptions;
	}


	private String getSelectedValuesDetail( boolean[] checked, CharSequence[] options) {
		StringBuilder selectedOptions = new StringBuilder();
		for (int i = 0; i < checked.length; i++) {
			if (checked[i]) {
				selectedOptions.append(options[i]).append(",");
			}
		}
		if(selectedOptions.toString().endsWith(",")) {
			selectedOptions = new StringBuilder(selectedOptions.substring(0, selectedOptions.length() - 1));
		}
		return selectedOptions.toString();
	}


	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();
		
		AplPerfilSeccionCampoValor campoValor = (AplPerfilSeccionCampoValor)this.entity;
		AplPerfilSeccionCampo campo = campoValor.getAplPerfilSeccionCampo();
		
		String valorDetail = this.getValorViewDetail();
		String valor = this.getValorView();

		Value value = new Value(campo, campoValor, null, valorDetail, valor, this.electroDTO.electro, this.electroDTO.electroCompleto);

		this.values.add(value);

		return this.values;
	}

	@Override
	public boolean isDirty() {
		String valorActual = this.getValorView();
		this.dirty = false;
		if (this.getInitialValues() != null	&& this.getInitialValues().size() == 1) {
			if (!valorActual.equals(this.getInitialValues().get(0).getValor())) {
				this.dirty = true;
			}
		} else if (!TextUtils.isEmpty(valorActual)) {
			this.dirty = true;
		}

		return this.dirty;
	}

	@Override
	public boolean validate() {
		if(this.isDirty())
			return isValid();
		
		return true;
	}	
	
	private boolean isValid(){
		if(!validateOtroRitmo()) {
			return false;
		}
		
		if(!validateTextBox("Frecuencia Cardiaca", editRitmoLpm)) {
			return false;
		}
		if(!validateTextBox("Onda P", editOndaP)) {
			return false;
		}
		if(!validateTextBox("PR", editPR)) {
			return false;
		}
		if(!validateDerivaciones("Derivaciones Segmento ST", spinnerSegmST, ParamHelper.getString(ParamHelper.DERIVACIONES_SEGMENTO,"-1"), checkedSegmDer, "mm Segmento ST", editSegmST)) {
			return false;
		}
		if(!validateDerivaciones("Derivaciones Onda T", spinnerOndaT, ParamHelper.getString(ParamHelper.DERIVACIONES_ONDA_T,"-1"), checkedOndaDer, "", null)) {
			return false;
		}

        return validateMinLenght("Conclusiones", editConclusiones);
    }

	private boolean validateMinLenght(String fieldName, EditText txt) {
		if(txt.getText().length() < MAX_LENGHT_CONCLUSION){
			Toast.makeText(context, "El campo " + fieldName + " debe tener como mínimo 5 caracteres", Toast.LENGTH_SHORT).show();
			txt.requestFocus();
			return false;
		}
		return true;
	}

	private boolean validateOtroRitmo() {
		String ritmo = "," + ParamHelper.getString(ParamHelper.RITMO_OPCION_OTRO,"-1") + ",";
		boolean otroRitmo = ritmo.indexOf("," + ((SpinnerDTO)spinnerRitmo.getSelectedItem()).id + ",") != -1;
		if(otroRitmo) {
			return validateTextBox("Otro Ritmo", editOtroRitmo);
		}
		return true;
	}
	
	private boolean validateTextBox(String fieldName, EditText txt) {
		if(txt != null && TextUtils.isEmpty(txt.getText().toString())) {
			Toast.makeText(context, "El campo " + fieldName + " es obligatorio", Toast.LENGTH_SHORT).show();
			txt.requestFocus();
			return false;
		}
		return true;
	}
		
	private boolean validateDerivaciones(String fieldName, Spinner spinner, String dervEnabled, boolean[] checkedDer, String txtFieldName, EditText txt) {
		String derv = "," + dervEnabled + ",";
		boolean derEnabled = derv.indexOf("," + ((SpinnerDTO)spinner.getSelectedItem()).id + ",") != -1;				
		if(derEnabled) {
			boolean anyChecked = false;
            for (boolean aCheckedDer : checkedDer) {
                if (aCheckedDer) {
                    anyChecked = true;
                    break;
                }
            }

			if(!anyChecked) {
				Toast.makeText(context, "El campo " + fieldName + " es obligatorio", Toast.LENGTH_SHORT).show();
				spinner.requestFocusFromTouch();
				return false;
			}
			if(!validateTextBox(txtFieldName, txt)) {
				return false;
			}
		}
		
		return true;
	}

	private void builAreaImage(TableLayout mainLayout) {
		imgLayout = new LinearLayout(context);
		imgLayout.setOrientation(LinearLayout.HORIZONTAL);
		imgLayout.setPadding(0, 10, 0, 10);	
		
		imageView = new ImageView(context);
		imageView.setMaxWidth(520);		
		imgLayout.addView(imageView);	

	    if(this.electroDTO.electro != null && this.electroDTO.electro.length > 0) {
			Options options = new BitmapFactory.Options();
		    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		    options.inDither = false;
	    	Bitmap bitmap = Utils.getBitmapWitheBackground(BitmapFactory.decodeByteArray(this.electroDTO.electro, 0, this.electroDTO.electro.length, options));
	    	imageView.setImageBitmap(bitmap);
	    }
    	
		LinearLayout buttonsLayout = new LinearLayout(context);
		buttonsLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		buttonsLayout.setOrientation(LinearLayout.VERTICAL);
		buttonsLayout.setGravity(Gravity.CENTER_VERTICAL);

		LinearLayout leftButtons = new LinearLayout(context);
		leftButtons.setOrientation(LinearLayout.VERTICAL);
		leftButtons.setGravity(Gravity.TOP);
		leftButtons.setPadding(10, 0, 0, 0);
		ImageButton btnLeft = this.createButtonScroll(DIR_LEFT);
		leftButtons.addView(btnLeft);
		ImageButton btnRight = this.createButtonScroll(DIR_RIGHT);
		leftButtons.addView(btnRight);
		ImageButton btnUp = this.createButtonScroll(DIR_UP);
		leftButtons.addView(btnUp);
		ImageButton btnDown = this.createButtonScroll(DIR_DOWN);
		leftButtons.addView(btnDown);
		buttonsLayout.addView(leftButtons);

		LinearLayout rigthButtons = new LinearLayout(context);
		rigthButtons.setOrientation(LinearLayout.VERTICAL);
		rigthButtons.setGravity(Gravity.BOTTOM);
		rigthButtons.setPadding(10, 0, 0, 0);
		ImageButton btnZoomIn = this.createButtonZoom(ZOOM_IN);
		rigthButtons.addView(btnZoomIn);
		ImageButton btnZoomOut = this.createButtonZoom(ZOOM_OUT);
		rigthButtons.addView(btnZoomOut);
		buttonsLayout.addView(rigthButtons);

		imgLayout.addView(buttonsLayout);	
		mainLayout.addView(imgLayout);
	}
	
	private void buildAreaRitmo(TableLayout mainLayout) {
		//Ritmo: + combo + caja de texto otroRitmo
		editOtroRitmo = createTextBox(3, 0D, 0D, TipoTeclado.ALFANUMERICO, this.electroDTO.otroRitmo);
		editOtroRitmo.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		TextView labelRitmo = new TextView(context);
		labelRitmo.setTextColor(context.getResources().getColor(R.color.label_text_color));
		labelRitmo.setText("Ritmo: ");
		spinnerRitmo = createSpinner("Ritmo", "hcd_ritmoElectro", this.electroDTO.idRitmo, 310, "1");

		spinnerRitmo.setOnTouchListener((v, event) -> {
            spinnerRitmo.requestFocus();
            return false;
        });
		spinnerRitmo.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		buildItemSelectedListenerRitmo(spinnerRitmo);
		setOtroRitmo(this.electroDTO.idRitmo);
		
		// Container: Ritmo: spinnerRitmo + editOtroRitmo
		LinearLayout layoutContainer = new LinearLayout(context);
		layoutContainer.setOrientation(LinearLayout.HORIZONTAL);
		layoutContainer.setWeightSum(100f);

		LinearLayout layoutRitmo = new LinearLayout(context);//buildLayout(labelFC);
		layoutRitmo.addView(labelRitmo);
		layoutRitmo.addView(spinnerRitmo);

		LinearLayout layoutOtroRitmo = new LinearLayout(context);//buildLayout(labelFC);
		layoutOtroRitmo.addView(new TextView(context));
		layoutOtroRitmo.addView(editOtroRitmo);
		
		// Container Params
		LinearLayout.LayoutParams param;
		
		// Ritmo
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 65f;
		layoutContainer.addView(layoutRitmo, param);
		// Separador
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 5f;
		layoutContainer.addView(new LinearLayout(context), param);
		//OtroRitmo
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 30f;
		layoutContainer.addView(layoutOtroRitmo, param);
		// Separador
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 5f;
		layoutContainer.addView(new LinearLayout(context), param);

		// Build Section
		TableLayout mainTableLayout = new TableLayout(context);
		mainTableLayout.addView(layoutContainer);		
		mainLayout.addView(mainTableLayout);

	}

	private void buildAreaOndaP(TableLayout mainLayout) {
		//FC
		TextView labelFC = createLabel("FC: ", 0, 0);
		//
		editRitmoLpm = createTextBox(3, 0D, 300D, TipoTeclado.NUMERICO, this.electroDTO.fc);
		editRitmoLpm.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		//lpm
		TextView labelLpm = createLabel("lpm", 0, 0);
		//Onda P
		TextView labelOndaP = createLabel("Onda P: ", 0, 0);
		editOndaP = createTextBox(4, 0D, 1D, TipoTeclado.DECIMAL, this.electroDTO.ondaP);
		editOndaP.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		//PR
		TextView labelPR = createLabel("PR: ", 0, 0);
		//
		editPR = createTextBox(4, 0D, 1D, TipoTeclado.DECIMAL, this.electroDTO.pr);
		editPR.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Container: FC | ondaP | PR
		LinearLayout layoutContainer = new LinearLayout(context);
		layoutContainer.setOrientation(LinearLayout.HORIZONTAL);
		layoutContainer.setWeightSum(100f);

		LinearLayout layoutFC = new LinearLayout(context);//buildLayout(labelFC);
		layoutFC.addView(labelFC);
		layoutFC.addView(editRitmoLpm);

		LinearLayout layoutLpm = new LinearLayout(context);//buildLayout(labelFC);
		layoutLpm.addView(labelLpm);
		layoutLpm.setGravity(Gravity.CENTER_VERTICAL); //trick

		//OndaP
		LinearLayout layoutOndaP = new LinearLayout(context);//buildLayout(labelFC);
		layoutOndaP.addView(labelOndaP);
		layoutOndaP.addView(editOndaP);
		
		//PR
		LinearLayout layoutPR = new LinearLayout(context);//buildLayout(labelFC);
		layoutPR.addView(labelPR);
		layoutPR.addView(editPR);

		// Container Params
		LinearLayout.LayoutParams param;
		
		// Separador
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 5f;
		layoutContainer.addView(new LinearLayout(context), param);
		// FC
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 25f;
		layoutContainer.addView(layoutFC, param);
		//etiqueta lpm
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 10f;
		param.gravity=Gravity.CENTER_VERTICAL;
		layoutContainer.addView(layoutLpm, param);
		// Separador

		//OndaP
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 30f;
		layoutContainer.addView(layoutOndaP, param);
		// Separador
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 5f;
		layoutContainer.addView(new LinearLayout(context), param);
		//PR
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 25f;
		layoutContainer.addView(layoutPR, param);
				
		// Build Section
		TableLayout mainTableLayout = new TableLayout(context);
		mainTableLayout.addView(layoutContainer);		
		mainLayout.addView(mainTableLayout);
	}

	
	private void buildAreaWifi(TableLayout mainLayout) {
		LinearLayout rowPrint = new LinearLayout(context);
		rowPrint.setOrientation(LinearLayout.VERTICAL);
					
		btnWifi = this.createWifiButton();
		
		rowPrint.addView(btnWifi);
		mainLayout.addView(rowPrint);
	}

	private void buildAreaBloqueoRama(TableLayout mainLayout) {
		TableRow rowBloqueoRama = new TableRow(context);
		TableLayout tblBloqueoRama = new TableLayout(context);
		TableRow layoutBloqueoRama = new TableRow(context);
		
		layoutBloqueoRama.setGravity(Gravity.CENTER_VERTICAL);
		layoutBloqueoRama.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
				
		layoutBloqueoRama.addView(createLabel("Bloqueo de rama: ", 0, labelWidth));
		spinnerBloqueoRama = createSpinner("Bloqueo de rama", "hcd_bloqueoRamaElectro", this.electroDTO.idBloqueoRama, 150, "abreviatura");
		spinnerBloqueoRama.setOnTouchListener((v, event) -> {
            spinnerBloqueoRama.requestFocus();
            return false;
        });
		layoutBloqueoRama.addView(spinnerBloqueoRama);
				
		tblBloqueoRama.addView(layoutBloqueoRama);
		rowBloqueoRama.addView(tblBloqueoRama);
		mainLayout.addView(rowBloqueoRama);
	}
	
	private void preBuildAreaSegm() {
		initializeOptionsSegmDer(this.electroDTO.derivacionesSegm);
		selectedSegmDer = createLabel("",0,0);	
		btnSegmDer = new ImageButton(context);
	}

	private void buildAreaSegm(TableLayout mainLayout) {
		// Container: Segmento ST: | spinnerSegmST | editSegmST | mm

		//editSegmST
		editSegmST = createTextBox(2, 0D, 15D, TipoTeclado.NUMERICO, this.electroDTO.segmento);
		editSegmST.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		//label Segmento ST
		TextView labelSegmento = createLabel("Segmento ST: ", 0, 0);
		//combo spinnerSegmST
		spinnerSegmST = createSpinner("Segmento ST", "hcd_segmentoElectro", this.electroDTO.idSegmento, "2");
		spinnerSegmST.setOnTouchListener((v, event) -> {
            spinnerSegmST.requestFocus();
            return false;
        });
		buildItemSelectedListenerDerv(spinnerSegmST, ParamHelper.getString(ParamHelper.DERIVACIONES_SEGMENTO,"-1"), 
									  selectedSegmDer, btnSegmDer, checkedSegmDer, optionsSegmDer, editSegmST);
		spinnerSegmST.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		//label mm
		TextView labelMM = createLabel("mm", 0, 0);

		// Container: Segmento ST: | spinnerSegmST | editSegmST | mm
		LinearLayout layoutContainer = new LinearLayout(context);
		layoutContainer.setOrientation(LinearLayout.HORIZONTAL);
		layoutContainer.setGravity(Gravity.BOTTOM);
		layoutContainer.setWeightSum(100f);

		LinearLayout layoutSegmento = new LinearLayout(context);//buildLayout(labelFC);
		layoutSegmento.addView(labelSegmento);
		layoutSegmento.addView(spinnerSegmST);

		LinearLayout layoutEditSegmento = new LinearLayout(context);//buildLayout(labelFC);
		layoutEditSegmento.addView(editSegmST);

		LinearLayout layoutMmEditSegmento = new LinearLayout(context);//buildLayout(labelFC);
		layoutMmEditSegmento.addView(labelMM);
		layoutMmEditSegmento.setGravity(Gravity.TOP);
		
		// Container Params
		LinearLayout.LayoutParams param;
		
		// Segmento
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 70f;
		layoutContainer.addView(layoutSegmento, param);
		// Separador

		// EditSegmento
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 20f;
		layoutContainer.addView(layoutEditSegmento, param);
		//mm
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 10f;
		layoutContainer.addView(layoutMmEditSegmento, param);
		
		// Build Section
		TableLayout mainTableLayout = new TableLayout(context);
		mainTableLayout.addView(layoutContainer);		
		mainLayout.addView(mainTableLayout);

	}
		
	private void buildAreaSegmDer(TableLayout mainLayout) {		
		// Titulo: Se define un layout lineal horizonal para mostrar la etiqueta del campo y el boton para retraer/expandir
		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOrientation(LinearLayout.HORIZONTAL);
		titleLayout.setGravity(Gravity.CENTER_VERTICAL);
		titleLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// 	 Opciones
		selectedSegmDer.setMaxWidth((int) (350 * Resources.getSystem().getDisplayMetrics().density));
		selectedSegmDer.setMinimumWidth((int) (350 * Resources.getSystem().getDisplayMetrics().density));
		selectedSegmDer.setPadding(0, 5, 0, 5);
		titleLayout.addView(selectedSegmDer);
		
		// Boton de seleccion de check en lista
		LinearLayout btnSegmDerLayout = new LinearLayout(context);	
		this.createCheckList(btnSegmDer, "Derivaciones Segmento", optionsSegmDer, checkedSegmDer, selectedSegmDer);
		btnSegmDerLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		btnSegmDerLayout.addView(btnSegmDer);
		
		LinearLayout layoutContainer = new LinearLayout(context);
		layoutContainer.setOrientation(LinearLayout.HORIZONTAL);
		layoutContainer.setGravity(Gravity.BOTTOM);
		layoutContainer.setWeightSum(100f);
		
		// Container Params
		LinearLayout.LayoutParams param;

		// label
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 90f;
		layoutContainer.addView(titleLayout, param);

		// boton
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 10f;
		layoutContainer.addView(btnSegmDerLayout, param);

		// Build Section
		TableLayout mainTableLayout = new TableLayout(context);
		mainTableLayout.addView(layoutContainer);		
		mainLayout.addView(mainTableLayout);
	}
	
	private void preBuildAreaOndaT() {
		initializeOptionsOndaDer(this.electroDTO.derivacionesOndaT);
		selectedOndaTDer = createLabel("",0,0);		
		btnOndaDer = new ImageButton(context);
	}
	
	private void buildAreaOndaT(TableLayout mainLayout) {
		TableRow rowOndaT = new TableRow(context);
		TableLayout tblOndaT = new TableLayout(context);
		TableRow layoutOndaT = new TableRow(context);

		layoutOndaT.setGravity(Gravity.CENTER_VERTICAL);
		layoutOndaT.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
				
		layoutOndaT.addView(createLabel("Onda T: ", 0, labelWidth));
		spinnerOndaT = createSpinner("Onda T", "hcd_ondaTElectro", this.electroDTO.idOndaT, "2");
		spinnerOndaT.setOnTouchListener((v, event) -> {
            spinnerOndaT.requestFocus();
            return false;
        });
		buildItemSelectedListenerDerv(spinnerOndaT, ParamHelper.getString(ParamHelper.DERIVACIONES_ONDA_T,"-1"), 
				selectedOndaTDer, btnOndaDer, checkedOndaDer, optionsOndaDer, null);
		
		layoutOndaT.addView(spinnerOndaT);
		
		tblOndaT.addView(layoutOndaT);
		rowOndaT.addView(tblOndaT);
		mainLayout.addView(rowOndaT);
	}
	
	private void buildAreaOndaTDer(TableLayout mainLayout) {		
		// Titulo: Se define un layout lineal horizonal para mostrar la etiqueta del campo y el boton para retraer/expandir
		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOrientation(LinearLayout.HORIZONTAL);
		titleLayout.setGravity(Gravity.CENTER_VERTICAL);
		titleLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// 	 Opciones
		selectedOndaTDer.setMaxWidth((int) (350 * Resources.getSystem().getDisplayMetrics().density));
		selectedOndaTDer.setMinimumWidth((int) (350 * Resources.getSystem().getDisplayMetrics().density));
		selectedOndaTDer.setPadding(0, 5, 0, 5);
		titleLayout.addView(selectedOndaTDer);
		
		// Boton de seleccion de check en lista
		LinearLayout btnOndaDerLayout = new LinearLayout(context);
		this.createCheckList(btnOndaDer, "Derivaciones Onda T", optionsOndaDer, checkedOndaDer, selectedOndaTDer);
		btnOndaDerLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		btnOndaDerLayout.addView(btnOndaDer);

		LinearLayout layoutContainer = new LinearLayout(context);
		layoutContainer.setOrientation(LinearLayout.HORIZONTAL);
		layoutContainer.setGravity(Gravity.BOTTOM);
		layoutContainer.setWeightSum(100f);
		
		// Container Params
		LinearLayout.LayoutParams param;

		// label
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 90f;
		layoutContainer.addView(titleLayout, param);

		// boton
		param = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
		param.weight = 10f;
		layoutContainer.addView(btnOndaDerLayout, param);

		// Build Section
		TableLayout mainTableLayout = new TableLayout(context);
		mainTableLayout.addView(layoutContainer);		
		mainLayout.addView(mainTableLayout);
	}
	
	private void buildAreaConclusiones(TableLayout mainLayout) {
		LinearLayout rowConc = new LinearLayout(context);
		rowConc.setOrientation(LinearLayout.VERTICAL);
					
		rowConc.addView(createLabel("Conclusiones: ", 0, labelWidth));

		editConclusiones = new EditText(context);
		editConclusiones.setGravity(Gravity.TOP);
		editConclusiones.setSingleLine(false); 
		editConclusiones.setInputType(InputType.TYPE_CLASS_TEXT |
										InputType.TYPE_TEXT_FLAG_MULTI_LINE |
										InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		editConclusiones.setLines(3);
		editConclusiones.setMaxLines(3);
		editConclusiones.setText(this.electroDTO.conclusiones);
		editConclusiones.setEnabled(this.enabled);
		editConclusiones.setFocusable(this.enabled);
		rowConc.addView(editConclusiones);

		mainLayout.addView(rowConc);
	}

	private ImageButton createButtonScroll(final int direction){
		final ImageButton btn = new ImageButton(context);
		Drawable draw = context.getResources().getDrawable(R.drawable.ic_arrow_left);
		switch(direction)
		{
			case DIR_UP:
				draw = context.getResources().getDrawable(R.drawable.ic_arrow_up);
			break;
			case DIR_RIGHT:
				draw = context.getResources().getDrawable(R.drawable.ic_arrow_right);
			break;
			case DIR_DOWN:
				draw = context.getResources().getDrawable(R.drawable.ic_arrow_down);
			break;
			case DIR_LEFT:
				draw = context.getResources().getDrawable(R.drawable.ic_arrow_left);
			break;
		}		

		btn.setPadding(10, 10, 10, 10);
		btn.setEnabled(true);
		btn.setFocusable(true);
		btn.setBackground(draw);
		btn.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));		
		btn.setOnClickListener(v -> moveImage(direction));
		return btn;
	}
	
	private void moveImage(int direction){
		try {
			ImageView view = imageView;
			view.setScaleType(ImageView.ScaleType.MATRIX);
			float scale = 10;
			switch(direction) {
				case DIR_UP:
					matrix.postTranslate(0, -scale);
				break;
				case DIR_RIGHT:
					matrix.postTranslate(scale, 0);
				break;
				case DIR_DOWN:
					matrix.postTranslate(0, scale);
				break;
				case DIR_LEFT:
					matrix.postTranslate(-scale, 0);
				break;
			}
	
			view.setImageMatrix(matrix);
		}
		catch(Exception ex){
        	Toast.makeText(context, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();   
		}
	}
	
	private ImageButton createButtonZoom(final int zoom){
		final ImageButton btn = new ImageButton(context);

		Drawable draw = context.getResources().getDrawable(R.drawable.ic_zoom_in);
		switch(zoom) {
			case ZOOM_IN:
				draw = context.getResources().getDrawable(R.drawable.ic_zoom_in);
			break;
			case ZOOM_OUT:
				draw = context.getResources().getDrawable(R.drawable.ic_zoom_out);
			break;
		}
		btn.setPadding(10, 10, 10, 10);
		btn.setEnabled(true);
		btn.setFocusable(true);
		btn.setBackground(draw);
		btn.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		btn.setOnClickListener(v -> zoomImage(zoom));
		return btn;
	}
	
	private void zoomImage(int zoom){
		try {
			ImageView view = imageView;
			view.setScaleType(ImageView.ScaleType.MATRIX);
			switch(zoom) {
				case ZOOM_IN:
					matrix.postScale(1.1F, 1.1F);
					break;
				case ZOOM_OUT:
					matrix.postScale(0.9F, 0.9F);
					break;
			}
	
			view.setImageMatrix(matrix);
		}
		catch(Exception ex){
        	Toast.makeText(context, ex.getMessage().toString(), Toast.LENGTH_SHORT).show();   
		}
	}

	private TextView createLabel(String lableText, int paddingLeft, int width){
		TextView label = new TextView(context);
		label.setGravity(Gravity.BOTTOM);
		label.setPadding(paddingLeft, 0, 0, 0);
		if(width != 0)
			label.setMinimumWidth(width);
		label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		label.setText(lableText);
		return label;
	}
	
	private Spinner createSpinner(String spinnerPrompt, String tableName, int selectedValue, String colOrder) {
		return createSpinner(spinnerPrompt, tableName, selectedValue, "", colOrder);
	}
	
	private Spinner createSpinner(String spinnerPrompt, String tableName, int selectedValue, String extraColumn, String colOrder) {
		return createSpinner(spinnerPrompt, tableName, selectedValue, 250, extraColumn, colOrder);
	}
	
	private Spinner createSpinner(String spinnerPrompt, String tableName, int selectedValue, int spinnerWidth, String colOrder) {
		return createSpinner(spinnerPrompt, tableName, selectedValue, spinnerWidth, "", colOrder);
	}
	
	private Spinner createSpinner(String spinnerPrompt, String tableName, int selectedValue, int spinnerWidth, String extraColumn, String colOrder) {
		Spinner spinner = new Spinner(context);
		String[] columns = new String[] {"id", "descripcion" };
		if(!TextUtils.isEmpty(extraColumn)) {
			columns = new String[] {"id", "descripcion", extraColumn };
		}
		buildSpinner(spinner, tableName, columns, selectedValue, colOrder);
		spinner.setPrompt(spinnerPrompt);
		spinner.setEnabled(this.enabled);
		spinner.setFocusable(this.enabled);
		spinner.setFocusableInTouchMode(this.enabled);
		spinner.setMinimumWidth(spinnerWidth);
		
		return spinner;
	}
	
	private void buildSpinner(Spinner spinner, String tableName, final String[] columns, int idSelected, String colOrder) {
		List<SpinnerDTO> listItem = new ArrayList<SpinnerDTO>();
		StringBuilder selection = new StringBuilder("deleted = 0");

		int position = 0;
		SQLHelper sqlHelper = new SQLHelper(context);
		sqlHelper.openDatabase();
		try {
			// Execute query
			Cursor cursor = sqlHelper.query(tableName, columns, selection.toString(), null, null, null, colOrder);
			
			while (cursor.moveToNext()) {
				SpinnerDTO item = new SpinnerDTO();
				item.id = cursor.getInt(0);
				item.description = cursor.getString(1);
				if(columns.length > 2) {
					item.shortDesc = cursor.getString(2);
				}

				if (idSelected == item.id){
					position = listItem.size();
				}
				listItem.add(item);
			} 
			cursor.close();
		} catch (Exception e) {
			Log.e(LOG_TAG, "build: **ERROR**", e);
		} finally {
			// Close connection
			sqlHelper.closeDatabase();
		}

		final ArrayAdapter<SpinnerDTO> arrayAdapter;
		arrayAdapter = new ArrayAdapter<SpinnerDTO>(context, android.R.layout.simple_spinner_item, listItem) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView holder;
				if (convertView == null) {
					convertView = View.inflate(context, android.R.layout.simple_spinner_item, null);
					holder = convertView.findViewById(android.R.id.text1);
					holder.setTextColor(context.getResources().getColor(R.color.label_text_color));
					convertView.setTag(holder);
				} else {
					holder = (TextView) convertView.getTag();
				}

				if(columns.length > 2) {
					holder.setText(this.getItem(position).shortDesc);
				}
				else {
					holder.setText(this.getItem(position).description);
				}
				return convertView;
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent) {
				TextView holder;
				if (convertView == null) {
					convertView = View.inflate(context, android.R.layout.simple_spinner_dropdown_item, null);
					holder = convertView.findViewById(android.R.id.text1);
					convertView.setTag(holder);
				} else {
					holder = (TextView) convertView.getTag();
				}
				if (columns.length > 2) {
					holder.setText(this.getItem(position).shortDesc + " - " + this.getItem(position).description);
				}
				else {
					holder.setText(this.getItem(position).description);
				}
				return convertView;
			}
		};
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner.setAdapter(arrayAdapter);
		spinner.setSelection(position, false);
	}
	
	private void buildItemSelectedListenerRitmo(Spinner spinner) {
		final ArrayAdapter<SpinnerDTO> arrayAdapter = (ArrayAdapter<SpinnerDTO>)spinner.getAdapter();
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {
                SpinnerDTO itemActual = arrayAdapter.getItem(pos);
                setOtroRitmo(itemActual.id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
	}

	private void setOtroRitmo(int selectedRitmoID){
		String ritmo = "," + ParamHelper.getString(ParamHelper.RITMO_OPCION_OTRO,"-1") + ",";
		boolean otroRitmo = ritmo.indexOf("," + selectedRitmoID + ",") != -1;
		if(!otroRitmo) {
			editOtroRitmo.setText("");
		}
		editOtroRitmo.setEnabled(otroRitmo);
		editOtroRitmo.setFocusable(otroRitmo);
		editOtroRitmo.setFocusableInTouchMode(otroRitmo);	
	}
	
	private void buildItemSelectedListenerDerv(Spinner spinner, final String dervEnabled, final TextView txtDeriv, 
			final ImageButton btnDeriv, final boolean[] checkedDer, final CharSequence[] options,
			final EditText editSegm) {
		final ArrayAdapter<SpinnerDTO> arrayAdapter = (ArrayAdapter<SpinnerDTO>)spinner.getAdapter();
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {
                String derv = "," + dervEnabled + ",";
                SpinnerDTO itemActual = arrayAdapter.getItem(pos);
                boolean derEnabled = derv.indexOf("," + itemActual.id + ",") != -1;
                btnDeriv.setEnabled(derEnabled);
                for(int i=0; i< checkedDer.length; i++){
                    checkedDer[i] = false;
                }
                if(editSegm != null) {
                    editSegm.setEnabled(derEnabled);
                    editSegm.setFocusable(derEnabled);
                    editSegm.setFocusableInTouchMode(derEnabled);
                }
                updateDisplay(options, checkedDer, txtDeriv);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });
	}

	private EditText createTextBox(int charCount, Double minValue, Double maxValue, TipoTeclado tipoTeclado, String initialValue) {
		final EditText editText = new EditText(context);

		editText.setText(initialValue);
		editText.setEnabled(this.enabled);
		editText.setFocusable(this.enabled);
		
		boolean allowDecimal;
		
		switch(tipoTeclado) {
			case NUMERICO:
				editText.setInputType(InputType.TYPE_CLASS_PHONE);
				allowDecimal = false;
				break;
			case DECIMAL:
				editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
				allowDecimal = true;
				break;
			default:
				editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE |
										InputType.TYPE_CLASS_TEXT |
										InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
				return editText;			
		}
		
		final Double min = minValue;
		final Double max = maxValue;

		final String minStr = allowDecimal ? minValue.toString() : Integer.valueOf(minValue.intValue()).toString();
		final String maxStr = allowDecimal ? maxValue.toString() : Integer.valueOf(maxValue.intValue()).toString();
		
		InputFilter maxLengthFilter = new InputFilter.LengthFilter(charCount);
		if(!allowDecimal) {
			InputFilter filterEntero = new InputFilter() { 
		        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) { 
                    for (int i = start; i < end; i++) {
                        if (!Character.isDigit(source.charAt(i))) {
                            return "";
                        }
                    }
                    return null;
		        } 
			}; 
			editText.setFilters(new InputFilter[]{filterEntero, maxLengthFilter}); 
		}
		else {
			editText.setFilters(new InputFilter[]{maxLengthFilter});
		} 

		
		editText.addTextChangedListener(
			new TextWatcher(){
			    public void afterTextChanged(Editable s) {}
			    
			    public void beforeTextChanged(CharSequence s, int start, int count, int after){}

			    public void onTextChanged(CharSequence s, int start, int before, int count){
			        String strEnteredVal = editText.getText().toString();

			        if (strEnteredVal.isEmpty()){ return; }

					if(strEnteredVal.startsWith(".")){
						Toast.makeText(context, "El valor ingresado es incorrecto", Toast.LENGTH_SHORT).show();
						editText.setText("");
					}
					else {
						Double value = Double.parseDouble(strEnteredVal);
						if(value > max){
							Toast.makeText(context, "El valor no debe superar " + maxStr, Toast.LENGTH_SHORT).show();
							editText.setText(max.toString());
						}
						else if(value < min){
							Toast.makeText(context, "El valor debe ser mayor a " + minStr, Toast.LENGTH_SHORT).show();
							editText.setText(min.toString());
						}
					}

				}
			}); 
		return editText;
	}

	private void createCheckList(ImageButton imgButton, final String dialogTitle, final CharSequence[] options, final boolean[] checked, final TextView selectedView) {
		imgButton.setBackgroundResource(R.drawable.ic_check_list);
		imgButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		imgButton.setEnabled(enabled);
		imgButton.setFocusable(enabled);
		imgButton.setOnClickListener(v -> {
            createDialog(dialogTitle, options, checked, selectedView).show();
		});
		imgButton.setEnabled(this.enabled);
		imgButton.setFocusable(this.enabled);
		updateDisplay(options, checked, selectedView);
	}
		
	private Dialog createDialog(String dialogTitle, final CharSequence[] options, final boolean[] checked, final TextView selectedView) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
		builder.setTitle(dialogTitle);
		builder.setPositiveButton(R.string.accept, (dialog, clicked) -> {
            switch(clicked){
            case DialogInterface.BUTTON_POSITIVE:
                dirty = true;
                updateDisplay(options, checked, selectedView);
                break;
            }
        });
		return builder.create();
	}
		
	private List<SpinnerDTO> initializeOptions(String tableName) {
		String[] columns = new String[] {"id", "descripcion" };
		List<SpinnerDTO> listItem = new ArrayList<SpinnerDTO>();
		StringBuilder selection = new StringBuilder("deleted = 0");

		SQLHelper sqlHelper = new SQLHelper(context);
		sqlHelper.openDatabase();
		try {
			// Execute query
			Cursor cursor = sqlHelper.query(tableName, columns, selection.toString(), null, null, null, "1");
			
			while (cursor.moveToNext()) {
				SpinnerDTO item = new SpinnerDTO();
				item.id = cursor.getInt(0);
				item.description = cursor.getString(1);
				listItem.add(item);
			} 
			cursor.close();
		} catch (Exception e) {
			Log.e(LOG_TAG, "build: **ERROR**", e);
		} finally {
			// Close connection
			sqlHelper.closeDatabase();
		}
		
		return listItem;
	}
	
	private void initializeOptionsSegmDer(int[] selectedIDs) {
		List<SpinnerDTO> listItem = initializeOptions("hcd_derivacionesSegmentoElectro");
		int i = 0;
		optionsSegmDer = new CharSequence[listItem.size()];
		checkedSegmDer = new boolean[listItem.size()];
		mapOptionsSegDer = new HashMap<>();
		
		for(SpinnerDTO item: listItem ){
			this.mapOptionsSegDer.put(i, item.id);			
			optionsSegmDer[i] = item.description;
            for (int selectedID : selectedIDs) {
                if (selectedID == item.id) {
                    checkedSegmDer[i] = true;
                    break;
                }
            }
			i++;
		}
	}
	
	private void initializeOptionsOndaDer(int[] selectedIDs) {
		List<SpinnerDTO> listItem = initializeOptions("hcd_derivacionesOndaTElectro");
		int i = 0;
		optionsOndaDer = new CharSequence[listItem.size()];
		checkedOndaDer = new boolean[listItem.size()];
		mapOptionsOndaDer = new HashMap<>();

		for(SpinnerDTO item: listItem ){
			this.mapOptionsOndaDer.put(i, item.id);	
			optionsOndaDer[i] = item.description;
            for (int selectedID : selectedIDs) {
                if (selectedID == item.id) {
                    checkedOndaDer[i] = true;
                    break;
                }
            }
			i++;
		}
	}
		
	private void updateDisplay(CharSequence[] options, boolean[] checked, TextView selectedView){
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < options.length; i++){
			if(checked[i]) {
				sb.append(options[i]);
				sb.append(" - ");
			}
		}
		String selectedOptions = sb.toString();
		if(selectedOptions.lastIndexOf(" - ") > -1) {
			selectedOptions = selectedOptions.substring(0, selectedOptions.lastIndexOf(" - "));
		}
		selectedView.setText(selectedOptions);
	}	
	
	@SuppressLint("ClickableViewAccessibility")
	public void setImageBitmap() {
		String path = this.getPathECG();
        String nombreEstudio = "";
        String filePath = path + nombreEstudio + ".jpg";
		
		try {
			File electroFile = new File(filePath);
			Uri imageData = Uri.fromFile(electroFile);
			
			InputStream iStream = context.getContentResolver().openInputStream(imageData);
			byte[] inputData = getBytes(iStream);
			this.electroDTO.electro = getCompressImage(inputData);	

			Options options = new BitmapFactory.Options();
		    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		    options.inDither = false;
	    	Bitmap bitmap = Utils.getBitmapWitheBackground(BitmapFactory.decodeByteArray(this.electroDTO.electro, 0, this.electroDTO.electro.length, options));
	    	imageView.setImageBitmap(bitmap);
	
			this.setImageBehavior(true);
			
			if(electroFile.exists()){
				electroFile.delete();
			}
			
			if(ParamHelper.getString(ParamHelper.ELECTRO_COMPLETO,"true").equals("true")) {
				File electroCompletoFile = new File(path + "/", nombreEstudio + ".eevm");
				if(electroCompletoFile.exists()){
					Uri electro = Uri.fromFile(electroCompletoFile);
					InputStream electroStream = context.getContentResolver().openInputStream(electro);
					this.electroDTO.electroCompleto = getBytes(electroStream);
					electroCompletoFile.delete();
				}
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			this.setImageBehavior(false);
		}
		catch (IOException e) {
			e.printStackTrace();
			this.setImageBehavior(false);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private byte[] getCompressImage(byte[] data){
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inSampleSize = 2; //try to decrease decoded image
	    options.inPurgeable = true; //purgeable to disk
		Bitmap imageBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
		Bitmap bitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(imageBitmap, 0, 0, null);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);		
		return outStream.toByteArray();
	}
	
	private byte[] getBytes(InputStream inputStream) throws IOException {
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		int bufferSize = 1024;
		byte[] buffer = new byte[bufferSize];
		int len = 0;
		while ((len = inputStream.read(buffer)) != -1) {
			byteBuffer.write(buffer, 0, len);
		}
		return byteBuffer.toByteArray();
    }
		
	private void setImageBehavior(boolean withImage){
		electroLayout.setVisibility(View.VISIBLE);
		imgLayout.setVisibility(View.GONE);
		
		if(withImage) {
			imgLayout.setVisibility(View.VISIBLE);
			imageView.setAdjustViewBounds(true);
			imageView.setMaxWidth(520);
			imageView.setOnTouchListener((v, event) -> {
                ImageView view = (ImageView) v;
                view.setScaleType(ImageView.ScaleType.MATRIX);
                float scale;

                switch (event.getAction() & MotionEvent.ACTION_MASK) {

                    /*
                      First finger down only
                     */
                    case MotionEvent.ACTION_DOWN:
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        mode = DRAG;
                        break;

                    /*
                     * Second finger lifted
                     */
                    case MotionEvent.ACTION_POINTER_UP:
                        mode = NONE;
                        break;

                    /*
                     * First and second finger down
                     */
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist = spacing(event);

                        if (oldDist > 5f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        /*
                         * Create the transformation in the matrix of points
                         */
                        if (mode == DRAG) {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);

                        } else if (mode == ZOOM) {
                            /*
                             * Pinch zooming
                             */
                            float newDist = spacing(event);

                            /*
                             * Setting the scaling of the matrix...if scale > 1 means zoom in...if scale < 1 means zoom out
                             */
                            if (newDist > 5f) {
                                matrix.set(savedMatrix);
                                scale = newDist / oldDist;
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                        break;
                }

                /*
                 * Display the transformation on screen
                 */
                view.setImageMatrix(matrix);
                return true;
            });
		}

		isAddAction = false;
		newElectro = false;
	}
		
	@Override
	public void clearData(){
		electroLayout.setVisibility(View.GONE);
		imageView.setImageResource(android.R.color.transparent);
		isAddAction = true;
		newElectro = true;

		spinnerRitmo.setSelection(0, false);
		editOtroRitmo.setText("");
		editRitmoLpm.setText("");
		
		editOndaP.setText("");
		editPR.setText("");
		
		spinnerSegmST.setSelection(0, false);
		editSegmST.setText("");
		for (int i = 0; i < checkedSegmDer.length; i++) {
			checkedSegmDer[i] = false;
		}
		updateDisplay(optionsSegmDer, checkedSegmDer, selectedSegmDer);

		spinnerOndaT.setSelection(0, false);	
		for (int i = 0; i < checkedOndaDer.length; i++) {
			checkedOndaDer[i] = false;
		}
		updateDisplay(optionsOndaDer, checkedOndaDer, selectedOndaTDer);		
		
		editConclusiones.setText("");
		
		this.electroDTO.electro = null;
		this.electroDTO.electroCompleto = null;
	}
		
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return Float.valueOf(String.valueOf(Math.sqrt(x * x + y * y)));
	}
		
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private String getPathECG() {
		String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath(); 
		path = path + "/" + ParamHelper.getString(ParamHelper.CARPETA_ELECTRO,"ecg_trazados") + "/";
		//path = path + "/" + ParamHelper.getString(ParamHelper.CARPETA_ELECTRO,"/storage/sdcard0/ecg_trazados/");
		return path;
	}
	
	@Override
	public void setFocus() {
		this.electroLayout.requestFocusFromTouch();
	}
	
	@Override
	public View getEditViewForCombo(){
		return this.view;
	}
	
	@Override
	public void customonItemSelected(){
		if("SI".equalsIgnoreCase(this.toString())){
		
			//no deberia activar el anclaje si la atención está cerrada:
			if(!enabled){
				return;
			}
			
			try {
				// si el anclaje de red está prendido, lo apago, si el anclaje de red está apagado, lo prendo
				boolean isHotSpotEnabled = HotspotUtils.isWifiHotspotEnabled(context);
				if (!isHotSpotEnabled) {
					// » si está Inactivo, deberá activarlo mostrando el mensaje "Anclaje Activo para transmitir el estudio ECG" [Aceptar]
					if (!HotspotUtils.enableWifiHotspotAndCheck(context)) {
						HotspotDialog.showDialog(getContext(), R.string.active_anclaje_para_transmitir);
					}
				}
				else {
					// » si está Activo, mensaje "Asegúrese que el estudio ECG sea transmitido" [Aceptar]
					HotspotDialog.showDialog(context, R.string.transmitir_ecg);
				}
			}
			catch (Exception e){
				Log.e(TAG, "Intent wifi: " + e);
			}
		}
	}

	private Button createWifiButton() {
		Button button = new Button(context);
		button.setPadding(10, 10, 10, 10);
		button.setEnabled(true);
		button.setFocusable(true);
		button.setText(R.string.conectar_anclaje);
		button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		button.setOnClickListener(v -> HotspotUtils.toggleHotspotStatus(context));
		return button;
	}

}
