package coop.tecso.hcd.gui.components.ecg;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.Component;
import coop.tecso.hcd.gui.components.LabelGUI;
import coop.tecso.hcd.gui.components.SeccionGUI;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;

/**
 *
 * @author tecso.coop
 *
 */
@TargetApi(18)
@SuppressLint({"SetTextI18n", "ClickableViewAccessibility"})
public class ComboECGInterpretadoGUI extends CampoGUI {

    private final String TAG = getClass().getSimpleName();

    private Spinner cmbValores;
    private LinearLayout mainLayout;

    // Constructs

    public ComboECGInterpretadoGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    // Metodos

    @Override
    public View build() {
        String label = this.getEtiqueta() + ": ";

        // Etiqueta
        this.label = new TextView(context);
        this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
        this.label.setText(label);

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
                if (viewForCombo != null) {
                    cmbLayout.addView(viewForCombo);
                    itemActual.customonItemSelected();
                    itemPrevio = itemActual;
                }
                dirty = true;

                evalCondicionalSoloLectura();

                if (ECGHelper.isObligatorioPatronECGInterpretado(context)) {
                    LabelGUI item = (LabelGUI) adapter.getItem(pos);
                    int idCampoValorSeleccionado = getIdCampoValorByLabelGUI(item);
                    int idSiCampoValorCmbECG = getIdSiCmbECGInterpretadoParametro();
                    if (idCampoValorSeleccionado == idSiCampoValorCmbECG) {
                        buildAlertCmbECG();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {}
        });

        this.cmbValores.setOnTouchListener((v, event) -> {
            cmbValores.requestFocus();
            return false;
        });

        // Se setea el valor precargado o valor por defecto en caso que exista
        int idOpcion = 0;
        int idOpcionDefault = 0;
        boolean isInitialValue = false;
        
        if (!CollectionUtils.isEmpty(this.getInitialValues())) {
            Value value = this.getInitialValues().get(0);
            if (this.entity instanceof AplPerfilSeccionCampo) {
                idOpcion = value.getCampoValor().getId();
            } else if (this.entity instanceof AplPerfilSeccionCampoValor) {
                idOpcion = value.getCampoValorOpcion().getId();
            }
            isInitialValue = true;
        } else if (!TextUtils.isEmpty(this.getValorDefault())) {
            try {
                idOpcionDefault = Integer.parseInt(this.getValorDefault());
            } catch (Exception e) {
                Log.d(TAG, "build(): el valor por defecto debe ser númerico: "+this.getValorDefault(), e);
            }
        }
        
        if (idOpcion > 0) {
            for (Component component: this.components) {
                CampoGUI campoGUI = (CampoGUI) component;
                if (campoGUI.getEntity().getId() == idOpcion) {
                    int pos = adapter.getPosition(campoGUI);
                    this.cmbValores.setSelection(pos, true); // el 2do parametro en true fuerza el ItemSelectedListener
                    if (isInitialValue) {
                        this.dirty = false;
                    }
                }
            }
        }
        else if (idOpcionDefault > 0) {
            for (Component co: this.components) {
                CampoGUI campoGUI = (CampoGUI) co;
                try {
                    if (((AplPerfilSeccionCampoValor) campoGUI.getEntity()).getCampoValor().getId() == idOpcionDefault) {
                        int pos = adapter.getPosition(campoGUI);
                        this.cmbValores.setSelection(pos, true); // el 2do parametro en true fuerza el ItemSelectedListener
                        if (isInitialValue) {
                            this.dirty = false;
                        }
                    }
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // Se agregan los componentes a la fila
        mainLayout.addView(this.label);
        mainLayout.addView(cmbLayout);

        final boolean isUserSucursalCordoba = (appState.getCurrentUser().getSucursal().getId() == 2);

        if (isUserSucursalCordoba) {
            this.enabled = false;
            if (this.cmbValores != null) {
                this.cmbValores.setEnabled(false);
            }
        }

        this.mainLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (!isUserSucursalCordoba) {
                SeccionGUI seccionGUI = getSeccionGUI();
                if (ECGHelper.isObligatorioPatronECGInterpretado(context)) {
                    seccionGUI.mostrar();
                    ECGHelper.changeValueCmbElectroSI(context);
                } else {
                    seccionGUI.ocultar();
                    ECGHelper.enableCmbEsElectrocardiograma(context);
                }
            }
        });

        this.view = mainLayout;

        return this.view;
    }

    @Override
    public View getEditViewForCombo() {
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
    public boolean isDirty() {
        if (super.isDirty()) {
            return true;
        }

        // En caso que el elemento seleccionado no sea de tipo 'NA' verificamos si su valor fue modificado
        CampoGUI opcion = (CampoGUI) this.cmbValores.getSelectedItem();
        if (opcion != null && !opcion.getTratamiento().equals(Tratamiento.NA)) {
            return opcion.isDirty();
        }

        return super.isDirty();
    }

    @Override
    public View disable() {
        super.disable();
        // Disable childs
        if (this.components != null) {
            for (Component co: this.components) {
                co.disable();
            }
        }
        return this.view;
    }

    @Override
    public View enable() {
        super.enable();
        if (this.components != null) {
            for (Component component: this.components) {
                component.enable();
            }
        }
        return this.view;
    }

    public void setOpcionesInvalidas(List<Integer> opcionesInvalidas) {}

    @Override
    public boolean validate() {
        boolean isValid = true;
        boolean isUserSucursalRosario = (appState.getCurrentUser().getSucursal().getId() == 1);

        if (isUserSucursalRosario) {
            boolean isObligatorioComboECGInterpretado = ECGHelper.isObligatorioPatronECGInterpretado(context);
            LabelGUI labelSeleccionado = (LabelGUI)cmbValores.getSelectedItem();
            if (isObligatorioComboECGInterpretado) {
                int idSeleccione = getIdCampoValorByLabelGUI(labelSeleccionado);
                int idSeleccioneParametro = getIdSeleccioneCmbECGInterpretadoParametro();
                if (idSeleccioneParametro == idSeleccione) {
                    this.getLabel().setError("Debe Seleccionar un valor");
                    this.getLabel().setFocusable(true);
                    this.getLabel().setFocusableInTouchMode(true);
                    if (this.cmbValores != null) {
                        this.clearData();
                        this.cmbValores.setEnabled(true);
                    }
                    isValid = false;
                } else {
                    this.getLabel().setError(null);
                    this.getLabel().setFocusable(false);
                    this.getLabel().setFocusableInTouchMode(false);
                }
            } else {
                this.getLabel().setError(null);
                this.getLabel().setFocusable(false);
                this.getLabel().setFocusableInTouchMode(false);
                if (this.cmbValores != null) {
                    this.clearData();
                    this.cmbValores.setEnabled(false);
                }
            }
        }
        return isValid;
    }

    @Override
    public void setFocus() {
        cmbValores.requestFocusFromTouch();
    }

    @Override
    public void clearData() {
        int idSeleccioneParametro = getIdSeleccioneCmbECGInterpretadoParametro();
        for (Component co: this.components) {
            LabelGUI labelGUI = (LabelGUI) co;
            int idSeleccione = getIdCampoValorByLabelGUI(labelGUI);
            if (idSeleccioneParametro == idSeleccione) {
                int pos = ((ArrayAdapter<CampoGUI>)this.cmbValores.getAdapter()).getPosition(labelGUI);
                this.cmbValores.setSelection(pos, true);
                this.dirty = false;
            }
        }

        TextView selectedView = ((TextView) this.cmbValores.getSelectedView());
        selectedView.setError(null);
    }

    private int getIdSeleccioneCmbECGInterpretadoParametro() {
        return ParamHelper.getInteger(ParamHelper.ID_VALOR_SELECCIONE_CMB_ECGINTERPRETADO,0);
    }

    private int getIdSiCmbECGInterpretadoParametro() {
        return ParamHelper.getInteger(ParamHelper.ID_VALOR_SI_CMB_ECGINTERPRETADO,0);
    }

    private int getIdCampoValorByLabelGUI(LabelGUI labelGUI) {
        if (labelGUI != null) {
            AplPerfilSeccionCampoValor aplPerfilSeccionCampoValor = (AplPerfilSeccionCampoValor)labelGUI.getEntity();
            return aplPerfilSeccionCampoValor.getCampoValor().getId();
        } else {
            return 0;
        }
    }

    private void buildAlertCmbECG() {
        String mensaje =  ParamHelper.getString(ParamHelper.MSJ_ALERTA_CMB_ECG_INTERPRETADO,"MUY ALTO RIESGO - Proceda a internar al Paciente");
        String[] arrayTituloCuerpo = mensaje.split("-");

        final Dialog alert = new Dialog(context);
        alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alert.setContentView(R.layout.alerta_con_divisor);

        TextView tvTitulo = alert.findViewById(R.id.titulo);
        TextView tvCuerpo = alert.findViewById(R.id.cuerpo);

        tvTitulo.setText(arrayTituloCuerpo[0].trim());
        tvCuerpo.setText(arrayTituloCuerpo[1].trim());

        Button btnAceptar = alert.findViewById(R.id.btnAceptar);
        btnAceptar.setOnClickListener(v -> alert.dismiss());

        alert.show();
    }

}