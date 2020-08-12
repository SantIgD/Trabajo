package coop.tecso.hcd.gui.components.difresscore;

import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.hcd.utils.ParamHelper;

public class DifRespHelper {

    static DifResScoreGUI getDifResScore(PerfilGUI perfilGUI) {
        int campoID = ParamHelper.getInteger(ParamHelper.CAMPO_DIF_RES_ID, 185);
        return (DifResScoreGUI) perfilGUI.getComponentByCampoID(campoID);
    }

    static CampoGUI getInsufRespiratoriaCampo(PerfilGUI perfilGUI) {
        int campoID = antecedenteInsufRespCampoID();
        return (CampoGUI) perfilGUI.getComponentByCampoID(campoID);
    }

    static SatOxigSIScoreGUI getSatOxigSiScore(PerfilGUI perfilGUI) {
        int campoID = ParamHelper.getInteger(ParamHelper.CAMPO_SATURACION_OXIGENO_SI_ID, 191);
        return (SatOxigSIScoreGUI) perfilGUI.getComponentByCampoID(campoID);
    }

    static SatOxigNOScoreGUI getSatOxigNoScore(PerfilGUI perfilGUI) {
        int campoID = ParamHelper.getInteger(ParamHelper.CAMPO_SATURACION_OXIGENO_NO_ID, 192);
        return (SatOxigNOScoreGUI) perfilGUI.getComponentByCampoID(campoID);
    }

    static ComboDifResGUI getDerivacionCampo(PerfilGUI perfilGUI) {
        int campoID = derivaPacienteCampoID();
        return (ComboDifResGUI) perfilGUI.getComponentByCampoID(campoID);
    }

    // ----------------------------------

    static Boolean getDerivaPacienteSelectedOption(PerfilGUI perfilGUI) {
        ComboDifResGUI derivaPaciente = DifRespHelper.getDerivacionCampo(perfilGUI);
        if (!derivaPaciente.getSeccionGUI().isVisible()) {
            return null;
        }

        Value valor = derivaPaciente.values().get(0);
        int id = valor.getCampoValor().getCampoValor().getId();

        int siOptionID = ParamHelper.getInteger(ParamHelper.DERIVA_PACIENTE_SI_OPTION_ID, 305);
        int noOptionID = ParamHelper.getInteger(ParamHelper.DERIVA_PACIENTE_NO_OPTION_ID, 306);
        if (id == siOptionID) { return true; }
        if (id == noOptionID) { return false; }
        return null;
    }

    static Boolean getInsufRespiratoriaSelectedOption(PerfilGUI perfilGUI) {
        CampoGUI campo = getInsufRespiratoriaCampo(perfilGUI);
        Value valor = campo.values().get(0);
        int id = valor.getCampoValor().getCampoValor().getId();

        int siOptionID = ParamHelper.getInteger(ParamHelper.INS_RESP_SI_OPTION_ID, 308);
        int noOptionID = ParamHelper.getInteger(ParamHelper.INS_RESP_NO_OPTION_ID, 309);
        if (id == siOptionID) { return true; }
        if (id == noOptionID) { return false; }
        return null;
    }

    // ----------------------------------

    public static Integer derivaPacienteCampoID() {
        return ParamHelper.getInteger(ParamHelper.CAMPO_DERIVA_PACIENTE_ID, 186);
    }

    public static Integer antecedenteInsufRespCampoID() {
        return ParamHelper.getInteger(ParamHelper.CAMPO_ANTECEDENTE_INS_RESP_ID, 187);
    }

    // ------

    static Integer scoreDifResTipo() {
        return ParamHelper.getInteger(ParamHelper.SCORE_TIPO_DIF_RES, 2);
    }

    static Integer scoreDifResSatOxSiTipo() {
        return ParamHelper.getInteger(ParamHelper.SCORE_DIF_RES_SAT_OX_TIPO_SI, 3);
    }

    static Integer scoreDifResSatOxNoTipo() {
        return ParamHelper.getInteger(ParamHelper.SCORE_DIF_RES_SAT_OX_TIPO_NO, 4);
    }

    // ------

    static List<Integer> llamadosDifRes() {
        String text = ParamHelper.getString(ParamHelper.SCORE_DIF_RES_LLAMADO_IDs, "");
        String[] components = text.split(",");
        return Helper.parseInts(components);
    }

    static String[] consultasDifRes() {
        String text = ParamHelper.getString(ParamHelper.SCORE_DIF_RES_CONSULTA_VALORES, "");
        return text.split(",");
    }

    // ------

    static String messageScoreDifResRiesgoBajo() {
        String defaultStr = "RESULTADO DEL SCORE: [X] PUNTOS - Riesgo Bajo";
        return ParamHelper.getString(ParamHelper.MSJ_SCORE_DIF_RES_RIESGO_BAJO, defaultStr);
    }

    static String messageScoreDifResRiesgoMedio() {
        String defaultStr = "RESULTADO DEL SCORE: [X] PUNTOS - RIESGO MEDIO Se sugiere intervención terapéutica y reevaluación";
        return ParamHelper.getString(ParamHelper.MSJ_SCORE_DIF_RES_RIESGO_MEDIO, defaultStr);
    }

    static String messageScoreDifResRiesgoAlto() {
        String defaultStr = "RESULTADO DEL SCORE: [X] PUNTOS - RIESGO ALTO Se sugiere derivación a Centro Asistencial";
        return ParamHelper.getString(ParamHelper.MSJ_SCORE_DIF_RES_RIESGO_ALTO, defaultStr);
    }


    static String messageScoreReDifResRiesgoBajo() {
        String defaultStr = "RIESGO BAJO";
        return ParamHelper.getString(ParamHelper.MSJ_SCORE_DIF_RES_RIESGO_BAJO_2, defaultStr);
    }

    static String messageScoreReDifResRiesgoAlto() {
        String defaultStr = "RIESGO ALTO - Se sugiere derivación a Centro Asistencial";
        return ParamHelper.getString(ParamHelper.MSJ_SCORE_DIF_RES_RIESGO_ALTO_2, defaultStr);
    }

}