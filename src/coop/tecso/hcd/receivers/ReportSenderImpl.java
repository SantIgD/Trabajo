package coop.tecso.hcd.receivers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;

public final class ReportSenderImpl  implements ReportSender  {

	private static final String LOG_TAG = ReportSenderImpl.class.getSimpleName();

	private Context context;
	private HCDigitalApplication appState;
	private static Map<String, ReportField> mapping ;

	public ReportSenderImpl(Context context) {
		this.context = context;
		this.appState = (HCDigitalApplication) context.getApplicationContext();
	}

	@Override
	public void send(CrashReportData reportData) throws ReportSenderException {
		String defaultValue = "APP_VERSION_NAME|AVAILABLE_MEM_SIZE|STACK_TRACE";
		String value = ParamHelper.getString(ParamHelper.COD_ERROR_REPORT, defaultValue);

		String[] values = value.split("\\|");

		Intent msg = new Intent();

		// Application version
		String versionName = "";
		try {
			versionName = "v" + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (Exception ignore) {}


		ReporteError reporteError = new ReporteError();
		reporteError.setFechaCaptura(new Date());
		reporteError.setDescripcion(Constants.COD_HCDIGITAL + "|" +versionName );
		if(appState.getDispositivoMovil()!=null)
			reporteError.setDispositivoMovil(appState.getDispositivoMovil().getId());


		if (values.length > 0){
			msg.setAction(Constants.ACTION_ACRA_ERROR_SEND);

			DetalleReporteError detalleReporteError;	
			List<DetalleReporteError> detalleReporteErrorList = new ArrayList<DetalleReporteError>() ;

			for (String val : values) {
				try {
					String errorlog = reportData.get(mapping.get(val));

					detalleReporteError = new DetalleReporteError();
					detalleReporteError.setDescripcion(errorlog);
					detalleReporteError.setReporteError(reporteError);
					detalleReporteError.setTipoDetalle(val);
					detalleReporteErrorList.add(detalleReporteError);
				} catch (Exception e) {
					Log.d(LOG_TAG, "**ERROR**", e);
				}
			}

			reporteError.setDetalleReporteErrorList(detalleReporteErrorList);

			//--
			GsonBuilder gsonBuilder = new GsonBuilder() 
			.setPrettyPrinting(); 
			new GraphAdapterBuilder()
			.addType(ReporteError.class) 
			.addType(DetalleReporteError.class)
			.registerOn(gsonBuilder); 
			Gson gson = gsonBuilder.create();
			//--

			msg.putExtra("REPORTE_ERROR", gson.toJson(reporteError));

			appState.sendBroadcast(msg);
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

		mapping.put( "DROPBOX",ReportField.DROPBOX); //  Content of the android.os.DropBoxManager API8
		mapping.put("DUMPSYS_MEMINFO", ReportField.DUMPSYS_MEMINFO);
		mapping.put("ENVIRONMENT", ReportField.ENVIRONMENT);
		mapping.put("EVENTSLOG", ReportField.EVENTSLOG);
		mapping.put("FILE_PATH", ReportField.FILE_PATH);//    Base path of the application's private file folder.
		mapping.put("INITIAL_CONFIGURATION", ReportField.INITIAL_CONFIGURATION);
		mapping.put("INSTALLATION_ID", ReportField.INSTALLATION_ID);
		mapping.put("IS_SILENT", ReportField.IS_SILENT);
		mapping.put("LOGCAT", ReportField.LOGCAT);
		mapping.put("MEDIA_CODEC_LIST", ReportField.MEDIA_CODEC_LIST);
		mapping.put("PACKAGE_NAME", ReportField.PACKAGE_NAME);
		mapping.put("PHONE_MODEL", ReportField.PHONE_MODEL);

		mapping.put( "PRODUCT",ReportField.PRODUCT); 
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