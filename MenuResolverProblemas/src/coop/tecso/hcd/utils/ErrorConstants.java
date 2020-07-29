package coop.tecso.hcd.utils;

/**
 *  Constantes de Errores
 * 
 * @author tecso
 *
 */
public final class ErrorConstants {

	// Errores
	public static final String ERROR_EXISTE_IM_SIN_CIERRE = "E1";
	
	// Errores Graves
	public static final String ERROR_MEDICAL_ALERT_UNKNONW = "E2";
	
	// Error Login
//	public static final String ERROR_LOGIN = "E4";	// 06-06-2013 - R. Iván G. Vesco
	public static final String ERROR_LOGIN = "E3";	// 06-06-2013 - R. Iván G. Vesco
	
	// Error CloseIAM
	public static final String ERROR_CLOSE_IAM = "E4";
	
	// Fatal Error CloseIAM
	public static final String ERROR_FATAL_CLOSE_IAM = "E5";
	
	// Error Init Session UDAA
	public static final String ERROR_INIT_SESSION_UDAA = "E6";
	
	// Error Permision Aplication
	public static final String ERROR_PERMISSION_APPLICATION = "E7";

	//Alertas Comunes a ambos sistemas (Rango 101 a 199)
	
//	alerta	A101	Ingreso de usuario	No se ha registrado ningún cambio, ¿desea continuar?
	public static final String ALERTA_SIN_FIRMA = "A101";
//	alerta	A102	Ingreso de usuario	Se ha detectado un nuevo ingreso, ¿confirma?
	public static final String ALERTA_NUEVA_FIRMA = "A102";
//	alerta	A103	Memoria Insuficiente	No se dispone memoria suficiente para realizar la acción. Guarde y reinicie la aplicación
	public static final String ALERTA_BAJA_MEMORIA = "A103";
//	alerta	A104	Memoria Insuficiente	No se dispone memoria suficiente para realizar la acción. La aplicación se reiniciará.
	public static final String ALERTA_BAJA_MEMORIA_REINICIO = "A104";
//	alerta	A108	Memoria Insuficiente	No se dispone memoria suficiente para abrir un IMD.
	public static final String ALERTA_BAJA_MEMORIA_REINICIO_OPCIONAL = "A108";

	
	public static final String ERROR_WS = "Error en la llamada al WS";

}