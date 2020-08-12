package coop.tecso.hcd.gui.components;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

/**
 * 
 * @author tecso.coop
 *
 */
@SuppressWarnings({"SimpleDateFormat"})
public final class TimeGUI extends CampoGUI {

	private int hora;
	private int minuto;
	
	private LinearLayout mainLayout;
	private LinearLayout buttonLayout;
	private Button button;
	private TimePicker timePicker;
	
	// MARK: - Constructors

	public TimeGUI(Context context, boolean enabled) {
		super(context, enabled);
	}
	
	// MARK: - Getters y Setters

	public String getValorView() {
		return this.button.getText().toString();
	}
	
	// MARK: -  Metodos

	protected Dialog crearDialog() {
		return new TimePickerDialog(this.context, mTimeSetListener, hora, minuto, true);
	}

	private TimePickerDialog.OnTimeSetListener mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			setTime(hourOfDay, minute);
		}
	};
	
	private void setTime(int hourOfDay, int minute) {
		hora = hourOfDay;
		minuto = minute;
		updateDisplay();

		evalCondicionalSoloLectura();
	}
	
	private void updateDisplay() {
	    String text = pad(hora) + ":" + pad(minuto);
		button.setText(text);
	}

	private static String pad(int c) {
		if (c >= 10) {
            return String.valueOf(c);
        } else {
            return "0" + String.valueOf(c);
        }
	}
	
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
		this.button.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.button.setOnClickListener(v -> {

			if (Build.VERSION.SDK_INT >= 24) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);

				timePicker = new TimePicker(context);
				timePicker.setIs24HourView(true);

				timePicker.setHour(hora);
				timePicker.setMinute(minuto);

				builder.setView(timePicker);
				builder.setNegativeButton("Cancelar", null);
				builder.setPositiveButton("Aceptar", (dialog, id) -> {
					timePicker.clearFocus();
					setTime(timePicker.getHour(), timePicker.getMinute());
				});

				AlertDialog dialog = builder.create();
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

				dialog.show();
			}
			else {
				crearDialog().show();
			}
		});
		this.button.setEnabled(enabled);

		// Se carga el valor inicial del campo: 'Valor Precargado', 'Valor por defecto de perfil' o 'Hora Actual'
		String hora;
		if (this.getInitialValues() != null && this.getInitialValues().size() == 1){
			hora = this.getInitialValues().get(0).getValor();
		} else {
			hora = this.getValorDefault();
		}

		Date date = new Date();
		if (!TextUtils.isEmpty(hora)){
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			try {
				date = sdf.parse(hora);
			} catch (ParseException e) {
				date = new Date();
				Log.d(TimeGUI.class.getSimpleName(), "build(): no se pudo parsear la hora: "+hora, e);
			} 
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		this.hora = calendar.get(Calendar.HOUR_OF_DAY);
		this.minuto = calendar.get(Calendar.MINUTE);
		updateDisplay();

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
		this.values = new ArrayList<Value>();

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
		String nombreCampo = campo!=null?campo.getCampo().getEtiqueta():"No identificado";
		String valor = this.getValorView();
		
		Log.d(TimeGUI.class.getSimpleName(),"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
				+" idCampo: "+(campo!=null?campo.getId():"null")
				+", idCampoValor: "+(campoValor!=null?campoValor.getId():"null")
				+", idCampoValorOpcion: "+(campoValorOpcion!=null?campoValorOpcion.getId():"null")
				+", Valor: "+valor);
		
		Value data = new Value(campo,campoValor,campoValorOpcion,valor, null);
		this.values.add(data);
		
		return this.values;
	}

	@Override
	public boolean isDirty(){
		if(!super.isDirty()){
			String valorActual = this.getValorView();
			this.dirty = !TextUtils.isEmpty(valorActual) && enabled;
		}
		
		return super.isDirty();
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
	public void clearData() {
		String hora = this.getValorDefault();
		Date date = new Date();
		if(!TextUtils.isEmpty(hora)){
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			try {
				date = sdf.parse(hora);
			} catch (ParseException e) {
				date = new Date();
				Log.d(TimeGUI.class.getSimpleName(), "build(): no se pudo parsear la hora: "+hora, e);
			} 
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		this.hora = calendar.get(Calendar.HOUR_OF_DAY);
		this.minuto = calendar.get(Calendar.MINUTE);
		updateDisplay();
	}
	
	@Override
	public void setFocus() {
		this.mainLayout.requestFocusFromTouch();
	}

}
