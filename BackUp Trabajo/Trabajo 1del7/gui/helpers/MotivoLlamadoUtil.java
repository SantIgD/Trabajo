package coop.tecso.hcd.gui.helpers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.hcd.utils.ParamHelper;

public class MotivoLlamadoUtil {

    // MARK: - Data

    private HCDigitalApplication application;

    private Integer motivoLlamadoID = null;

    private List<String> answers = new ArrayList<>();

    // MARK: - Init

    public MotivoLlamadoUtil(Context context) {
        this.application = HCDigitalApplication.getApplication(context);

        this.loadData();
    }

    // MARK: - Interface

    /**
     * Comprueba si el motivo de consulta y preguntas aplican a un protocolo
     * @param motivosLlamadoParam Motivos de llamados donde se puede disparar el protocolo
     *                            Tienen la forma [21],122,150 donde los corchetes indican que se
     *                            debe aplicar el chequeo de protocolo
     * @param protocolsParam Valores que debe cumplir el protocolo para poder aplicar
     */
    public boolean apply(String motivosLlamadoParam, String protocolsParam) {
        if (motivoLlamadoID == null) {
            return false;
        }

        String motivoLlamadoID = this.motivoLlamadoID + "";

        String[] motivosLlamadoParamArray = motivosLlamadoParam.split(",");
        boolean motivoLlamadoMatch = false;
        boolean shouldCheckProtocol = false;

        for (String param: motivosLlamadoParamArray) {
            if (param.equals(motivoLlamadoID)) {
                motivoLlamadoMatch = true;
                break;
            }
            else if (param.equals("[" + motivoLlamadoID  + "]")) {
                shouldCheckProtocol = true;
                motivoLlamadoMatch = true;
                break;
            }
        }

        if (!motivoLlamadoMatch) {
            return false;
        }
        if (!shouldCheckProtocol) {
            return true;
        }

        return apply(protocolsParam);
    }

    public boolean apply(String protocolsParam) {
        String[] protocolsParamArray = protocolsParam.toLowerCase().split(",");
        return answers.containsAll(Arrays.asList(protocolsParamArray));
    }

    public boolean isMotivoLlamado(int motivoLlamadoID) {
        if (this.motivoLlamadoID != null) {
            return this.motivoLlamadoID == motivoLlamadoID;
        } else {
            return false;
        }
    }

    public Integer getMotivoLlamado() {
        return this.motivoLlamadoID;
    }

    // MARK: - Internal

    private void loadData() {
        Integer motivoLlamadoCampoID = ParamHelper.getInteger(ParamHelper.CAMPO_SCR_MOTIVO_LLAMADO_ID, null);
        if (motivoLlamadoCampoID == null) {
            return;
        }

        Value value = application.getForm().getValorByCampoID(motivoLlamadoCampoID);
        if (value == null) {
            return;
        }

        String valor = value.getValor().toLowerCase();
        String[] components = valor.split("\\|");

        this.motivoLlamadoID = Helper.parseInt(components[0]);

        if (components.length > 1) {
            String[] answers = components[1].split(",");
            this.answers = Arrays.asList(answers);
        }
    }

}
