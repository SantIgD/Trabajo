package coop.tecso.udaa.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.graph.GraphAdapterBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.domain.error.DetalleReporteError;
import coop.tecso.udaa.domain.error.ReporteError;

public class LogUtils {

    // MARK: - Data

    private Context context;
    private UdaaApplication application;

    private SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S", Locale.getDefault());

    // MARK: - Init

    public LogUtils(Context context) {
        this.context = context;
        this.application = (UdaaApplication) context.getApplicationContext();
    }

    // MARK: - Interface

    public void generateLoginLog(String message) {
        Integer loginTimeReport = ParamHelper.getInteger(ParamHelper.LOGIN_TIME_REPORT, 1);

        if (loginTimeReport != null && loginTimeReport == 1) {
            generateACRA_LOG(dateformat.format(new Date()) + " - "+ message, "LOGIN_TIME_REPORT");
        }
    }

    public void generateACRA_LOG(String message, String operation) {
        try {
            ReporteError reporteError = this.generateError(message, operation);

            Intent msg = new Intent();
            msg.setAction(Constants.ACTION_ACRA_ERROR_SEND);

            GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

            new GraphAdapterBuilder()
                    .addType(ReporteError.class)
                    .addType(DetalleReporteError.class)
                    .registerOn(gsonBuilder);
            Gson gson = gsonBuilder.create();
            // --

            msg.putExtra("REPORTE_ERROR", gson.toJson(reporteError));

            application.sendBroadcast(msg);
        }
        catch(Exception ex) {
            String logTag = context.getClass().getSimpleName();
            Log.d(logTag, "Error al generar reporte en ACRA. Exception: " + ex.getMessage());
        }
    }

    // MARK: - Internal

    private ReporteError generateError(String message, String operation) {
        String versionName = "";
        try {
            String packageName = context.getPackageName();
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            versionName = "v" + packageInfo.versionName;
        } catch (Exception ignore) {}

        ReporteError reporteError = new ReporteError();
        reporteError.setFechaCaptura(new Date());
        reporteError.setDescripcion(Constants.COD_UDAA + "_"+ operation +"|" + versionName);
        if (application.getDispositivoMovil() != null) {
            reporteError.setDispositivoMovil(application.getDispositivoMovil().getId());
        }

        DetalleReporteError detalleReporteError = new DetalleReporteError();
        detalleReporteError.setDescripcion(message);
        detalleReporteError.setReporteError(reporteError);
        detalleReporteError.setTipoDetalle("COD_UDAA");

        List<DetalleReporteError> detalleReporteErrorList = new ArrayList<>();
        detalleReporteErrorList.add(detalleReporteError);

        reporteError.setDetalleReporteErrorList(detalleReporteErrorList);

        return reporteError;
    }

}
