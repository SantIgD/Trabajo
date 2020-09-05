package coop.tecso.hcd.gui.components;

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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

/**
 * 
 * @author tecso.coop
 *
 */
public final class TextGUI extends CampoGUI {

	public enum Teclado {
		ALFANUMERICO, NUMERICO, DECIMAL, NUMERICO_EXTENDIDO
	}

	private boolean multiLine;
	private Teclado teclado;
    private int maxChar = 250;

	private LinearLayout mainLayout;
	private EditText textBox;

	// MARK: - Constructor

	public TextGUI(Context context, Teclado teclado, boolean multiLine, boolean enabled) {
		super(context, enabled);
		this.multiLine = multiLine;
		this.textBox = new EditText(context);
		setInputType(teclado);
	}

	public TextGUI(Context context, Teclado teclado, boolean multiLine, boolean enabled, int maxLength) {
		super(context, enabled);
		this.multiLine = multiLine;
		this.textBox = new EditText(context);
		setInputType(teclado);
		if (maxLength > 0){
			maxChar = maxLength;
		}
	}
	
	// MARK: - Getters y Setters

	private void setInputType(Teclado teclado) {
		switch (teclado) {
		case NUMERICO:
            int inputType = InputType.TYPE_CLASS_PHONE;
			this.textBox.setInputType(inputType);
			break;
		case DECIMAL:
			inputType = InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL;
			this.textBox.setInputType(inputType);
			break;
		case NUMERICO_EXTENDIDO:
			inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE| InputType.TYPE_CLASS_PHONE; // Se utiliza el teclado de telefono para permitir el caracter / en un teclado numerico
			this.textBox.setInputType(inputType);
			break;
		default:
			inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE |
					InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
			this.textBox.setInputType(inputType);
			break;
		}
		this.teclado = teclado;
	}

	public String getValorView() {
		return this.textBox.getText().toString().trim();
	}

	// MARK: - Metodos

	@Override
	public View build() {
		String label = this.getEtiqueta()+ ": ";

		// Etiqueta
		this.label = new TextView(context);
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(label);

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
		InputFilter maxLengthFilter = new InputFilter.LengthFilter(maxChar);
		this.textBox.setFilters(new InputFilter[]{ maxLengthFilter });
		
		// Se agrega filtro sobre caracteres permitidos para los teclados numericos
		switch (this.teclado) {
		case NUMERICO:
			InputFilter filterEntero = (source, start, end, dest, dstart, dend) -> {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i))) {
                        return "";
                    }
                }
                return null;
            };
			this.textBox.setFilters(new InputFilter[]{filterEntero, maxLengthFilter}); 
			break;
		case NUMERICO_EXTENDIDO:
			InputFilter filterDecimal = (source, start, end, dest, dstart, dend) -> {
                for (int i = start; i < end; i++) {
                    if (!Character.isDigit(source.charAt(i)) && source.charAt(i) != '.' && source.charAt(i) != '/') {
                        return "";
                    }
                }
                return null;
            };
			this.textBox.setFilters(new InputFilter[]{filterDecimal, maxLengthFilter}); 
			break;
		default:
			break;
		}
		this.textBox.addTextChangedListener(new TextWatcher() {
           @Override
           public void afterTextChanged(Editable s) {}

           @Override
           public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

           @Override
           public void onTextChanged(CharSequence s, int start, int before, int count) {
               textBox.setError(null);
           }
        });

		//   Se agrega validacion de requerido al cambio de foco
		this.textBox.setOnFocusChangeListener((view, hasFocus) -> {
		    String currentValue = this.getValorView();

            if (!hasFocus && isObligatorio()) {
                if(TextUtils.isEmpty(currentValue)){
                    textBox.setError(context.getString(R.string.field_required, getEtiqueta()));
                }
            }

            if (!hasFocus) {
                updateEntidadesBusquedaCondicional();
            }

            evalCondicionalSoloLectura();
        });

		//   Se agrega cambio de foco luego de editar
		if (!multiLine) {
			this.textBox.setOnEditorActionListener((view, actionId, event) -> {
				view.clearFocus();
				return true;
			});
		}

		if(isLabel()) {
            this.mainLayout.addView(this.label);
        }

		this.mainLayout.addView(textBox);

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
		} else if (this.entity instanceof AplPerfilSeccionCampoValor) {
			campoValor = (AplPerfilSeccionCampoValor) this.entity;
			campo = campoValor.getAplPerfilSeccionCampo();
		} else if (this.entity instanceof AplPerfilSeccionCampoValorOpcion) {
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) this.entity;
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}
		String nombreCampo = campo!=null?campo.getCampo().getEtiqueta():"No identificado";
		String currentValue = this.getValorView();

		Log.d(TextGUI.class.getSimpleName(),"save() : " + this.getTratamiento()
                + " :Campo: " + nombreCampo
				+ " idCampo: " + (campo!=null?campo.getId():"null")
				+ ", idCampoValor: " + (campoValor!=null?campoValor.getId():"null")
				+ ", idCampoValorOpcion: " + (campoValorOpcion!=null?campoValorOpcion.getId():"null")
				+ ", Valor: " + currentValue);

		Value data = new Value(campo, campoValor, campoValorOpcion, currentValue, null);
		this.values.add(data);

		return this.values;
	}

	@Override
	public boolean isDirty(){
		if(!super.isDirty()){
			String currentValue = this.getValorView();
			this.dirty = !TextUtils.isEmpty(currentValue) && enabled;
		}

		return super.isDirty();
	}

	@Override
	public boolean validate() {
		String currentValue = this.getValorView();

		if (isObligatorio() && TextUtils.isEmpty(currentValue)){
			textBox.setError(context.getString(R.string.field_required, getEtiqueta()));
			setFocus();
			return false;
		}
		
		//Mín caracteres 
		if (this.textBox.getText().length() < this.getMinCaracteres()){
			setFocus();
			textBox.setError(context.getString(R.string.min_lenght));
			return false;
		}

        return validateValMin() && validateValMax();
    }

	private boolean validateValMin() {
	    String currentValue = this.getValorView();

        if (this.getMinNumeros() != null && !TextUtils.isEmpty(currentValue)) {
        	Double valActual = Double.parseDouble(currentValue);
        	if(valActual < this.getMinNumeros()) {
        		// ERROR
				setFocus();
				this.textBox.setError(context.getString(R.string.min_value, getEtiqueta(), this.getMinNumeros().toString()));

				return false;
        	}
        }

		return true;
	}
	
	private boolean validateValMax(){
	    String currentValue = this.getValorView();

		if (this.getMaxNumeros() != null && !TextUtils.isEmpty(currentValue)) {
			Double valActual = Double.parseDouble(currentValue);
			if (valActual > this.getMaxNumeros()) {
				// ERROR
				setFocus();
				this.textBox.setError(context.getString(R.string.max_value, getEtiqueta(), this.getMaxNumeros().toString()));

				return false;
			}
		}
		
		return true;
	}

	@Override
	public View getEditViewForCombo(){
		return this.textBox;
	}
	
	@Override
	public void setFocus() {		
		textBox.requestFocus();
	}

	@Override
	public void removeAllViewsForMainLayout(){
		this.mainLayout.removeAllViews();
	}
	
	@Override
	public void clearData() {
		this.textBox.setText(this.getValorDefault());
		this.textBox.setError(null);
	}

	private void updateEntidadesBusquedaCondicional() {
	    if (TextGUI.this.perfilGUI == null || TextGUI.this.perfilGUI.components == null ) {
	        return;
        }

		for (Component section: TextGUI.this.perfilGUI.components) {
			for (Component fieldGUI: section.getComponents()) {
				if (!(fieldGUI instanceof SearchListsDirectGUI)) { continue; }
				SearchListsDirectGUI sldGUI = (SearchListsDirectGUI) fieldGUI;

				if (!sldGUI.entidadBusquedaCondConcerns(this)) { continue; }

				boolean shouldChange = sldGUI.shouldApplyEntidadBusquedaCondicional(this, this.getValorView());

				String entidadBusqueda = sldGUI.getSeccionCampo().getCampo().getEntidadBusqueda();
				if (shouldChange) {
				    entidadBusqueda = sldGUI.getEntidadBusquedaCondVal();
                }

                if (TextUtils.equals(sldGUI.getEntidadBusqueda(), entidadBusqueda)) {
				    continue;
                }

                sldGUI.setEntidadBusqueda(entidadBusqueda);
				sldGUI.updateEntidadBusquedaTable();
			}
		}
	}

}

