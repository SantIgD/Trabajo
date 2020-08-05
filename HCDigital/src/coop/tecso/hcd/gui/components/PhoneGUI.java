package coop.tecso.hcd.gui.components;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

/**
 * Tratamiento llamada Telefónica
 * 
 * @author tecso.coop
 *
 */
@SuppressWarnings({"ClickableViewAccessibility"})
public final class PhoneGUI extends CampoGUI {

	private static final String TAG = PhoneGUI.class.getSimpleName();

	private LinearLayout mainLayout;
	private LinearLayout buttonLayout;
	private Button button;
	private String phoneNumber;

	// MARK: - Constructs

	public PhoneGUI(Context context, boolean enabled) {
		super(context, enabled);
	}

	// MARK: - Getters y Setters
	public String getValorView() {
		if (getInitialValues() != null) {
			return getInitialValues().get(0).getValor();
		}
		else {
			return this.valorDefault;
		}
	}

	// MARK: - Metodos

	@Override
	public View build() {
		String label = this.getEtiqueta() + ": ";

		// Etiqueta
		this.label = new TextView(context);
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(label);

		// Se define un LinearLayout para ubicar: 'Label / EditText'
		this.mainLayout = new LinearLayout(context);
		this.mainLayout.setOrientation(LinearLayout.VERTICAL);
		this.mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.mainLayout.setGravity(Gravity.CENTER_VERTICAL);

		phoneNumber = "";
		if (getInitialValues() != null && getInitialValues().size() >= 1 && !TextUtils.isEmpty(getInitialValues().get(0).getValor())) {
			phoneNumber = getInitialValues().get(0).getValor();
			Log.d(TAG,"Value from InitialValues: " + phoneNumber);
		}
		
		// Se arma el boton para disparar control se seleccion de fecha
		this.button = new Button(context);
		this.button.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.button.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(android.R.drawable.sym_action_call), null, null, null);
		this.button.setText(PhoneNumberUtils.formatNumber(phoneNumber));
		this.button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.button.setFocusable(true);
		this.button.setFocusableInTouchMode(true);

		if (TextUtils.isEmpty(phoneNumber)) {
			this.button.setText(context.getString(R.string.sin_numero));
			this.button.setEnabled(false);
		} else {
			this.button.setOnTouchListener((v, event) -> {
				button.requestFocus();
				return false;
			});
			final String phonePrefix = ParamHelper.getString(ParamHelper.PHONE_PREFIX_CODE,"");
			this.button.setOnClickListener(v -> {
				Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(phonePrefix + phoneNumber)));
				Log.d(TAG, "Intent without destination: Call " + phonePrefix + phoneNumber);
				context.startActivity(intent);
			});
		}

		// Se contiene el boton en un linear layout para que no se expanda junto a la columna
		this.buttonLayout = new LinearLayout(context);
		this.buttonLayout.addView(button);

		// Se cargan los componentes en el layout
		this.mainLayout.addView(this.label);
		this.mainLayout.addView(this.buttonLayout);

		this.view = mainLayout;

		return this.view;
	}

	@Override
	public View redraw() {
		this.button.setEnabled(enabled);
		return this.view;
	}

	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();

		AplPerfilSeccionCampo campo = null;
		AplPerfilSeccionCampoValor campoValor = null;
		AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;
		if (this.entity instanceof AplPerfilSeccionCampo) {
			campo = (AplPerfilSeccionCampo) this.entity;
		} else if (this.entity instanceof AplPerfilSeccionCampoValor) {
			campoValor = (AplPerfilSeccionCampoValor) this.entity;
			campo = campoValor.getAplPerfilSeccionCampo();
		} else if (this.entity instanceof AplPerfilSeccionCampoValorOpcion) {
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) this.entity;
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}
		String nombreCampo = campo!=null?campo.getCampo().getEtiqueta():"No identificado";
		String valor = this.getValorView();

		Log.d(TAG, "save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
				+" idCampo: "+(campo!=null?campo.getId():"null")
				+", idCampoValor: "+(campoValor!=null?campoValor.getId():"null")
				+", idCampoValorOpcion: "+(campoValorOpcion!=null?campoValorOpcion.getId():"null")
				+", Valor: "+valor);

		Value data = new Value(campo,campoValor,campoValorOpcion,valor, null);
		this.values.add(data);

		return this.values;
	}

	@Override
	public View getEditViewForCombo(){
		return this.buttonLayout;
	}

	@Override
	public void removeAllViewsForMainLayout(){
		this.mainLayout.removeAllViews();
	}

	@Override
	public void setFocus() {
		this.mainLayout.requestFocusFromTouch();
	}

}