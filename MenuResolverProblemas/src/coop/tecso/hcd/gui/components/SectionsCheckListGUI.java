package coop.tecso.hcd.gui.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

public final class SectionsCheckListGUI extends CampoGUI {

	private CharSequence[] options;
	private boolean[] checked;
	private Map<Integer, CampoGUI> mapOptions;
	
	private String etiqueta;
	private ImageButton btnEdit;
	private LinearLayout layout;
	
	// MARK: - Constructores

	public SectionsCheckListGUI(Context context, boolean enabled) {
		super(context, enabled);
	}
	
	// MARK: - Getters y Setters

	public String getEtiqueta() {
		return etiqueta;
	}
	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	// Metodos
	@Override
	public View build() {
		// Titulo: Se define un layout lineal horizonal para mostrar la etiqueta del campo y el boton para retraer/expandir
		this.layout = new LinearLayout(context);
		this.layout.setOrientation(LinearLayout.HORIZONTAL);
		this.layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.layout.setOrientation(LinearLayout.HORIZONTAL);
		this.layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		// 	 Etiqueta
		this.label = new TextView(context);
		this.label.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(this.getEtiqueta());
		this.label.setGravity(Gravity.CENTER_VERTICAL);
		this.label.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		// Boton de seleccion de check en lista
		this.btnEdit = new ImageButton(context);
		this.btnEdit.setBackgroundResource(R.drawable.ic_so);
		this.btnEdit.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.btnEdit.setEnabled(enabled);
		this.btnEdit.setFocusable(enabled);
		this.btnEdit.setOnClickListener(v -> {
			createDialog().show();
		});
		// Se crea layout para alinear icono a la derecha
		LinearLayout btnEditLayout = new LinearLayout(context);
		btnEditLayout.setGravity(Gravity.RIGHT);
		btnEditLayout.addView(this.btnEdit);
		btnEditLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Se agregan partes del componente al layout contenedor
		this.layout.addView(this.label);
		this.layout.addView(btnEditLayout);

		this.view = this.layout;
		
		this.initializeOptions();
		
		return this.view;
	}
	
	@Override
	public View redraw() {
		this.btnEdit.setEnabled(enabled);
		return this.view;
	}
	
	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();

		// Recorremos los elementos seleccionados
		for(int i = 0; i < options.length; i++){
			if(checked[i]){
				CampoGUI opcion = this.mapOptions.get(i);
				this.values.addAll(opcion.values());
			}
		}
		
		return this.values;
	}
	
	@Override
	public View disable() {
		super.disable();
		if(this.components != null){
			for(Component co: this.components){
				co.disable();
			}
		}
		return this.view;
	}

	/**
	 * Inicializa la lista de opciones
	 */
	private void initializeOptions(){
		int i = 0;
		this.options = new CharSequence[this.components.size()];
		this.checked = new boolean[this.components.size()];
		this.mapOptions = new HashMap<>();
		for(Component co: this.components){
			CampoGUI campoGUI = (CampoGUI) co;
			this.options[i] = campoGUI.getEtiqueta();
			
			// Toma valores del campo
			AplPerfilSeccionCampo campo = null;
			AplPerfilSeccionCampoValor campoValor = null;
			AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;
			if(campoGUI.getEntity() instanceof AplPerfilSeccionCampoValor){
				campoValor = (AplPerfilSeccionCampoValor) campoGUI.getEntity();
				campo = campoValor.getAplPerfilSeccionCampo();
			}else if(campoGUI.getEntity() instanceof AplPerfilSeccionCampoValorOpcion){
				campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) campoGUI.getEntity();
				campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
				campo = campoValor.getAplPerfilSeccionCampo();
			}
			
			this.checked[i] = false; 
			// Verifica si la opcion viene precargada
			if(this.getInitialValues() != null){
				for(Value value: this.getInitialValues()){
					// Verificamos el campo
					if(campo != null && value.getCampo() != null && campo.getId() == value.getCampo().getId()){
						// Verificamos el campoValor
						if(campoValor != null && value.getCampoValor() != null && campoValor.getId() == value.getCampoValor().getId()){
							// Verificamos el campoValorOpcion
							if(campoValorOpcion == null || 
									(campoValorOpcion != null && value.getCampoValorOpcion() != null && campoValorOpcion.getId() == value.getCampoValorOpcion().getId())){
								this.checked[i] = true;
								break;
							}
						}
					}
				}
			}
			
			this.mapOptions.put(i, campoGUI);
			i++;
		}
	}

	public void updateDisplay() {
		PerfilGUI perfil = this.getPerfilGUI();
		for (int i = 0; i < options.length; i++) {
			CampoGUI opcion = mapOptions.get(i);
			int idSeccion = -1;
			try {
				idSeccion = Integer.valueOf(opcion.getValorDefault());
			} catch (Exception e) {
				Log.d("SectionsCheckListGUI","Error al obtener id de seccion opcional. Valor por defecto del campo: " + opcion.getValorDefault());
            }
			if (checked[i]) {
				perfil.mostrarSeccion(idSeccion);
			} else {
				perfil.ocultarSeccion(idSeccion);
			}
		}
	}

	/**
	 * Crea el componente para seleccionar opciones de la lista
	 */
	private Dialog createDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
		builder.setTitle(this.etiqueta);
		builder.setPositiveButton(R.string.accept, ((dialog, which) -> {
            dirty = true;
            updateDisplay();
        }));
		builder.setMultiChoiceItems(options, checked, mOnMultiChoiceClickListener);
		return builder.create();
	}

	final Context co = this.context;
	private DialogInterface.OnMultiChoiceClickListener
			mOnMultiChoiceClickListener = (dialog, which, isChecked) -> {
                if(!isChecked){
                    AlertDialog.Builder builder = new AlertDialog.Builder(co);
                    builder.setTitle(R.string.disable_section_title);
                    builder.setMessage(co.getString(R.string.disable_section_msg));
                    builder.setCancelable(false);
                    builder.setPositiveButton(R.string.accept, null);

                    builder.create().show();
                }

            };
	
	@Override
	public boolean isDirty(){
		if(!super.isDirty()){
			// Recorremos los elementos seleccionados verificando si alguno fue modificado
			for(int i = 0; i < options.length; i++){
				if(checked[i]){
					CampoGUI opcion = this.mapOptions.get(i);
					if(!opcion.getTratamiento().equals(Tratamiento.NA) && opcion.isDirty()){
						return true;
					}
				}
			}
		}
		
		return super.isDirty();
	}
	
	@Override
	public boolean validate() {
		boolean isValid = true;
		
		for(int i = 0; i < options.length; i++){
			if(checked[i]){
				CampoGUI opcion = this.mapOptions.get(i);
				isValid = opcion.validate();
				
				if(!isValid){
					layout.requestFocusFromTouch();
					break;
				}
			}

		}

		return isValid;
	}
	
	@Override
	public void setFocus() {
		layout.requestFocusFromTouch();
	}

}
