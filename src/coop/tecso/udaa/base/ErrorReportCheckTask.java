package coop.tecso.udaa.base;

import java.util.List;
import java.util.TimerTask;

import android.content.Context;
import android.util.Log;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.common.WebServiceDAO;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;
import coop.tecso.udaa.domain.util.DeviceContext;

// CATA CODE
public final class ErrorReportCheckTask extends TimerTask {

	private static final String LOG_TAG = ErrorReportCheckTask.class.getSimpleName();

	private UdaaApplication appState;
	private Context context;
	private UDAADao udaaDao;

	ErrorReportCheckTask(Context context) {
		this.context  = context;
		this.appState = (UdaaApplication) context.getApplicationContext();
		this.udaaDao  = new UDAADao(context);
	}

	@Override
	public void run() {
		if(appState.getCurrentUser() == null){
			Log.i(LOG_TAG, "UDAA sin login, se omite la verificacion del servicio C2DM");
			return;
		}
		if(!DeviceContext.hasConnectivity(context)){
			Log.i(LOG_TAG," UDA sin conexion disponible ");
			return;
		}
				
		WebServiceDAO wService = WebServiceDAO.getInstance(context);
		
		try {
			Log.i(LOG_TAG, "Servicio C2DM no disponible: Se intenta sincronizar por canal de contingencias...");

			List<ReporteError> listRep = null;			
			listRep = udaaDao.getListReporteError();
			
			if(listRep==null || listRep.size()<=0){
				Log.i(LOG_TAG, " No hay reportes de error para enviar");
				return;
			}
			
			try {
				boolean response;

				for (ReporteError reporteError : listRep) {
					response = wService.SendReporteError(reporteError);
					
					Log.d(LOG_TAG, "Response confirmReceiptReporteError: " + String.valueOf(response) );
					
					if(response){
							Log.i(LOG_TAG, "Eliminando REPORTE ERROR");
							udaaDao.deleteReporteError(reporteError);
							
							Log.i(LOG_TAG, "Eliminando DETALLE REPORTE ERROR");
							for (DetalleReporteError detalleError : reporteError.getDetalleReporteErrorList()) {
								udaaDao.deleteDetalleReporteError(detalleError);	
							}
						
					} else{
						Log.i(LOG_TAG, "Error al enviar reporte error: ");
					}
				}				
				
			} catch (Exception e) {
				Log.i(LOG_TAG, "No se pudo sincronismo Reporte Error 1. Causa:", e);				
				e.printStackTrace();
			}			
			Log.i(LOG_TAG, "Finalizando correctamente sincronismo Reporte Error");
		} catch (Exception e) {
			Log.i(LOG_TAG, "No se pudo sincronismo Reporte Error 2. Causa:", e);
		}
	}

}
