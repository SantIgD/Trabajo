package coop.tecso.hcd.gui.components;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.components.TextGUI.Teclado;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.CampoValor;

/**
 * 
 * @author tecso.coop
 *
 */
public final class ResumenGUI extends CampoGUI {

	private String etiqueta;
	private ImageButton btnAdd;
	private LinearLayout componentLayout;

	private List<ImageButton> listBtnRemove;
	private List<CampoGUI> elements;

	// MARK: - Constructor

	public ResumenGUI(Context context, boolean enabled) {
		super(context, enabled);
		this.elements = new ArrayList<>();
		this.listBtnRemove = new ArrayList<>();
	}

	// MARK: - Getters & Setters

	public String getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	// Metodos
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

		// Etiqueta
		this.label = new TextView(context);
		this.label.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(this.getEtiqueta());
		this.label.setGravity(Gravity.CENTER_VERTICAL);
		this.label.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        this.label.setFocusable(true);
        this.label.setFocusableInTouchMode(true);

		// Boton para agregar elemento a la lista
		this.btnAdd = new ImageButton(context);
		this.btnAdd.setBackgroundResource(R.drawable.ic_menu_add);
		this.btnAdd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.btnAdd.setEnabled(enabled);
		this.btnAdd.setOnClickListener(v -> {
			if (!isElementExist()) {
				this.addItem(summaryValue());
			}

            this.label.setError(null);
			this.dirty = true;
            this.evalCondicionalSoloLectura();
		});

		// Se crea layout para alinear icono a la derecha
		LinearLayout btnAddLayout = new LinearLayout(context);
		btnAddLayout.setGravity(Gravity.END);
		btnAddLayout.addView(this.btnAdd);
		btnAddLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		titleLayout.addView(this.label);
		titleLayout.addView(btnAddLayout);

		// Se define un layout de tipo Linear para contener los distintos Campos
		this.componentLayout = new LinearLayout(context);
		this.componentLayout.setOrientation(LinearLayout.VERTICAL);
		this.componentLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		// Precarga
		if (this.getInitialValues() != null) {
            this.addItem(this.getInitialValues().get(0));
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
		this.btnAdd.setEnabled(enabled);

		for (CampoGUI element: this.elements){
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
		for(CampoGUI campo: this.elements){
			values.addAll(campo.values());
		}

		return this.values;
	}

	@Override
	public boolean isDirty(){
		if (!super.isDirty()) {
			// Recorremos los elementos de la lista verificando si alguno fue modificado
			for (CampoGUI campo: this.elements) {
				if (campo.isDirty()) {
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
			for (Component component: this.components) {
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
		btnAdd.requestFocus();
	}

	/**
	 * Elimina un item de la lista
	 */
	private void removeItem(View btnRemove){
		// Se busca el indice del componente en la lista
		int index = this.listBtnRemove.indexOf(btnRemove);

		if (index != -1) {
			this.elements.remove(index);
			this.listBtnRemove.remove(index);
			this.componentLayout.removeViewAt(index);
		}
	}

	/**
	 * Agrega un item TAM único a la lista.
	 */
	private void addItem(Value initialValue) {
		if (!CollectionUtils.isEmpty(elements)) {
            return;
        }

		// Identificamos el CampoValor de detalle (solo debe existir uno)
		AplPerfilSeccionCampoValor perfilSeccionCampoValor = null;
		CampoGUI campo = null;

		if (!CollectionUtils.isEmpty(components)){
			campo = (CampoGUI) this.components.get(0);
			if (campo != null && campo.getEntity() instanceof AplPerfilSeccionCampoValor) {
				perfilSeccionCampoValor = (AplPerfilSeccionCampoValor) campo.getEntity();
			}
		}

		if (perfilSeccionCampoValor == null) {
			return;
		}

		// Creamos nuevo elemento
		final CampoValor campoValor = perfilSeccionCampoValor.getCampoValor();
		Tratamiento tratamiento = Tratamiento.getByCod(campoValor.getTratamiento());

		if (tratamiento != Tratamiento.TAM) {
			return;
		}

		CampoGUI newElement = new TextGUI(context,Teclado.ALFANUMERICO, true, enabled, 1000);

		newElement.setPerfilGUI(perfilGUI);
		newElement.setEntity(perfilSeccionCampoValor);
		newElement.setEtiqueta(campoValor.getEtiqueta());
		newElement.setLabel(false);
		newElement.setObligatorio(perfilSeccionCampoValor.isObligatorio());
		newElement.setValorDefault(campoValor.getValorDefault());
		newElement.setTratamiento(tratamiento);
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
		    String label = campoValor.getEtiqueta();
            confirmItemDeletion(view, label);
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
	}

	private boolean isElementExist() {
		final boolean[] res = { false };

		if (!CollectionUtils.isEmpty(elements)) {
			res[0] = true;
            AlertDialog.Builder builder = new AlertDialog.Builder(this.context);
            builder.setTitle(R.string.confirm_title);
            builder.setCancelable(false);
            builder.setMessage(R.string.update_item_confirm_msg);
            builder.setPositiveButton(R.string.yes, (dialogInterface, i) -> updItem());
            builder.setNegativeButton(R.string.no, null);
            builder.create().show();
		}
		return res[0];
	}

	private void updItem() {
		this.elements.get(0).setValorDefault(summaryGenerate(getCamposRelacionados()));
		this.elements.get(0).clearData();
	}

	private String getCamposRelacionados(){
		AplPerfilSeccionCampo campo;
		AplPerfilSeccionCampoValor campoValor;
		CampoGUI campoGUI;

		if (!CollectionUtils.isEmpty(components)) {
			campoGUI = (CampoGUI) this.components.get(0);
			if (campoGUI != null && campoGUI.getEntity() instanceof AplPerfilSeccionCampoValor) {
				campoValor = (AplPerfilSeccionCampoValor) campoGUI.getEntity();
				campo = campoValor.getAplPerfilSeccionCampo();
				return campo.getCamposRelacionados();
			}
		}
		return null;
	}

	private Value summaryValue(){
		AplPerfilSeccionCampo campo;
		AplPerfilSeccionCampoValor campoValor;
		CampoGUI campoGUI;

		if (!CollectionUtils.isEmpty(components)){
			campoGUI = (CampoGUI) this.components.get(0);
			if (campoGUI != null && campoGUI.getEntity() instanceof AplPerfilSeccionCampoValor) {
				campoValor = (AplPerfilSeccionCampoValor) campoGUI.getEntity();
				campo = campoValor.getAplPerfilSeccionCampo();
				return new Value(campo, campoValor, null, summaryGenerate(campo.getCamposRelacionados()), null);
			}
		}
		return null;
	}

	private String summaryGenerate(String camposRelacionados) {
		if (TextUtils.isEmpty(camposRelacionados)) {
		    return "";
        } else {
		    return this.perfilGUI.getEpicrisisFromKeys(camposRelacionados);
        }
	}

	@Override
	public void clearData() {
		for (int index = 0; index < this.elements.size(); index++) {
			// Con el indice se elimina el elemento en la misma posicion en la lista de elementos
			this.elements.remove(index);
			this.listBtnRemove.remove(index);
			
			// Con el indice se elimina el view del componente de la tabla de visualizacion
			this.componentLayout.removeViewAt(index);

		}
		this.dirty = false;
	}

	private void confirmItemDeletion(final View view, String label) {
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

        builder.setTitle(R.string.confirm_title);
        builder.setMessage(view.getContext().getString(R.string.delete_item_confirm_msg, label));
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
