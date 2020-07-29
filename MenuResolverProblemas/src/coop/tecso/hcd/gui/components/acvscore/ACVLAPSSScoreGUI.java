package coop.tecso.hcd.gui.components.acvscore;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import coop.tecso.hcd.gui.components.AlertaConDivisor;
import coop.tecso.hcd.gui.components.GenericScoreGUI;
import coop.tecso.hcd.helpers.SQLHelper;
import coop.tecso.hcd.utils.Helper;

public class ACVLAPSSScoreGUI extends GenericScoreGUI {

    // MARK: - Init

    public ACVLAPSSScoreGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    // MARK: - Override

    @Override
    protected Cursor getCamposCursor(SQLHelper sqlHelper) {
        Integer scoreType = ACVHelper.lappsScoreTipo();
        String selection = "tipo = " + scoreType;
        String[] columnas = {"idSucursal","grupo","titulo","ordenTitulo","opcion","ordenOpcion", "peso"};
        return sqlHelper.query("hcd_score", columnas , selection, null,null,null, "ordenTitulo, ordenOpcion");
    }

    @Override
    protected void globalLayoutDidChange() {
        super.globalLayoutDidChange();

        Helper.setSectionVisibilityByCampo(this, shouldBeVisible());
    }

    @Override
    protected void onScoreFilled(int puntosSumados) {
        this.buildAlertSumaScore(puntosSumados);
    }

    @Override
    public boolean isObligatorio() {
        return shouldBeVisible();
    }

    @Override
    public boolean validate() {
        return (super.validate() && ValidateAllGroupsSelected());
    }

    // MARK: - Internal

    private boolean shouldBeVisible() {
        ACVHelper.motivosDeConsulta();
        Boolean persistenceSelectedOption = ACVHelper.persistenceSelectedOption(perfilGUI);
        return Helper.compareBoolean(persistenceSelectedOption, true);
    }

    private void buildAlertSumaScore(final int puntosSumados) {
        AlertaConDivisor alert = new AlertaConDivisor(context);

        if (puntosSumados > 0) {
            String message = ACVHelper.messageScoreACVLappsNO();
            alert.setText(message, "");
        } else {
            String message = ACVHelper.messageScoreACVLappsACV();
            alert.setText(message, "");
        }

        alert.color = Color.RED;
        alert.show();
    }

}
