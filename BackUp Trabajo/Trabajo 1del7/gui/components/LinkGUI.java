package coop.tecso.hcd.gui.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

/**
 * Tratamiento Link a Aplicaciones o Web pages.
 * @author tecso.coop
 *
 */
public final class LinkGUI extends CampoGUI {
	
	private LinearLayout mainLayout;
	private LinearLayout buttonLayout;
	private Button button;

	// MARK: - Constructs

	public LinkGUI(Context context, boolean enabled) {
		super(context, enabled);
	}

	// MARK: - Getters y Setters
	public String getValorView() {
		return this.valorDefault;
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

		// Se arma el boton para disparar control se seleccion de fecha
		this.button = new Button(context);
		this.button.setText(context.getString(R.string.abrir));
		this.button.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.button.setOnClickListener(v -> {

			Intent intent;
			if (valorDefault.contains("#")){
				String[] params = valorDefault.split("#");
				// Custom Application Launcher
				intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_LAUNCHER);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setComponent(new ComponentName(params[0], params[1]));
			} else {
				// Browser Launcher
				intent = new Intent(Intent.ACTION_VIEW, Uri.parse(valorDefault));
			}

			if (Utils.canHandleIntent(context, intent)){
				context.startActivity(intent);
				return;
			}

			// BugFix Error 22 – Acceso a Link incorrecto
			String err = "No se puede ejecutar el enlace: " + valorDefault + "\nPor favor, contactese con el administrador.";
			final AlertDialog alertDialog = new AlertDialog.Builder(context).create();
			alertDialog.setTitle("Error al ejecutar enlace");
			alertDialog.setMessage(err);
			alertDialog.setCancelable(false);
			alertDialog.setIcon(R.drawable.ic_error_default);
			alertDialog.setButton(Dialog.BUTTON_POSITIVE, context.getString(R.string.accept),(dialog, id) -> alertDialog.dismiss());
			alertDialog.show();
		});
		this.button.setEnabled(enabled);

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

		if(this.entity instanceof AplPerfilSeccionCampo){
			campo = (AplPerfilSeccionCampo) this.entity;
		}else if(this.entity instanceof AplPerfilSeccionCampoValor){
			campoValor = (AplPerfilSeccionCampoValor) this.entity;
			campo = campoValor.getAplPerfilSeccionCampo();
		}else if(this.entity instanceof AplPerfilSeccionCampoValorOpcion){
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) this.entity;
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}
		String nombreCampo = campo != null ? campo.getCampo().getEtiqueta() : "No identificado";
		String valor = this.getValorView();

		Log.d(LinkGUI.class.getSimpleName(),"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
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