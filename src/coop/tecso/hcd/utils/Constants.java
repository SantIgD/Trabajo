package coop.tecso.hcd.utils;

/**
 * 
 * Constantes generales
 * 
 * @author tecso
 *
 */
public final class Constants {
	
	public static final String COD_HCDIGITAL = "HCDigital";
	public static final int APLICACION_HCDIGITAL_ID = 1;

	public static final String ACTION = "action";
	
	public static final String ACTION_CREATE = "create";
	public static final String ACTION_CREATE_IMM = "createIMM";
	public static final String ACTION_UPDATE = "update";
	public static final String ACTION_DELETE = "delete";
	
	public static final String ENTITY_ID = "ID";
	public static final String ESTADO_IM = "EstadoIM";
	public static final String DOMICILIO_IM = "DomicilioIM";
	
	public static final String TAB_TAG = "tid";
	public static final String MAIN_TAB_TAG = "tid0";
	
	public static final String CODIGO_ATENCION = "codigoAtencion";
	
	// Codigo de Preferencias
	public static final String PREF_DEFAULT_ORIENTATION = "orientation";
	
	// ErrorAtencion : tipos
	public static final String ERROR_ATENCION_TIPO_ERROR  = "error"; 
	public static final String ERROR_ATENCION_TIPO_ALERTA = "alerta"; 
	
	// Block Message
	public static final String ACTION_BLOCK_APPLICATION   = "coop.tecso.udaa.custom.intent.action.BLOCK_APPLICATION";
	// UnBlock Message
	public static final String ACTION_UNBLOCK_APPLICATION = "coop.tecso.udaa.custom.intent.action.UNBLOCK_APPLICATION";
	// UnBlock Message
	public static final String ACTION_UNBLOCK_UDAA_APPLICATION = "coop.tecso.udaa.custom.intent.action.UNBLOCK_UDAA_APPLICATION";
	// Lose Session Message
	public static final String ACTION_LOSE_SESSION = "coop.tecso.udaa.custom.intent.action.LOSE_SESSION";
	// New Notification Message
	public static final String ACTION_NEW_NOTIFICATION = "coop.tecso.udaa.custom.intent.action.NEW_NOTIFICATION";
	
	public static final String ACTION_ACRA_ERROR_SEND = "coop.tecso.udaa.custom.intent.action.ACTION_ACRA_ERROR_SEND";
	
	public static final String ACTION_ELECTROCARDIOGRAMA = "ecgUrg";
	
	public static final int REQUEST_NEW_GESTURE = 1;
	public static final int REQUEST_NEW_PHOTO 	= 2;
	public static final int REQUEST_NEW_GESTURE_DIALOG = 3;
	public static final int REQUEST_NEW_GESTURE_DIALOG_ACLARATION = 4;

    // Bluetooth
    public static final int REQUEST_BLUETOOTH_DISCOVERY = 5;
    public static final int REQUEST_BLUETOOTH_TRANSFER = 6;
	public static final int REQUEST_UPGRADE_APP = 7;
    public static final int BLUETOOTH_DISCOVER_DURATION = 300;
	
	public static final String GESTURE_ID = "GestureID";
	//
	public static final String GESTURE_TITLE = "GestureTitle";
	//
	public static final String GESTURE_SIGN = "GestureTitle";
	public static final String GESTURE_SIGNAC = "GestureTitleAC";
	
	public static final String SIGN_ACLARATION = "SIGN_ACLARATION";

	public static final String REFRESH = "REFRESH";
	public static final String ACTION_REFRESH = "coop.tecso.hcd.ACTION_REFRESH";
	
	// Imágenes permitidas en lista dinámica
	public static final int MAX_NUMBER_OF_IMAGES_IN_DL = 5;

	// URGMNT-217
	public static final String TEMPORAL_GESTURE_SUFFIX = "_T";

	public static final String GESTURE_VALID = "GestureValid"; 
	public static final String LAST_EXIT_TIMESTAMP = "lastExitTimestamp";
	public static final String NO_TIMESTAMP = "noTimestamp";
	public static final long ONE_MINUTE_IN_MILLIS = 60000;
	public static final String ACTION_TITLE = "ACTION_TITLE";
	public static final String ACTION_REFRESH_LIST = "ACTION_REFRESH_LIST";
	public static final String NEW_ATTENTION = "NEW_ATTENTION";

	public static final String WIFI_AP_STATE_CHANGED = "android.net.wifi.WIFI_AP_STATE_CHANGED";

	public static final int PROBLEM_SOLVER_REQUEST_CODE = 69;

}
