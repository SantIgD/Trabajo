package coop.tecso.hcd.gui.components;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.helpers.FilaTablaBusqueda;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

public final class DataSearchGUI extends CampoGUI {

	private AutoCompleteTextView txtBusqueda;
	private LinearLayout mainLayout;
	private LinearLayout searchLayout; 
	private EditText valorSelected;
	private ImageButton btnSearch;
	private Map<Long,FilaTablaBusqueda> mapBusquedaById;
	private String codigoEntidadBusqueda = null; 
	
	// MARK: - Constructor
	
	public DataSearchGUI(Context context, boolean enabled) {
		super(context, enabled);
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
        this.mainLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

		this.mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.mainLayout.setGravity(Gravity.CENTER_VERTICAL);
		
		// Layout de busqueda
		this.searchLayout = new LinearLayout(context);
		this.searchLayout.setOrientation(LinearLayout.HORIZONTAL);
		this.searchLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		
		// Texto para Busqueda
		this.txtBusqueda = new AutoCompleteTextView(context);
		this.txtBusqueda.setHint("Buscar");
		this.txtBusqueda.setEnabled(enabled);
		this.txtBusqueda.setFocusable(enabled);
		Map<String, FilaTablaBusqueda> mapBusquedaByCod = new HashMap<>();
		this.mapBusquedaById = new HashMap<>();
		if (this.tablaBusqueda != null) {
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
			valorSelected.setText(value);
			valorSelected.setVisibility(View.VISIBLE);
			btnSearch.setVisibility(View.VISIBLE);
			txtBusqueda.setText("");
			txtBusqueda.setVisibility(View.GONE);

			evalCondicionalSoloLectura();
		});
		// Valor seleccionado
		this.valorSelected = new EditText(context);
		this.valorSelected.setEnabled(false);
		this.valorSelected.setFocusable(false);
		this.valorSelected.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.valorSelected.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.valorSelected.setGravity(Gravity.CENTER_VERTICAL);
		this.valorSelected.setEnabled(enabled);
		this.valorSelected.setVisibility(View.GONE);
		this.valorSelected.setOnClickListener(v -> iniSearch());
		// Boton de busqueda
		this.btnSearch = new ImageButton(context);
		this.btnSearch.setBackgroundResource(R.drawable.ic_menu_search);
		this.btnSearch.setEnabled(enabled);
		this.btnSearch.setVisibility(View.GONE);
		this.btnSearch.setOnClickListener(v -> {
			iniSearch();

			evalCondicionalSoloLectura();
		});
		// Se setea el valor precargado o valor por defecto en caso que exista
		FilaTablaBusqueda filaSeleccionada = null;
		List<Value> initialValues = this.getInitialValues();
		if(!CollectionUtils.isEmpty(initialValues)){
			this.codigoEntidadBusqueda = initialValues.get(0).getCodigoEntidadBusqueda();
			if(!TextUtils.isEmpty(this.codigoEntidadBusqueda))
				filaSeleccionada = mapBusquedaByCod.get(this.codigoEntidadBusqueda);
		}else if(!TextUtils.isEmpty(this.getValorDefault())){
			this.codigoEntidadBusqueda = this.getValorDefault();
			if(!TextUtils.isEmpty(this.codigoEntidadBusqueda))
				filaSeleccionada = mapBusquedaByCod.get(this.codigoEntidadBusqueda);
			if(filaSeleccionada != null)
				this.dirty = true;
		}
		if(filaSeleccionada != null){
			String value = filaSeleccionada.getDescripcion();
			this.valorSelected.setText(value);
			this.valorSelected.setVisibility(View.VISIBLE);
			this.btnSearch.setVisibility(View.VISIBLE);
			this.txtBusqueda.setText("");
			this.txtBusqueda.setVisibility(View.GONE);
		}
		this.searchLayout.addView(this.valorSelected);
		this.searchLayout.addView(this.btnSearch, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.searchLayout.addView(this.txtBusqueda, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		this.mainLayout.addView(this.label);
		this.mainLayout.addView(this.searchLayout);
		
		this.view = mainLayout;
		
		return this.view;
	}

	/**
	 * Cambia el modo observacion a búsqueda.
	 */
	public void iniSearch(){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(R.string.confirm_title);
        builder.setMessage(context.getString(R.string.delete_item_confirm_msg, valorSelected.getText()));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            valorSelected.setText("");
            valorSelected.setVisibility(View.GONE);
            btnSearch.setVisibility(View.GONE);
            txtBusqueda.setText("");//value);
            txtBusqueda.setVisibility(View.VISIBLE);
            codigoEntidadBusqueda = null;
            dirty = true;
        });
        builder.setNegativeButton(R.string.no, null);
		builder.create().show();
	}
	
	@Override
	public View redraw() {
		this.txtBusqueda.setEnabled(enabled);
		this.txtBusqueda.setFocusable(enabled);
		this.txtBusqueda.setFocusableInTouchMode(enabled);
		this.valorSelected.setEnabled(enabled);
		this.btnSearch.setEnabled(enabled);
		return this.view;
	}
	
	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();

		if(!TextUtils.isEmpty(this.codigoEntidadBusqueda)){
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
			
			Log.d(DataSearchGUI.class.getSimpleName(),"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
					+" idCampo: "+(campo!=null?campo.getId():"null")
					+", idCampoValor: "+(campoValor!=null?campoValor.getId():"null")
					+", idCampoValorOpcion: "+(campoValorOpcion!=null?campoValorOpcion.getId():"null")
					+", CodEntBus: "+this.codigoEntidadBusqueda);
			
			Value data = new Value(campo,campoValor,campoValorOpcion,null, this.codigoEntidadBusqueda);
			this.values.add(data);
		}
		
		return this.values;
	}

	@Override
	public View getEditViewForCombo(){
		return this.searchLayout;
	}
	
	@Override
	public void removeAllViewsForMainLayout(){
		this.mainLayout.removeAllViews();
	}
	
	@Override
	public void setFocus() {
		this.searchLayout.requestFocusFromTouch();
	}
	
	/**
	 * Adapter para busqueda
	 */
    private class SearchAdapter extends BaseAdapter implements Filterable {

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
            return tablaBusqueda == null ? 0 : tablaBusqueda.size();
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
        	TextView textView = new TextView(context);
        	String text = tablaBusqueda.get(position).getDescripcion();
        	textView.setText(text);
        	
        	return textView;
        }
     
        @Override
        public Filter getFilter() {
            return filter;
        }    
       
        private class BUFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
            	FilterResults oReturn = new FilterResults();
            	List<FilaTablaBusqueda> results = new ArrayList<>();

            	if (orig == null) {
            		orig = tablaBusqueda;
            	}

            	if (charSequence != null)	{
            		if (orig != null && orig.size() > 0) {
            			for (FilaTablaBusqueda f : orig) {
            				String descUpper = f.getDescripcion().toUpperCase();
            				String strUpper  = String.valueOf(charSequence).toUpperCase();
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

}
