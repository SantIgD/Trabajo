package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;

/**
 * 
 * @author tecso.coop
 *
 */
@SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
public class ComboGUI extends CampoGUI {

	private final String TAG = getClass().getSimpleName();

	protected Spinner cmbValores;

	private LinearLayout mainLayout;

	private  List<Integer> opcionesInvalidas;

	// MARK: - Constructs

	public ComboGUI(Context context) {
		super(context);
	}

	public ComboGUI(Context context,List<Value> values) {
		super(context,values);
	}

	public ComboGUI(Context context, boolean enabled) {
		super(context, enabled);
	}

	// MARK: - Metodos

    @Override
	public View build() {
	    String label = this.getEtiqueta() + ": ";

		// Etiqueta
		this.label = new TextView(context);
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(label);
		this.label.setFocusable(true);
		this.label.setFocusableInTouchMode(true);

        // Se define un LinearLayout para ubicar: 'Label / EditText'
        mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

		mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		// Combo Layout
		final LinearLayout cmbLayout = new LinearLayout(context);
		cmbLayout.setOrientation(LinearLayout.VERTICAL);
		cmbLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		// Opciones
		final List<CampoGUI> items = new ArrayList<>();
		for (Component component: this.components) {
			CampoGUI campoGUI = (CampoGUI) component;
			campoGUI.removeAllViewsForMainLayout();
			items.add(campoGUI);
		}

		// Adapter de Opciones
		final ArrayAdapter<CampoGUI> adapter;
		adapter = new ArrayAdapter<CampoGUI>(context, android.R.layout.simple_spinner_item, items) {
			@NonNull
			@Override
			public View getView(int position, View convertView, @NonNull ViewGroup parent) {
				TextView view = new TextView(context);
				view.setTextColor(context.getResources().getColor(R.color.label_text_color));
				view.setText(this.getItem(position).getEtiqueta());
				view.setFocusable(true);
				return view;
			}
		};
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.notifyDataSetChanged();

		// Combo Spinner
		this.cmbValores = new Spinner(context);
		this.cmbValores.setPrompt(this.getEtiqueta());
		this.cmbValores.setEnabled(enabled);
		this.cmbValores.setAdapter(adapter);
		this.cmbValores.setFocusable(true);
		this.cmbValores.setFocusableInTouchMode(true);

		// Se agrega el spinner al Layout
		cmbLayout.addView(this.cmbValores);

		this.cmbValores.setOnItemSelectedListener(new OnItemSelectedListener() {
			private CampoGUI itemPrevio = null;
			@Override
			public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {
				if (itemPrevio != null) {					
					cmbLayout.removeView(itemPrevio.getEditViewForCombo());
				}
				CampoGUI itemActual = adapter.getItem(pos);
				View viewForCombo = itemActual.getEditViewForCombo(); 
				if (viewForCombo != null){
					cmbLayout.addView(viewForCombo);				
					itemActual.customonItemSelected();
					itemPrevio = itemActual;
				}
				ComboGUI.this.label.setError(null);
				dirty = true;

				evalCondicionalSoloLectura();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});

		this.cmbValores.setOnTouchListener((v, event) -> {
            cmbValores.requestFocus();
            return false;
        });

		this.loadData(adapter);

		// Se agregan los componentes a la fila
		mainLayout.addView(this.label);
		mainLayout.addView(cmbLayout);

		this.mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(this::globalLayoutDidChange);

		this.view = mainLayout;

		return this.view;
	}

	@Override
	public View getEditViewForCombo(){
		this.mainLayout.removeView(this.label);
		return this.mainLayout;
	}

	@Override
	public View redraw() {
		this.cmbValores.setEnabled(enabled);
		return this.view;
	}

	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();

		CampoGUI opcion = (CampoGUI) this.cmbValores.getSelectedItem();
		if (opcion != null) {
			this.values.addAll(opcion.values());
		}

		return this.values;
	}

	@Override
	public boolean isDirty(){
		if (!super.isDirty()) {
			// En caso que el elemento seleccionado no sea de tipo 'NA' verificamos si su valor fue modificado 
			CampoGUI opcion = (CampoGUI) this.cmbValores.getSelectedItem();
			if (opcion != null && !opcion.getTratamiento().equals(Tratamiento.NA)) {
				return opcion.isDirty();
			}
		}

		return super.isDirty();
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

	@Override
	public View enable() {
		super.enable();
		if (this.components != null) {
			for(Component component: this.components){
				component.enable();
			}
		}
		return this.view;
	}

	public void setOpcionesInvalidas(List<Integer> opcionesInvalidas) {
		this.opcionesInvalidas = opcionesInvalidas;
	}

	public boolean isOpcionSeleccionadaValida() {
		CampoGUI selected = (CampoGUI) cmbValores.getSelectedItem();
		return opcionesInvalidas == null || !opcionesInvalidas.contains(selected.getEntity().getId());
	}

	@Override
	public boolean validate() {
		boolean isValid = isOpcionSeleccionadaValida();

		//VALIDACION ELECTROGUI

		if (isValid && cmbValores.getSelectedItem() instanceof ElectroGUI) {
			ElectroGUI selected = (ElectroGUI) cmbValores.getSelectedItem();
			return selected.validate();
		} else if (isValid && cmbValores.getSelectedItem() instanceof TextGUI) {
			TextGUI selected = (TextGUI) cmbValores.getSelectedItem();
			return selected.validate();
		} else if (isValid && cmbValores.getSelectedItem() instanceof ComboGUI) {
			ComboGUI selected = (ComboGUI) cmbValores.getSelectedItem();
			return selected.validate();
		}

		if (!isValid && isObligatorio()) {
			TextView selectedView = ((TextView) cmbValores.getSelectedView());

			if (selectedView != null && context != null) {
				selectedView.setError(context.getString(R.string.invalid_option));
				GUIHelper.showError(context, context.getString(R.string.invalid_option));
				this.setFocus();
			}
		}
		else if (!isValid) {
			this.label.setError(context.getString(R.string.invalid_option));
			this.setFocus();
		}

		return isValid;
	}

	@Override
	public void setFocus() {
		cmbValores.requestFocusFromTouch();
	}

	@Override
	public void clearData() {
		int idOpcion = 0;
		if (!TextUtils.isEmpty(this.getValorDefault())) {
			try{
				idOpcion = Integer.valueOf(this.getValorDefault());
			} catch (Exception e) {
                Log.d(TAG, "build(): el valor por defecto debe ser númerico: "+this.getValorDefault(), e);
			}
		}
		for(Component component: this.components){
			CampoGUI campoGUI = (CampoGUI) component;
			if (campoGUI.getEntity().getId() == idOpcion) {
				int pos = ((ArrayAdapter<CampoGUI>) this.cmbValores.getAdapter()).getPosition(campoGUI);
				this.cmbValores.setSelection(pos, true);
				this.dirty = false;
			}
		}

		TextView selectedView = ((TextView) this.cmbValores.getSelectedView());
		selectedView.setError(null);
	}

	protected void globalLayoutDidChange() {}

	private void loadData(ArrayAdapter<CampoGUI> adapter) {
        // Se setea el valor precargado o valor por defecto en caso que exista
        int idOpcion = 0;
        int idOpcionDefault = 0;
        boolean isInitialValue = false;

        if (this.getInitialValues() != null && this.getInitialValues().size() == 1){
            Value value = this.getInitialValues().get(0);
            if (this.entity instanceof AplPerfilSeccionCampo) {
                idOpcion = value.getCampoValor().getId();
            } else if (this.entity instanceof AplPerfilSeccionCampoValor) {
                idOpcion = value.getCampoValorOpcion().getId();
            }
            isInitialValue = true;
        } else if(!TextUtils.isEmpty(this.getValorDefault())) {
            try {
                idOpcionDefault = Integer.valueOf(this.getValorDefault());
            } catch (Exception e) {
                Log.d(TAG, "build(): el valor por defecto debe ser númerico: "+this.getValorDefault(), e);
            }
        }

        if (idOpcion > 0) {
            for(Component component: this.components){
                CampoGUI campoGUI = (CampoGUI) component;
                if (campoGUI.getEntity().getId() == idOpcion) {
                    int position = adapter.getPosition(campoGUI);
                    this.cmbValores.setSelection(position, true); // el 2do parametro en true fuerza el ItemSelectedListener
                    if (isInitialValue) {
                        this.dirty = false;
                    }
                }
            }
        }
        else if (idOpcionDefault > 0) {
            for (Component component: this.components) {
                CampoGUI campoGUI = (CampoGUI) component;
                try {
                    if (((AplPerfilSeccionCampoValor) campoGUI.getEntity()).getCampoValor().getId() == idOpcionDefault) {
                        int position = adapter.getPosition(campoGUI);
                        this.cmbValores.setSelection(position, true); // el 2do parametro en true fuerza el ItemSelectedListener
                        if (isInitialValue) {
                            this.dirty = false;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

}