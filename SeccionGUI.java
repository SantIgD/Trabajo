package coop.tecso.hcd.gui.components;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;

public final class SeccionGUI extends Component {

	private String etiqueta;
	protected ImageButton btnExpandible;
	private TableLayout tableLayout;

	private boolean noDesplegar = false;
	private boolean opcional = false;
	private boolean expanded = true;

	// MARK: - Constructores

	public SeccionGUI(Context context) {
		super();
		this.context = context;
	}

	public SeccionGUI(Context context, boolean enabled) {
		super();
		this.context = context;
		this.enabled = enabled;
	}

	// MARK: - Getters y Setters

	public String getEtiqueta() {
		return etiqueta;
	}
	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}
	public TableLayout getTableLayout() {
		return tableLayout;
	}
	public boolean isNoDesplegar() {
		return noDesplegar;
	}
	public void setNoDesplegar(boolean noDesplegar) {
		this.noDesplegar = noDesplegar;
	}
	public boolean isOpcional() {
		return opcional;
	}
	public void setOpcional(boolean opcional) {
		this.opcional = opcional;
	}

	// MARK: - Metodos

	@Override
	public void addValue(Value value) { }

	@Override
	public List<Value> values() {
		List<Value> values = new ArrayList<>();
		if (this.components != null) {
			for (Component component: this.components) {
				CampoGUI campo = (CampoGUI) component;
				values.addAll(campo.values());
			}
		}
		return values;
	}

	public List<Value> dirtyValues() {
		List<Value> values = new ArrayList<>();
		if (this.components != null) {
			for(Component component: this.components){
				CampoGUI campo = (CampoGUI) component;
				values.addAll(campo.dirtyValues());
			}
		}
		return values;
	}

	@Override
	public boolean isDirty(){
		if (!super.isDirty()) {
			// Recorremos los elementos de la seccion verificando si alguno fue modificado
			for(Component component: this.components){
				CampoGUI campo = (CampoGUI) component;
				if (campo.isDirty()) {
					return true;
				}
			}
		}

		return super.isDirty();
	}

	@Override
	public boolean validate() {
		// Recorremos los elementos de la seccion verificando si algun requerido es vacio
		for (Component component: this.components) {
			CampoGUI campo = (CampoGUI) component;

			if (!campo.validate()) {
				this.expand();
				View campoGui = campo.getView();

				if (campoGui != null) {
                    campoGui.clearFocus();
                    campoGui.requestFocus();
				}
				return false;
			}
			else{
				this.contract();
			}
		}
		return true;
	}

	@Override
	public View build() {
		// Se define un layout lineal vertical para armar la seccion desplegable
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		// Titulo: Se define un layout lineal horizonal para mostrar la etiqueta de seccion y el boton para retraer/expandir
		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOrientation(LinearLayout.HORIZONTAL);
		titleLayout.setBackgroundColor(context.getResources().getColor(R.color.label_background_color));
		titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// Boton para Expander/Retraer
		this.btnExpandible = new ImageButton(context);
		this.btnExpandible.setBackgroundResource(R.drawable.ic_menu_contraer);
		this.btnExpandible.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.btnExpandible.setOnClickListener(view -> {
			if (expanded) {
				contract();
			} else {
				expand();
			}
		});

		// Etiqueta
		TextView label = new TextView(context);
		label.setBackgroundColor(context.getResources().getColor(R.color.label_background_color));
		label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		label.setText(this.getEtiqueta());
		label.setGravity(Gravity.CENTER_VERTICAL);
		label.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		titleLayout.addView(this.btnExpandible);
		titleLayout.addView(label);

		// Se define un layout de tipo Tabla para contener los distintos Campos
		this.tableLayout = new TableLayout(context);
		this.tableLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.tableLayout.setPadding(10, 2, 10, 2);

		this.tableLayout.setColumnShrinkable(0, true);
		this.tableLayout.setColumnStretchable(1, true);

		this.tableLayout.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		this.tableLayout.setGravity(Gravity.CENTER);

		this.addComponents();

		// Espacio separador
		View gap = new View(context);
		gap.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		// Se agregan partes del componente al layout contenedor
		layout.addView(titleLayout);
		layout.addView(gap, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
		layout.addView(this.tableLayout);

		// Se verifica si debe inicializarse sin desplegar
		if (this.isNoDesplegar()) {
            contract();
        }

		this.view = layout;

		// Se verifica si la seccion es opcional. En dicho caso se oculta
		if (this.isOpcional()) {
			ocultar();
		}

		return this.view;
	}

	@Override
	public View redraw() {
		if (this.components != null) {
			for(Component component: this.components){
				component.redraw();
			}
		}
		return this.view;
	}

	@Override
	public View disable() {
		if(this.components != null){
			for(Component component: this.components){
				component.disable();
			}
		}
		return this.view;
	}

	/**
	 * Expande la seccion
	 */
	protected void expand() {
		if (this.expanded) {
		    return;
        }

		this.tableLayout.setVisibility(View.VISIBLE);
		this.btnExpandible.setBackgroundResource(R.drawable.ic_menu_contraer);
		this.expanded = true;
	}

	/**
	 * Contrae la seccion
	 */
	protected void contract() {
		if (!this.expanded) {
		    return;
        }

		this.tableLayout.setVisibility(View.GONE);
		this.btnExpandible.setBackgroundResource(R.drawable.ic_menu_expandir);
		this.expanded = false;
	}

	/**
	 * Oculta la seccion
	 */
	public View ocultar() {
		if (isVisible()) {
			this.view.setVisibility(View.GONE);
		}
		return this.view;
	}

	/**
	 * Muestra la seccion oculta
	 */
	public View mostrar() {
		if (this.view != null && this.view.getVisibility() == View.GONE) {
			this.view.setVisibility(View.VISIBLE);
		}
		return this.view;
	}

	/**
	 * Devuelve true si la seccion es visible
	 */
	public boolean isVisible(){
		return (this.view != null && this.view.getVisibility() == View.VISIBLE);
	}

    /**
     * Aagrega los componentes correspondientes a los campos al layout contenedor
     */
	private void addComponents() {
        if (this.components == null) {
            return;
        }

        Iterator<Component> iteratorComponents = this.components.iterator();

        while (iteratorComponents.hasNext()) {
            try {
                TableRow tableRow = new TableRow(context);
                tableRow.setLayoutParams(new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, 0,1));
                Component component = iteratorComponents.next();
                AplPerfilSeccionCampo aplPerfilSeccionCampo = (AplPerfilSeccionCampo) component.getEntity();
                int ancho = aplPerfilSeccionCampo.getAncho();

                if (ancho == 0) {
                    ancho = 100;
                }

                float anchoIt = (float)ancho / 100;

                if (anchoIt < 0.9) {
                    while (anchoIt <= 1) {
                        aplPerfilSeccionCampo = (AplPerfilSeccionCampo) component.getEntity();
                        ancho = aplPerfilSeccionCampo.getAncho();
                        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(0, LayoutParams.WRAP_CONTENT, (float)ancho / 100);
                        layoutParams.rightMargin = GUIHelper.dpToPixel(15, getContext()); // right-margin = 10dp

                        component.getView().setLayoutParams(layoutParams);
                        tableRow.addView(component.getView());

                        if (anchoIt == 0.99f || anchoIt == 1) {
                            break;
                        } else {
                            component = iteratorComponents.next();
                            aplPerfilSeccionCampo = (AplPerfilSeccionCampo) component.getEntity();
                            ancho = aplPerfilSeccionCampo.getAncho();
                            anchoIt += (float)ancho / 100;
                        }
                    }
                } else {
                    component.getView().setLayoutParams(new TableRow.LayoutParams(0,LayoutParams.WRAP_CONTENT, 1));
                    tableRow.addView(component.getView());
                }

                this.tableLayout.addView(tableRow, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)); //(TableRow)

                // Separador de campos
                View line = new View(context);
                line.setBackgroundColor(context.getResources().getColor(R.color.line_background_color));

                this.tableLayout.addView(line, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
