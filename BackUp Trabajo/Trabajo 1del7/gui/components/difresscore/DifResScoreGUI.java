package coop.tecso.hcd.gui.components.difresscore;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.text.TextUtils;

import java.util.List;

import coop.tecso.hcd.gui.components.AlertaConDivisor;
import coop.tecso.hcd.gui.components.CampoGUI;
import coop.tecso.hcd.gui.components.GenericScoreGUI;
import coop.tecso.hcd.gui.components.SeccionGUI;
import coop.tecso.hcd.gui.helpers.MotivoLlamadoUtil;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.helpers.SQLHelper;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.Helper;

public class DifResScoreGUI extends GenericScoreGUI {

    // MARK: - Init

    public DifResScoreGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    @Override
    protected Cursor getCamposCursor(SQLHelper sqlHelper) {
        Integer scoreType = DifRespHelper.scoreDifResTipo();
        String selection = "tipo = " + scoreType;
        String[] columnas = {"idSucursal","grupo","titulo","ordenTitulo","opcion","ordenOpcion", "peso"};
        return sqlHelper.query("hcd_score", columnas , selection, null,null,null, "ordenTitulo, ordenOpcion");
    }

    @Override
    protected void globalLayoutDidChange() {
        SeccionGUI seccionGUI = this.getSeccionGUI();
        boolean shouldDisplaySection = this.shouldDisplaySection();

        // Dificultad Respiratoria
        if (seccionGUI.isVisible() == shouldDisplaySection) {
            return;
        }

        if (shouldDisplaySection) {
            Helper.setSectionVisibilityByCampo(this, true);
        } else {
            Helper.setSectionVisibilityByCampo(this, false);
            Helper.setSectionVisibilityByCampo(DifRespHelper.getInsufRespiratoriaCampo(perfilGUI), false);
            Helper.setSectionVisibilityByCampo(DifRespHelper.getSatOxigSiScore(perfilGUI), false);
            Helper.setSectionVisibilityByCampo(DifRespHelper.getSatOxigNoScore(perfilGUI), false);
        }
    }

    @Override
    protected void onScoreFilled(int puntosSumados) {
        ComboDifResGUI insufRespCampo = (ComboDifResGUI) DifRespHelper.getInsufRespiratoriaCampo(perfilGUI);
        insufRespCampo.didCompleteDifRespScore();
        this.showAlertIfNeeded();
    }

    @Override
    public boolean isObligatorio() {
        return shouldDisplaySection();
    }

    @Override
    public boolean validate() {
        return super.validate() && isAllGroupsSelected();
    }

    // MARK: - Interface

    Integer getSuma() {
        Boolean insufResp = DifRespHelper.getInsufRespiratoriaSelectedOption(perfilGUI);
        if (insufResp == null || !this.isAllGroupsSelected()) {
            return null;
        }

        GenericScoreGUI score;
        if (insufResp) {
            score = DifRespHelper.getSatOxigSiScore(perfilGUI);
        } else {
            score = DifRespHelper.getSatOxigNoScore(perfilGUI);
        }

        if (!score.isAllGroupsSelected()) {
            return null;
        }
        return score.getSumaPuntosCheckboxes() + this.getSumaPuntosCheckboxes();
    }

    boolean isRiesgoMedio(int suma) {
        return suma > 4 && suma < 7;
    }

    void showAlertIfNeeded() {
        Boolean insufRespSelectedOption = DifRespHelper.getInsufRespiratoriaSelectedOption(perfilGUI);
        if (insufRespSelectedOption == null) { return; }

        if (!this.isAllGroupsSelected()) { return; }

        GenericScoreGUI seccondScore;
        if (insufRespSelectedOption) {
            seccondScore = DifRespHelper.getSatOxigSiScore(perfilGUI);
        } else {
            seccondScore = DifRespHelper.getSatOxigNoScore(perfilGUI);
        }
        if (!seccondScore.isAllGroupsSelected()) { return; }

        buildAlertSumaScore(this.getSuma());
    }

    // MARK: - Internal

    private boolean shouldDisplaySection() {
        // Tipo Movil
        if (!Helper.isAmbulancia(context)) {
            return false;
        }

        Integer edad = Helper.getEdadFromForm(context);
        if (edad == null || edad < 14) {
            return false;
        }

        return dificultadRespiratoriaMotivoLlamado() || dificultadRespiratoriaMotivoConsulta();
    }

    private boolean dificultadRespiratoriaMotivoLlamado() {
        MotivoLlamadoUtil motivoLlamadoUtil = new MotivoLlamadoUtil(context);

        for (Integer motivoLlamadoID: DifRespHelper.llamadosDifRes()) {
            if (motivoLlamadoUtil.isMotivoLlamado(motivoLlamadoID)) {
                return true;
            }
        }

        return false;
    }

    private boolean dificultadRespiratoriaMotivoConsulta() {
        Integer motivoConsultaCampoID = Helper.motivoConsultaCampoID();
        List<Value> motivoConsultaValores = perfilGUI.getValoresByCampoID(motivoConsultaCampoID);

        if (CollectionUtils.isEmpty(motivoConsultaValores)) {
            return false;
        }

        for (String code: DifRespHelper.consultasDifRes()) {
            for (Value value: motivoConsultaValores) {
                if (TextUtils.equals(value.getCodigoEntidadBusqueda(), code)) {
                   return true;
                }
            }
        }

        return false;
    }

    // --------------------------------

    private boolean setDerivacionVisibility(boolean visible) {
        CampoGUI campo = DifRespHelper.getDerivacionCampo(perfilGUI);
        return Helper.setSectionVisibilityByCampo(campo, visible);
    }

    private void buildAlertSumaScore(final int puntosSumados) {
        AlertaConDivisor alert = new AlertaConDivisor(context);
        String param = String.valueOf(puntosSumados);

        if (puntosSumados <= 4) {
            String message = DifRespHelper.messageScoreDifResRiesgoBajo();
            alert.color = Color.parseColor("#FF4AB955");
            alert.setText(message, param);
        } else if (puntosSumados < 7) {
            String message = DifRespHelper.messageScoreDifResRiesgoMedio();
            alert.color = Color.parseColor("#FF950E");
            alert.setText(message, param);
        } else {
            String message = DifRespHelper.messageScoreDifResRiesgoAlto();
            alert.color = Color.RED;
            alert.setText(message, param);
        }

        alert.show(() -> {
            boolean isAlertMedio = puntosSumados > 4 && puntosSumados < 7;
            setDerivacionVisibility(isAlertMedio);
        });
    }

}
