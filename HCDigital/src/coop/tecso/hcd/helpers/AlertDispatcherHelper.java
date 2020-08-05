package coop.tecso.hcd.helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import coop.tecso.hcd.activities.TabHostActivity;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.AccionHC;
import coop.tecso.hcd.entities.CondicionAlertaHC;
import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.perfiles.CampoValor;

public final class AlertDispatcherHelper {

	private Map<String,List<Value>> mapValues;
	private HCDigitalApplication appState;
	private String tabId;

	public AlertDispatcherHelper(PerfilGUI perfilGUI) {
		Activity activity =  (Activity) perfilGUI.getContext();
		this.appState = (HCDigitalApplication) activity.getApplicationContext();

		TabHostActivity tabHostActivity = (TabHostActivity) activity.getParent();
		this.tabId = tabHostActivity.getTabHost().getCurrentTabTag();

		// Mapa con valores iniciales
		this.mapValues = Utils.fillInitialValuesMaps(perfilGUI.values());
	}
	
	public List<AccionHC> obtenerAcciones(int idPerfil){
		return appState.getNotificationManager().obtenerAcciones(idPerfil);
	}
	
	public List<CondicionAlertaHC> obtenerCondicionesAlertas(int idPerfil){
		return appState.getNotificationManager().obtenerCondicionesAlertas(idPerfil);
	}

	public List<CondicionAlertaHC> obtenerCondicionesAlertas(int idPerfil, String codAlerta){
		return appState.getNotificationManager().obtenerCondicionesAlertas(idPerfil, codAlerta);
	}

	public boolean addMedicalNotification(String code){
		return appState.getNotificationManager().addMedicalNotification(tabId, code);
	} 

	public void addErrorNotification(String code, boolean esGrave){
		appState.getNotificationManager().addErrorNotification(tabId, code, esGrave);
	}

	public void clearNofiticationArea() {
		appState.getNotificationManager().clearNotificationArea(tabId);
	}

	public int getCampoID(CampoGUI campoGUI){
		String key = formatIniValueKey(campoGUI);
		this.mapValues.put(key, campoGUI.values());

		AplPerfilSeccionCampo campo = null;
		AplPerfilSeccionCampoValor campoValor = null;
		AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;

		if(campoGUI.getEntity() instanceof AplPerfilSeccionCampo){
			campo = (AplPerfilSeccionCampo) campoGUI.getEntity();
		}else if(campoGUI.getEntity() instanceof AplPerfilSeccionCampoValor){
			campoValor = (AplPerfilSeccionCampoValor) campoGUI.getEntity();
			campo = campoValor.getAplPerfilSeccionCampo();
		}else{
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) campoGUI.getEntity();
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}

		return campo.getId();
	}

	public Object getFormatedValue(int campoID){
		return this.getFormatedValue(campoID, 0);
	}

	public Object getFormatedValue(int campoID, int campoValorID){
		//TODO: Falta implementar
		//	LD("LD"),  // Lista de Campos Dinamica 

		String key = campoID +"|"+campoValorID+"|0";

		List<Value> values = mapValues.get(key);
		if(null==values||values.isEmpty()) return null;

		Value firstValue = values.get(0);
		AplPerfilSeccionCampo perfilSeccionCampo = firstValue.getCampo();
		Campo campo = perfilSeccionCampo.getCampo();

		Tratamiento tratamiento = Tratamiento.getByCod(campo.getTratamiento());
		
		if(tratamiento.equals(Tratamiento.DESCONOCIDO) && campo.getTratamientoDefault() != null){
			tratamiento = Tratamiento.getByCod(campo.getTratamientoDefault());
		}
		
		// Lista de Busqueda (Lista de entidades tomadas de tabla busqueda)
		if(tratamiento.equals(Tratamiento.LB)){
			Map<String, Boolean> mResult = new HashMap<String, Boolean>();
			for (Value value : values) {
				mResult.put(value.getCodigoEntidadBusqueda(), true);
			}
			return mResult;
		}
		if(tratamiento.equals(Tratamiento.LBD) || tratamiento.equals(Tratamiento.LBDIMPR)){
			Map<String, Boolean> mResult = new HashMap<String, Boolean>();
			for (Value value : values) {
				mResult.put(value.getCodigoEntidadBusqueda(), true);
			}
			return mResult;
		}		

		// Lista de opciones (CheckList)
		if(tratamiento.equals(Tratamiento.LO) || tratamiento.equals(Tratamiento.LOIMPR)){

			//			Map<Integer, Boolean> mResult = new HashMap<Integer, Boolean>();
//			for (Value value : values) {
//				mResult.put(value.getCampoValor().getId(), true);
//			}
//			return mResult;
			
			Value firstValueValor = values.get(0);
			AplPerfilSeccionCampoValor perfilSeccionCampoValor = firstValueValor.getCampoValor();
			CampoValor campoValor = perfilSeccionCampoValor.getCampoValor();

			Tratamiento tratamientoValor = Tratamiento.getByCod(campoValor.getTratamiento());

			if(tratamientoValor.equals(Tratamiento.DESCONOCIDO) && campoValor.getTratamientoDefault() != null){
				tratamientoValor = Tratamiento.getByCod(campoValor.getTratamientoDefault());
			}

			if(tratamientoValor.equals(Tratamiento.TNE)){
				return this.formatValue(tratamientoValor, firstValueValor);
			}
			
			Map<Integer, Boolean> mResult = new HashMap<Integer, Boolean>();
			for (Value value : values) {
				mResult.put(value.getCampoValor().getId(), true);
			}
			return mResult;
		}

		// Lista de Campos Estatica
		if(tratamiento.equals(Tratamiento.LC) || tratamiento.equals(Tratamiento.LD)){
			for (Value value : values) {
				if(value.getCampoValor().getId() == campoValorID){
					if(null == value.getCampoValorOpcion() || value.getCampoValorOpcion().getId() == 0){
						return this.formatValue(tratamiento, value);
					}else{
						return value.getCampoValorOpcion().getId();
					}
				}
			}
		}

		// Correponde a un Campo Simple
		return this.formatValue(tratamiento, firstValue);
	}


	/**
	 * Retorna un Object formateado dependiendo del Tratamiento.
	 * Todos l
	 */
	private Object formatValue(Tratamiento tratamiento, Value value){
		// Teclado Alfanumerico
		// Teclado Alfanumerico Multilinea
		// Tiempo
		if(tratamiento.equals(Tratamiento.TA) 
				|| tratamiento.equals(Tratamiento.TAM) 
				|| tratamiento.equals(Tratamiento.TT)){

			return value.getValor();
		} 

		// Numerico Entero
		if(tratamiento.equals(Tratamiento.TNE)){
			return Integer.parseInt(value.getValor());
		}
		// Numerico Decimal
		if(tratamiento.equals(Tratamiento.TND)){
			return Double.parseDouble(value.getValor());
		}
		// Fecha
		if(tratamiento.equals(Tratamiento.TF)){
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); 
			try { return sdf.parse(value.getValor());
			} catch (ParseException e) {return null;} 
		}
		// Opciones simple seleccion (Combo)
		if(tratamiento.equals(Tratamiento.OP)){
			if(Utils.isNotNull(value.getCampoValorOpcion())){
				return value.getCampoValorOpcion().getId();
			}else{
				return value.getCampoValor().getId();
			}
		} 
		// Busqueda en Tabla (EntidadBusqueda) (Opcion Simple)
		if(tratamiento.equals(Tratamiento.BU)){
			return value.getCodigoEntidadBusqueda();
		}
				
		return null;
	}

	/**
	 * Actualiza el mapa con todos los valores iniciales del perfil indicado
	 */
	public void refreshAllValues(PerfilGUI perfilGUI){
		// Mapa con valores iniciales
		this.mapValues = Utils.fillInitialValuesMaps(perfilGUI.values());		
	}

	private String formatIniValueKey(CampoGUI campoGUI){
		String key;
		
		int idCampo = 0;
		int idCampoValor = 0;
		int idCampoValorOpcion = 0;

		AplPerfilSeccionCampo campo = null;
		AplPerfilSeccionCampoValor campoValor = null;
		AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;
		if(campoGUI.getEntity() instanceof AplPerfilSeccionCampo){
			campo = (AplPerfilSeccionCampo) campoGUI.getEntity();
		}else if(campoGUI.getEntity() instanceof AplPerfilSeccionCampoValor){
			campoValor = (AplPerfilSeccionCampoValor) campoGUI.getEntity();
			campo = campoValor.getAplPerfilSeccionCampo();
		}else if(campoGUI.getEntity() instanceof AplPerfilSeccionCampoValorOpcion){
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) campoGUI.getEntity();
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}

		if(Utils.isNotNull(campo))
			idCampo = campo.getId();
		if(Utils.isNotNull(campoValor))
			idCampoValor = campoValor.getId();
		if(Utils.isNotNull(campoValorOpcion))
			idCampoValorOpcion = campoValorOpcion.getId();

		key = idCampo+"|"+idCampoValor+"|"+idCampoValorOpcion;

		return key;
	}
}