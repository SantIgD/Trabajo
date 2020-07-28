package coop.tecso.hcd.gui.components;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TextView;
import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;

public final class ListGUI extends CampoGUI {

	private String etiqueta;

	// MARK: - Constructor

	public ListGUI(Context context, boolean enabled) {
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
		layout.setGravity(Gravity.END);

		// Titulo: Se define un layout lineal horizonal para mostrar la etiqueta del campo y el boton para retraer/expandir
		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOrientation(LinearLayout.HORIZONTAL);
		titleLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// 	 Etiqueta
		this.label = new TextView(context);
		this.label.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(this.getEtiqueta());
		this.label.setGravity(Gravity.CENTER_VERTICAL);
		this.label.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

		titleLayout.addView(this.label);

		// Se define un layout de tipo Tabla para contener los distintos Campos
		TableLayout tableLayout = new TableLayout(context);
		tableLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		tableLayout.setPadding(10, 1, 0, 1);
		tableLayout.setColumnShrinkable(0, true);
		tableLayout.setColumnStretchable(1, true);

		// Se agregan los componentes correspondientes a los camposValor al layout contenedor
		if(this.components != null){
			int size = this.components.size();
			for (Component component: this.components) {
				tableLayout.addView(component.getView()); //(TableRow)

				// Separador de campos
				View line = new View(context);
				line.setBackgroundColor(context.getResources().getColor(R.color.line_background_color));

				if(--size > 0) {
                    tableLayout.addView(line, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                }
			}
		}

		// Espacio separador
		View gap = new View(context);
		gap.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		// Se agregan partes del componente al layout contenedor
		layout.addView(titleLayout);
		layout.addView(gap, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
		layout.addView(tableLayout);

		this.view = layout;

		return this.view;
	}

	@Override
	public View redraw() {
		for(Component co: this.components){
			CampoGUI campo = (CampoGUI) co;
			campo.redraw();
		}
		return this.view;
	}

	@Override
	public List<Value> values() {
		List<Value> values = new ArrayList<>();
		for(Component co: this.components){
			CampoGUI campo = (CampoGUI) co;
			values.addAll(campo.values());
		}
		return values;
	}

	@Override
	public boolean isDirty(){
		if(!super.isDirty()){
			// Recorremos los elementos de la lista verificando si alguno fue modificado
			for(Component co: this.components){
				CampoGUI campo = (CampoGUI) co;
				if(campo.isDirty()){
					return true;
				}
			}
		}

		return super.isDirty();
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
	
	@Override
	public View enable() {
		super.enable();
		if(this.components != null){
			for(Component co: this.components){
				co.enable();
			}
		}
		return this.view;
	}
	
	@Override
	public void clearData() {
		for(Component co: this.components){
			CampoGUI campo = (CampoGUI) co;
			campo.clearData();
		}
	}

}