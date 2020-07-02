package coop.tecso.hcd.gui.components;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TableRow;
import android.widget.TextView;
import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

public final class LabelGUI extends CampoGUI {

	private TableRow row;

	private CheckBox checkBox;

	// MARK: - Constructors
	
	public LabelGUI(Context context) {
		super(context);
	}

	public LabelGUI(Context context, boolean enabled) {
		super(context, enabled);
	}
	
	// MARK: - Getters & Setters

	public void setEnabled(boolean enabled){
		if (row != null) {
			row.setEnabled(enabled);
		}
		if (checkBox != null) {
			checkBox.setEnabled(enabled);
		}
		if (label != null) {
			label.setEnabled(enabled);
		}
	}

	public CheckBox getCheckBox(){
		return this.checkBox;
	}

	// MARK: - Metodos

	@Override
	public View build() {
		// Se define un Table Row para ubicar: ' check - Label'
		this.row = new TableRow(context);
		this.row.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.row.setGravity(Gravity.CENTER_VERTICAL);

		// Icono de seleccion
		this.checkBox = new CheckBox(context);
		this.checkBox.setChecked(true);
		this.checkBox.setEnabled(false);
		
		// Etiqueta
		this.label = new TextView(context);
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(this.getEtiqueta());
		this.label.setGravity(Gravity.LEFT);
		
		// Se carga componente en el layout
		this.row.addView(this.checkBox);
		this.row.addView(this.label);
		this.view = this.row;
		
		// Ya que representa a una opcion seleccionada
		this.dirty = true;
		
		return this.view;
	}
	
	/**
	 * Solo tiene sentido en la impresion
	 */
	public String getValorView() {
		return this.getEtiqueta();
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
		String valor = this.getValorView(); // No lleva valor 
		
		Log.d(LabelGUI.class.getSimpleName(),"save() : " + this.getTratamiento()
                + " :Campo: " + nombreCampo
				+ " idCampo: " +(campo!=null?campo.getId():"null")
				+ ", idCampoValor: " + (campoValor!=null?campoValor.getId():"null")
				+ ", idCampoValorOpcion: " + (campoValorOpcion!=null?campoValorOpcion.getId():"null")
				+ ", Valor: "+valor);
		
		Value data = new Value(campo,campoValor,campoValorOpcion,valor, null);
		this.values.add(data);
		
		return this.values;
	}

}