package coop.tecso.udaa.base;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.content.Context;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.utils.Constants;

public final class UDAAReportSender implements ReportSender  {

	private Context context;
	private UdaaApplication appState;
	private static Map<String, ReportField> mapping ;

	private String[] valueErrorParams;

	UDAAReportSender(Context context, String params) {
		this.context = context;
		this.appState = (UdaaApplication) context.getApplicationContext();
		
		valueErrorParams= params.split("\\|");
	}

	@Override
	public void send(CrashReportData reportData) throws ReportSenderException {
		// Application version
		String versionName = "";
		try {
			versionName = "v" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (Exception ignore) {}
		
		if (appState.getDispositivoMovil() == null) {
			return;
		}

		UDAADao udaaDao = new UDAADao(context);

		ReporteError reporteError = new ReporteError();
		reporteError.setFechaCaptura(new Date());
		reporteError.setDescripcion(Constants.COD_UDAA + "|" + versionName);
		reporteError.setDispositivoMovil(appState.getDispositivoMovil().getId());
		//
		udaaDao.createReporteError(reporteError);		

		DetalleReporteError detalleReporteError;	
		for (int i = 0; i < valueErrorParams.length; i++) {
			detalleReporteError = new DetalleReporteError();
			
			String aux;
			String auxData =  reportData.get( mapping.get(valueErrorParams[i])); 
			if (auxData.length() > 5000) {		
				aux=auxData.substring(auxData.length()- 4999, auxData.length()-1 );
			} else {
				aux=auxData;
			}
			
			detalleReporteError.setDescripcion(	aux	);
			detalleReporteError.setReporteError(reporteError);
			detalleReporteError.setTipoDetalle(valueErrorParams[i]);
			udaaDao.createDetalleReporteError(detalleReporteError);	
			System.out.println("Save error DealleError - " + valueErrorParams[i]  );
		}
	}

	static {
		mapping = new HashMap<>();
		mapping.put("ANDROID_VERSION",ReportField.ANDROID_VERSION); //Device android version name.
		mapping.put("APP_VERSION_CODE", ReportField.APP_VERSION_CODE);//      Application version code.
		mapping.put("APP_VERSION_NAME", ReportField.APP_VERSION_NAME);//Application version name.
		mapping.put("APPLICATION_LOG", ReportField.APPLICATION_LOG);//Content of your own application log file.
		mapping.put("AVAILABLE_MEM_SIZE", ReportField.AVAILABLE_MEM_SIZE);//  Estimation of the available device memory size based on filesystem stats.
		mapping.put("BRAND", ReportField.BRAND);
		mapping.put("BUILD", ReportField.BUILD);
		mapping.put("CRASH_CONFIGURATION", ReportField.CRASH_CONFIGURATION);
		mapping.put("CUSTOM_DATA", ReportField.CUSTOM_DATA);
		mapping.put("DEVICE_FEATURES", ReportField.DEVICE_FEATURES);
		mapping.put("DEVICE_ID", ReportField.DEVICE_ID);// Device unique ID (IMEI).
		mapping.put("DISPLAY", ReportField.DISPLAY);

		mapping.put("DROPBOX",ReportField.DROPBOX); //  Content of the android.os.DropBoxManager API8
		mapping.put("DUMPSYS_MEMINFO", ReportField.DUMPSYS_MEMINFO);
		mapping.put("ENVIRONMENT", ReportField.ENVIRONMENT);
		mapping.put("EVENTSLOG", ReportField.EVENTSLOG);
		mapping.put("FILE_PATH", ReportField.FILE_PATH);//    Base path of the application's private file folder.
		mapping.put("INITIAL_CONFIGURATION", ReportField.INITIAL_CONFIGURATION);
		mapping.put("INSTALLATION_ID", ReportField.INSTALLATION_ID);
		mapping.put("IS_SILENT", ReportField.IS_SILENT);
		mapping.put("LOGCAT", ReportField.LOGCAT);
		mapping.put("MEDIA_CODEC_LIST", ReportField.MEDIA_CODEC_LIST);
		mapping.put("PHONE_MODEL", ReportField.PHONE_MODEL);

		mapping.put("PRODUCT",ReportField.PRODUCT);
		mapping.put("RADIOLOG", ReportField.RADIOLOG);
		mapping.put("REPORT_ID", ReportField.REPORT_ID );
		mapping.put("SETTINGS_SECURE", ReportField.SETTINGS_SECURE );
		mapping.put("SETTINGS_SYSTEM", ReportField.SETTINGS_SYSTEM );
		mapping.put("SHARED_PREFERENCES", ReportField.SHARED_PREFERENCES );
		mapping.put("STACK_TRACE", ReportField.STACK_TRACE );
		mapping.put("THREAD_DETAILS", ReportField.THREAD_DETAILS );
		mapping.put("TOTAL_MEM_SIZE", ReportField.TOTAL_MEM_SIZE );//Estimation of the total device memory size based on filesystem stats.
		mapping.put("USER_COMMENT", ReportField.USER_COMMENT );//
		mapping.put("PACKAGE_NAME", ReportField.APP_VERSION_NAME);// Comment added by the user in the CrashReportDialog 
		mapping.put("USER_CRASH_DATE", ReportField.APP_VERSION_NAME);  //   User date immediately after the crash occurred.
		mapping.put("USER_EMAIL", ReportField.USER_EMAIL );  //   User email
	}
}
