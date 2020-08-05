package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.gui.helpers.FilaTablaBusqueda;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.EvalEvent;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfilSeccion;
import coop.tecso.udaa.domain.base.AbstractEntity;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.Seccion;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("WeakerAccess")
public class CampoGUI extends Component {

	protected final String TAG = getClass().getSimpleName();
	
	protected PerfilGUI perfilGUI;
	protected Tratamiento tratamiento;
	protected String etiqueta;
	protected String valorDefault = "";
	protected boolean soloLectura;
	protected boolean obligatorio;
	protected List<AbstractEntity> options;
	protected List<FilaTablaBusqueda> tablaBusqueda;
	protected List<Value> values;
	protected TextView label;
	protected List<Value> initialValues;

	public boolean isLabel() {
		return bLabel;
	}

	public void setLabel(boolean bLabel) {
		this.bLabel = bLabel;
	}

	private boolean bLabel = true;
	
	protected int idCampo;
	protected boolean condicional;
	protected int campoCondicional;
	protected String condicion;
	protected String valorCondicional;
	protected int caracteres;
	private boolean campoValido = true;
	protected int orden;
	protected int minCaracteres;
	protected Integer minNumeros;
	protected Integer maxNumeros;

	protected HCDigitalApplication appState;

	private String entidadBusqueda;

	private Map<String, List<FilaTablaBusqueda>> mapEntBus;

		
	// Constructores
	public CampoGUI(Context context) {
		super();
		this.context = context;
		this.appState = (HCDigitalApplication) context.getApplicationContext();

		this.setCondCampoValorIDs(new ArrayList<>());
	}

	public CampoGUI(Context context, boolean enabled) {
		super();
		this.context = context;
		this.enabled = enabled;
		this.appState = (HCDigitalApplication) context.getApplicationContext();
		
		this.setCondCampoValorIDs(new ArrayList<>());
	}

	public CampoGUI(Context context, List<Value> values) {
		super();
		this.context = context;
		this.values = values;
		this.appState = (HCDigitalApplication) context.getApplicationContext();

		this.setCondCampoValorIDs(new ArrayList<>());
	}

	// Getters y Setters
	public PerfilGUI getPerfilGUI() {
		return perfilGUI;
	}
	public void setPerfilGUI(PerfilGUI perfilGUI) {
		this.perfilGUI = perfilGUI;
	}

	public int getIdCampo() {
		return idCampo;
	}
	public void setIdCampo(int campoId) {
		this.idCampo = campoId;
	}
	
	public Tratamiento getTratamiento() {
		return tratamiento;
	}
	public void setTratamiento(Tratamiento tratamiento) {
		this.tratamiento = tratamiento;
	}

	public String getEtiqueta() {
		return etiqueta;
	}
	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
	}

	public boolean isSoloLectura() {
		return soloLectura;
	}
	public void setSoloLectura(boolean soloLectura) {
		this.soloLectura = soloLectura;
	}

	public int getCampoCondicional() {
		return campoCondicional;
	}

	public void setCampoCondicional(int campoCondicional) {
		this.campoCondicional = campoCondicional;
	}

	public String getCondicion() {
		return condicion;
	}

	public void setCondicion(String condicion) {
		this.condicion = condicion;
	}

	public String getValorCondicional() {
		return valorCondicional;
	}

	public void setValorCondicional(String valorCondicional) {
		this.valorCondicional = valorCondicional;
	}
	
	public int getCaracteres() {
		return caracteres;
	}

	public void setCaracteres(int caracteres) {
		this.caracteres = caracteres;
	}
	
	public int getMinCaracteres() {
		return minCaracteres;
	}

	public void setMinCaracteres(int minCaracteres) {
		this.minCaracteres = minCaracteres;
	}
	
	public Integer getMinNumeros() {
		return minNumeros;
	}

	public void setMinNumeros(Integer minNumeros) {
		this.minNumeros = minNumeros;
	}

	public Integer getMaxNumeros() {
		return maxNumeros;
	}

	public void setMaxNumeros(Integer maxNumeros) {
		this.maxNumeros = maxNumeros;
	}

	public boolean isObligatorio() {
		if(obligatorio && isCondicional())
		{                                                                                                                                                                                                                                                                                                                                       			
			Value valor = this.getPerfilGUI().getValorForCampoID(this.getCampoCondicional()); 
			return evalCondicion(valor);
		}
		return obligatorio;
	}

	public void setObligatorio(boolean obligatorio) {
		this.obligatorio = obligatorio;
	}

	private int tipoTeclado;

	public int getTipoTeclado() {
		return tipoTeclado;
	}

	public void setTipoTeclado(int tipoTeclado) {
		this.tipoTeclado = tipoTeclado;
	}

	public static final String COND_IGUAL = "=";
    public static final String COND_DISTINTO = "<>";
	
	public boolean evalCondicion(Value valor) {
		return this.evalCondicion(valor, this.getCondicion(), this.getValorCondicional(), this.getCampoCondicional());
	}
	
	public boolean evalCondicion(Value valor, String condicion, String valorCondicional, int campoCondicional) {
		if(valor == null) {
			if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) {
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else{
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}	
		switch (Tratamiento.getByCod(valor.getTratamiento())) {
			case PIC:  	// Adjunto Imagen - AttacherGUI
			case FIR:  	// Firma Digital - SignGUI			
				return condicionalIndividualImagen(valor, condicion, valorCondicional, campoCondicional); 
			case TA: 	// Alfanumerico - TextGUI
			case TAM: 	// Alfanumerico Multilinea - TextGUI
			case TNE: 	// Entero - TextGUI
			case TND: 	// Decimal - TextGUI
			case TN2: 	// Numerico Extendido - TextGUI
			case TF:  	// Fecha - DateGUI
			case TT:  	// Hora - TimeGUI
			case LNK: 	// Link - LinkGUI
			case NAV: 	// Google Navigation - NavGUI
			case PAD: 	// Consulta en Padron de Inhibidos - PadronGUI
			case CBU: 	// CBU - CbuGUI
			case DOM:  	// Domicilio - DomicilioGUI		
				return condicionalIndividual(valor, condicion, valorCondicional, campoCondicional);
			case OP2:  	// Opcion simple extendida ComboExtGUI		
				return condicionalIndividualOpciones(valor, condicion, valorCondicional, campoCondicional);
			case OP:  	// Opciones simple seleccion (Combo) - ComboGUI
			case LO:  	// Lista de Opciones - CheckListGUI
			case LOIMPR: // Lista de Opciones - CheckListGUI Impresion
			case LC:  	// Lista de Campos Estatica - ListGUI
			case LD:  	// Lista de Campos Dinamica - DynamicListGUI
			case SO:  	// Secciones Opcionales (SectionsCheckList) - SectionsCheckListGUI
				return condicionalMultiple(valor, condicion, valorCondicional, campoCondicional);
			case LB: 	// Lista de Busqueda - SearchListsGUI
			case LBD: 	// Lista de Busqueda  Directa - SearchListsDirectGUI
			case LBDIMPR:
				return condicionalBusqueda(valor, condicion, valorCondicional, campoCondicional);
			case BU:  	// Busqueda en Tabla (EntidadBusqueda) - DataSearchGUI
				return condicionalBusquedaTabla(valor, condicion, valorCondicional, campoCondicional);
			case NA: 	// Etiqueta - LabelGUI
			case LABEL:
			default:
				return false; 				
		}
	}
		
	public boolean condicionalIndividual(Value valor, String condicion, String valorCondicional, int campoCondicional) {			
		if(valor.getValor() == null || TextUtils.isEmpty(valor.getValor())) {
			if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) {
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else{
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}		
		if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) { 
			if(valor.getValor() == null || TextUtils.isEmpty(valor.getValor().toString())){
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else {
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}
		switch (Tratamiento.getByCod(valor.getTratamiento())) {
			case TNE: 	// Entero - TextGUI
				int valCondInt = Integer.parseInt(valorCondicional);
				int valInt = Integer.parseInt(valor.getValor());
				return (valCondInt == valInt && TextUtils.equals(condicion, COND_IGUAL))
						|| (valCondInt != valInt && TextUtils.equals(condicion, COND_DISTINTO));
			case TND: 	// Decimal - TextGUI
				double valCondDec = Double.parseDouble(valorCondicional);
				double valDec = Double.parseDouble(valor.getValor());
				return (valCondDec == valDec && TextUtils.equals(condicion, COND_IGUAL))
						|| (valCondDec != valDec && TextUtils.equals(condicion, COND_DISTINTO));
			case TF:  	// Fecha - DateGUI
				try {
					SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
					Date valCond = sdfDate.parse(valorCondicional);
					Date val = sdfDate.parse(valor.getValor());
					return (valCond.equals(val) && TextUtils.equals(condicion, COND_IGUAL))
							|| (!valCond.equals(val) && TextUtils.equals(condicion, COND_DISTINTO));
				} catch (ParseException e) {
					Log.d(TAG, "condicionalIndividual(): no se puede validar el campo Fecha - CampoID: " + campoCondicional, e);
					return false;
				}
			case TT: 	// Hora - TimeGUI
				try {
					SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
					Date valCond = sdfTime.parse(valorCondicional);
					Date val = sdfTime.parse(valor.getValor());
					return (valCond.equals(val) && TextUtils.equals(condicion, COND_IGUAL))
							|| (!valCond.equals(val) && TextUtils.equals(condicion, COND_DISTINTO));
				} catch (ParseException e) {
					Log.d(TAG, "condicionalIndividual(): no se puede validar el campo Hora - CampoID: " + campoCondicional, e);
					return false;
				}
			case DOM:  	// Domicilio - DomicilioGUI
				return (valor.getValor().toUpperCase().contains("|" + valorCondicional.toUpperCase() + "|") && TextUtils.equals(condicion, COND_IGUAL))
						|| (!valor.getValor().toUpperCase().contains("|" + valorCondicional.toUpperCase() + "|") && TextUtils.equals(condicion, COND_DISTINTO));
			default:
				return (TextUtils.equals(valor.getValor().toUpperCase(), valorCondicional.toUpperCase()) && TextUtils.equals(condicion, COND_IGUAL))
						|| (!TextUtils.equals(valor.getValor().toUpperCase(), valorCondicional.toUpperCase()) && TextUtils.equals(condicion, COND_DISTINTO));
		}
	}

	public boolean condicionalIndividualOpciones(Value valor, String condicion, String valorCondicional, int campoCondicional) {	
		if(valor.getValor() == null || TextUtils.isEmpty(valor.getValor())) {
			if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) {
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else{
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}		
		if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) { 
			if(valor.getValor() == null || TextUtils.isEmpty(valor.getValor()) || TextUtils.equals(valor.getValor(), "-1")){
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else {
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}

		return (TextUtils.equals(valor.getValor().toUpperCase(), valorCondicional.toUpperCase()) && TextUtils.equals(condicion, COND_IGUAL))
				|| (!TextUtils.equals(valor.getValor().toUpperCase(), valorCondicional.toUpperCase()) && TextUtils.equals(condicion, COND_DISTINTO));
	}
	
	public boolean condicionalIndividualImagen(Value valor, String condicion, String valorCondicional, int campoCondicional) {	
		if(valor.getValor() == null || TextUtils.isEmpty(valor.getValor())) {
			if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) {
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else{
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}
		if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)){ 
			if((valor.getImagen() == null || valor.getImagen().length == 0) &&
				(valor.getValor() == null || TextUtils.isEmpty(valor.getValor()))){
				return TextUtils.equals(condicion, COND_IGUAL);				
			}
			else {
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}
		
		return false;
	}
	
	public boolean condicionalMultiple(Value valor, String condicion, String valorCondicional, int campoCondicional) {	
		List<Value> valores = this.getPerfilGUI().getValoresForCampoID(campoCondicional);
		if(valores == null) {
			if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) {
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else{
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}
		if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) { 
			if(valores.size() == 0 || valores.get(0).getValor().equals("-1")){
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else {
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}

		for(Value val: valores){
			if(val.getCampoValor() != null) {
				if(val.getCampoValor().getId() == Integer.parseInt(valorCondicional)) {
					return TextUtils.equals(condicion, COND_IGUAL);
				}
			}
		}
		return TextUtils.equals(condicion, COND_DISTINTO);
	}

	public boolean condicionalBusqueda(Value valor, String condicion, String valorCondicional, int campoCondicional) {	
		List<Value> valores = this.getPerfilGUI().getValoresForCampoID(campoCondicional);
		if(valores == null) {
			return false;
		}
		if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) { 
			if(valores.size() == 0){
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else {
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}

		for(Value val: valores){
			for(String valCondicional : valorCondicional.split(",")) {
				if(val.getCodigoEntidadBusqueda() != null) {
					if((TextUtils.equals(val.getCodigoEntidadBusqueda().toUpperCase(), valCondicional.toUpperCase()) && TextUtils.equals(condicion, COND_IGUAL))
						|| (!TextUtils.equals(val.getCodigoEntidadBusqueda().toUpperCase(), valCondicional.toUpperCase()) && TextUtils.equals(condicion, COND_DISTINTO))) {
						return true;
					}
				}
				else if(val.getCampoValor() != null && val.getCampoValor().getId() == Integer.parseInt(valCondicional)) {
					if(val.getValor() == null || TextUtils.isEmpty(val.getValor())){
						return TextUtils.equals(condicion, COND_DISTINTO);
					}
					else {
						return TextUtils.equals(condicion, COND_IGUAL);						
					}
				}
			}
		}
		return false;
	}

	public boolean condicionalBusquedaTabla(Value valor, String condicion, String valorCondicional, int campoCondicional) {		
		if(valor.getCodigoEntidadBusqueda() == null) {
			if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) {
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else{
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}		
		if(valorCondicional == null || TextUtils.isEmpty(valorCondicional)) { 
			if(valor.getValor() == null || TextUtils.isEmpty(valor.getValor())){
				return TextUtils.equals(condicion, COND_IGUAL);
			}
			else {
				return TextUtils.equals(condicion, COND_DISTINTO);
			}
		}

		String valorCodigoEntidadBusqueda = valor.getCodigoEntidadBusqueda().toUpperCase();
		return (TextUtils.equals(valorCodigoEntidadBusqueda, valorCondicional.toUpperCase()) && TextUtils.equals(condicion, COND_IGUAL))
				|| (!TextUtils.equals(valorCodigoEntidadBusqueda, valorCondicional.toUpperCase()) && TextUtils.equals(condicion, COND_DISTINTO));
	}
	
	public String getValorDefault() {
		return valorDefault;
	}
	public void setValorDefault(String valorDefault) {
		this.valorDefault = valorDefault;
	}

	public List<FilaTablaBusqueda> getTablaBusqueda() {
		return tablaBusqueda;
	}
    public void setTablaBusqueda(List<FilaTablaBusqueda> tablaBusqueda) {
	    this.tablaBusqueda = tablaBusqueda;
    }

	public String getEntidadBusqueda() {
		return entidadBusqueda;
	}
	public void setEntidadBusqueda(String entidadBusqueda) {
		this.entidadBusqueda = entidadBusqueda;
    }

	public Map<String, List<FilaTablaBusqueda>> getMapEntBus() {
		return mapEntBus;
	}
	public void setMapEntBus(Map<String, List<FilaTablaBusqueda>> mapEntBus) {
		this.mapEntBus = mapEntBus;
	}

	public List<AbstractEntity> getOptions() {
		return options;
	}
	public void setOptions(List<AbstractEntity> options) {
		this.options = options;
	}

	public List<Value> getInitialValues() {
		return initialValues;
	}
	public void setInitialValues(List<Value> initialValues) {
		this.initialValues = initialValues;
	}

	public TextView getLabel() {
		return label;
	}

	public boolean isCondicional() {
		return condicional;
	}
	public void setCondicional(boolean condicional) {
		this.condicional = condicional;
	}
	
	// Metodos
	@Override
	public void addValue(Value value) {
		this.values.add(value);
	}

	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();
		return this.values;
	}

	public List<Value> dirtyValues() {
		if(this.isDirty())
			return this.values();
		else
			return new ArrayList<>();
	}

	@Override
	public View build() {
		// View generico para campos de tratamientos no implementados
		TableRow layout = new TableRow(context);
		layout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		layout.setGravity(Gravity.CENTER_VERTICAL);

		TextView label = new TextView(context);
		label.setTextColor(context.getResources().getColor(R.color.label_text_color));//this.DEFAULT_LABEL_TEXT_COLOR);
		label.setText(this.getEtiqueta()+": ");
		label.setGravity(Gravity.END);

		TextView msg = new TextView(context);
		msg.setTextColor(context.getResources().getColor(R.color.label_text_color));//this.DEFAULT_LABEL_TEXT_COLOR);
		msg.setText("Tratamiento '"+this.getTratamiento()+"' no implementado.");
		msg.setGravity(Gravity.CENTER);

		layout.addView(label);
		layout.addView(msg);

		this.view = layout; 
		return this.view;
	}

	@Override
	public View redraw() {
		return this.view;
	}

	@NonNull
	@Override
	public String toString(){
		return this.getEtiqueta();
	}

	public View getEditViewForCombo(){
		return null;
	}

	public void removeAllViewsForMainLayout(){}
	
	public void customonItemSelected(){}

	/**
	 * Setea un mensaje de error para el campo
	 * @param message Mensaje a configurar. Si es nulo se borrará el error
	 */
	public void setError(String message) {}

	/**
	 * Metodo encargado de evaluar alertas asociadas al campo.
	 */
	public void evalAlert(EvalEvent event){
		if(null != this.perfilGUI)
			this.perfilGUI.getAlertDispatcher().eval(this, event);
	}

	public boolean isCampoValido() {
		return campoValido;
	}
	public void setCampoValido(boolean campoValido) {
		this.campoValido = campoValido;
	}

	public int getOrden() {
		return orden;
	}
	public void setOrden(int orden) {
		this.orden = orden;
	}

	public void evalCondicionalSoloLectura() {
		for(int campoID: this.getCondCampoValorIDs()){
			Value valorCondicional = this.getPerfilGUI().getValorForCampoID(this.getIdCampo());
			Component compValor = this.getPerfilGUI().getComponentForCampoID(campoID);
			AplPerfilSeccionCampo campoDest = (AplPerfilSeccionCampo)compValor.getEntity();

			if(campoDest.isSoloLectura()) {	
				this.evalCondicionalSoloLectura(valorCondicional, campoDest, compValor);
			}			
		}
	}
	
	public void evalCondicionalSoloLectura(int idCampoCondicional, int idCampoDestino) {
		Value valorCondicional = this.getPerfilGUI().getValorForCampoID(idCampoCondicional);
		Component compValor = this.getPerfilGUI().getComponentForCampoID(idCampoDestino);
		AplPerfilSeccionCampo campoDest = (AplPerfilSeccionCampo)compValor.getEntity();

		if(campoDest.isSoloLectura()) {		
			this.evalCondicionalSoloLectura(valorCondicional, campoDest, compValor);
		}			
		
	}
	
	public void evalCondicionalSoloLectura(Value valorCondicional, AplPerfilSeccionCampo campoDest, Component compValor) {

		boolean condicion = this.evalCondicion(valorCondicional, 
												campoDest.getCondicion(), 
												campoDest.getValorCondicional(), 
												campoDest.getCampoCondicional());
		
		if(campoDest.isSoloLectura()) {
			
			if((condicion && !campoDest.isObligatorio()) || 
			   (!condicion && campoDest.isObligatorio())) {
				compValor.disable();
				compValor.clearData();
			}
			else {			
				compValor.enable();
			}
		}			
	}
	
	public void setFocus() throws Exception {
		throw new Exception("SetFocus not implemented idCampo: " + this.getIdCampo());
	}



	// MARK: - Entidad de búsqueda condicional

	private Object[] parsedEntidadBusquedaCondicional;

	public void setEntity(AbstractEntity entity) {
		this.entity = entity;
		this.updateParsedEntidadBusquedaCondicional();
	}

	public AplPerfilSeccionCampo getSeccionCampo() {
		AplPerfilSeccionCampo campo = null;
		AplPerfilSeccionCampoValor campoValor;
		AplPerfilSeccionCampoValorOpcion campoValorOpcion;

		if (this.entity instanceof AplPerfilSeccionCampo) {
			campo = (AplPerfilSeccionCampo) entity;
		} else if (this.entity instanceof AplPerfilSeccionCampoValor) {
			campoValor = (AplPerfilSeccionCampoValor) entity;
			campo = campoValor.getAplPerfilSeccionCampo();
		} else if (this.entity instanceof AplPerfilSeccionCampoValorOpcion) {
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) entity;
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}

		return campo;
	}

	public boolean shouldApplyEntidadBusquedaCondicional(Value value) {
		if (parsedEntidadBusquedaCondicional == null) {
			return false;
		}

		Integer arg1 = (Integer) parsedEntidadBusquedaCondicional[0];
		String arg2 = (String)parsedEntidadBusquedaCondicional[1];
		String comparator = (String)parsedEntidadBusquedaCondicional[2];

		boolean isApplyable = value.getCampo().getId() == arg1;
		if (!isApplyable) { return false; }

        boolean areEqual = TextUtils.equals(value.getValor(), arg2);
		boolean isEquality = TextUtils.equals(comparator, CampoGUI.COND_IGUAL);
		if (TextUtils.equals(comparator, CampoGUI.COND_DISTINTO)) {
			isEquality = false;
		}

		return  (isEquality && areEqual) || (!isEquality && !areEqual);
	}

	public boolean shouldApplyEntidadBusquedaCondicional(CampoGUI campoGUI, String value) {
		if (parsedEntidadBusquedaCondicional == null) {
			return false;
		}

		Integer arg1 = (Integer) parsedEntidadBusquedaCondicional[0];
		String arg2 = (String)parsedEntidadBusquedaCondicional[1];
		String comparator = (String)parsedEntidadBusquedaCondicional[2];

		boolean isApplyable = campoGUI.getSeccionCampo().getId() == arg1;
		if (!isApplyable) { return false; }

        boolean areEqual = TextUtils.equals(value, arg2);
		boolean isEquality = TextUtils.equals(comparator, CampoGUI.COND_IGUAL);
		if (TextUtils.equals(comparator, CampoGUI.COND_DISTINTO)) {
			isEquality = false;
		}

        Log.i("PEDROSO", "isEquality " + isEquality + " | equals " + areEqual + " | arg *" + arg2 + "*" + " | val *" + value + "*");
        return (isEquality && areEqual) || (!isEquality && !areEqual);
	}

	/**
	 * Retorna el dato que viene con la entidadBusquedaCondicional
	 * (La última parte)
	 */
	public String getEntidadBusquedaCondVal() {
		if (parsedEntidadBusquedaCondicional == null) {
			return null;
		}

		return (String) parsedEntidadBusquedaCondicional[3];
	}

	public boolean entidadBusquedaCondConcerns(CampoGUI campoGUI) {
        if (parsedEntidadBusquedaCondicional == null) {
            return false;
        }

        Integer arg1 = (Integer) parsedEntidadBusquedaCondicional[0];
        return campoGUI.getSeccionCampo().getId() == arg1;
    }

	private void updateParsedEntidadBusquedaCondicional() {
		AplPerfilSeccionCampo campo = getSeccionCampo();
		this.parsedEntidadBusquedaCondicional = parseEntidadBusquedaCondicional(campo);
	}

	private Object[] parseEntidadBusquedaCondicional(AplPerfilSeccionCampo campo ) {
		String entidadBusquedaCondicional = campo.getEntidadBusquedaCondicional();
		if (entidadBusquedaCondicional == null) {
			return null;
		}

		String[] components = entidadBusquedaCondicional.split("\\|");
		if (components.length < 4) {
			return null;
		}

		Integer arg1 = Helper.parseInt(components[0]);
		String arg2 = components[2];
		String comparator = components[1];
		String data = components[3];

		if (arg1 == null || comparator.isEmpty()) {
			return null;
		}

		return new Object[] { arg1, arg2, comparator, data };
	}

	public SeccionGUI getSeccionGUI(){
		AplPerfilSeccionCampo aplPerfilSeccionCampo = (AplPerfilSeccionCampo)this.getEntity();
		Seccion seccion = aplPerfilSeccionCampo.getAplPerfilSeccion().getSeccion();
		int idSeccion = seccion.getId();
		List<Component> components = appState.getForm().getComponents();

		for (Component component: components ) {
			if(component instanceof SeccionGUI){
				SeccionGUI seccionGUI = (SeccionGUI)component;
				AplicacionPerfilSeccion perfilSeccion = (AplicacionPerfilSeccion)seccionGUI.getEntity();
				if(perfilSeccion.getSeccion().getId() == idSeccion){
					return seccionGUI;
				}
			}
		}

		return null;
	}

}