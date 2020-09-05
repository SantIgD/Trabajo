package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
@SuppressLint({"SimpleDateFormat", "ClickableViewAccessibility"})
public final class DateGUI extends CampoGUI {

	private int mes;
	private int dia;
	private int anio;
	
	private LinearLayout mainLayout;
	private LinearLayout buttonLayout;
	private Button button;
	private DatePicker picker;
	
	// MARK: - Constructor

	public DateGUI(Context context, boolean enabled) {
		super(context, enabled);
	}
	
	// MARK: - Getters y Setters

	public String getValorView() {
		return this.button.getText().toString();
	}
	
	// MARK: -  Metodos

    private Dialog crearDialog() {
		return new DatePickerDialog(this.context, mDateSetListener, anio, mes, dia);
	}

	/**
	 *  Updates the date in the TextView
	 */
    private void updateDisplay() {
		Calendar date = Calendar.getInstance();
		date.set(anio, mes, dia);
		
		button.setText(new SimpleDateFormat("dd/MM/yyyy").format(date.getTime()));
	}

	/**
	 *  The callback received when the user "sets" the date in the dialog
	 */
	private DatePickerDialog.OnDateSetListener mDateSetListener = (view, year, monthOfYear, dayOfMonth) -> {
	    setDate(year, monthOfYear, dayOfMonth);
    };
	
	private void setDate(int year, int monthOfYear, int dayOfMonth) {
		anio = year;
		mes = monthOfYear;
		dia = dayOfMonth;
		updateDisplay();

		evalCondicionalSoloLectura();
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
		this.button.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.button.setFocusable(true);
		this.button.setFocusableInTouchMode(true);
		this.button.setOnTouchListener((v, event) -> {
			button.requestFocus();
			return false;
		});
		this.button.setOnClickListener(v -> {
			if (Build.VERSION.SDK_INT >= 24) {
				AlertDialog.Builder builder = new AlertDialog.Builder(context);

				picker = new DatePicker(context);
				picker.setCalendarViewShown(false);
				picker.updateDate(anio, mes, dia);

				builder.setView(picker);
				builder.setNegativeButton("Cancelar", null);
				builder.setPositiveButton("Aceptar", (dialog, id) -> {
						picker.clearFocus();
						setDate(picker.getYear(), picker.getMonth(), picker.getDayOfMonth());
				});

				AlertDialog dialog = builder.create();
				dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

				dialog.show();
			} else {
				crearDialog().show();
			}
		});
		this.button.setEnabled(enabled);

		// Se carga el valor inicial del campo: 'Valor Precargado', 'Valor por defecto de perfil' o 'Fecha Actual'
		String fecha;
		List<Value> initialValues = this.getInitialValues();
		if (!CollectionUtils.isEmpty(initialValues)) {
			fecha = this.getInitialValues().get(0).getValor();
		} else {
			fecha = this.getValorDefault();
		}

		Date date = new Date();
		if (!TextUtils.isEmpty(fecha)){
			SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy"); 
			try {
				date = sdf.parse(fecha);
			} catch (ParseException e) {
				date = new Date();
				Log.e(DateGUI.class.getSimpleName(), "build(): no se pudo parsear la fecha: "+fecha, e);
			} 
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		this.anio = calendar.get(Calendar.YEAR);
		this.mes = calendar.get(Calendar.MONTH);
		this.dia = calendar.get(Calendar.DAY_OF_MONTH);
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
		if (this.entity instanceof AplPerfilSeccionCampo){
			campo = (AplPerfilSeccionCampo) this.entity;
		} else if(this.entity instanceof AplPerfilSeccionCampoValor){
			campoValor = (AplPerfilSeccionCampoValor) this.entity;
			campo = campoValor.getAplPerfilSeccionCampo();
		} else if(this.entity instanceof AplPerfilSeccionCampoValorOpcion){
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) this.entity;
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}
		String nombreCampo = campo!=null?campo.getCampo().getEtiqueta():"No identificado";
		String valor = this.getValorView();
		
		Log.d(DateGUI.class.getSimpleName(),"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
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
	public void setFocus() {
		this.mainLayout.requestFocusFromTouch();
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
		this.anio = calendar.get(Calendar.YEAR);
		this.mes = calendar.get(Calendar.MONTH);
		this.dia = calendar.get(Calendar.DAY_OF_MONTH);
		updateDisplay();
	}

}
