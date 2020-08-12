package coop.tecso.hcd.gui.components;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.components.TextGUI.Teclado;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.CampoValor;

/**
 * 
 * @author tecso.coop
 *
 */
public final class DynamicListGUI extends CampoGUI {

	private String etiqueta;
	private ImageButton addButton;
	private LinearLayout componentLayout;
	
	private List<ImageButton> listBtnRemove; 
	private List<CampoGUI> elements;
	private int numberOfImageFields = 0; 
	
	// MARK: - Constructors

	public DynamicListGUI(Context context, boolean enabled) {
		super(context, enabled);
		this.elements = new ArrayList<>();
		this.listBtnRemove = new ArrayList<>();
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
		LinearLayout mainLayout = new LinearLayout(context);
		mainLayout.setOrientation(LinearLayout.VERTICAL);
		mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
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
		this.label.setFocusable(true);
		this.label.setFocusableInTouchMode(true);

		// Boton para agregar elemento a la lista
		this.addButton = new ImageButton(context);
		this.addButton.setBackgroundResource(R.drawable.ic_menu_add);
		this.addButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.addButton.setEnabled(enabled);
		this.addButton.setOnClickListener(v -> {
		    this.label.setError(null);
			this.addItem(null);
			this.dirty = true;

            this.evalCondicionalSoloLectura();
		});

		// Se crea layout para alinear icono a la derecha
		LinearLayout btnAddLayout = new LinearLayout(context);
		btnAddLayout.setGravity(Gravity.END);
		btnAddLayout.addView(this.addButton);
		btnAddLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		titleLayout.addView(this.label);
		titleLayout.addView(btnAddLayout);
		
		// Se define un layout de tipo Linear para contener los distintos Campos
		this.componentLayout = new LinearLayout(context);
		this.componentLayout.setOrientation(LinearLayout.VERTICAL);
		this.componentLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		
		// Se recorren los valores precargados y por cada uno se agrega un elemento
		if (this.getInitialValues() != null) {
			for (Value initialValue: getInitialValues()) {
				addItem(initialValue);
			}
		}

		// Espacio separador
		View gap = new View(context);
		gap.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		
		// Se agregan partes del componente al layout contenedor
		mainLayout.addView(titleLayout);
		mainLayout.addView(gap, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
		mainLayout.addView(this.componentLayout);
		
		this.view = mainLayout;
		
		return this.view;
	}

	@Override
	public View redraw() {
		this.addButton.setEnabled(enabled);
		for (CampoGUI element: this.elements) {
			if (this.enabled) {
                element.enable();
            }
			else {
                element.disable();
            }
		}
		
		for (ImageButton button : this.listBtnRemove) {
			button.setEnabled(this.enabled);
		}
		return this.view;
	}
	
	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();
		// Recorremos los elementos de la lista y se toman los valores cargados
		for (CampoGUI campo: this.elements) {
			values.addAll(campo.values());
		}
		return this.values;
	}
	
	@Override
	public boolean isDirty(){
		if (!super.isDirty()) {
			// Recorremos los elementos de la lista verificando si alguno fue modificado
			for(CampoGUI campo: this.elements){
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
		if (this.components != null) {
			for(Component component: this.components){
				component.disable();
			}
		}

		return this.view;
	}

	@Override
	public boolean validate() {
		if (isObligatorio() && this.elements.size() == 0) {
		    String errorMessage = context.getString(R.string.field_required, getEtiqueta());
			this.label.setError(errorMessage);
			setFocus();
			return false;
		}

        for (CampoGUI campo: this.elements) {
			if (!campo.validate()) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public void setFocus() {
		GUIHelper.showError(context, context.getString(R.string.field_required, getEtiqueta()));
		addButton.requestFocus();
	}
	
	/**
	 * Elimina un item de la lista
	 */
	private void removeItem(View btnRemove){
		// Se busca el indice del componente en la lista
		int index = this.listBtnRemove.indexOf(btnRemove);
		
		if (index == -1) {
            return;
        }

        // Con el indice se elimina el elemento en la misma posicion en la lista de elementos
        CampoGUI removedButton = this.elements.remove(index);

        this.listBtnRemove.remove(index);

        // Con el indice se elimina el view del componente de la tabla de visualizacion
        this.componentLayout.removeViewAt(index);

        // Se chequea si hay que volver a habilitar la inserción de imágenes
        if (removedButton instanceof AttacherGUI){
            this.numberOfImageFields--;
            if (!this.addButton.isEnabled()){
                this.addButton.setEnabled(true);
                this.addButton.setClickable(true);
                this.addButton.getBackground().setColorFilter(null);
            }
        }
	}

	/**
	 * Agrega un item a la lista. Para esto crea un nuevo comopente a partir del CampoValor detalle.
	 */
	private void addItem(Value initialValue) {
		// Identificamos el CampoValor de detalle (solo debe existir uno)
		AplPerfilSeccionCampoValor perfilSeccionCampoValor = null; 
		CampoGUI campo = null; 
		if (!CollectionUtils.isEmpty(components)) {
			campo = (CampoGUI) this.components.get(0);
			if (campo != null && campo.getEntity() instanceof AplPerfilSeccionCampoValor) {
				perfilSeccionCampoValor = (AplPerfilSeccionCampoValor) campo.getEntity();
			}
		}

		if(perfilSeccionCampoValor == null){
			return;
		}
		
		// Creamos nuevo elemento
		final CampoValor campoValor = perfilSeccionCampoValor.getCampoValor();
		Tratamiento tratamiento = Tratamiento.getByCod(campoValor.getTratamiento());
		
		if (tratamiento.equals(Tratamiento.DESCONOCIDO) && campoValor.getTratamientoDefault() != null) {
			tratamiento = Tratamiento.getByCod(campoValor.getTratamientoDefault());
		}

		boolean showKeyboard = false;
		
		CampoGUI newElement;
		switch (tratamiento) {
		case TA: // Alfanumerico 
			newElement = new TextGUI(context, Teclado.ALFANUMERICO, false, enabled);
			showKeyboard = true;	
			break;
		case TAM: // Alfanumerico Multilinea
			newElement = new TextGUI(context,Teclado.ALFANUMERICO, true, enabled);
			showKeyboard = true;
			break;
		case TNE: // Entero
			newElement = new TextGUI(context,Teclado.NUMERICO, false, enabled);
			showKeyboard = true;
			break;
		case TND: // Decimal
			newElement = new TextGUI(context,Teclado.DECIMAL, false, enabled);
			showKeyboard = true;
			break;
		case TN2: // Numerico Extendido
			newElement = new TextGUI(context,Teclado.NUMERICO_EXTENDIDO, false, enabled);
			showKeyboard = true;
			break;
		case TF:  // Fecha
			newElement = new DateGUI(context, enabled);
			break;
		case TT:  // Hora
			newElement = new TimeGUI(context, enabled);
			break;
		case BU:  // Busqueda en Tabla (EntidadBusqueda)
			newElement = new DataSearchGUI(context, enabled);
			break;
		case PIC:  // Adjuntar Imagen
			if (this.numberOfImageFields >= Constants.MAX_NUMBER_OF_IMAGES_IN_DL){
				String errMsg = context.getResources().getString(R.string.maximo_adjuntos);
				Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show();
				return;
			}
			else {
				newElement = new AttacherGUI(context, enabled);
				((AttacherGUI)newElement).setEscala((float)perfilSeccionCampoValor.getResolucion()/100);
				this.numberOfImageFields++;
			}
			break;
		default:
			// Tratamiento no implementado
			newElement = new CampoGUI(context);
			break;
		}
		newElement.setPerfilGUI(perfilGUI);
		newElement.setEntity(perfilSeccionCampoValor);
		newElement.setEtiqueta(campoValor.getEtiqueta());
		newElement.setObligatorio(perfilSeccionCampoValor.isObligatorio());
		newElement.setValorDefault(campoValor.getValorDefault());
		
		newElement.setTratamiento(Tratamiento.getByCod(campoValor.getTratamiento()));
		if (newElement.getTratamiento().equals(Tratamiento.DESCONOCIDO) && campoValor.getTratamientoDefault() != null) {
			newElement.setTratamiento(Tratamiento.getByCod(campoValor.getTratamientoDefault()));
		}
		
		newElement.setTablaBusqueda(campo.getTablaBusqueda());
		newElement.setComponents(new ArrayList<>());
		// Carga de valor inicial (precargado)
		if (initialValue != null) {
			List<Value> initialValues = new ArrayList<>();
			initialValues.add(initialValue);
			newElement.setInitialValues(initialValues);
		}
		newElement.build();

		// Se crea el boton para eliminar el elemento
		ImageButton btnRemove = new ImageButton(context);
		btnRemove.setBackgroundResource(R.drawable.ic_menu_remove);
		btnRemove.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		btnRemove.setEnabled(enabled);
		btnRemove.setOnClickListener(view -> {
		    confirmAndDeleteItem(view, campoValor.getEtiqueta());
        });
		
		// Agregamos nuevo elemento a lista
		this.elements.add(newElement);
		// Se agrega el boton de eliminacion asociado al nuevo elemento a una lista (esto permite obtener el indice del elemento en la lista)
		this.listBtnRemove.add(btnRemove);

		// Layour para centrar el boton eliminar
		LinearLayout btnRemoveLayout = new LinearLayout(context);
		btnRemoveLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		btnRemoveLayout.setGravity(Gravity.CENTER);
		btnRemoveLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		btnRemoveLayout.addView(btnRemove);
		
		// Layout contenedor del elemento mas el boton eliminar
		LinearLayout container = new LinearLayout(context);
		container.setOrientation(LinearLayout.HORIZONTAL);
		container.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		container.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		container.addView(btnRemoveLayout);
		container.addView(newElement.getView(), new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		// Se agrega elemento al layout de componentes
		this.componentLayout.addView(container);

		try {
			newElement.setFocus();

			if (showKeyboard) {
				InputMethodManager imm = (android.view.inputmethod.InputMethodManager) this.context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(((LinearLayout)newElement.getView()).getFocusedChild(), InputMethodManager.SHOW_IMPLICIT);
			}
		}
		catch (Exception ignore) {}
	}

	@Override
	public void clearData() {
		for (int index=0; index<this.elements.size(); index++) {
			// Con el indice se elimina el elemento en la misma posicion en la lista de elementos
			CampoGUI removedButton = this.elements.remove(index);
			this.listBtnRemove.remove(index);
			
			// Con el indice se elimina el view del componente de la tabla de visualizacion
			this.componentLayout.removeViewAt(index);
			
			// Se resta la cantidad de imágenes adjuntas
			if (removedButton instanceof AttacherGUI){
				this.numberOfImageFields--;
			}
		}
		this.dirty = false;
	}

	private void confirmAndDeleteItem(View view, String etiqueta) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle(R.string.confirm_title);
        builder.setMessage(view.getContext().getString(R.string.delete_item_confirm_msg, etiqueta));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            // Elimina el item
            removeItem(view);
            dirty = true;

            evalCondicionalSoloLectura();
        });
        builder.setNegativeButton(R.string.no, null);

        builder.create().show();
    }

}