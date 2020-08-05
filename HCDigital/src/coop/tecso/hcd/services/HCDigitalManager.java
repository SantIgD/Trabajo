package coop.tecso.hcd.services;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.FirmaActivity;
import coop.tecso.hcd.activities.RegisterActivity;
import coop.tecso.hcd.activities.TabHostActivity;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.AtencionCerrada;
import coop.tecso.hcd.entities.AtencionValor;
import coop.tecso.hcd.entities.EstadoAtencion;
import coop.tecso.hcd.entities.MotivoCierreAtencion;
import coop.tecso.hcd.gui.GUIVisitor;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.components.SeccionGUI;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.hcd.utils.PrePrintUtil;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

@SuppressLint("SimpleDateFormat")
public final class HCDigitalManager {

	private static final String TAG = HCDigitalManager.class.getSimpleName();

	private Context context;
	private UDAACoreServiceImpl udaaService;
	private HCDigitalApplication appState;
	private boolean ignorarValidacion;
	private boolean firma = false;
	private boolean isSaving = false;

	public HCDigitalManager(Context context) {
		this.context = context;
		this.udaaService = new UDAACoreServiceImpl(context);
		this.appState = (HCDigitalApplication) context.getApplicationContext();
	}

	public Atencion getAtencionForRegistrarIM(Integer atencionId) throws Exception{
		return getAtencionForRegistrarIM(atencionId, new HashMap<>());
	}

	/**
	 * JIRA - HCDDM-156: HCDigital (app.) - Modificación a IMD múltiples
	 */
	public Atencion getAtencionForRegistrarIM(Integer atencionId, Map<Integer,String> mapValues) throws Exception{
		Log.i(TAG, "getAtencionForRegistrarIM: enter");

		// Si se informa el idAtencion buscar en db local
		if(atencionId != 0){
			Atencion atencion;
			try {
				atencion = appState.getHCDigitalDAO().getAtencionById(atencionId);	
			} catch (Exception e) {
				Log.e(TAG, "error getAtencionForRegistrarIM: " + e.getStackTrace());
				return null;
			}
			
			Log.i(TAG, "Buscando perfil");
			AplicacionPerfil aplicacionPerfil = udaaService.getAplicacionPerfilById(atencion.getAplicacionPerfil().getId());
			Log.i(TAG, "Buscando perfil OK");
			atencion.setAplicacionPerfil(aplicacionPerfil);
			Log.i(TAG, "getAtencionForRegistrarIM: exit with atencionId="+atencion.getId());
			System.gc();
			return atencion;
		}

		// Creamos la nueva atencion (IM)
		Atencion atencion = new Atencion();
		// Perfil por defecto
		int aplicacionPerfilId = udaaService.getIdAplicacionPerfilDefaultBy(Constants.COD_HCDIGITAL);
		AplicacionPerfil aplicacionPerfil = udaaService.getAplicacionPerfilById(aplicacionPerfilId);
		atencion.setAplicacionPerfil(aplicacionPerfil);

		// EstadoAtencion: EN_PREPARACION
		EstadoAtencion estadoAtencion = appState.getHCDigitalDAO().getEstadoAtencionById(EstadoAtencion.ID_EN_PREPARACION); 
		Log.d(TAG, "getAtencionForRegistrarIM: DEBUG 0: estadoAtencion:"+estadoAtencion);
		
		//Generamos ACRA LOG 
		try {
			StringBuilder mensaje = new StringBuilder("REVISION ESTADO ATENCION - AtencionDMID: " + atencion.getAtencionDMID() +
                    "  - Estado Atencion anterior: " + (atencion.getEstadoAtencion() != null ? atencion.getEstadoAtencion().getDescripcion() : "No contiene estado atencion") +
                    "  - Estado Atencion nuevo: " + (estadoAtencion != null ? estadoAtencion.getDescripcion() : "No contiene estado atencion"));
				   
				   StackTraceElement[] elements = Thread.currentThread().getStackTrace();
				   int numberOfLinesToPrint = elements.length;
				   if(elements.length > 15) {
					   numberOfLinesToPrint = 15;
				   }
				   for (int i = 1; i < numberOfLinesToPrint; i++) {
				        StackTraceElement s = elements[i];
				        mensaje.append("\tat ");
                        mensaje.append(s.getClassName());
                        mensaje.append(".");
                        mensaje.append(s.getMethodName());
                        mensaje.append("(");
                        mensaje.append(s.getFileName());
                        mensaje.append(":");
                        mensaje.append(s.getLineNumber());
mensaje.append(")");
				   }
			generateACRA_LOG(mensaje.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
				
		atencion.setEstadoAtencion(estadoAtencion);
		// Setear el id del dispositivo movil 
		atencion.setIdUsuarioApmAsignado(appState.getCurrentUser().getId());
		atencion.setDispositivoMovilID(appState.getDispositivoMovil().getId());
		atencion.setFechaDescarga(new Date());
		
		List<AtencionValor> listAtencionValor = new ArrayList<>();
		// Valores a copiar del IM principal
		for (Integer key : mapValues.keySet()) {
			AtencionValor atencionValor = new AtencionValor();
			AplPerfilSeccionCampo aplPerfilSeccionCampo = udaaService.getAplPerfilSeccionCampoById(key);
			atencionValor.setAplPerfilSeccionCampo(aplPerfilSeccionCampo);
			atencionValor.setValor(mapValues.get(key)+" ");//No borrar espacio, necesario para marcar dirty = true
			
			listAtencionValor.add(atencionValor);
		}
		atencion.setAtencionValores(listAtencionValor);

		Log.i(TAG, "getAtencionForRegistrarIM: exit");
		System.gc();
		return atencion;
	}

	public PerfilGUI buildIM(Atencion atencion) {
		Log.i(TAG, "buildPerfil: enter");

		List<Value> listValue = new ArrayList<>();
		
		Collection<AtencionValor> cav = atencion.getAtencionValores();
		if(cav.size()>0){
			for (AtencionValor atencionValor : cav) {
				Log.i(TAG, "AtencionValor:" + atencionValor.getValor() );
			}
		}
		
		for (AtencionValor atencionValor : atencion.getAtencionValores()) {
			Log.d(TAG, "Atencion valor " + atencionValor.getValor());
			if (atencionValor.getAplPerfilSeccionCampo() != null) {
				Log.d(TAG, "Campo valor " + atencionValor.getAplPerfilSeccionCampo().getId());
			}
			Value value = new Value(atencionValor.getAplPerfilSeccionCampo(),
					atencionValor.getAplPerfilSeccionCampoValor(),
					atencionValor.getAplPerfilSeccionCampoValorOpcion(),
					atencionValor.getValor(),					 
					atencionValor.getCodigoEntidadBusqueda(),
					atencionValor.getImagen()
					);
			value.setThumbnail(atencionValor.getThumbnail());
			value.setExtraFile(atencionValor.getExtraFile());
			listValue.add(value);
		}
		boolean enabled = atencion.enPreparacion();
		Log.d(TAG, "buildPerfil: DEBUG 1: atencion: "+atencion);
		Log.d(TAG, "buildPerfil: DEBUG 2: atencionId: "+atencion.getId());
		Log.d(TAG, "buildPerfil: DEBUG 3: estadoAtencion: "+atencion.getEstadoAtencion());
		Log.d(TAG, "buildPerfil: DEBUG 4: estadoAtencionId: "+atencion.getEstadoAtencion().getId());
		if(atencion.getAplicacionPerfil() != null){
			Log.d(TAG, "buildPerfil: DEBUG 5: aplicacionPerfilId: "+atencion.getAplicacionPerfil().getId());
		}
		Log.d(TAG, "buildPerfil: DEBUG 6: enabled:" +enabled);
		
		GUIVisitor visitor = new GUIVisitor(context);
		PerfilGUI form = (PerfilGUI) visitor.buildComponents(atencion.getAplicacionPerfil(), appState.getListEntidadBusqueda(), listValue, enabled);
		form.setBussEntity(atencion);
		// Se actualiza la visualizacion de secciones opcionales segun campos con tratamiento 'SO'
		form.actualizarSeccionesOpcionales();
		form.actualizarSoloLecturaCondicional();
		Log.i(TAG, "buildPerfil: exit");
		return form;
	}

	/**
	 *  Guarda el IM persistiendo los datos en la db local.
	 */
	public Atencion saveIM(PerfilGUI form, String tabId) throws Exception{
		while (isSaving) {}
		
		//Seteamos bandera de que se esta guardando
		isSaving = true;
		
		Log.i(TAG, "saveIM: enter");
		List<Value> listValue = form.values(); 
		Map<Integer, AplPerfilSeccionCampo> mapIdCampo = new HashMap<>();
		for(Value value: listValue){
			mapIdCampo.put(value.getCampo().getId(), value.getCampo());
		}

		Atencion atencion = (Atencion) form.getBussEntity();

		atencion.setListaCodigoAlertaMedica(appState.getNotificationManager().saveNotificationCodes(tabId));

		// Si la atencion es local (con perfil por defecto) se deben completar los campos claves de atencion
		
		// Recorremos los valores tomados y guardamos los datos claves para la atencion segun el idCampo indicado en el parametro
		String numeroAtencion = atencion.getNumeroAtencion();
		Date fechaAtencion = atencion.getFechaAtencion();
		String domicilioAtencion = atencion.getDomicilioAtencion();
		
		String paramNumerosID = ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_NUMERO, null);
		String paramFechasID = ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_FECHA, null);
		String paramDomiciliosID = ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_DOMICILIO, null); 

		List<Integer> listaIdNumero = new ArrayList<>();
		List<Integer> listaIdFecha = new ArrayList<>();
		List<Integer> listaIdDomicilio = new ArrayList<>();

		if (paramNumerosID != null) {
			String[] paramNumerosArray = paramNumerosID.split("&");
			for (String paramNumero : paramNumerosArray) {
				try {
					int paramNumeroCampo = Integer.parseInt(paramNumero);
					listaIdNumero.add(paramNumeroCampo);
				} catch (Exception ignore) {}
			}
		}

		if (paramDomiciliosID != null) {
			String[] paramDomiciliosArray = paramDomiciliosID.split("&");
			for (String paramDomicilio : paramDomiciliosArray) {
				try {
					int paramDomicilioCampo = Integer.parseInt(paramDomicilio);
					listaIdDomicilio.add(paramDomicilioCampo);
				} catch (Exception ignore) {}
			}
		}

		if (paramFechasID != null) {
			String[] paramFechasArray = paramFechasID.split("&");
			for (String paramFecha : paramFechasArray) {
				try {
					int paramFechaCampo = Integer.parseInt(paramFecha);
					listaIdFecha.add(paramFechaCampo);
				} catch (Exception ignore) {}
			}
		}

		for(Value value: listValue){
			for (Integer numeroId : listaIdNumero) {
				if (value.getCampo().getCampo() != null && value.getCampo().getCampo().getId() == numeroId){
					numeroAtencion = value.getValor();
				}
			}
			
			for (Integer fechaId : listaIdFecha) {
				if(value.getCampo().getCampo() != null 
						&& value.getCampo().getCampo().getId() == fechaId){
					String strFechaAtencion = value.getValor();
					SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); 
					try {
						fechaAtencion = sdf.parse(strFechaAtencion);
					} catch (ParseException e) {
						fechaAtencion = new Date();
						Log.d(TAG, "saveIM(): no se pudo parsear la fecha: "+strFechaAtencion, e);
					} 
				}
			}
			for (Integer domicilioId : listaIdDomicilio) {
				if(value.getCampo().getCampo() != null 
						&& value.getCampo().getCampo().getId() == domicilioId){
					domicilioAtencion = value.getValor();
				}
			}
			
		}
		
		atencion.setNumeroAtencion(numeroAtencion);
		atencion.setFechaAtencion(fechaAtencion);
		atencion.setDomicilioAtencion(domicilioAtencion);
		
		// Generacion de codigo de atencion
		String codigoAtencion = this.generarCodigoAtencion(atencion.getFechaAtencion(), atencion.getNumeroAtencion());
		if(codigoAtencion == null || codigoAtencion.equals("")){
			String codAtencionNulo =  "saveIM(): ParamHelper.ATENCION_NUMERO: " + ParamHelper.ATENCION_CAMPOS_NUMERO +
										" - idCampoNumero: " + paramNumerosID +
										" ParamHelper.ATENCION_FECHA: " + ParamHelper.ATENCION_CAMPOS_FECHA +
										" - idCampoFecha: " + paramFechasID +
										" ParamHelper.ATENCION_DOMICILIO: " + ParamHelper.ATENCION_CAMPOS_DOMICILIO +
										" - paramDomicilio: " + paramDomiciliosID +
										" - atencion.getFechaAtencion(): " + atencion.getFechaAtencion() +
										" - atencion.getNumeroAtencion(): " + atencion.getNumeroAtencion();
			generateACRA_LOG(codAtencionNulo);
		}
		atencion.setCodigoAtencion(codigoAtencion);
				
		List<String> gestKeys = new ArrayList<>();
		boolean newAtencion = false;
		if(atencion.getId() != 0) {
			// Update atencion
			appState.getHCDigitalDAO().updateAtencion(atencion);

			// Se eliminan los valores precargados que se modificaron
			for(AtencionValor atencionValor: atencion.getAtencionValores()){

				if(atencionValor.getImagen() == null || atencionValor.getImagen().length == 0) {
					gestKeys.add(atencionValor.getAplPerfilSeccionCampo().getId() + "|" + atencion.getId());
				}

				if(mapIdCampo.get(atencionValor.getAplPerfilSeccionCampo().getId()) != null){
					appState.getHCDigitalDAO().deleteAtencionValor(atencionValor);
				}
			}
		} else {
			newAtencion = true;
			// Create Atencion
			appState.getHCDigitalDAO().createAtencion(atencion);
		}
		
		Log.i(TAG, "Total atencionValor to create = "+listValue.size());
		List<AtencionValor> listAtencionValor = new ArrayList<>();
		
		// Guardar AtencionValor
		for(Value value: listValue){
			AtencionValor atencionValor = new AtencionValor();
			atencionValor.setAtencion(atencion);
			atencionValor.setAplPerfilSeccionCampo(value.getCampo());
			atencionValor.setAplPerfilSeccionCampoValor(value.getCampoValor());
			atencionValor.setAplPerfilSeccionCampoValorOpcion(value.getCampoValorOpcion());
			atencionValor.setValor(value.getValor());
			atencionValor.setImagen(value.getImagen());
			// URGMNT-213
			if(value.getImagen() != null) {
			    String gestKey = value.getCampo().getId() + "|" + atencion.getId();
                gestKeys.remove(gestKey);
			}
			// URGMNT-213
			atencionValor.setThumbnail(value.getThumbnail());
			atencionValor.setCodigoEntidadBusqueda(value.getCodigoEntidadBusqueda());
			atencionValor.setSyncronize(true);

			Log.i(TAG, "\tcreating atencionValor : "+atencionValor.getValor());
			listAtencionValor.add(appState.getHCDigitalDAO().createAtencionValor(atencionValor));
		
		
			String tratamiento = value.getCampo().getCampo().getTratamiento();
			if(tratamiento != null && Tratamiento.getByCod(tratamiento).equals(Tratamiento.DESCONOCIDO) && value.getCampo().getCampo().getTratamientoDefault() != null){
				tratamiento = value.getCampo().getCampo().getTratamientoDefault();
			}
			
			if (newAtencion && Tratamiento.FIR.getCod().equals(tratamiento)) {
				String key = value.getCampo().getId() + "|0";
				
				List<Gesture> gestures = appState.getGestureStore().getGestures(key);
				Gesture finalGesture = getFinalGesture(key, appState.getGestureStore(), gestures);
				String newKey = value.getCampo().getId() + "|" + atencion.getId();
				appState.getGestureStore().removeEntry(newKey);

				if(finalGesture != null) {
					//Se borra el gesture temporal
					String temporalKey = key + Constants.TEMPORAL_GESTURE_SUFFIX;

					appState.getGestureStore().removeEntry(key);
					appState.getGestureStore().removeEntry(temporalKey);
					
					//Se guarda el gesture definitivo
					appState.getGestureStore().addGesture(newKey,finalGesture);
				}
			} else {
				if(value.getImagen() != null && Tratamiento.FIR.getCod().equals(tratamiento)) {
					String key = value.getCampo().getId() + "|" + atencion.getId();
					String temporalKey = key + Constants.TEMPORAL_GESTURE_SUFFIX;
					if (value.getImagen().length == 0) {
						appState.getGestureStore().removeEntry(key);
						appState.getGestureStore().removeEntry(temporalKey);
						
					} else {
						List<Gesture> gestures = appState.getGestureStore().getGestures(key);
						Gesture finalGesture = getFinalGesture(key, appState.getGestureStore(), gestures);
						if(finalGesture != null) {
							//Se borra el gesture temporal
							appState.getGestureStore().removeEntry(key);
							appState.getGestureStore().removeEntry(temporalKey);
							
							//Se guarda el gesture definitivo
							String newKey = value.getCampo().getId() + "|" + atencion.getId();
							appState.getGestureStore().addGesture(newKey,finalGesture);
						}
						
					}
					
				}

			}
		
		}
		
		for(String keyToRemove : gestKeys) {
			appState.getGestureStore().removeEntry(keyToRemove);
		}

		atencion.setAtencionValores(listAtencionValor);
		appState.saveGestures();
		
		isSaving = false;
		
		return atencion;
	}

	//#################################  | buildDialogForCierreIM   | ########################################

	public Dialog buildDialogForCierreIM(final String tabTag, Atencion atencion, boolean anular, PerfilGUI form) {
		return buildDialogForCierreIM(tabTag, atencion, anular, false, form);
	}
	
	public Dialog buildDialogForCierreIM(final String tabTag, Atencion atencion, boolean anular, final boolean cierrreSinActo, final PerfilGUI form) {
		Log.i(TAG, "buildDialogForCierreIM: enter");

		// Si la atencion esta en estado en preparacion se debe generar el codigo de atencion
		if (atencion.enPreparacion()) {
			// Generar Codigo de Atencion
			Date fechaAtencion = atencion.getFechaAtencion();
			String numeroAtencion = atencion.getNumeroAtencion(); 
			
			String paramCampoNumero =  ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_NUMERO, null);
			List<Integer> listaIdNumero = new ArrayList<>();

			if (paramCampoNumero != null) {
				String[] paramNumerosArray = paramCampoNumero.split("&");
				for (String paramNumero : paramNumerosArray) {
					try {
						int paramNumeroCampo = Integer.parseInt(paramNumero);
						listaIdNumero.add(paramNumeroCampo);
					} catch (Exception ignore) {}
				}
			} 
			
			String paramCampoFecha =  ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_FECHA, null);
			List<Integer> listaIdFecha = new ArrayList<>();

			if (paramCampoFecha != null) {
				String[] paramFechasArray = paramCampoFecha.split("&");
				for (String paramFecha : paramFechasArray) {
					try {
						int paramFechaCampo = Integer.parseInt(paramFecha);
						listaIdFecha.add(paramFechaCampo);
					} catch (Exception ignore) {}
				}
			}
			
			// Si el IM es local se toman los datos desde los completados en el perfil
			try {
				if(atencion.getId() == 0){
					TabHostActivity tabHostActivity = (TabHostActivity) context;
					RegisterActivity currentActivity = (RegisterActivity) tabHostActivity.getLocalActivityManager().getActivity(tabTag);
					PerfilGUI perfil = currentActivity.getForm();
					List<Value> listValue = perfil.dirtyValues(); 
					// Recorremos los valores tomados y guardamos los datos claves para la atencion segun el idCampo indicado en el parametro
					for(Value value: listValue){ 
						for (Integer idCampoNumero :listaIdNumero){	
							if(idCampoNumero != null && value.getCampo().getCampo() != null &&
									value.getCampo().getCampo().getId() == idCampoNumero){
								numeroAtencion = value.getValor();
							}
						}
						for (Integer idCampoFecha :listaIdFecha){	
							if(idCampoFecha != null && value.getCampo().getCampo() != null &&
									value.getCampo().getCampo().getId() == idCampoFecha){
								String strFechaAtencion = value.getValor();
								SimpleDateFormat sdf= new SimpleDateFormat("dd/MM/yyyy"); 
								fechaAtencion = sdf.parse(strFechaAtencion);
							}
						}
					}
				}
			}catch (Exception e) {
				fechaAtencion = null;
				numeroAtencion = null;
				Log.d(TAG, "buildDialogForCierreIM(): Error al generar codigo de atencion.",e);
			}
			Log.d(TAG,"Datos para generar codigo, Fecha: "+fechaAtencion+" Nro: "+numeroAtencion);
			String codigoAtencion = this.generarCodigoAtencion(fechaAtencion, numeroAtencion); 
			if(codigoAtencion == null || codigoAtencion.equals("")){
				String codAtencionNulo =  "buildDialogForCierreIM(): ParamHelper.ATENCION_NUMERO: " + ParamHelper.ATENCION_CAMPOS_NUMERO +
											" - idCampoNumero: " + paramCampoNumero +
											" ParamHelper.ATENCION_FECHA: " + ParamHelper.ATENCION_CAMPOS_FECHA +
											" - idCampoFecha: " + paramCampoFecha ;
				generateACRA_LOG(codAtencionNulo);
			}
			atencion.setCodigoAtencion(codigoAtencion);
		}
		
		// Logged User
		UsuarioApm currentUser = appState.getCurrentUser();

		// List : MotivoCierreAtencion
		List<MotivoCierreAtencion> motivoCierreAtencionList = appState.getHCDigitalDAO().getListMotivoCierreAtencion(context);

		// List : EstadoAtencion
		List<EstadoAtencion> estadoAtencionList = new ArrayList<>();

		//Estado del IM
		final EstadoAtencion estadoAtencion =  atencion.getEstadoAtencion();
		if (!anular) {
			switch (estadoAtencion.getId()) {
			case EstadoAtencion.ID_EN_PREPARACION:
				// IM creado a partir de perfil
				estadoAtencionList.add(appState.getHCDigitalDAO().getEstadoAtencionById(EstadoAtencion.ID_CERRADA_PROVISORIA));
				estadoAtencionList.add(appState.getHCDigitalDAO().getEstadoAtencionById(EstadoAtencion.ID_CERRADA_DEFINITIVA));
				//estadoAtencionList.add(appState.getHcDigitalDAO().getEstadoAtencionById(EstadoAtencion.ID_ANULADA));
				break;
			case EstadoAtencion.ID_CERRADA_PROVISORIA:
				// IM cerrado provisorio
				estadoAtencionList.add(appState.getHCDigitalDAO().getEstadoAtencionById(EstadoAtencion.ID_CERRADA_DEFINITIVA));
				break;
			}
		} else{
			estadoAtencionList.add(appState.getHCDigitalDAO().getEstadoAtencionById(EstadoAtencion.ID_ANULADA));
		}

		final Dialog dialog = new Dialog(context);

		dialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		dialog.setContentView(R.layout.close_im_dialog);
		dialog.setTitle(context.getString(R.string.close_IM_title));
		dialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_lock_lock);
		dialog.setCancelable(true);

		// Codigo de Atencion
		TextView codigoAtencionView = dialog.findViewById(R.id.textViewCodigoAtencion);
		final String codigoAtencionText = context.getResources().getString(R.string.codigo_atencion)+atencion.getCodigoAtencion();
		codigoAtencionView.setText(codigoAtencionText);
		
		if(null != currentUser){
			//Campo01: Nombre
			TextView campo1 = dialog.findViewById(R.id.textViewCampo1);
			campo1.setText(currentUser.getNombre());
			//Campo02: Especialidad
			TextView campo2 = dialog.findViewById(R.id.textViewCampo2);
			campo2.setText(currentUser.getEspecialidad());
			//Campo03: Matricula 
			TextView campo3 = dialog.findViewById(R.id.textViewCampo3);
			campo3.setText(currentUser.getMatricula());
			
			// IdMedico:
			final EditText idMedico = dialog.findViewById(R.id.editTextIdMedico);
			idMedico.setText(currentUser.getUsername());
			idMedico.setOnFocusChangeListener((arg0, hasFocus) -> {
				if(!hasFocus && TextUtils.isEmpty(idMedico.getText()))
					idMedico.setError(context.getString(R.string.field_required, idMedico.getHint()));
			});
		}

		// Password: 
		final EditText password = dialog.findViewById(R.id.editTextPassword);
		password.setOnFocusChangeListener((arg0, hasFocus) -> {
			if(!hasFocus && TextUtils.isEmpty(password.getText()))
				password.setError(context.getString(R.string.field_required, password.getHint()));
		});
		// ObservacionCierreDefinitivo
		EditText obsCieDef = dialog.findViewById(R.id.editTextObsCieDef);
		obsCieDef.setText(atencion.getObservacionCierreDefinitivo());

		InputFilter maxLengthFilter = new InputFilter.LengthFilter(ParamHelper.getInteger(ParamHelper.OBS_CIERRE_DEF_MAX_LGH, 1000));
		obsCieDef.setFilters(new InputFilter[]{ maxLengthFilter});
		
		// Firma Digital
		byte[] imageBytes = atencion.getFirmaDigital();
		Resources res = context.getResources();
		int inset = (int) res.getDimension(R.dimen.gesture_thumbnail_inset);
		int size = (int) res.getDimension(R.dimen.gesture_thumbnail_size);
		if(null == imageBytes){
			appState.getGestureStore().removeEntry(tabTag);
			List<Gesture> gestures = appState.getGestureStore().getGestures(tabTag);
			if(null != gestures && !gestures.isEmpty()){
				firma = true;

				ImageView imageViewSign = dialog.findViewById(R.id.imageViewSign);
				imageViewSign.setImageBitmap(gestures.get(0).toBitmap(size, size, inset, Color.BLACK));
			} 
		} else {
			firma = true;
			try {
				ImageView imageViewSign = dialog.findViewById(R.id.imageViewSign);
				imageViewSign.setImageBitmap(Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length), size, size, true));
			} catch (Exception e) {
				Log.e(TAG," outOfMemory " + e.getStackTrace() );
			}
		}

		
		// sign button
		Button buttonSign = dialog.findViewById(R.id.buttonSign);
		buttonSign.setOnClickListener(v -> {
			firma = true;
			Intent intent = new Intent(v.getContext(), FirmaActivity.class);
			intent.putExtra(Constants.TAB_TAG, tabTag);
			intent.putExtra(Constants.CODIGO_ATENCION,codigoAtencionText);
			((Activity) context).startActivityForResult(intent, 1);
		});
		
		// print button
		Button printButton = dialog.findViewById(R.id.printButton);
		printButton.setOnClickListener(v -> {
            PrePrintUtil prePrintUtil = new PrePrintUtil(context);
            prePrintUtil.printIfPossible(context, () -> new PrintReportTask().execute(form));
		});

		//cancel button
		dialog.setCancelable(false);
		Button buttonCancel = dialog.findViewById(R.id.buttonCancel);
		buttonCancel.setOnClickListener(v -> {
			if(firma) {
				TabHostActivity currentActivity = (TabHostActivity) context;
				Toast.makeText(currentActivity,"El IMD ya no puede ser editado", Toast.LENGTH_SHORT).show();
			}
			else {
				dialog.dismiss();
			}
		});
		
		// Combo: MotivoCierreAtencion
		Spinner spinnerMotivoCierreAtencion;
		spinnerMotivoCierreAtencion = dialog.findViewById(R.id.spinnerMotivoCierreAtencion);
		//
		ArrayAdapter<MotivoCierreAtencion> adapterMotivo;
		adapterMotivo = new ArrayAdapter<MotivoCierreAtencion>(context, 
				android.R.layout.simple_spinner_item , motivoCierreAtencionList){
			@Override
			public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
				TextView view = (TextView) super.getDropDownView(position, convertView, parent);
				view.setText(this.getItem(position).getDescripcion());
				return view;
			}

			@NonNull
			@Override
			public View getView(int position, View convertView, @NonNull ViewGroup parent) {
				if (super.getView(position, convertView, parent) == null) {
					return new TextView(appState);
				}
				TextView label = (TextView) super.getView(position, convertView, parent);
				label.setText(this.getItem(position).getDescripcion());
				return label;
			}
		};
		adapterMotivo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerMotivoCierreAtencion.setAdapter(adapterMotivo);

		// Si ya se realizo el cierre provisorio se carga el motivo de cierre seleccionado en el combo
		if(estadoAtencion != null && estadoAtencion.getId() == EstadoAtencion.ID_CERRADA_PROVISORIA){
			MotivoCierreAtencion motivoCierre = null ;
			for(MotivoCierreAtencion motivo: motivoCierreAtencionList){
				if(motivo.getId() == atencion.getMotivoCierreAtencion().getId()){
					motivoCierre = motivo;
					break;
				}
			}
			int pos = adapterMotivo.getPosition(motivoCierre);
			spinnerMotivoCierreAtencion.setSelection(pos, true);
			firma = false;
		}
		else {
			Integer motCierre = ParamHelper.getInteger(ParamHelper.MOTIVO_CIERRE_ATENCION_DEFAULT, 1);
			if(cierrreSinActo) {
				motCierre = ParamHelper.getInteger(ParamHelper.MOTIVO_CIERRE_SIN_ACTO_MED, 1);
				spinnerMotivoCierreAtencion.setEnabled(false);				
			}
			int posicion = 0;
			for (MotivoCierreAtencion motivoCierre : motivoCierreAtencionList) {
				if(motivoCierre.getId() == motCierre) {
					break;
				}
				posicion++;
			}
			spinnerMotivoCierreAtencion.setSelection(posicion);
		}

		// Combo: EstadoAtencion
		Spinner spinnerEstadoAtencion = dialog.findViewById(R.id.spinnerEstadoAtencion);
		ArrayAdapter<EstadoAtencion> adapterEstadoAtencion;
		adapterEstadoAtencion = new ArrayAdapter<EstadoAtencion>(context, android.R.layout.simple_spinner_item, estadoAtencionList){
			@Override
			public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
				TextView view = (TextView) super.getDropDownView(position, convertView, parent);
				view.setText(this.getItem(position).getDescripcion());
				return view;
			}
			@NonNull
			@Override
			public View getView(int position, View convertView, @NonNull ViewGroup parent) {
				if(super.getView(position, convertView, parent) == null)
					return new TextView(appState);
				TextView label = (TextView) super.getView(position, convertView, parent);
				
				EstadoAtencion eaItem = this.getItem(position);
				
				if (eaItem != null) {
					label.setText(eaItem.getDescripcion());
				}
				
				return label;
			}
		};
		adapterEstadoAtencion.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerEstadoAtencion.setAdapter(adapterEstadoAtencion);
		
		// URGMNT-15 Tipo de cierre por defecto  
		String paramEstado = ParamHelper.getString(ParamHelper.COD_ESTADO_ATENCION, "ID_CERRADA_PROVISORIA");
		spinnerEstadoAtencion.setSelection(0);
		// Cata code
		try {
			int codParam,i= -1;
			if (paramEstado.equals("ID_EN_PREPARACION")){
				codParam=1;			
			} else if(paramEstado.equals("ID_CERRADA_PROVISORIA")){
				codParam=2;
			} else if (paramEstado.equals("ID_CERRADA_DEFINITIVA")){
				codParam=3;
			} else {
				codParam=4;
			}
			for (EstadoAtencion item : estadoAtencionList) {
				i++;
				if(item.getId() == codParam){
					spinnerEstadoAtencion.setSelection(i);
					break;
				}
			}
			// SI NO ENCUENTRA ITEM ESTADO ATENCION DEJAMOS EL PRIMERO
		} catch (Exception e) {
			spinnerEstadoAtencion.setSelection(0);
			e.printStackTrace();
		}
		//-------
		
		
		spinnerEstadoAtencion.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				EstadoAtencion estadoSelected = (EstadoAtencion) parentView.getItemAtPosition(position);
				//
				LinearLayout layoutSign = dialog.findViewById(R.id.layoutSign);
				TableRow tableRowMotivo = dialog.findViewById(R.id.tableRowMotivo);
				EditText editTextPassword = dialog.findViewById(R.id.editTextPassword);
				Button buttonSign = dialog.findViewById(R.id.buttonSign);
				
				switch (estadoSelected.getId()) {
				case EstadoAtencion.ID_ANULADA:
					layoutSign.setVisibility(View.GONE);
					tableRowMotivo.setVisibility(View.GONE);
					editTextPassword.setVisibility(View.VISIBLE);
					// jira HCDDM-197: Cierre de IMD sin firma
					ignorarValidacion = true;
					break;
				case EstadoAtencion.ID_CERRADA_PROVISORIA:
					if(cierrreSinActo) {
						layoutSign.setVisibility(View.GONE);	
					}
					else {
						layoutSign.setVisibility(View.VISIBLE);
					}
					tableRowMotivo.setVisibility(View.VISIBLE);
					editTextPassword.setVisibility(View.VISIBLE);
					break;
				case EstadoAtencion.ID_CERRADA_DEFINITIVA:
					if(estadoAtencion.getId() == EstadoAtencion.ID_CERRADA_PROVISORIA){
						buttonSign.setEnabled(false);
						Spinner spinnerMotivoCierreAtencion = dialog.findViewById(R.id.spinnerMotivoCierreAtencion);
						spinnerMotivoCierreAtencion.setEnabled(false);
						EditText obsCieDef = dialog.findViewById(R.id.editTextObsCieDef);
						obsCieDef.setEnabled(false);
						// jira HCDDM-197: Cierre de IMD sin firma
						ignorarValidacion = true;
					}

					if(cierrreSinActo) {
						layoutSign.setVisibility(View.GONE);	
					}
					else {
						layoutSign.setVisibility(View.VISIBLE);
					}
					tableRowMotivo.setVisibility(View.VISIBLE);
					editTextPassword.setVisibility(View.VISIBLE);
					break;
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parentView) {}
		});
		spinnerEstadoAtencion.setFocusableInTouchMode(true);
		spinnerEstadoAtencion.requestFocus();
		
		Button buttonConfirm = dialog.findViewById(R.id.buttonConfirm);
		buttonConfirm.setOnClickListener(v -> {
			TabHostActivity currentActivity = (TabHostActivity) context;
			boolean hasError = false;

			if(TextUtils.isEmpty(password.getText())){
				// No se completo la firma digital (Password)
				Toast.makeText(currentActivity,"El password es requerido", Toast.LENGTH_SHORT).show();
				return;
			}
			if(!ignorarValidacion){
				LinearLayout layoutSign = dialog.findViewById(R.id.layoutSign);
				if(layoutSign.getVisibility() == View.VISIBLE){
					Spinner spinnerMotivo = dialog.findViewById(R.id.spinnerMotivoCierreAtencion);
					MotivoCierreAtencion motivo = (MotivoCierreAtencion) spinnerMotivo.getSelectedItem();
					Integer motCierre = ParamHelper.getInteger(ParamHelper.MOTIVO_CIERRE_ATENCION, 1);
					if(motivo.getId() == motCierre){
						hasError = true;
						Toast.makeText(currentActivity,"El motivo es requerido", Toast.LENGTH_SHORT).show();
					}
					else if (motivo.getId() == MotivoCierreAtencion.ID_CON_ACTO_MEDICO){
						ImageView imageSign = dialog.findViewById(R.id.imageViewSign);
						// Firma
						imageSign.setDrawingCacheEnabled(true);
						if(imageSign.getDrawingCache() == null){
							hasError = true;
							//No firmaron
							Toast.makeText(currentActivity,"La firma del paciente es requerida", Toast.LENGTH_SHORT).show();
						}
					}
				}

			}

			if(!hasError){
				currentActivity.excuteCloseIM();
			}
		});

		Log.i(TAG, "buildDialogForCierreIM: exit");
		return dialog;
	}

    @SuppressLint("StaticFieldLeak")
    private class PrintReportTask extends AsyncTask<PerfilGUI, Float, String> {

	    private static final String PRINTER_SHARE_PKG = "com.dynamixsoftware.printershare";

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			this.dialog = ProgressDialog.show(context, "", context.getString(R.string.printing_msg), true);
		}

		@Override
		protected String doInBackground(PerfilGUI... form) {
			String result;
			
			try {
				Thread.sleep(1000);
				result = generateReport(form[0], false);
				
			} catch (Exception e) {
				Log.e(TAG, "error: print report", e);
				result = "Error al generar reporte PDF, contactese con el administrador";
			}

	        try {
	            if (this.dialog != null && this.dialog.isShowing()) {
		        	this.dialog.dismiss();
	            }
	        } catch (Exception ignore) {}
	        
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			boolean hasError = result.toUpperCase().contains("ERROR");

			if (hasError) {
				GUIHelper.showMessage(context, result);
				return;
			}

			if (this.isPrinterShareInstalled()) {
			    this.printWithPrintshare(result);
			} else {
			    this.printWithSystem(result);
			}
		}

		private Uri getFileUri(String path) {
            String autority = "com.fantommers.hc.fileprovider";
            return FileProvider.getUriForFile(appState, autority, new File(path));
        }

        private boolean isPrinterShareInstalled() {
		    return Utils.isAppInstalled(context, PRINTER_SHARE_PKG);
        }

        private void printWithPrintshare(String filePath) {
            Uri data = this.getFileUri(filePath);

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(data, "application/pdf");
            intent.setPackage(PRINTER_SHARE_PKG);

            context.startActivity(intent);
        }

        private void printWithSystem(String filePath) {
            Uri data = this.getFileUri(filePath);

            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, data);

            context.startActivity(intent);
        }
	}

	
	//#######################################################################################################################

	public View buildSeccionPostAtencion(final Atencion atencion){
		Log.i(TAG, "buildSeccionForObsPostAtencion: enter");
		SeccionGUI seccion = new SeccionGUI(context);
		seccion.setEtiqueta(context.getString(R.string.obs_post_atencion_tittle));

		// Build seccion
		View view = seccion.build();
		LinearLayout tLayout = seccion.getTableLayout();

		LinearLayout tRow = new TableRow(context);
		tRow.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		tRow.setGravity(Gravity.CENTER_VERTICAL);

		EditText textBox = new EditText(context);
		
		textBox.setGravity(Gravity.TOP|Gravity.START);
		textBox.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE|InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
		textBox.setLines(3);
		textBox.setText(atencion.getObservacionPostAtencion());
		// Watcher usado para setear la observacion
		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
			@Override
			public void afterTextChanged(Editable s) {
				atencion.setObservacionPostAtencion(s.toString());
			}
		};
		textBox.addTextChangedListener(textWatcher);
		// Limite en la cantidad de caracteres
		InputFilter maxLengthFilter = new InputFilter.LengthFilter(ParamHelper.getInteger(ParamHelper.OBS_POST_AT_MAX_LGH, 1000));
		textBox.setFilters(new InputFilter[]{ maxLengthFilter});

		if (EstadoAtencion.ID_CERRADA_DEFINITIVA == atencion.getEstadoAtencion().getId()){
			textBox.setEnabled(false);
			textBox.setFocusable(false);
		} else {
			textBox.requestFocus();
		}

		tRow.addView(new View(context));
		tRow.addView(textBox);

		tLayout.addView(tRow);
		Log.i(TAG, "buildSeccionForObsPostAtencion: exit");
		return view;
	}

	/**
	 *  Cierra el IM persistiendo los datos en la db local.
	 */
	public Atencion closeIM(PerfilGUI form, Dialog dialog, UsuarioApm usuarioCierre, String tabId) throws Exception {
		Log.i(TAG, "closeIM: enter");
		// EstadoAtencion 
		Spinner spinnerEstado = dialog.findViewById(R.id.spinnerEstadoAtencion);
		// Motivo
		Spinner spinnerMotivo = dialog.findViewById(R.id.spinnerMotivoCierreAtencion);
		// Observacion 
		EditText observacion = dialog.findViewById(R.id.editTextObsCieDef);
		
		int idEstado = EstadoAtencion.ID_EN_PREPARACION;
		
		// Atencion
		Atencion atencion = (Atencion) form.getBussEntity();		

		EstadoAtencion estadoAnterior = atencion.getEstadoAtencion();
		EstadoAtencion estadoSelected = (EstadoAtencion) spinnerEstado.getSelectedItem();
		atencion.setMotivoCierreAtencion((MotivoCierreAtencion) spinnerMotivo.getSelectedItem());

		atencion.setObservacionCierreDefinitivo(observacion.getText().toString());
		atencion.setEstadoAtencion(estadoSelected);

		//Generamos ACRA LOG 
		try {
			StringBuilder mensajeBuilder = new StringBuilder();
			mensajeBuilder.append("REVISION ESTADO ATENCION - AtencionDMID: ");
			mensajeBuilder.append(atencion.getAtencionDMID());
			mensajeBuilder.append("  - Estado Atencion anterior: ");
			mensajeBuilder.append(estadoAnterior != null ? estadoAnterior.getDescripcion() : "No contiene estado atencion");
			mensajeBuilder.append("  - Estado Atencion nuevo: ");
			mensajeBuilder.append(estadoSelected != null ? estadoSelected.getDescripcion() : "No contiene estado atencion");
				   
		   StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		   int numberOfLinesToPrint = elements.length;
		   if (elements.length > 15) {
			   numberOfLinesToPrint = 15;
		   }

		   for (int i = 1; i < numberOfLinesToPrint; i++) {
			 StackTraceElement s = elements[i];
			 mensajeBuilder.append("\tat ");
			 mensajeBuilder.append(s.getClassName());
			 mensajeBuilder.append(".");
			 mensajeBuilder.append(s.getMethodName());
			 mensajeBuilder.append("(");
			 mensajeBuilder.append(s.getFileName());
			 mensajeBuilder.append(":");
			 mensajeBuilder.append(s.getLineNumber());
			 mensajeBuilder.append(")");
		   }
		   generateACRA_LOG(mensajeBuilder.toString());
		} catch(Exception ignore) {}
		
		switch (estadoSelected.getId()) {
		case EstadoAtencion.ID_CERRADA_DEFINITIVA:
			atencion.setIdUsuarioCierreDefinitivo(usuarioCierre.getId());
			if(EstadoAtencion.ID_EN_PREPARACION == estadoAnterior.getId()){
				atencion.setIdUsuarioCierreProvisorio(usuarioCierre.getId());
			}
			atencion.setFechaCierreDefinitivo(new Date());
			idEstado = EstadoAtencion.ID_CERRADA_DEFINITIVA;
			break;
		case EstadoAtencion.ID_CERRADA_PROVISORIA:
			atencion.setIdUsuarioCierreProvisorio(usuarioCierre.getId());
			atencion.setFechaCierreProvisorio(new Date());
			break;
		case EstadoAtencion.ID_ANULADA:
			idEstado = EstadoAtencion.ID_ANULADA;
			atencion.setIdUsuarioCierreProvisorio(usuarioCierre.getId());
			atencion.setIdUsuarioCierreDefinitivo(usuarioCierre.getId());
			MotivoCierreAtencion motivoCierreAtencion = appState.getHCDigitalDAO().getMotivoCierreAtencionById(MotivoCierreAtencion.ID_SIN_ACTO_MEDICO);
			atencion.setMotivoCierreAtencion(motivoCierreAtencion);
			atencion.setFechaAnulacion(new Date());
			break;
		}
		if(atencion.getIdUsuarioApmAsignado() == 0 ){
			atencion.setIdUsuarioApmAsignado(appState.getCurrentUser().getId());
		}
		
		// Firma
		ImageView imageSign = dialog.findViewById(R.id.imageViewSign);
		if(imageSign.getVisibility() == View.VISIBLE && (null != imageSign.getDrawingCache() || imageSign.getTag() != null)){
			imageSign.setDrawingCacheEnabled(true);
			
			Bitmap bitmap = (Bitmap) (imageSign.getTag() != null ? imageSign.getTag() : imageSign.getDrawingCache());
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
			atencion.setFirmaDigital(outStream.toByteArray());
		}

		String jsonPreGuardado = generateJsonToSave(atencion);

		// Saving atencion...
		if(estadoAnterior.getId() == EstadoAtencion.ID_EN_PREPARACION){
			// Guarda los datos del formulario, modificando o creando la Atencion
			atencion = this.saveIM(form, tabId);
		}else{
			// Update atencion
			appState.getHCDigitalDAO().updateAtencion(atencion);

		}

		Atencion atencionGuardada = appState.getHCDigitalDAO().getAtencionById(atencion.getId());
		String jsonPostGuardado = generateJsonToSave(atencionGuardada);

		List<String> savedJsons = new ArrayList<>();
		savedJsons.add(jsonPreGuardado);
		savedJsons.add(jsonPostGuardado);


		saveClosedAtention(idEstado,atencion,savedJsons);

		appState.getGestureStore().removeEntry(tabId);

		Log.i(TAG, "closeIM: exit");
		return atencion;
	}

	private String generateJsonToSave(Atencion atencion){
		try {
			return atencion.toJSONWithoutFirmas().toString();
		}
		catch(Exception ignore){
			return "";
		}
	}

	private void saveClosedAtention(int idEstado,Atencion atencion,List<String> savedJsons){
		String jsonPreGuardado = savedJsons.get(0);
		String jsonPostGuardado = savedJsons.get(1);

		try {
			if (idEstado == EstadoAtencion.ID_ANULADA || idEstado == EstadoAtencion.ID_CERRADA_DEFINITIVA){
				AtencionCerrada atCerrada = new AtencionCerrada();
				atCerrada.setIdAtencion(atencion.getId());
				atCerrada.setIdEstadoAtencion(idEstado);
				atCerrada.setIsSyncHeader(false);
				atCerrada.setJsonPreGuardado(jsonPreGuardado);
				atCerrada.setJsonPostGuardado(jsonPostGuardado);
				if (idEstado == EstadoAtencion.ID_ANULADA) {
					atCerrada.setFechaAnulacion(new Date());
				}
				if (idEstado == EstadoAtencion.ID_CERRADA_DEFINITIVA) {
					atCerrada.setFechaCierreDefinitivo(new Date());
				}
				appState.getHCDigitalDAO().createAtencionCerrada(atCerrada);
			}
		}
		catch(Exception ignore){}
	}

	public String validateParams(int aplicacionPerfilId) {
		try {
			// Validacion 1 : Existen los parametros en el Perfil ?
			String msg = "Error en Perfil. No se encuentra parametro '%s' para la aplicación '%s'. Contáctese con el administrador";
			
			String paramNumeros =  ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_NUMERO, null); 
			List<Integer> listaIdNumero = new ArrayList<>();

			if (paramNumeros != null) {
				String[] paramNumerosArray = paramNumeros.split("&");
				for (String paramNumero : paramNumerosArray) {
					try {
						int paramNumeroCampo = Integer.parseInt(paramNumero);
						listaIdNumero.add(paramNumeroCampo);
					} catch (Exception ignore) {}
				}
			} 

			String paramFechas = ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_FECHA, null); 
			List<Integer> listaIdFecha = new ArrayList<>();

			if (paramFechas != null) {
				String[] paramFechasArray = paramFechas.split("&");
				for (String paramFecha : paramFechasArray) {
					try {
						int paramFechaCampo = Integer.parseInt(paramFecha);
						listaIdFecha.add(paramFechaCampo);
					} catch (Exception ignore) {}
				}
			} else {
				return String.format(msg, ParamHelper.ATENCION_CAMPOS_FECHA, Constants.COD_HCDIGITAL);
			}

			// Domicilio 
			String paramDomicilios = ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_DOMICILIO, null); 
			List<Integer> listaIdDomicilio = new ArrayList<>();
			
			if (paramDomicilios != null) {
				String[] paramDomiciliosArray = paramDomicilios.split("&");
				for (String paramDomicilio : paramDomiciliosArray) {
					try {
						int paramDomicilioCampo = Integer.parseInt(paramDomicilio);
						listaIdDomicilio.add(paramDomicilioCampo);
					} catch (Exception ignore) {}
				}
			} else {
				return String.format(msg, ParamHelper.ATENCION_CAMPOS_DOMICILIO, Constants.COD_HCDIGITAL);
			}
			
			
			// Validacion 3: Los parametros referencian a campos correctos?
			msg = "Error en Perfil. El parametro '%s' valor = %s hace referencia a un valor de Campo inexistente. Contáctese con el administrador";
			Campo campo = null;
			for (Integer fechaId : listaIdFecha) {
				if (campo == null) {
					campo = udaaService.getCampoBy(fechaId, aplicacionPerfilId);
				}
			}		
			if(null == campo) return String.format(msg, ParamHelper.ATENCION_CAMPOS_FECHA, Constants.COD_HCDIGITAL);

			String codigoTratamiento = campo.getTratamiento().trim(); 
			if(Tratamiento.getByCod(codigoTratamiento).equals(Tratamiento.DESCONOCIDO) && campo.getTratamientoDefault() != null){
				codigoTratamiento = campo.getTratamientoDefault().trim();
			}

			if(!Tratamiento.TF.getCod().equals(codigoTratamiento)){
				return String.format("Error en Perfil. El Campo asociado al parametro '%s' no posee tratamiento válido.", 
						ParamHelper.ATENCION_CAMPOS_DOMICILIO);
			}
			//
			campo = null;
			for (Integer numeroId : listaIdNumero) {
				if (campo == null) {
					campo = udaaService.getCampoBy(numeroId ,aplicacionPerfilId);
				}
			}
			if(null == campo) return String.format(msg, ParamHelper.ATENCION_CAMPOS_NUMERO, Constants.COD_HCDIGITAL);

			codigoTratamiento = campo.getTratamiento().trim(); 
			if(Tratamiento.getByCod(codigoTratamiento).equals(Tratamiento.DESCONOCIDO) && campo.getTratamientoDefault() != null){
				codigoTratamiento = campo.getTratamientoDefault().trim();
			}

			if(!Tratamiento.TNE.getCod().equals(codigoTratamiento)){
				return String.format("Error en Perfil. El Campo asociado al parametro '%s' no posee tratamiento válido.", 
						ParamHelper.ATENCION_CAMPOS_NUMERO);
			}
			//
			campo = null;
			for (Integer domicilioId : listaIdDomicilio) {
				if (campo == null) {
					campo = udaaService.getCampoBy(domicilioId ,aplicacionPerfilId);
				}
			}
			if(null == campo) return String.format(msg, ParamHelper.ATENCION_CAMPOS_DOMICILIO, Constants.COD_HCDIGITAL);

			codigoTratamiento = campo.getTratamiento().trim(); 
			if(Tratamiento.getByCod(codigoTratamiento).equals(Tratamiento.DESCONOCIDO) && campo.getTratamientoDefault() != null){
				codigoTratamiento = campo.getTratamientoDefault().trim();
			}

			if(!Tratamiento.TA.getCod().equals(codigoTratamiento)){
				return String.format("Error en Perfil. El Campo asociado al parametro '%s' no posee tratamiento válido.", 
						ParamHelper.ATENCION_CAMPOS_DOMICILIO);
			}
		} catch (Exception e) {
			Log.e(TAG, "validateParams", e);
			return "Error en Perfil. No se pudo verificar parametros";
		}

		return "";
	}

	/**
	 * Se genera el codigo de atencion segun las siguientes caracteristicas:
	 * 
	 * <p>. Tipo de dato: numerico
	 * <p>. Cantidad de caracteres: 9
	 * <p>. Formato :
     *    <p>120116334 -> Fecha: 11/06/2012 Nro de Atencion: 134
     *    <p>12: ultimos dos digitos del anioo (2).
     *    <p>01: primeros dos digitos del numero de atencion (2).
     *    <p>163: dia del anio (3).
     *    <p>34: ultimos dos digitos del numero de atencion (2).
	 */
	private String generarCodigoAtencion(Date fechaAtencion, String numeroAtencion){
		String codigoAtencion = "";
		try{
			Calendar fechaC = new GregorianCalendar();
		    fechaC.setTime(fechaAtencion);
			String anio = String.valueOf(fechaC.get(Calendar.YEAR));
			Log.d(TAG, "CodigoAtencion - Anio: "+anio);
			String diaDelAnio = String.valueOf(fechaC.get(Calendar.DAY_OF_YEAR));
			Log.d(TAG, "CodigoAtencion - Dia del anio: "+diaDelAnio);
			numeroAtencion = Utils.completarCerosIzq(numeroAtencion, 4);
			Log.d(TAG, "CodigoAtencion - Nro Atencion: "+numeroAtencion);
			// Ultimos dos digitos del anio (2)
			codigoAtencion += anio.substring(anio.length()-2);
			// Primeros dos digitos del numero de atencion (2) 
			codigoAtencion += numeroAtencion.substring(0, 2);
			// Dia del anio  (3)
			codigoAtencion += Utils.completarCerosIzq(diaDelAnio,3);
			// Ultimos dos digitos del numero de atencion (2)
			codigoAtencion += numeroAtencion.substring(numeroAtencion.length()-2);
			Log.d(TAG, "CodigoAtencion - Generado: "+codigoAtencion);
		} catch (Exception e) {
			Log.i(TAG, "Error al generar codigo de atencion: ",e);
			codigoAtencion = "";
		}
		
		return codigoAtencion;
	}
	
	
	/**
	 * Genera reporte PDF para el perfil pasado como parametro.
	 * Tanto el perfil como las secciones pueden tener asociado un template.
	 * La cantidad de hojas depende del numero de templates.
	 *
     * @param bTransfer (true => pdf Template parametrizado | false => pdf Template de perfil)
	 * @return ruta del archivo pdf generado
	 */
	public String generateReport(PerfilGUI form, boolean bTransfer) throws Exception {
		Log.i(TAG, "generateReport: enter");

		//Prefijo
		String pFix = "cierre.";
		
		//Valores del reporte formateados 
		Map<String,String> mValues = form.getValuesForReport();

		//Datos Atencion
		Atencion atencion = (Atencion) form.getBussEntity();
		EstadoAtencion estadoAtencion = atencion.getEstadoAtencion();

        // Datos del Medico
        UsuarioApm ua = null;

        // Datos del usuario según el estado de la atención (URGMNT-131):
        //      IMD pendiente -> Muestra datos del usuario logueado
        //      IMD Anulado -> Muestra datos del usuario que realizó la anulación (el usuario se guarda en cierre definitivo)
        //      IMD Cerrado provisoriamente -> Muestra datos del usuario que realizó el cierre provisorio
        //      IMD Cerrado definitivamente con cierre Provisorio -> Muestra datos del usuario que realizó el cierre provisorio
        //      IMD Cerrado definitivamente sin cierre provisorio -> Muestra datos del usuario que realizó el cierre Definitivo
        switch(estadoAtencion.getId()) {
            case EstadoAtencion.ID_EN_PREPARACION:
                // Datos del usuario logueado actualmente en la app
                ua = appState.getCurrentUser();
		    	break;

            // El usuario que anula se guarda en campo de cierre definitivo
            case EstadoAtencion.ID_ANULADA:
            case EstadoAtencion.ID_CERRADA_DEFINITIVA:
                // verifico si antes se realizó cierre provisorio.
                if( atencion.getFechaCierreProvisorio() != null )
                     ua = udaaService.getUserById( atencion.getIdUsuarioCierreProvisorio() );
                else
                     ua = udaaService.getUserById( atencion.getIdUsuarioCierreDefinitivo() );

            break;

            case EstadoAtencion.ID_CERRADA_PROVISORIA:

                ua = udaaService.getUserById( atencion.getIdUsuarioCierreProvisorio() );

            break;

        }

        if(ua != null) {
            //Campos
            mValues.put( pFix + "usuario", ua.getNombre() );
            mValues.put( pFix + "especialidad", ua.getEspecialidad() );
            mValues.put( pFix + "matricula", ua.getMatricula() );

            if(ua.getFirmaUsuario() != null)
                mValues.put( "img:" + pFix + "firmaUsuario", Utils.encodeToBase64( ua.getFirmaUsuario() ) );
        }

        if(EstadoAtencion.ID_EN_PREPARACION != estadoAtencion.getId()) {
            // Tipo de Cierre
            mValues.put( pFix + "tipoDeCierre", estadoAtencion.getDescripcion() );
            // Observacion
            if( !TextUtils.isEmpty( atencion.getObservacionCierreDefinitivo() ) )
                mValues.put( pFix + "observacion", atencion.getObservacionCierreDefinitivo() );
            // Observacion Post Atencion
            if( !TextUtils.isEmpty( atencion.getObservacionPostAtencion() ) )
                mValues.put( pFix + "observacionPostAtencion", atencion.getObservacionPostAtencion() );

            // Codigo Atencion
            mValues.put( pFix + "codigoAtencion", atencion.getCodigoAtencion() );

            if(EstadoAtencion.ID_ANULADA != estadoAtencion.getId()) {
                // Motivo de Cierre
                mValues.put( pFix + "motivo", atencion.getMotivoCierreAtencion().getDescripcion() );
                // Jira HCDDM-204: Error en Impresión de IMD
                if(atencion.getFirmaDigital() != null) // Si la firma digital es nula, no se imprime
                    mValues.put( "img:" + pFix + "firma", Utils.encodeToBase64( atencion.getFirmaDigital() ) );
            }
        }
		
		Log.d(TAG, "Values printables: ");
		for (String mKey : mValues.keySet())
        {
			Log.d(TAG, mKey+" : "+mValues.get(mKey));
		}
		
		// Secciones Visibles
		Map<String,Boolean> mSeccion = form.getMapSeccionesVisibles();

        String template;

        // El reporte es para transferir (true) o imprimir (false)?
		if(bTransfer) {
            // Template parametrizado asociado al perfil según el prefijo
            // Ej.: 1.ReportTransfer => Reporte a Transferir del perfil 1

            String sParamPrefix = ((Integer)atencion.getAplicacionPerfil().getId()).toString();
            String sParamBody = ParamHelper.REPORT_TRANSFER_TEMPLATE;

            String sParam = sParamPrefix + sParamBody;

            template = ParamHelper.getString( sParam );

        }
        else {
            mValues.put("hcdigital.perfilID", ((Integer)atencion.getAplicacionPerfil().getId()).toString());

            String ecgValor = "0";
			for (String campoECG : ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_ECG).split("&")) {
				try {
					if(mValues.containsKey(campoECG) && !TextUtils.isEmpty(mValues.get(campoECG)) && mValues.get(campoECG).toUpperCase().startsWith("S")) {
						ecgValor = "1";
					}
				} catch (Exception e) {
					// error de conversion;
				}
				
			}
			
			String seguimientoValor = "0";
			for (String campoSeguimiento : ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_SEG_EVOL).split("&")) {
				try {
					if(mValues.containsKey(campoSeguimiento) && !TextUtils.isEmpty(mValues.get(campoSeguimiento))) {
						seguimientoValor = "1";
					}
				} catch (Exception e) {
					// error de conversion;
				}
				
			}
			
            mValues.put("hcdigital.showECG", ecgValor);
            mValues.put("hcdigital.showSeguimiento", seguimientoValor);
        	
            // Template asociado al perfil
            template = form.getPrintingTemplate();
        }

        Log.i( TAG, "Report Template = " + template );

 		// Genero reporte y retorno URL
		String path = udaaService.generateReport(mValues, mSeccion, template);

		Log.i(TAG, "generateReport: exit");
		return path;
	}
	
	private void generateACRA_LOG(String message) {
		// Application version
		Intent msg = new Intent();
		String versionName = "";
		try {
			versionName = "v"
					+ context.getPackageManager().getPackageInfo(
							context.getPackageName(), 0).versionName;
		} catch (Exception ignore) {}

		ReporteError reporteError = new ReporteError();
		reporteError.setFechaCaptura(new Date());
		reporteError.setDescripcion(Constants.COD_HCDIGITAL +
				"_ErrorCodAtencion|" + versionName);
		if (appState.getDispositivoMovil() != null)
			reporteError.setDispositivoMovil(appState.getDispositivoMovil().getId());

		msg.setAction(Constants.ACTION_ACRA_ERROR_SEND);

		DetalleReporteError detalleReporteError;

		detalleReporteError = new DetalleReporteError();
		detalleReporteError.setDescripcion(message);
		detalleReporteError.setReporteError(reporteError);
		detalleReporteError.setTipoDetalle("HCDIGITAL");
		
		List<DetalleReporteError> detalleReporteErrorList = new ArrayList<>();
		detalleReporteErrorList.add(detalleReporteError);

		reporteError.setDetalleReporteErrorList(detalleReporteErrorList);

		// --
		GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
		
		new GraphAdapterBuilder()
		.addType(ReporteError.class)
		.addType(DetalleReporteError.class)
		.registerOn(gsonBuilder);
		Gson gson = gsonBuilder.create();
		// --

		msg.putExtra("REPORTE_ERROR", gson.toJson(reporteError));

		this.appState.sendBroadcast(msg);
	}
	
	private Gesture getFinalGesture(String key, GestureLibrary gestureStore, List<Gesture> defaultGestures){
		String temporalKey = key + Constants.TEMPORAL_GESTURE_SUFFIX;
		ArrayList<Gesture> temporalGestureList = gestureStore.getGestures(temporalKey);
		return (temporalGestureList != null ? temporalGestureList.get(0) : (defaultGestures != null) ? defaultGestures.get(0) : null);
	}

	public String getCodigoAtencion(PerfilGUI form) {
		String strFechaAtencion = this.getFieldValue(form, ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_FECHA, null));
		String numeroAtencion = this.getFieldValue(form, ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_NUMERO, null));
		Date fechaAtencion = null;

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		try {
			fechaAtencion = sdf.parse(strFechaAtencion);
		} catch (ParseException ignore) {}

		return this.generarCodigoAtencion(fechaAtencion, numeroAtencion);
	}

	private String getFieldValue(PerfilGUI form, String paramFieldsIDs) {
		if(paramFieldsIDs == null || form == null)
			return "";
		
		String fieldValue = "";
		List<Value> listValue = form.values(); 
		List<Integer> listaIDs = new ArrayList<>();
		
		String[] paramFieldsArray = paramFieldsIDs.split("&");
		for (String paramNumero : paramFieldsArray) {
			try {
				int paramCampo = Integer.parseInt(paramNumero);
				listaIDs.add(paramCampo);
			} catch (Exception ignore) {}
		}

		for(Value value: listValue){ 			
			for (Integer campoID : listaIDs) {
				if (value.getCampo().getCampo() != null && value.getCampo().getCampo().getId() == campoID) {
					fieldValue = value.getValor();
					if(value.getCampoValor() != null) {
						fieldValue = Integer.valueOf(value.getCampoValor().getId()).toString();
					}
				}
			}
		}
		
		return fieldValue;
	}
}