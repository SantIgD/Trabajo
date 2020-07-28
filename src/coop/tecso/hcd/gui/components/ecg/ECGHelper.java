package coop.tecso.hcd.gui.components.ecg;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.Component;
import coop.tecso.hcd.gui.components.SectionsComboGUI;
import coop.tecso.hcd.gui.helpers.MotivoLlamadoUtil;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.hcd.utils.ParamHelper;

class ECGHelper {

    static boolean isObligatorioPatronECGInterpretado(Context context) {
        HCDigitalApplication appState = HCDigitalApplication.getApplication(context);

        Integer edadMinimaECG = getEdadMinimaECG(context);
        Integer edad = Helper.getEdadFromForm(appState);

        boolean aplicaEdadMayor = edad >= edadMinimaECG;
        boolean isAmbulacia = Helper.isAmbulancia(context);
        boolean aplicaMotivoLlamado = verificarAplicaMotivoLlamado(context);
        boolean aplicaMotivoConsulta = verificarAplicaMotivoConsulta(context);

        return aplicaEdadMayor && isAmbulacia && ( aplicaMotivoLlamado || aplicaMotivoConsulta );
    }

    // --------------------

    static void enableCmbEsElectrocardiograma(Context context) {
        HCDigitalApplication appState = HCDigitalApplication.getApplication(context);
        String idCampoEsElectro = ParamHelper.getString(ParamHelper.CAMPO_ES_ELECTRO_ID,null);
        Component component = appState.getForm().getComponentByCampoID(Integer.parseInt(idCampoEsElectro));

        if (component != null) {
            SectionsComboGUI cmbEsElectro = (SectionsComboGUI) component;
            cmbEsElectro.setEnabled(true);
        }
    }

    static void changeValueCmbElectroSI(Context context) {
        HCDigitalApplication appState = HCDigitalApplication.getApplication(context);
        String idCampoEsElectro = ParamHelper.getString(ParamHelper.CAMPO_ES_ELECTRO_ID,null);
        Component component = appState.getForm().getComponentByCampoID(Integer.parseInt(idCampoEsElectro));

        SectionsComboGUI cmbEsElectro = null;
        if (component != null) {
            cmbEsElectro = (SectionsComboGUI) component;
        }

        String selectedValue = cmbEsElectro.getSelectedValue();

        if (selectedValue.toLowerCase().contains("seleccione") || selectedValue.toLowerCase().contains("no")) {
            List<Component> components = cmbEsElectro.getComponents();

            if (CollectionUtils.isEmpty(components)) {
                return;
            }

            for (Component componentCurrent: components ) {
                Value value = componentCurrent.values().get(0);
                String currentValue = value.getValor();
                if (!currentValue.toLowerCase().contains("seleccione") && !currentValue.toLowerCase().contains("no")) {
                    int index = cmbEsElectro.getIndexByItem((CampoGUI) componentCurrent);
                    cmbEsElectro.setSelectedIndex(index);
                    cmbEsElectro.setEnabled(false);
                    break;
                }
            }
        }
    }

    // MARK: - Internal
    // --------------------

    private static int getEdadMinimaECG(Context context) {
        boolean isUserSucursalRosario = Helper.isUserSucursalRosario(context);
        String paramName = isUserSucursalRosario ? ParamHelper.VALOR_EDAD_ROSARIO : ParamHelper.VALOR_EDAD_CORDOBA;
        return ParamHelper.getInteger(paramName, 30);
    }

    private static String getParametroValoresMotivoConsultaECG(Context context) {
        boolean isUserSucursalRosario = Helper.isUserSucursalRosario(context);
        String paramName = isUserSucursalRosario ? ParamHelper.VALORES_CONSULTA_ROSARIO : ParamHelper.VALORES_CONSULTA_CORDOBA;
        return ParamHelper.getString(paramName, null);
    }

    private static String getParametroIDsProtocoloECG(Context context) {
        boolean isUserSucursalRosario = Helper.isUserSucursalRosario(context);
        String paramName = isUserSucursalRosario ? ParamHelper.PROTOCOLO_ECG_IDs_ROSARIO : ParamHelper.PROTOCOLO_ECG_IDs_CORDOBA;
        return ParamHelper.getString(paramName, null);
    }

    private static String getParametroIDsMotivoLlamadoECG(Context context) {
        boolean isUserSucursalRosario = Helper.isUserSucursalRosario(context);
        String paramName = isUserSucursalRosario ? ParamHelper.MOTIVO_LLAMADO_ECG_IDs_ROSARIO : ParamHelper.MOTIVO_LLAMADO_ECG_IDs_CORDOBA;
        return ParamHelper.getString(paramName, null);
    }

    // --------------------

    private static boolean verificarAplicaMotivoLlamado(Context context) {
        String motivosLlamadoIDsParam = getParametroIDsMotivoLlamadoECG(context);
        String protocolsIDsParam = getParametroIDsProtocoloECG(context);

        MotivoLlamadoUtil motivoLlamadoUtil = new MotivoLlamadoUtil(context);

        return motivoLlamadoUtil.apply(motivosLlamadoIDsParam, protocolsIDsParam);
    }

    private static boolean verificarAplicaMotivoConsulta(Context context) {
        HCDigitalApplication appState = HCDigitalApplication.getApplication(context);

        Integer idCampoMotivoConsulta = Helper.motivoConsultaCampoID();
        List<Value> valuesMotivoConsulta = appState.getForm().getValoresByCampoID(idCampoMotivoConsulta);
        List<String> codigoMotivoConsultaList = getCodigosMotivosConsulta(valuesMotivoConsulta);

        String parametroValoresConsulta = ECGHelper.getParametroValoresMotivoConsultaECG(context);
        String[] parametroMotivoConsultaArray = parametroValoresConsulta.split(",");
        List<String> parametroCodigoMotivoConsultaList = Arrays.asList(parametroMotivoConsultaArray);

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

    private static List<String> getCodigosMotivosConsulta(List<Value> valuesMotivoConsulta) {
        if (CollectionUtils.isEmpty(valuesMotivoConsulta)) {
            return new ArrayList<>();
        }

        List<String> codigoMotivoConsultaStringList = new ArrayList<>();

        for (Value valueMotivocConsulta: valuesMotivoConsulta) {
            codigoMotivoConsultaStringList.add(valueMotivocConsulta.getCodigoEntidadBusqueda());
        }

        return codigoMotivoConsultaStringList;
    }

}