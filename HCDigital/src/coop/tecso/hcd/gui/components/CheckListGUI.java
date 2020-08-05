package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

@SuppressLint("UseSparseArrays")
public final class CheckListGUI extends CampoGUI {

	private CharSequence[] options;
	private boolean[] checked;
	private Map<Integer, CampoGUI> mapOptions;

	private String etiqueta;
	private ImageButton btnEdit;
	private TableLayout tableLayout;

	// MARK: - Constructores

	public CheckListGUI(Context context, boolean enabled) {
		super(context, enabled);
	}

	// MARK: - Getters y Setters

	public String getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	// MARK: - Metodos

	@Override
	public View build() {
		// Se define un layout lineal vertical para armar el campo desplegable
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		// Titulo: Se define un layout lineal horizonal para mostrar la etiqueta del campo y el boton para retraer/expandir
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

		// Boton de seleccion de check en lista
		this.btnEdit = new ImageButton(context);
		this.btnEdit.setBackgroundResource(R.drawable.ic_check_list);
		this.btnEdit.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.btnEdit.setEnabled(enabled);
		this.btnEdit.setFocusable(enabled);
		this.btnEdit.setOnClickListener(v -> {
			createDialog().show();
		});
		// Se crea layout para alinear icono a la derecha
		LinearLayout btnEditLayout = new LinearLayout(context);
		btnEditLayout.setGravity(Gravity.END);
		btnEditLayout.addView(this.btnEdit);
		btnEditLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		titleLayout.addView(this.label);
		titleLayout.addView(btnEditLayout);

		// Se define un layout de tipo Tabla para contener los distintos Campos
		this.tableLayout = new TableLayout(context);
		this.tableLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.tableLayout.setColumnStretchable(0, false);
		this.tableLayout.setColumnShrinkable(0, true);
		this.tableLayout.setColumnStretchable(1, true);

		// Espacio separador
		View gap = new View(context);
		gap.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		// Se agregan partes del componente al layout contenedor
		layout.addView(titleLayout);
		layout.addView(gap, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
		layout.addView(this.tableLayout);

		this.view = layout;

		this.initializeOptions();
		this.updateDisplay();

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
		for (int i = 0; i < options.length; i++) {
			CampoGUI opcion = this.mapOptions.get(i);
			if (checked[i] && opcion != null) {
				this.values.addAll(opcion.values());
			}
		}

		return this.values;
	}

	@Override
	public View disable() {
		super.disable();
		if (this.components != null) {
			for(Component component: this.components){
				component.disable();
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

		for (Component component: this.components) {
			CampoGUI campoGUI = (CampoGUI) component;
			this.options[i] = campoGUI.getEtiqueta();

			// Toma valores del campo
			AplPerfilSeccionCampo campo = null;
			AplPerfilSeccionCampoValor campoValor = null;
			AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;
			if (campoGUI.getEntity() instanceof AplPerfilSeccionCampoValor) {
				campoValor = (AplPerfilSeccionCampoValor) campoGUI.getEntity();
				campo = campoValor.getAplPerfilSeccionCampo();
			} else if(campoGUI.getEntity() instanceof AplPerfilSeccionCampoValorOpcion) {
				campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) campoGUI.getEntity();
				campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
				campo = campoValor.getAplPerfilSeccionCampo();
			}

			this.checked[i] = false;
			// Verifica si la opcion viene precargada
			if (this.getInitialValues() != null) {
				for (Value value: this.getInitialValues()){
					// Verificamos el campo
					if (campo != null && value.getCampo() != null && campo.getId() == value.getCampo().getId()) {
						// Verificamos el campoValor
						if (value.getCampoValor() != null && campoValor.getId() == value.getCampoValor().getId()) {
							// Verificamos el campoValorOpcion
							if (campoValorOpcion == null || value.getCampoValorOpcion() != null && campoValorOpcion.getId() == value.getCampoValorOpcion().getId()) {
								this.checked[i] = true;
								break;
							}
						}
					}
				}
			}
			if(this.checked[i]){
				this.addItem(campoGUI.getView());
			}

			this.mapOptions.put(i, campoGUI);
			i++;
		}
	}

	public void loadOptions(ArrayList<Integer> values) {
		int i = 0;
		this.options = new CharSequence[this.components.size()];
		this.checked = new boolean[this.components.size()];
		this.mapOptions = new HashMap<>();
		for(Component component: this.components){
			CampoGUI campoGUI = (CampoGUI) component;
			this.options[i] = campoGUI.getEtiqueta();

			// Toma valores del campo
			AplPerfilSeccionCampoValor campoValor = null;
			AplPerfilSeccionCampoValorOpcion campoValorOpcion;
			if (campoGUI.getEntity() instanceof AplPerfilSeccionCampoValor) {
				campoValor = (AplPerfilSeccionCampoValor) campoGUI.getEntity();
			} else if (campoGUI.getEntity() instanceof AplPerfilSeccionCampoValorOpcion) {
				campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) campoGUI.getEntity();
				campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			}

			if (!this.checked[i]) {
				if (values.contains(campoValor.getId())) {
					this.checked[i] = true;
				}

				if (this.checked[i]) {
					this.addItem(campoGUI.getView());
				}
			}

			this.mapOptions.put(i, campoGUI);
			i++;
		}
	}

	public void setChecked(CampoGUI campoGUI) {
		if (campoGUI == null) {
			return;
		}

		for (int i = 0; i < options.length; i++) {
			if (mapOptions.get(i) == campoGUI) {
				this.checked[i] = true;
				updateDisplay(i);
			}
		}
	}

	private void updateDisplay() {
		for (int i = 0; i < options.length; i++) {
			updateDisplay(i);
		}
	}

	private void updateDisplay(int index) {
		CampoGUI campoGUI = mapOptions.get(index);
		if (campoGUI == null) {
			return;
		}

		View view = campoGUI.getView();
		if (checked[index]) {
			this.addItem(view);
		}  else {
			this.removeItem(view);
		}
	}

	/**
	 * Elimina un item de la tabla
	 */
	private void removeItem(View view){
		// Parent View  
		ViewParent viewParent = view.getParent();
		// Has parent, remove view to grid
		if (viewParent == null)	{
			return;
		}

		// Get component index in table
		int index = this.tableLayout.indexOfChild(view);
		// Remove Selected Component
		this.tableLayout.removeViewAt(index);
		// Remove Separator Line
		if (this.tableLayout.getChildAt(index) != null) {
			this.tableLayout.removeViewAt(index);
		}
	}

	/**
	 * Agrega un item a la tabla
	 */
	private void addItem(View view) {
		// Parent View  
		ViewParent viewParent = view.getParent();
		// Hasn't parent, add view to grid
		if (viewParent != null) {
			return;
		}

		this.tableLayout.addView(view);

		// Separador de campos
		View line = new View(context);
		line.setBackgroundColor(context.getResources().getColor(R.color.line_background_color));

		this.tableLayout.addView(line, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
	}

	/**
	 * Crea el componente para seleccionar opciones de la lista
	 */
	private Dialog createDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
		builder.setTitle(this.etiqueta);
		builder.setCancelable(false);
		builder.setMultiChoiceItems(options, checked, (dialog, index, isChecked) -> {
			this.checked[index] = isChecked;
		});
		builder.setPositiveButton(R.string.accept, ((dialog, which) -> {
			this.dirty = true;
			updateDisplay();
			evalCondicionalSoloLectura();
		}));
		return builder.create();
	}

	@Override
	public boolean isDirty(){
		if (!super.isDirty()) {
			// Recorremos los elementos seleccionados verificando si alguno fue modificado
			for (int i = 0; i < options.length; i++) {
				CampoGUI opcion = this.mapOptions.get(i);
				if (checked[i] && opcion != null) {
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

		for (int i = 0; i < options.length; i++) {
			CampoGUI opcion = this.mapOptions.get(i);
			if (checked[i] && opcion != null) {
				isValid = opcion.validate();
				if (!isValid) {
					break;
				}
			}
		}

		return isValid;
	}

	@Override
	public void setFocus() {
		tableLayout.requestFocusFromTouch();
	}

	@Override
	public void clearData() {
		for (int i = 0; i < options.length; i++) {
			this.checked[i] = false;
			this.removeItem(mapOptions.get(i).getView());
		}
	}

}
