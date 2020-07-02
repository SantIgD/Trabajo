package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.HotspotUtils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

/**
 * Tratamiento Link a Aplicaciones o Web pages.
 * @author tecso.coop
 *
 */
public final class AnclajeGUI extends CampoGUI {

    private static final String TAG = AnclajeGUI.class.getSimpleName();

    private LinearLayout mainLayout;
    private LinearLayout buttonLayout;
    private Button button;

    // Constructs

    public AnclajeGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    // Getters y Setters

    public String getValorView() {
        if (getInitialValues() != null) {
            return getInitialValues().get(0).getValor();
        }
        else {
            return this.valorDefault;
        }
    }

    // Metodos

    @Override
    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    public View build() {
        // Etiqueta
        this.label = new TextView(context);
        this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
        this.label.setText(this.getEtiqueta()+": ");

        // Se define un LinearLayout para ubicar: 'Label / EditText'
        this.mainLayout = new LinearLayout(context);
        this.mainLayout.setOrientation(LinearLayout.VERTICAL);
        this.mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
        this.mainLayout.setGravity(Gravity.CENTER_VERTICAL);

        // Se arma el boton para disparar control se seleccion de fecha
        this.button = new Button(context);
        this.button.setTextColor(context.getResources().getColor(R.color.label_text_color));

        if (!HotspotUtils.isWifiHotspotEnabled(context)) {
            button.setText(R.string.conectar_anclaje);
        } else{
            button.setText(R.string.desconectar_anclaje);
        }

        this.button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        this.button.setFocusable(true);
        this.button.setFocusableInTouchMode(true);
        this.button.setOnTouchListener((v, event) -> {
            button.requestFocus();
            return false;
        });
        this.button.setOnClickListener(v -> {
            //no deberia activar el anclaje si la atención está cerrada:
            if (!enabled) {
                return;
            }

            try {
                HotspotUtils.toggleHotspotStatus(context);

                if(!HotspotUtils.isWifiHotspotEnabled(context)){
                    button.setText(R.string.conectar_anclaje);
                } else{
                    button.setText(R.string.desconectar_anclaje);
                }
            }
            catch (Exception e){
                Log.e(TAG, "Intent wifi: " + e);
            }
        });
        this.button.setEnabled(enabled);


        BroadcastReceiver electroGUIReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
            boolean isHotSpotEnabled = HotspotUtils.isWifiHotspotEnabled(context);
            if (isHotSpotEnabled) {
                button.setText(R.string.desconectar_anclaje);
            } else {
                button.setText(R.string.conectar_anclaje);
            }
            }
        };

        context.registerReceiver(electroGUIReceiver, new IntentFilter(Constants.WIFI_AP_STATE_CHANGED));

        // Se contiene el boton en un linear layout para que no se expanda junto a la columna
        this.buttonLayout = new LinearLayout(context);
        this.buttonLayout.addView(button);

        // Se cargan los componentes en el layout
        this.mainLayout.addView(label);
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

        Log.d(NavGUI.class.getSimpleName(),"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
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