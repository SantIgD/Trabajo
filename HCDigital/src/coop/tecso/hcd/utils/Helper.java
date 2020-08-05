package coop.tecso.hcd.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.Component;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.components.SeccionGUI;
import coop.tecso.hcd.gui.helpers.Value;

public class Helper {

    public static Integer parseInt(String str) {
        if (str == null) { return null; }
        try {
            return Integer.parseInt(str);
        } catch (Exception e) { return null; }
    }

    public static List<Integer> parseInts(String[] items) {
        ArrayList<Integer> result = new ArrayList<>();
        for (String component: items) {
            Integer value = Helper.parseInt(component);
            if (value != null) {
                result.add(value);
            }
        }
        return result;
    }

    public static boolean compareBoolean(Boolean b1, Boolean b2) {
        if (b1 == null || b2 == null) {
            return b1 == b2;
        }
        return b1.booleanValue() == b2.booleanValue();
    }

    public static boolean setSectionVisibilityByCampo(CampoGUI campo, boolean visible) {
        SeccionGUI seccionGUI = campo.getSeccionGUI();
        if (visible && !seccionGUI.isVisible()) {
            seccionGUI.mostrar();
            return true;
        } else if (!visible && seccionGUI.isVisible()){
            seccionGUI.ocultar();
            return true;
        }
        return false;
    }

    public static Boolean getCampoSelectedOption(PerfilGUI perfilGUI, int campoID) {
        Component component = perfilGUI.getComponentByCampoID(campoID);

        if (!(component instanceof CampoGUI)) {
            return null;
        }

        CampoGUI campo = (CampoGUI) component;
        SeccionGUI seccion = campo.getSeccionGUI();
        if (seccion == null || !seccion.isVisible()) {
            return null;
        }

        return getBooleanSelection(campo);
    }

    private static Boolean getBooleanSelection(CampoGUI campoGUI) {
        if (campoGUI == null) {
            return null;
        }

        List<Value> values = campoGUI.values();
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }

        String value = values.get(0).getValor();
        if (value == null) {
            return null;
        }

        value = value.toLowerCase();

        if (TextUtils.equals(value,"si")) {
            return true;
        }
        if (TextUtils.equals(value,"no")) {
            return false;
        }

        return null;
    }

    /***
     * Android L (lollipop, API 21) introduced a new problem when trying to invoke implicit intent,
     * "java.lang.IllegalArgumentException: Service Intent must be explicit"
     *
     * If you are using an implicit intent, and know only 1 target would answer this intent,
     * This method will help you turn the implicit intent into the explicit form.
     *
     * Inspired from SO answer: http://stackoverflow.com/a/26318757/1446466
     * @param implicitIntent - The original implicit intent
     * @return Explicit Intent created from the implicit original intent
     */
    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }

    public static void sleep(int lapso) {
        try {
            Thread.sleep(lapso);
        } catch (InterruptedException ignore) {}
    }

    public static void logD(String TAG, String message) {
        int maxLogSize = 2000;
        for (int i = 0; i <= message.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = Math.min(end, message.length());
            Log.d(TAG, message.substring(start, end));
        }
    }

    // -------

    private static List<String> getCodigosMotivosConsulta(List<Value> valuesMotivoConsulta){
        List<String> codigoMotivoConsultaStringList = new ArrayList<>();

        if (CollectionUtils.isEmpty(valuesMotivoConsulta)) {
            return codigoMotivoConsultaStringList;
        }

        for (Value valueMotivocConsulta: valuesMotivoConsulta) {
            codigoMotivoConsultaStringList.add(valueMotivocConsulta.getCodigoEntidadBusqueda());
        }
        return codigoMotivoConsultaStringList;
    }

    public static boolean verificarAplicaMotivoConsulta(List<Value> valuesMotivoConsulta, String parametroMotivoConsultaString){
        String[] parametroMotivoConsultaArray = parametroMotivoConsultaString.split(",");
        List<String> parametroCodigoMotivoConsultaList = Arrays.asList(parametroMotivoConsultaArray);
        List<String> codigoMotivoConsultaList = Helper.getCodigosMotivosConsulta(valuesMotivoConsulta);

        if (CollectionUtils.isEmpty(parametroCodigoMotivoConsultaList)) {
            return false;
        }

        for (String codigoMotivoConsulta: codigoMotivoConsultaList) {
            if (parametroCodigoMotivoConsultaList.contains(codigoMotivoConsulta)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isAmbulancia(Context context) {
        HCDigitalApplication appState = HCDigitalApplication.getApplication(context);
        String valorTipoMovil = appState.getDispositivoMovil().getAsignadoA();
        return TextUtils.equals(valorTipoMovil, "MOVIL");
    }

    public static Integer getEdadFromForm(Context context) {
        PerfilGUI perfilGUI = HCDigitalApplication.getApplication(context).getForm();
        Integer edadCampoID = ParamHelper.getInteger(ParamHelper.CAMPO_EDAD_ID, 5);
        Value edadCampoValor = perfilGUI.getValorByCampoID(edadCampoID);
        if (edadCampoValor == null || TextUtils.isEmpty(edadCampoValor.getValor())) {
            return 0;
        }

        return Helper.parseInt(edadCampoValor.getValor());
    }

    // --------

    public static Integer motivoConsultaCampoID() {
        return ParamHelper.getInteger(ParamHelper.CAMPO_MOTIVOCONSULTA_ID, 14);
    }

    public static boolean isUserSucursalRosario(Context context) {
        HCDigitalApplication appState = HCDigitalApplication.getApplication(context);
        return appState.getCurrentUser().getSucursal().getId() == 1;
    }

}
