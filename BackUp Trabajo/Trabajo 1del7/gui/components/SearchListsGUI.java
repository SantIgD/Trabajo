package coop.tecso.hcd.gui.components;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.components.TextGUI.Teclado;
import coop.tecso.hcd.gui.helpers.FilaTablaBusqueda;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.CampoValor;

public final class SearchListsGUI extends CampoGUI {

	private String etiqueta;
	private ImageButton btnAdd;
	private LinearLayout elementsLayout;

	private List<String> codigos;    
	private List<List<CampoGUI>> elements;
	// La lista de botones para eliminar elementos queda asociada por indice con el elemento
	private List<ImageButton> listBtnRemove; 

	private AutoCompleteTextView txtBusqueda;

	private Map<String,FilaTablaBusqueda> mapBusquedaByCod;
	private Map<Long,FilaTablaBusqueda> mapBusquedaById;
	private String codigoEntidadBusqueda = null; 

	// MARK: - Constructor

	public SearchListsGUI(Context context, boolean enabled) {
		super(context, enabled);
		this.codigos = new ArrayList<>();
		this.elements = new ArrayList<>();
		this.listBtnRemove = new ArrayList<>();
	}

	// Getters y Setters
	public String getEtiqueta() {
		return etiqueta;
	}
	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}


	// Metodos
	@Override
	public View build() {
	    String label = this.getEtiqueta() + "";

		// Se define un layout lineal vertical para armar el campo desplegable
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		layout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		// Titulo: Se define un layout lineal horizonal para mostrar la etiqueta del campo
		LinearLayout titleLayout = new LinearLayout(context);
		titleLayout.setOrientation(LinearLayout.HORIZONTAL);
		titleLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		titleLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		// 	Etiqueta
		this.label = new TextView(context);
		this.label.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(label);
		this.label.setGravity(Gravity.CENTER_VERTICAL);
		this.label.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
		if (this.label.getWidth() > 100) {
            this.label.setLayoutParams(new LayoutParams(100, LayoutParams.MATCH_PARENT));
        }

		// Layout de busqueda
		LinearLayout searchLayout = new LinearLayout(context);
		searchLayout.setOrientation(LinearLayout.HORIZONTAL);
		searchLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		searchLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		// Texto para Busqueda
		this.txtBusqueda = new AutoCompleteTextView(context);
		this.txtBusqueda.setHint("Buscar");
		this.txtBusqueda.setEnabled(enabled);
		this.txtBusqueda.setFocusable(enabled);
		int minSize = 250;
		this.txtBusqueda.setMinimumWidth(minSize);
		this.txtBusqueda.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		this.txtBusqueda.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

		this.mapBusquedaByCod = new HashMap<>();
		this.mapBusquedaById = new HashMap<>();
		if(this.tablaBusqueda != null){
			for (FilaTablaBusqueda ftb : this.tablaBusqueda) {
				mapBusquedaById.put(ftb.getId(),ftb);
				mapBusquedaByCod.put(ftb.getCodigo(),ftb);
			}
		}
		SearchAdapter adapter = new SearchAdapter(context, tablaBusqueda);
		this.txtBusqueda.setAdapter(adapter);
		this.txtBusqueda.setOnItemClickListener((parent, v, pos, row) -> {
			FilaTablaBusqueda fila = mapBusquedaById.get(row);
			codigoEntidadBusqueda = fila.getCodigo();
			String value = fila.getDescripcion();
			txtBusqueda.setText(value);
		});
		// Boton para agregar elemento a la lista
		this.btnAdd = new ImageButton(context);
		this.btnAdd.setBackgroundResource(R.drawable.ic_menu_add);
		this.btnAdd.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.btnAdd.setEnabled(enabled);
		this.btnAdd.setOnClickListener(v -> {
			if (!TextUtils.isEmpty(codigoEntidadBusqueda)) {
				addItem(null);
				dirty = true;
			}
		});
		searchLayout.addView(this.txtBusqueda);

		// Layout para alinear icono a la derecha
		LinearLayout btnAddLayout = new LinearLayout(context);
		btnAddLayout.setGravity(Gravity.END);
		btnAddLayout.addView(this.btnAdd);
		btnAddLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		titleLayout.addView(this.label);
		titleLayout.addView(btnAddLayout);

		// Se define un layout de tipo Tabla para contener los distintos Campos
		this.elementsLayout = new LinearLayout(context);
		this.elementsLayout.setOrientation(LinearLayout.VERTICAL);
		this.elementsLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		if(getInitialValues() != null){
			Map<String, List<Value>> mapInitialFieldValues = new HashMap<>();
			List<Value> initialElements = new ArrayList<>();
			// Se recorren los valores precargados identificando el que corresponde a la entidad de busqueda seleccionada y separando los valores asociados a esta. 
			for(Value value: getInitialValues()){
				if(!TextUtils.isEmpty(value.getCodigoEntidadBusqueda())){
					if(Utils.isNotNull(value.getCampoValor())){
						List<Value> initialFieldValues = mapInitialFieldValues.get(value.getCodigoEntidadBusqueda());
						if(initialFieldValues == null){
							initialFieldValues = new ArrayList<>();
						}
						initialFieldValues.add(value);
						mapInitialFieldValues.put(value.getCodigoEntidadBusqueda(), initialFieldValues);
					}else{
						initialElements.add(value);
					}
				}
			}
			// Se recorren y agregran los elementos iniciales identificados
			for(Value value: initialElements){
				this.codigoEntidadBusqueda = value.getCodigoEntidadBusqueda();
				try {
					addItem(mapInitialFieldValues.get(value.getCodigoEntidadBusqueda()));
				} catch (Exception e) {
					Log.d(DataSearchGUI.class.getSimpleName(),
							"ERROR al cargar el campo: " + this.etiqueta + 
							" no existe el código '" + this.codigoEntidadBusqueda + "'");
				}
			}
		}

		// Espacio separador
		View gap = new View(context);
		gap.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

		// Se agregan partes del componente al layout contenedor
		layout.addView(titleLayout);
		layout.addView(searchLayout);
		layout.addView(gap, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 4));
		layout.addView(this.elementsLayout);

		this.view = layout;

		return this.view;
	}

	@Override
	public View redraw() {
		this.txtBusqueda.setEnabled(enabled);
		this.txtBusqueda.setFocusable(enabled);
		this.txtBusqueda.setFocusableInTouchMode(enabled);
		this.btnAdd.setEnabled(enabled);
		for (List<CampoGUI> campos: this.elements) {
			for(CampoGUI campo: campos){
				if (this.enabled) {
                    campo.enable();
                }
				else {
                    campo.disable();
                }
			}
		}
		
		for (ImageButton btn : this.listBtnRemove) {
			btn.setEnabled(this.enabled);
		}
		return this.view;
	}

	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();

		int index = 0;
		for(String codigoEntidadBusqueda: this.codigos){
			// Agregamos un valor por cada codigo seleccionado
			AplPerfilSeccionCampo campo = (AplPerfilSeccionCampo) this.entity;
			AplPerfilSeccionCampoValor campoValor = null;
			AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;
			String nombreCampo = campo!=null?campo.getCampo().getEtiqueta():"No identificado";
			Log.d(DataSearchGUI.class.getSimpleName(),"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
					+" idCampo: "+(campo!=null?campo.getId():"null")
					+", idCampoValor: null"
					+", idCampoValorOpcion: null"
					+", CodEntBus: "+codigoEntidadBusqueda);
			Value data = new Value(campo, campoValor, campoValorOpcion, null, codigoEntidadBusqueda);
			this.values.add(data);
			// Agregamos los valores adicionales asociados
			if(this.elements.size() > 0){
				List<CampoGUI> campos = this.elements.get(index);
				for(CampoGUI campoGUI: campos){
					for(Value adicionalData: campoGUI.values()){
						adicionalData.setCodigoEntidadBusqueda(codigoEntidadBusqueda);
						this.values.add(adicionalData);
					}
				}
				index++;
			}
		}

		return this.values;
	}

	@Override
	public boolean isDirty(){
		if(!super.isDirty()){
			// Recorremos los elementos de la lista verificando si alguno fue modificado
			if(this.elements.size() > 0){
				for(List<CampoGUI> campos: this.elements){
					for(CampoGUI campo: campos){
						if(campo.isDirty()){
							return true;
						}
					}
				}
			}
		}

		return super.isDirty();
	}


	/**
	 * Elimina un item de la lista
	 */
	private void removeItem(View btnRemove){
		// Se busca el indice del componente en la lista
		int index = this.listBtnRemove.indexOf(btnRemove);

		// Con el indice se elimina el elemento en la misma posicion en la lista de elementos
		this.codigos.remove(index);
		this.elements.remove(index);
		this.listBtnRemove.remove(index);

		// Con el indice se elimina el view del componente de la tabla de visualizacion
		this.elementsLayout.removeViewAt(index);

		evalCondicionalSoloLectura();
	}
	
	@Override
	public boolean validate() {
		if(isObligatorio() && this.elements.size() == 0){
			txtBusqueda.setError(context.getString(R.string.field_required_lb, getEtiqueta()));
			txtBusqueda.requestFocus();
			return false;
		}
		
		txtBusqueda.setError(null);
		return true;
	}

	/**
	 * Agrega un item a la lista. Para esto crean los nuevos comopentes que forman el elemento a partir de los CampoValor.
	 */
	private void addItem(List<Value> initialValues) {
		// Se separan los valores iniciales por campo 
		Map<Integer,Value> mapInitialValues = new HashMap<>();
		if(initialValues != null){
			for (Value value : initialValues) {
				if(Utils.isNotNull(value.getCampoValor())){
					mapInitialValues.put(value.getCampoValor().getId(), value);
				}
			}
		}

		// Creamos nuevo elemento
		FilaTablaBusqueda filaSeleccionada = mapBusquedaByCod.get(this.codigoEntidadBusqueda);
		// Sea crea la etiqueta para visualizar el elemento de busqueda cargado
		final String labelValue = filaSeleccionada.getDescripcion();
		// Creamos la lista de Campos que lo forman
		List<CampoGUI> elementFields = new ArrayList<CampoGUI>();
		for(Component co: this.components){
			CampoGUI template = (CampoGUI) co;
			AplPerfilSeccionCampoValor perfilSeccionCampoValor = (AplPerfilSeccionCampoValor) template.getEntity(); 

			CampoValor campoValor = perfilSeccionCampoValor.getCampoValor();

			Tratamiento tratamiento = Tratamiento.getByCod(campoValor.getTratamiento());
			if(tratamiento.equals(Tratamiento.DESCONOCIDO) && campoValor.getTratamientoDefault() != null){
				tratamiento = Tratamiento.getByCod(campoValor.getTratamientoDefault());
			}

			CampoGUI campo;
			switch (tratamiento) {
			case TA: // Alfanumerico 
				campo = new TextGUI(context, Teclado.ALFANUMERICO, false, enabled);
				break;
			case TAM: // Alfanumerico Multilinea
				campo = new TextGUI(context,Teclado.ALFANUMERICO, true, enabled);
				break;
			case TNE: // Entero
				campo = new TextGUI(context,Teclado.NUMERICO, false, enabled);
				break;
			case TND: // Decimal
				campo = new TextGUI(context,Teclado.DECIMAL, false, enabled, 0);
				break;
			case TN2: // Numerico Extendido
				campo = new TextGUI(context,Teclado.NUMERICO_EXTENDIDO, false, enabled, 0);
				break;
			case TF:  // Fecha
				campo = new DateGUI(context, enabled);
				break;
			case TT:  // Hora
				campo = new TimeGUI(context, enabled);
				break;
			case OP:  // Opciones simple seleccion (Combo)
				campo = new InnerComboGUI(context, enabled); 
				break;
			default:
				// Tratamiento no implementado
				campo = new CampoGUI(context);
				break;
			}
			campo.setEntity(perfilSeccionCampoValor);
			campo.setEtiqueta(campoValor.getEtiqueta());
			campo.setObligatorio(campoValor.isObligatorio());
			campo.setValorDefault(campoValor.getValorDefault());
			
			campo.setTratamiento(Tratamiento.getByCod(campoValor.getTratamiento()));
			if(campo.getTratamiento().equals(Tratamiento.DESCONOCIDO) && campoValor.getTratamientoDefault() != null){
				campo.setTratamiento(Tratamiento.getByCod(campoValor.getTratamientoDefault()));
			}
			
			campo.setTablaBusqueda(campo.getTablaBusqueda());
			campo.setComponents(template.getComponents());
			// Carga de valor inicial (precargado)
			Value initialValue = mapInitialValues.get(perfilSeccionCampoValor.getId());
			if(initialValue != null){
				List<Value> initialFieldValues = new ArrayList<Value>();
				initialFieldValues.add(initialValue);
				campo.setInitialValues(initialFieldValues);
			}
			campo.build();

			elementFields.add(campo);
		}

		// Label con descripcion de item seleccionado
		TextView itemLabel = new TextView(context);
		itemLabel.setTextColor(context.getResources().getColor(R.color.label_text_color));
		itemLabel.setText(labelValue);
		itemLabel.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		// Se crea el boton para eliminar el item
		ImageButton btnRemove = new ImageButton(context);
		btnRemove.setBackgroundResource(R.drawable.ic_menu_remove);
		btnRemove.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		btnRemove.setEnabled(enabled);
		btnRemove.setOnClickListener(v -> {
			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());

			builder.setTitle(R.string.confirm_title);
            builder.setMessage(v.getContext().getString(R.string.delete_item_confirm_msg, labelValue));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.yes, (dialog, id) -> {
                // Elimina el item
                removeItem(v);
                dirty = true;
            });
            builder.setNegativeButton(R.string.no, null);
			builder.create().show();
		});

		// Agregamos nuevo elemento a lista
		this.codigos.add(this.codigoEntidadBusqueda);
		this.elements.add(elementFields);
		// Se agrega el boton de eliminacion asociado al nuevo elemento a una lista (esto permite obtener el indice del elemento en la lista)
		this.listBtnRemove.add(btnRemove);

		// Separador de campos
		View line = new View(context);
		line.setBackgroundColor(context.getResources().getColor(R.color.line_background_color));

		// Layout Contenedor del Item
		RelativeLayout itemLayout = new RelativeLayout(context);
		itemLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		itemLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		itemLayout.addView(line, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

		btnRemove.setId(1);
		itemLabel.setId(2);
		RelativeLayout.LayoutParams lpBtnRem = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lpBtnRem.addRule(RelativeLayout.CENTER_VERTICAL);
		itemLayout.addView(btnRemove, lpBtnRem);

		RelativeLayout.LayoutParams lpLabel = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lpLabel.addRule(RelativeLayout.RIGHT_OF, btnRemove.getId());
		itemLayout.addView(itemLabel, lpLabel);
		
		int i = itemLabel.getId();
		for(CampoGUI campo: elementFields){
			
			RelativeLayout.LayoutParams lpcpo = new RelativeLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			lpcpo.addRule(RelativeLayout.BELOW, i++);
			lpcpo.addRule(RelativeLayout.RIGHT_OF, btnRemove.getId());
			View view = campo.getView();
			view.setId(i);
			itemLayout.addView(view, lpcpo);
		}
		
		this.elementsLayout.addView(itemLayout);

		// Al agregar un nuevo elemento se inicializa la busqueda
		this.txtBusqueda.setText("");
		this.codigoEntidadBusqueda = null;

		evalCondicionalSoloLectura();
	}

	/**
	 * Adapter para busqueda
	 */
	private static class SearchAdapter extends BaseAdapter implements Filterable {
		protected Context context;
		protected List<FilaTablaBusqueda> tablaBusqueda;
		private BUFilter filter;
		protected List<FilaTablaBusqueda> orig;

		public SearchAdapter(Context context, List<FilaTablaBusqueda> tablaBusqueda) {
			this.context = context;
			this.tablaBusqueda = tablaBusqueda;
			orig = tablaBusqueda;
			filter = new BUFilter();
		}

		@Override
		public int getCount() {
			if (tablaBusqueda != null) {
                return tablaBusqueda.size();
            }
			else {
                return 0;
            }
		}

		@Override
		public Object getItem(int arg0) {
			return tablaBusqueda.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return tablaBusqueda.get(arg0).getId();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView txt= new TextView(context);
			String text = tablaBusqueda.get(position).getDescripcion();
			txt.setText(text);

			return txt;
		}

		@Override
		public Filter getFilter() {
			return filter;
		}

		private class BUFilter extends Filter {

			@Override
			protected FilterResults performFiltering(CharSequence cs) {
				FilterResults oReturn = new FilterResults();
				List<FilaTablaBusqueda> results = new ArrayList<FilaTablaBusqueda>();
				if (orig == null)
					orig = tablaBusqueda;

				if (cs != null)	{
					if (orig != null && orig.size() > 0) {
						for (FilaTablaBusqueda f : orig) {
							String descUpper = f.getDescripcion().toUpperCase();
							String strUpper  = String.valueOf(cs).toUpperCase();
							if (descUpper.contains(strUpper)) 
								results.add(f);
						}
					}
					oReturn.values = results;
				}
				return oReturn;
			}

			@SuppressWarnings("unchecked")
			@Override
			protected void publishResults(CharSequence constraint, FilterResults results) {
				tablaBusqueda = (List<FilaTablaBusqueda>)results.values;
				notifyDataSetChanged();
			}
		}
	}

	/**
	 * Classe Inner creada para soportar el tratamiento ComboGUI en dispositivo 
	 * Samsumg Galaxy Tab, ya que este no soporta demasiados layouts anidados :(
	 * 
	 * @author tecso.coop
	 *
	 */
	private class InnerComboGUI extends ComboGUI {

		private final String TAG = getClass().getSimpleName();

		// Constructs

		public InnerComboGUI(Context context, boolean enabled) {
			super(context, enabled);
		}

		@Override
		public View build() {
			// Etiqueta
			this.label = new TextView(context);
			this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
			this.label.setText(this.getEtiqueta()+": ");

			// Layout Principal
			final ViewGroup mainLayout;

            // Se define un LinearLayout para ubicar: 'Label / EditText'
            mainLayout = new LinearLayout(context);
            ((LinearLayout) mainLayout).setOrientation(LinearLayout.VERTICAL);

			mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));

			final List<CampoGUI> items = new ArrayList<CampoGUI>();
			for(Component co: this.components){
				CampoGUI campoGUI = (CampoGUI) co;
				campoGUI.removeAllViewsForMainLayout();
				items.add(campoGUI);
			}

			// Adapter de Opciones
			final ArrayAdapter<CampoGUI> adapter;
			adapter = new ArrayAdapter<CampoGUI>(context, android.R.layout.simple_spinner_item, items) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					TextView view = new TextView(context);
					view.setTextColor(context.getResources().getColor(R.color.label_text_color));
					view.setText(this.getItem(position).getEtiqueta());
					return view;
				}
			};
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			this.cmbValores = new Spinner(context);
			this.cmbValores.setEnabled(enabled);
			this.cmbValores.setAdapter(adapter);
			this.cmbValores.setPrompt(this.getEtiqueta());
			this.cmbValores.setOnItemSelectedListener(new OnItemSelectedListener() {
				private CampoGUI itemPrevio = null;
				@Override
				public void onItemSelected(AdapterView<?> parent, View v, int pos, long row) {
					if (itemPrevio != null) {					
						mainLayout.removeView(itemPrevio.getEditViewForCombo());
					}

					CampoGUI itemActual = adapter.getItem(pos);
					if (itemActual != null && itemActual.getEditViewForCombo() != null) {
                        mainLayout.addView(itemActual.getEditViewForCombo());
						itemPrevio = itemActual;
					}
					dirty = true;
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0) { }
			});

			// Se setea el valor precargado o valor por defecto en caso que exista
			int idOpcion = 0;
			boolean isInitialValue = false;
			if(this.getInitialValues() != null && this.getInitialValues().size() == 1){
				Value value = this.getInitialValues().get(0);
				if(this.entity instanceof AplPerfilSeccionCampo){
					idOpcion = value.getCampoValor().getId();
				}else if(this.entity instanceof AplPerfilSeccionCampoValor){
					idOpcion = value.getCampoValorOpcion().getId();
				}
				isInitialValue = true;
			}else if(!TextUtils.isEmpty(this.getValorDefault())){
				try{
					idOpcion = Integer.valueOf(this.getValorDefault());
				}catch (Exception e) {
                    Log.d(TAG, "build(): el valor por defecto debe ser númerico: "+this.getValorDefault(), e);
				}
			}
			if(idOpcion > 0){
				for(Component co: this.components){
					CampoGUI campoGUI = (CampoGUI) co;
					if(campoGUI.getEntity().getId()==idOpcion){
						int pos = adapter.getPosition(campoGUI);
						this.cmbValores.setSelection(pos, true); // el 2do parametro en true fuerza el ItemSelectedListener
						if(isInitialValue) this.dirty = false;
					}
				}
			}

			// Se agregan los componentes a la fila
            //Add views to LinearLayout
            mainLayout.addView(this.label);
            mainLayout.addView(this.cmbValores);

			this.view = mainLayout;

			return this.view;
		}
	}

	@Override
	public void clearData() {
		for(int index=0; index<this.codigos.size(); index++) {
			this.codigos.remove(index);
			this.elements.remove(index);
			this.listBtnRemove.remove(index);
	
			// Con el indice se elimina el view del componente de la tabla de visualizacion
			this.elementsLayout.removeViewAt(index);
		}
		this.dirty = false;
		this.txtBusqueda.setError(null);
	}
	
	@Override
	public void setFocus() {
		this.txtBusqueda.requestFocusFromTouch();
	}

}