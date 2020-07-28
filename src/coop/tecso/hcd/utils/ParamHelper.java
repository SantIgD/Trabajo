package coop.tecso.hcd.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import coop.tecso.hcd.dao.HCDigitalDAO;
import coop.tecso.udaa.domain.aplicaciones.AplicacionParametro;

@SuppressWarnings("WeakerAccess")
public final class ParamHelper {

	private static final String LOG_TAG = ParamHelper.class.getSimpleName();

	private Map<String, String> values = new HashMap<>();

	// TimeOut's
	public static final String CONNECTION_TIMEOUT = "ConnectionTimeout";
	public static final String SOCKET_TIMEOUT	  = "SocketTimeout";

	public static final String COD_ESTADO_ATENCION = "CodigoEstadoAtencion";
	// ACRA Report Sender
	public static final String COD_ERROR_REPORT	   = "AcraReportError";
	// 
	public static final String COD_DELETE_IMD_TIME = "TiempoEliminacionIMD";
	// 
	public static final String SYNC_TIMER_PERIOD = "SyncTimerPeriod";

	@Deprecated
	public static final String ATENCION_DOMICILIO = "CampoDomicilioAtencionID";
	@Deprecated
	public static final String ATENCION_NUMERO 	  = "CampoNumeroAtencionID";
	@Deprecated
	public static final String ATENCION_FECHA 	  = "CampoFechaAtencionID";

	public static final String ATENCION_CAMPOS_DOMICILIO	= "CamposDomicilioAtencionIDs";
	public static final String ATENCION_CAMPOS_NUMERO		= "CamposNumeroAtencionIDs";
	public static final String ATENCION_CAMPOS_FECHA 	  	= "CamposFechaAtencionIDs";
	public static final String ATENCION_CAMPOS_ECG 	  		= "CamposECGIDs";
	public static final String ATENCION_CAMPOS_SEG_EVOL 	= "CamposSeguimientoEvolutivoIDs";
	
	
	public static final String SAVE_IM_TIME = "SaveIMTime";

	public static final String MOTIVO_CIERRE_ATENCION = "MotivoCierreAtencionID";
	public static final String MOTIVO_CIERRE_ATENCION_DEFAULT = "MotivoCierreAtencionDefault";
	public static final String MOTIVO_CIERRE_SIN_ACTO_MED = "MotivoCierreSinActoMed";
	
	public static final String OBS_POST_AT_MAX_LGH = "ObsPostAtenMaxLength";
	public static final String OBS_CIERRE_DEF_MAX_LGH = "ObsCierreDefMaxLength";

    public static final String REPORT_TRANSFER_TEMPLATE = ".ReportTransfer";

	public static final String EXPORT_DB_PATH= "ExportDataBasePath";
	
	public static final String LOGIN_TIME_REPORT = "LoginTimeReport";

	public static final String MENSAJE_ERROR_SYNC = "MensajeErrorSync";

	public static final String ELECTRO_COMPLETO = "ElectroCompleto";
	public static final String CARPETA_ELECTRO = "CarpetaElectro";
	
	public static final String ATENCION_CAMPOS_NUMERO_AFILIADO = "CamposNumeroAfiliadoIDs";
	public static final String ATENCION_CAMPOS_FECHA_NACIMIENTO = "CamposFechaNacimientoIDs";
	public static final String ATENCION_CAMPOS_SEXO = "CamposSexoIDs";
	public static final String ATENCION_OPCIONES_SEXO_MASCULINO = "OpcionesMasculinoIDs";
	public static final String ATENCION_OPCIONES_SEXO_FEMENINO = "OpcionesFemeninoIDs";
	
	public static final String DERIVACIONES_SEGMENTO = "DerivacionesSegmentoHabilitadas";
	public static final String DERIVACIONES_ONDA_T = "DerivacionesOndaTHabilitadas";
	public static final String RITMO_OPCION_OTRO = "RitmoOpcionOtro";

	public static final String PHONE_PREFIX_CODE = "PhonePrefixCode";
	
	public static final String SSID_NAME = "SSIDnameDefault";
	public static final String SSID_PASW = "SSIDpasswordDefault";

	public static final String VALOR_EDAD_CORDOBA = "SCRValorEdad_COR";
	public static final String VALOR_EDAD_ROSARIO = "SCRValorEdad_ROS";
	public static final String PROTOCOLO_ECG_IDs_ROSARIO = "SCRIDValoresProtocolo_ROS";
	public static final String PROTOCOLO_ECG_IDs_CORDOBA = "SCRIDValoresProtocolo_COR";
	public static final String MOTIVO_LLAMADO_ECG_IDs_ROSARIO = "SCRIDValoresLlamado_ROS";
	public static final String MOTIVO_LLAMADO_ECG_IDs_CORDOBA = "SCRIDValoresLlamado_COR";

	public static final String VALORES_CONSULTA_ROSARIO = "SCRValoresConsulta_ROS";
	public static final String VALORES_CONSULTA_CORDOBA = "SCRValoresConsulta_COR";

	public static final String CAMPO_EDAD_ID = "idCampoEdad";
	public static final String CAMPO_ES_ELECTRO_ID = "idCampoEsElectrocardiograma";
	public static final String CAMPO_MOTIVOCONSULTA_ID = "idCampoMotivoConsulta";
	public static final String CAMPO_MOTIVOLLAMADO_ID = "idCampoMotivoLlamado";
	public static final String CAMPO_PROTOCOLO_ID = "idCampoProtocolo";
	public static final String CAMPO_PATRON_ECG_INTERPRETADO_ID = "idCampoPatronECGInterpretado";
	public static final String CAMPO_SCR_MOTIVO_LLAMADO_ID = "SCRCampoIdMotivoLlamado";

	public static final String ID_VALOR_SELECCIONE_CMB_ECGINTERPRETADO = "idValorSeleccionCmbECGInterpretado";
	public static final String ID_VALOR_SI_CMB_ECGINTERPRETADO = "idValorSICmbECGInterpretado";
	public static final String ID_VALOR_NO_CMB_ECGINTERPRETADO = "idValorNOCmbECGInterpretado";

	public static final String MSJ_ALERTA_CMB_ECG_INTERPRETADO = "alertaCmbECGInterpretado";
    public static final String MSJ_ALERTA_SCORE_RIESGO_ALTO_CORDOBA = "alertaScrRiesgoAltoCordoba";
    public static final String MSJ_ALERTA_SCORE_RIESGO_BAJO_CORDOBA  = "alertaScrRiesgoBajoCordoba";
    public static final String MSJ_ALERTA_SCORE_RIESGO_ALTO_ROSARIO = "alertaScrRiesgoAltoRosario";
    public static final String MSJ_ALERTA_SCORE_RIESGO_BAJO_ROSARIO  = "alertaScrRiesgoBajoRosario";

	public static final String SCORE_DIF_RES_LLAMADO_IDs = "SCRDifResIDValoresLlamado";
    public static final String SCORE_DIF_RES_CONSULTA_VALORES = "SCRDifResValoresConsulta";

    public static final String MSJ_SCORE_DIF_RES_RIESGO_ALTO = "alertaScrDifResRiesgoAlto";
    public static final String MSJ_SCORE_DIF_RES_RIESGO_MEDIO = "alertaScrDifResRiesgoMedio";
    public static final String MSJ_SCORE_DIF_RES_RIESGO_BAJO = "alertaScrDifResRiesgoBajo";

    public static final String MSJ_SCORE_DIF_RES_RIESGO_ALTO_2 = "alertaScrDifResRiesgoAlto2";
    public static final String MSJ_SCORE_DIF_RES_RIESGO_BAJO_2 = "alertaScrDifResRiesgoBajo2";

    public static final String DERIVA_PACIENTE_SI_OPTION_ID = "idValorSICmbDerivaPaciente";
    public static final String DERIVA_PACIENTE_NO_OPTION_ID = "idValorNOCmbDerivaPaciente";


    public static final String INS_RESP_SI_OPTION_ID = "idValorSICmbInsuficienciaRespiratoria";
    public static final String INS_RESP_NO_OPTION_ID = "idValorNOCmbInsuficienciaRespiratoria";

    public static final String SCORE_TIPO_CARDIO = "SCRCardioTipo";
    public static final String SCORE_TIPO_DIF_RES = "SCRDifResTipo";

    public static final String SCORE_DIF_RES_SAT_OX_TIPO_SI = "SCRDifResSaturacionOxigenoSITipo";
    public static final String SCORE_DIF_RES_SAT_OX_TIPO_NO = "SCRDifResSaturacionOxigenoNOTipo";

	public static final String CAMPO_ANTECEDENTE_INS_RESP_ID = "idValorAntecedentesDeInsuficiencia";

	public static final String CAMPO_DIF_RES_ID = "SCRDifRes";
	public static final String CAMPO_DERIVA_PACIENTE_ID = "idDerivaPaciente";
	public static final String CAMPO_SATURACION_OXIGENO_SI_ID = "idSaturacionOxigenoSI";
	public static final String CAMPO_SATURACION_OXIGENO_NO_ID = "idSaturacionOxigenoNO";

    public static final String ACV_MOTIVOS_CONSULTA = "SCRACVMotivosDeConsulta";

	public static final String ACV_MOTIVOS_ID_LLAMADO_PROTOCOLO = "SCRACVIDMotLlamProtocolo";

	public static final String MSJ_SCORE_ACV_LAPPS_NO = "alertaScrACVLapssNO";
	public static final String MSJ_SCORE_ACV_LAPPS_ACV = "alertaScrACVLapssACV";

	public static final String ALERTA_SCR_ACVABCD2_BAJO = "alertaScrACVABCD2Bajo";
	public static final String ALERTA_SCR_ACVABCD2_ALTO = "alertaScrACVABCD2Alto";

    public static final String ACV_COMBO_SOSPECHA_CAMPO_ID = "SCRACVComboSospecha";
    public static final String ACV_COMBO_PERSIST_CAMPO_ID = "SCRACVComboPersistSintoma";

    public static final String ACV_SCORE_ABCD2_CAMPO_ID = "SCRACVIdABCD2";
    public static final String ACV_SCORE_LAPPS_CAMPO_ID = "SCRACVIdLAPSS";

    public static final String ACV_SCORE_ABCD2_TIPO = "SCRACVABCD2Tipo";
    public static final String ACV_SCORE_LAPPS_TIPO = "SCRACVLAPSSTipo";

    public static final String ACV_SCORE_ID_MOTIVO_CHEQUEA_EDAD = "SCRACVIDChequeaEdad";
    public static final String ACV_SCORE_EDAD_MINIMA_CHEQUEO = "SCRACVEdad_11";

	public static final String COVID_OPTIONAL_SECTION_FIELD_DATA_ID = "MostrarSeccionesOcultas";

	/**
	 * Singleton instance.
	 */
	private static ParamHelper INSTANCE;

	/**
	 * Private Constructor
	 */
	private ParamHelper(Context context) {
		List<AplicacionParametro> paramList = new HCDigitalDAO(context).getListAplicacionParametro();
		for (AplicacionParametro param : paramList) {
			Log.d(LOG_TAG, String.format("param: *%s* - value: *%s*",
					param.getCodigo(), param.getValor()));
			values.put(param.getCodigo(), param.getValor());
		}
	}

	/**
	 * Initialize the Param.
	 * 
	 * @param context The Android context.
	 */
	public static void initialize(Context context) {
		Log.d(LOG_TAG, "Initializing...");
		INSTANCE = new ParamHelper(context);
	}

	/**
	 * Get the singleton instance of {@link ParamHelper}.
	 * 
	 * @return The singleton instance.
	 */
	public static ParamHelper getInstance() {
		if (INSTANCE == null) {
			Log.e(LOG_TAG, "ParamHelper not initialized!");
			throw new RuntimeException("ParamHelper not initialized!");
		}
		return INSTANCE;
	}

	protected String getValue(String key){
		return values.get(key);
	}

	static public int getInteger(String key) throws IllegalArgumentException {
		String ret = ParamHelper.getInstance().getValue(key);
		if (ret == null) {
		    throw new IllegalArgumentException(String.format("No existe parámetro %s", key));
        }
		return Integer.parseInt(ret);
	}

	static public Integer getInteger(String key, Integer defaultValue) {
		try {
			return getInteger(key);
		} catch (Exception e) {
			Log.e(LOG_TAG, String.format("**ERROR**: %s", e.getMessage()));
			return defaultValue;
		}
	}

	static public String getString(String key) throws IllegalArgumentException {
		String ret = ParamHelper.getInstance().getValue(key);
		if (ret == null) {
		    throw new IllegalArgumentException(String.format("No existe parámetro %s", key));
        }
		return ret;
	}

	static public String getString(String key, String defaultValue) {
		try {
			return getString(key);
		} catch (Exception e) {
			Log.e(LOG_TAG, String.format("**ERROR**: %s", e.getMessage()));
			return defaultValue;
		}
	}

	static public Long getLong(String key) throws IllegalArgumentException {
		String ret = ParamHelper.getInstance().getValue(key);
		if (ret == null) {
			throw new IllegalArgumentException(String.format("No existe parámetro %s", key));
		}
		return Long.parseLong(ret);
	}

	static public Long getLong(String key, Long defaultValue) {
		try {
			return getLong(key);
		} catch (Exception e) {
			Log.e(LOG_TAG, String.format("**ERROR**: %s", e.getMessage()));
			return defaultValue;
		}
	}

	static public Double getDouble(String key) throws IllegalArgumentException {
		String ret = ParamHelper.getInstance().getValue(key);
		if (ret == null) {
		    throw new IllegalArgumentException(String.format("No existe parámetro %s", key));
        }
		return Double.parseDouble(ret);
	}

	static public Double getDouble(String key, Double defaultValue) {
		try {
			return getDouble(key);
		} catch (Exception e) {
			Log.e(LOG_TAG, String.format("**ERROR**: %s", e.getMessage()));
			return defaultValue;
		}
	}

}