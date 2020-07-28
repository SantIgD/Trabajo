package coop.tecso.hcd.gui.components.acvscore;

import java.util.HashMap;
import java.util.Map;

import coop.tecso.hcd.gui.components.PerfilGUI;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.hcd.utils.ParamHelper;

public class ACVHelper {

    // MARK: - Motivos de llamado y consulta

    static Map<Integer, String> motivosDeLlamado() {
        String str = ParamHelper.getString(ParamHelper.ACV_MOTIVOS_ID_LLAMADO_PROTOCOLO, "");
        String[] items = str.split(",");
        HashMap<Integer, String> result = new HashMap<>();

        for (String item: items) {
            String[] components = item.split("\\|");
            if (components.length < 2) {
                continue;
            }

            Integer key = Helper.parseInt(components[0]);
            if (key == null) {
                continue;
            }

            String value = components[1];
            result.put(key, value);
        }

        return result;
    }

    static String motivosDeConsulta() {
        return ParamHelper.getString(ParamHelper.ACV_MOTIVOS_CONSULTA,"");
    }

    // -----

    public static int suspicionComboID() {
        return ParamHelper.getInteger(ParamHelper.ACV_COMBO_SOSPECHA_CAMPO_ID, 195);
    }

    public static int persistenceComboID() {
        return ParamHelper.getInteger(ParamHelper.ACV_COMBO_PERSIST_CAMPO_ID, 196);
    }

    // -----

    public static int abcddScoreID() {
        return ParamHelper.getInteger(ParamHelper.ACV_SCORE_ABCD2_CAMPO_ID, 197);
    }

    public static int lappsScoreID() {
        return ParamHelper.getInteger(ParamHelper.ACV_SCORE_LAPPS_CAMPO_ID, 198);
    }

    // ----- Score Tipos

    static int abcddScoreTipo() {
        return ParamHelper.getInteger(ParamHelper.ACV_SCORE_ABCD2_TIPO, 5);
    }

    static int lappsScoreTipo() {
        return ParamHelper.getInteger(ParamHelper.ACV_SCORE_LAPPS_TIPO, 6);
    }

    // -----

    static Boolean suspicionSelectedOption(PerfilGUI perfilGUI) {
        int campoID = suspicionComboID();
        return Helper.getCampoSelectedOption(perfilGUI, campoID);
    }

    static Boolean persistenceSelectedOption(PerfilGUI perfilGUI) {
        int campoID = persistenceComboID();
        return Helper.getCampoSelectedOption(perfilGUI, campoID);
    }

    // -----

    static String messageScoreACVLappsNO() {
        return ParamHelper.getString(ParamHelper.MSJ_SCORE_ACV_LAPPS_NO, "");
    }

    static String messageScoreACVLappsACV() {
        return ParamHelper.getString(ParamHelper.MSJ_SCORE_ACV_LAPPS_ACV, "");
    }

    static String messageScoreACVAbcd2Bajo() {
        return ParamHelper.getString(ParamHelper.ALERTA_SCR_ACVABCD2_BAJO, "");
    }

    static String messageScoreACVAbcd2Alto() {
        return ParamHelper.getString(ParamHelper.ALERTA_SCR_ACVABCD2_ALTO, "");
    }

    // -----

    static Integer motivoACVScoreChequeaEdad () {
        return ParamHelper.getInteger(ParamHelper.ACV_SCORE_ID_MOTIVO_CHEQUEA_EDAD, 0);
    }

    static Integer edadMinimaChequeoACVScore () {
        return ParamHelper.getInteger(ParamHelper.ACV_SCORE_EDAD_MINIMA_CHEQUEO, 0);
    }

}
