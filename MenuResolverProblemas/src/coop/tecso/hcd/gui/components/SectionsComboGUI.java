package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.HotspotDialog;
import coop.tecso.hcd.utils.HotspotUtils;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;

@SuppressWarnings("unchecked")
public final class SectionsComboGUI extends ComboGUI {

	private final String LOG_TAG = getClass().getSimpleName();
	
	private CharSequence[] options;
	private boolean[] checked;
	private SparseArray<CampoGUI> arrayOptions;
	
	private String etiqueta;

	private boolean onLoad;

	// MARK: - Constructors

	public SectionsComboGUI(Context context, boolean enabled) {
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
	@SuppressLint("SetTextI18n")
	@Override
	public View build() {
		Log.d(LOG_TAG, " build: enter");

		String label = this.getEtiqueta() + ": ";
		// Titulo: Se define un layout lineal horizonal para mostrar la etiqueta del campo y el boton para retraer/expandir
		LinearLayout layout = new LinearLayout(context);
		layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		// 	 Etiqueta
		this.label = new TextView(context);
		this.label.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(label);
		
        // Se define un LinearLayout para ubicar: 'Label / EditText'
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		// Combo Layout
		final LinearLayout cmbLayout = new LinearLayout(context);
		cmbLayout.setOrientation(LinearLayout.VERTICAL);
		cmbLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		
		// Opciones
		final List<CampoGUI> items = new ArrayList<>();
		for (Component component : this.components) {
			CampoGUI campoGUI = (CampoGUI) component;
			campoGUI.removeAllViewsForMainLayout();
			items.add(campoGUI);
		}
		
		// Adapter de Opciones
		final ArrayAdapter<CampoGUI> adapter;
		adapter = new ArrayAdapter<CampoGUI>(context,android.R.layout.simple_spinner_item, items) {
			@NonNull
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				TextView holder;
				if (convertView == null) {
					convertView = View.inflate(context, android.R.layout.simple_spinner_item, null);
					// Creates a ViewHolder and store references to the two children
					// views we want to bind data to.
					holder = convertView.findViewById(android.R.id.text1);
					holder.setTextColor(context.getResources().getColor(R.color.label_text_color));
					convertView.setTag(holder);
				} else {
					// Get the ViewHolder back to get fast access to the TextView
					// and the ImageView.
					holder = (TextView) convertView.getTag();
				}
				String text = this.getItem(position).getEtiqueta();
				if(text.contains(":")) text = text.split(":")[1];
				
				holder.setText(text);
				return convertView;
			}
		};
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		// Combo Spinner
		this.cmbValores = new Spinner(context);
		this.cmbValores.setPrompt(this.getEtiqueta());
		this.cmbValores.setEnabled(enabled);
		this.cmbValores.setAdapter(adapter);
		
		// Se agrega el spinner al Layout
		cmbLayout.addView(this.cmbValores);
		
		onLoad = false;
		
		this.cmbValores.setOnItemSelectedListener(new OnItemSelectedListener() {
			private CampoGUI itemPrevio = null;

			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {
				if (onLoad) {
					onLoad = false;
				}

				if (itemPrevio != null) {
					cmbLayout.removeView(itemPrevio.getEditViewForCombo());
				}
				CampoGUI itemActual = adapter.getItem(pos);
				if (itemActual.getEditViewForCombo() != null) {
					cmbLayout.addView(itemActual.getEditViewForCombo());
					itemPrevio = itemActual;
				}
				dirty = true;
				
				// Limpio el array y checkeo la seleccionada
				Arrays.fill(checked, Boolean.FALSE);
				checked[pos] = true;
				updateDisplay();
				
				//Refresco valor seleccionado
				values();

				if (isComboElectrocardiograma()) {
                    String selectedValue = getSelectedValue();

                    if(!selectedValue.toLowerCase().contains("Seleccione".toLowerCase()) && !selectedValue.toLowerCase().contains("No".toLowerCase())){
                        //no deberia activar el anclaje si la atencion esta cerrada:
                        if(!enabled){
                            return;
                        }

						enableHotspotIfNeeded();
                    }
                }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
		
		// Se setea el valor precargado o valor por defecto en caso que exista
		int idOpcion = 0;
		boolean isInitialValue = false;
		if (this.getInitialValues() != null && this.getInitialValues().size() >= 1) {
			Value value = this.getInitialValues().get(0);
			if (this.entity instanceof AplPerfilSeccionCampo) {
				idOpcion = value.getCampoValor().getId();
			} else if (this.entity instanceof AplPerfilSeccionCampoValor) {
				idOpcion = value.getCampoValorOpcion().getId();
			}
			isInitialValue = true;
		} else if (!TextUtils.isEmpty(this.getValorDefault())) {
			try {
				idOpcion = Integer.parseInt(this.getValorDefault());
			} catch (Exception e) {
                Log.d(LOG_TAG,"build(): el valor por defecto debe ser numerico: " + this.getValorDefault(), e);
			}
		}
		
		this.initializeOptions();
		
		if (idOpcion > 0) {
			for (Component co: this.components) {
				CampoGUI campoGUI = (CampoGUI) co;
				if (campoGUI.getEntity().getId()==idOpcion) {
					int pos = adapter.getPosition(campoGUI);
					this.cmbValores.setSelection(pos, true); // el 2do parametro en true fuerza el ItemSelectedListener
					if (isInitialValue) {
					    this.dirty = false;
                    }
				}
			}
		}
		
		// Se agregan partes del componente al layout contenedor
		layout.addView(this.label);
		layout.addView(cmbLayout);

		this.view = layout;
		
		return this.view;
	}

	@Override
	public View redraw() {
		return this.view;
	}
	
	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();
		// Recorremos los elementos seleccionados
		for (int i = 0; i < options.length; i++) {
			if (checked[i]) {
				CampoGUI opcion = this.arrayOptions.get(i);
				this.values.addAll(opcion.values());
			}
		}
		
		return this.values;
	}
	
	@Override
	public View disable() {
		super.disable();
		if (this.components != null) {
			for (Component co : this.components) {
				co.disable();
			}
		}
		return this.view;
	}

	/**
	 * Inicializa la lista de opciones
	 */
	private void initializeOptions() {
		int i = 0;
		this.options = new CharSequence[this.components.size()];
		this.checked = new boolean[this.components.size()];
		this.arrayOptions = new SparseArray<>();
		for (Component co: this.components) {
			CampoGUI campoGUI = (CampoGUI) co;
			String text = campoGUI.getEtiqueta();
			if(text.contains(":")) text = text.split(":")[1];
			this.options[i] = text;
			Arrays.fill(checked, Boolean.FALSE);
			this.arrayOptions.put(i, campoGUI);
			i++;
		}
	}

	private void updateDisplay() {
		PerfilGUI perfil = this.getPerfilGUI();
		for (int i = 0; i < options.length; i++) {
			CampoGUI opcion = arrayOptions.get(i);
			int idSeccion;
			try {
				idSeccion = Integer.parseInt(opcion.getValorDefault());
			} catch (Exception e) {
				Log.d(LOG_TAG, "Error al obtener id de seccion opcional. Valor por defecto del campo: " + opcion.getValorDefault());
				idSeccion = -1;
			}
			if (checked[i]) {
				perfil.mostrarSeccion(idSeccion);
			} else {
				perfil.ocultarSeccion(idSeccion);
			}
		}
	}
	
	@Override
	public boolean isDirty() {
		if (!super.isDirty()) {
			// Recorremos los elementos seleccionados verificando si alguno fue
			// modificado
			for (int i = 0; i < options.length; i++) {
				if (checked[i]) {
					CampoGUI opcion = this.arrayOptions.get(i);
					if (!opcion.getTratamiento().equals(Tratamiento.NA) && opcion.isDirty()) {
						return true;
					}
				}
			}
		}
		return super.isDirty();
	}

	public String getSelectedValue(){
	    if (cmbValores == null) {
	        return "";
        }

        return cmbValores.getSelectedItem().toString();
	}
	
	@Override
	public boolean validate() {
		return true;
	}

	public int getIndexByItem(CampoGUI campoGUI){
		AplPerfilSeccionCampoValor campoParam = (AplPerfilSeccionCampoValor) campoGUI.getEntity();
		int index = 0;
		if (cmbValores != null && campoGUI != null) {
            ArrayAdapter<CampoGUI> adapter = (ArrayAdapter<CampoGUI>) cmbValores.getAdapter();
            int size = adapter.getCount();
            for (int i = 0; i < size; i++) {
                CampoGUI currentCampoGUI = adapter.getItem(i);
                if (currentCampoGUI == null) {
                    continue;
                }
                if(currentCampoGUI.getEntity() instanceof AplPerfilSeccionCampoValor){
                    AplPerfilSeccionCampoValor currentAplPerfilSeccionCampoValor = (AplPerfilSeccionCampoValor)currentCampoGUI.getEntity();
                    int currentId = currentAplPerfilSeccionCampoValor.getCampoValor().getId();
                    int argId = campoParam.getCampoValor().getId();
                    if (currentId == argId) {
                        index = i;
                    }
                }
            }
		}
		return index;
	}

	public void setSelectedIndex(int index){
		if (cmbValores != null) {
			cmbValores.setSelection(index, true);
		}
	}

    public void setEnabled(boolean enabled){
        if (cmbValores != null) {
            cmbValores.setEnabled(enabled);
        }
    }

    // ------------

	private boolean isComboElectrocardiograma(){
		boolean isComboElectrocardiograma = false;
		try {
			String idCmbElectroOffOn = ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_ECG);
			if (idCmbElectroOffOn != null) {
				String[] idsCmbElectroOnOffArray = idCmbElectroOffOn.split("&");
				int idCmbElectroOffline = Integer.parseInt(idsCmbElectroOnOffArray[0]);
				int idCmbElectroOnline = Integer.parseInt(idsCmbElectroOnOffArray[1]);

				int id = this.getEntity().getId();

				isComboElectrocardiograma = id == idCmbElectroOffline || id == idCmbElectroOnline;
			}
        } catch(Exception e){
			e.printStackTrace();
		}

		return isComboElectrocardiograma;
	}

	private void enableHotspotIfNeeded() {
		if (HotspotUtils.isWifiHotspotEnabled(context)) {
			this.showAlertWithMessage(R.string.transmitir_ecg);
		} else {
            this.enableHotspot();
		}
	}

	private void enableHotspot() {
	    if (HotspotUtils.enableWifiHotspotAndCheck(getContext())) {
            this.showAlertWithMessage(R.string.anclaje_activo);
        } else {
            HotspotDialog.showDialog(getContext(), R.string.active_anclaje_para_transmitir);
        }
	}

    private void showAlertWithMessage(int messageStringID){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.atencion);
        builder.setMessage(messageStringID);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.accept, null);
        builder.create().show();
    }

}
