package coop.tecso.hcd.gui.components.difresscore;

import android.content.Context;

import coop.tecso.hcd.gui.components.ComboGUI;
import coop.tecso.hcd.gui.components.GenericScoreGUI;
import coop.tecso.hcd.utils.Helper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;

public class ComboDifResGUI extends ComboGUI {

    // MARK: - Init

    public ComboDifResGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    @Override
    protected void globalLayoutDidChange() {
        int antecedenteInsufRespCampoID = DifRespHelper.antecedenteInsufRespCampoID();
        int derivaPacienteCampoID = DifRespHelper.derivaPacienteCampoID();
        int campoID = ((AplPerfilSeccionCampo) this.getEntity()).getCampo().getId();

        if (campoID == antecedenteInsufRespCampoID) {
            this.updateInsufRespVisibility();
        }
        else if (campoID == derivaPacienteCampoID) {
            this.updateDerivacionPacienteVisibility();
        }
    }

    @Override
    public boolean isObligatorio() {
        int antecedenteInsufRespCampoID = DifRespHelper.antecedenteInsufRespCampoID();
        int derivaPacienteCampoID = DifRespHelper.derivaPacienteCampoID();
        int campoID = ((AplPerfilSeccionCampo) this.getEntity()).getCampo().getId();

        if (campoID == antecedenteInsufRespCampoID) {
           return shouldInsufRespVisible();
        }

        else if (campoID == derivaPacienteCampoID) {
            return shouldDerivacionPacienteVisible();
        }

        return super.isObligatorio();
    }

    void didCompleteDifRespScore() {
        Helper.setSectionVisibilityByCampo(this, true);
    }

    // MARK: - Internal

    private void updateInsufRespVisibility() {
        Helper.setSectionVisibilityByCampo(this, shouldInsufRespVisible());
    }

    private void updateDerivacionPacienteVisibility() {
        Helper.setSectionVisibilityByCampo(this, shouldDerivacionPacienteVisible());
    }

    private boolean shouldInsufRespVisible() {
        GenericScoreGUI difResScore = DifRespHelper.getDifResScore(perfilGUI);
        return difResScore.getSeccionGUI().isVisible() && difResScore.isAllGroupsSelected();
    }

    private boolean shouldDerivacionPacienteVisible() {
        DifResScoreGUI difResScore = DifRespHelper.getDifResScore(perfilGUI);
        Integer suma = difResScore.getSuma();
        return suma != null && difResScore.isRiesgoMedio(suma);
    }

}