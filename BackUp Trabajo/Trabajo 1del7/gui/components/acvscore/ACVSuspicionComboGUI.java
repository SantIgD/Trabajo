package coop.tecso.hcd.gui.components.acvscore;

import android.content.Context;

import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.components.ComboGUI;
import coop.tecso.hcd.gui.helpers.MotivoLlamadoUtil;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.hcd.utils.ParamHelper;

public class ACVSuspicionComboGUI extends ComboGUI {

    // MARK: - Data

    private Map<Integer, String> motivosLlamadoParam;

    // MARK: - Init

    public ACVSuspicionComboGUI(Context context, boolean enabled) {
        super(context, enabled);
        this.motivosLlamadoParam = ACVHelper.motivosDeLlamado();
    }

    // MARK: - Override

    @Override
    protected void globalLayoutDidChange() {
        super.globalLayoutDidChange();

        Helper.setSectionVisibilityByCampo(this, shouldBeVisible());
    }

    @Override
    public boolean validate() {
        if (!shouldBeVisible()) {
            return true;
        }
        if (ACVHelper.suspicionSelectedOption(perfilGUI) == null){
            label.setError(context.getString(R.string.field_required, getEtiqueta()));
            return false;
        }
        return true;
    }

    // MARK: - Internal

    private boolean shouldBeVisible() {
        return true;//shouldBeVisibleMotivosLlamado() || shouldBeVisibleMotivosConsulta();
    }

    private boolean shouldBeVisibleMotivosLlamado() {
        MotivoLlamadoUtil motivoLlamadoUtil = new MotivoLlamadoUtil(context);

        if (!isMotivoLlamadoEdadValida(motivoLlamadoUtil)) {
            return false;
        }

        String motivoLlamadoParamKey = this.getMotivoLlamadoParamKey(motivoLlamadoUtil);
        if (motivoLlamadoParamKey == null) {
            return false;
        }

        String protocolStr = ParamHelper.getString(motivoLlamadoParamKey, "");
        return motivoLlamadoUtil.apply(protocolStr);
    }

    private boolean shouldBeVisibleMotivosConsulta() {
        String motivosConsultaParam = ACVHelper.motivosDeConsulta();
        Integer motivoConstultaCampoID = Helper.motivoConsultaCampoID();
        List<Value> motivoConstultaValores = perfilGUI.getValoresByCampoID(motivoConstultaCampoID);

        boolean isMotivo = Helper.verificarAplicaMotivoConsulta(motivoConstultaValores, motivosConsultaParam);

        return isMotivo && Helper.isAmbulancia(context);
    }

    private String getMotivoLlamadoParamKey(MotivoLlamadoUtil motivoLlamadoUtil) {
        Integer motivoLlamado = motivoLlamadoUtil.getMotivoLlamado();

        if (motivoLlamado != null) {
            return motivosLlamadoParam.get(motivoLlamado);
        } else {
            return null;
        }
    }

    private boolean isMotivoLlamadoEdadValida(MotivoLlamadoUtil motivoLlamadoUtil) {
        Integer edadMinimaChequeoACVScore = ACVHelper.edadMinimaChequeoACVScore();
        Integer motivoACVScoreChequeaEdadID = ACVHelper.motivoACVScoreChequeaEdad();
        Integer patientAge = Helper.getEdadFromForm(context);

        if (motivoLlamadoUtil.isMotivoLlamado(motivoACVScoreChequeaEdadID)) {
            return patientAge > edadMinimaChequeoACVScore;
        } else {
            return true;
        }
    }

}
