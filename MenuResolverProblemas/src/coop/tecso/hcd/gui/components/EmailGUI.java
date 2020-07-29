package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

public final class EmailGUI extends CampoGUI {

	private final static int MAX_LENGHT = 250;

	private LinearLayout mainLayout;

	private EditText textBox;

	// MARK: - Constructor

	public EmailGUI(Context context, boolean enabled) {
		super(context, enabled);
	}

    // MARK: - Methods

	public String getValorView() {
		return this.textBox.getText().toString().trim();
	}

	@SuppressLint("SetTextI18n")
	@Override
	public View build() {
		// Etiqueta
		this.label = new TextView(context);
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(this.getEtiqueta()+": ");

		this.textBox = new EditText(context);
		this.textBox.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE |
				 					InputType.TYPE_CLASS_TEXT |
									InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
		this.textBox.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f));

		// Se define un LinearLayout para ubicar: 'Label / EditText'
		this.mainLayout = new LinearLayout(context);
		this.mainLayout.setOrientation(LinearLayout.VERTICAL);
		this.mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.mainLayout.setGravity(Gravity.CENTER_VERTICAL);

		// Texto editable
		this.textBox.setEnabled(enabled);
		this.textBox.setFocusable(enabled);

        List<Value> initialValues = this.getInitialValues();
		if (!CollectionUtils.isEmpty(initialValues)) {
			this.textBox.setText(initialValues.get(0).getValor());
		} else {
			this.textBox.setText(this.getValorDefault());
		}

		// Se crea y aplica un filtro para definir la cantidad maxima de caracteres de la caja de texto
		InputFilter maxLengthFilter = new InputFilter.LengthFilter(MAX_LENGHT);
		this.textBox.setFilters(new InputFilter[]{ maxLengthFilter });

		// Se agrega validacion de requerido al cambio de foco
		this.textBox.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                validate();
            }

            evalCondicionalSoloLectura();
        });
		
		this.textBox.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(!isCampoValido()){
					textBox.setError(null);
					setCampoValido(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {}
		});

		this.mainLayout.addView(label);
		LinearLayout container = new LinearLayout(context);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.addView(textBox);
		this.mainLayout.addView(container);
		this.view = mainLayout;

		return this.view;
	}

	@Override
	public View redraw() {
		this.textBox.setEnabled(enabled);
		this.textBox.setFocusable(enabled);	
		this.textBox.setFocusableInTouchMode(enabled);
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
		} else if(this.entity instanceof AplPerfilSeccionCampoValor) {
			campoValor = (AplPerfilSeccionCampoValor) this.entity;
			campo = campoValor.getAplPerfilSeccionCampo();
		} else if(this.entity instanceof AplPerfilSeccionCampoValorOpcion) {
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) this.entity;
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}
		String nombreCampo = campo!=null?campo.getCampo().getEtiqueta():"No identificado";
		String valor = this.getValorView();

		Log.d(EmailGUI.class.getSimpleName(),"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
				+" idCampo: "+(campo!=null?campo.getId():"null")
				+", idCampoValor: "+(campoValor!=null?campoValor.getId():"null")
				+", idCampoValorOpcion: "+(campoValorOpcion!=null?campoValorOpcion.getId():"null")
				+", Valor: "+valor);

		Value data = new Value(campo, campoValor, campoValorOpcion, valor, null);
		this.values.add(data);

		return this.values;
	}

	@Override
	public boolean isDirty() {
		if (!super.isDirty()) {
			String valorActual = this.getValorView();
            List<Value> initialValues = this.getInitialValues();
			if (!CollectionUtils.isEmpty(initialValues)) {
				if (!valorActual.equals(initialValues.get(0).getValor())) {
					this.dirty = true;
				}
			} else if(!TextUtils.isEmpty(valorActual)) {
				this.dirty = true;
			}
		}

		return super.isDirty();
	}

	@Override
	public boolean validate() {
		String errMsg = context.getResources().getString(R.string.mail_invalido, getEtiqueta());
		if (isObligatorio() || !TextUtils.isEmpty(this.textBox.getText().toString())) {
			if (TextUtils.isEmpty(this.textBox.getText().toString())) {
				this.textBox.setError(context.getString(R.string.field_required, getEtiqueta()));
				return false;
			}
			else if (!isValidEmailAddress(this.textBox.getText().toString())) {
				this.textBox.setError(errMsg);
				Toast.makeText(context, errMsg,Toast.LENGTH_LONG).show();
				return false;
			} 
		}

		return true;
	}
	
	@Override
	public void setFocus() {
		this.textBox.requestFocus();
	}
	
	@Override
	public View getEditViewForCombo() {
		return this.textBox;
	}

	@Override
	public void removeAllViewsForMainLayout() {
		this.mainLayout.removeAllViews();
	}

	
	/**
	 * Valida formato de e-mail
	 */
	private boolean isValidEmailAddress(String email) {
       Pattern pattern = Pattern.compile(".+@.+\\.[a-z]+");
       Matcher matcher = pattern.matcher(email);
       return matcher.matches();
	}

	public void setFocus(boolean focus){
		if (this.textBox == null) {
		    return;
		}

		if (focus) {
			this.textBox.requestFocus();
		}
		else {
			this.textBox.clearFocus();
		}
	}
	
	@Override
	public void clearData() {
		this.textBox.setText(this.getValorDefault());
		this.textBox.setError(null);
	}

}
