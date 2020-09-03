package coop.tecso.udaa.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.domain.aplicaciones.AplicacionParametro;

public final class ParamHelper {

	private static final String LOG_TAG = ParamHelper.class.getSimpleName();

	private Map<String, String> values = new HashMap<String, String>();

	// TimeOut's
	public static final String CONNECTION_TIMEOUT = "ConnectionTimeout";
	public static final String SOCKET_TIMEOUT = "SocketTimeout";

	// Codigos LocationGPS
	public static final String COD_TIMER_PERIOD = "TimerLocationInterval";
	public static final String COD_TIPO_POS = "TipoPosicionamiento";
	public static final String COD_UMBRAL_BAT = "UmbralBateria";
	public static final String COD_NUM_MAX_POS_TRANS = "NumMaxPosTrans";
	
	public static final String EMERGENCY_CHANNEL_PERIOD = "C2DMServiceTestInterval";
	
	public static final String COD_GCM_PROJECT_ID = "GCMProjectID";

	public static final String COD_LOCAL_LOGIN_MAX_COUNT = "maxlocalloginattempt";
	public static final String COD_ERROR_REPORT= "AcraReportError";
	
	public static final String EXPORT_DB_PATH= "ExportDataBasePath";
	
	public static final String LOGIN_TIME_REPORT = "LoginTimeReport";
	
	public static final String MENSAJE_ERROR_SYNC = "MensajeErrorSync";

	public static final String SERVER_URL_CONFIGURATION = "ServerURLConfiguracion";
	
	/**
	 * Singleton instance.
	 */
	private static ParamHelper INSTANCE;

	/**
	 * Private Constructor
	 */
	private ParamHelper(Context context) {
		List<AplicacionParametro> paramList = new UDAADao(context).getListAplicacionParametro();
		for (AplicacionParametro param : paramList) {
			Log.d(LOG_TAG, String.format("param: %s - value: %s",
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
	private static ParamHelper getInstance() {
		if (INSTANCE == null) {
			Log.e(LOG_TAG, "ParamHelper not initialized!");
			throw new RuntimeException("ParamHelper not initialized!");
		}
		return INSTANCE;
	}

	private String getValue(String key){
		return values.get(key);
	}

	static public int getInteger(String key) throws Exception {
		String ret = ParamHelper.getInstance().getValue(key);
		if (ret == null) throw new IllegalArgumentException(String.format("No existe parámetro %s", key));
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

	static public String getString(String key) throws Exception {
		String ret = ParamHelper.getInstance().getValue(key);
		if (ret == null) throw new IllegalArgumentException(String.format("No existe parámetro %s", key));
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

	static public Long getLong(String key) throws Exception {
		String ret = ParamHelper.getInstance().getValue(key);
		if (ret == null) throw new IllegalArgumentException(String.format("No existe parámetro %s", key));
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

	static public Double getDouble(String key) throws Exception {
		String ret = ParamHelper.getInstance().getValue(key);
		if (ret == null) throw new IllegalArgumentException(String.format("No existe parámetro %s", key));
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