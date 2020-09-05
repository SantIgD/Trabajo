package coop.tecso.hcd.gui.components.difresscore;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import coop.tecso.hcd.gui.components.AlertaConDivisor;
import coop.tecso.hcd.gui.components.GenericScoreGUI;
import coop.tecso.hcd.helpers.SQLHelper;
import coop.tecso.hcd.utils.Helper;

public class ReDifResScoreGUI extends GenericScoreGUI {

    // MARK: - Init

    public ReDifResScoreGUI(Context context, boolean enabled) {
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
        Helper.setSectionVisibilityByCampo(this, shouldDisplaySection());
    }

    @Override
    protected void onScoreFilled(int puntosSumados) {
        buildAlertSumaScore(puntosSumados);
    }

    @Override
    public boolean isObligatorio() {
        return shouldDisplaySection();
    }

    @Override
    public boolean validate() {
        return super.validate() && isAllGroupsSelected();
    }

    // MARK: - Internals

    private boolean shouldDisplaySection() {
        Boolean derivacionPacienteOption = DifRespHelper.getDerivaPacienteSelectedOption(perfilGUI);
        return Helper.compareBoolean(derivacionPacienteOption, false);
    }

    private void buildAlertSumaScore(final int puntosSumados) {
        AlertaConDivisor alert = new AlertaConDivisor(context);
        String param = String.valueOf(puntosSumados);

        if (puntosSumados <= 4) {
            String message = DifRespHelper.messageScoreReDifResRiesgoBajo();
            alert.color = Color.parseColor("#FF4AB955");
            alert.setText(message, param);
        } else {
            String message = DifRespHelper.messageScoreReDifResRiesgoAlto();
            alert.color = Color.RED;
            alert.setText(message, param);
        }

        alert.show();
    }

}
