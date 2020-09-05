package coop.tecso.hcd.gui.components.acvscore;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import coop.tecso.hcd.gui.components.AlertaConDivisor;
import coop.tecso.hcd.gui.components.GenericScoreGUI;
import coop.tecso.hcd.helpers.SQLHelper;
import coop.tecso.hcd.utils.Helper;

public class ACVABCDDScoreGUI extends GenericScoreGUI {

    // MARK: - Init

    public ACVABCDDScoreGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    // MARK: - Override

    @Override
    protected Cursor getCamposCursor(SQLHelper sqlHelper) {
        int scoreType = ACVHelper.abcddScoreTipo();
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
        Boolean persistenceSelectedOption = ACVHelper.persistenceSelectedOption(perfilGUI);
        return Helper.compareBoolean(persistenceSelectedOption, false);
    }

    private void buildAlertSumaScore(final int puntosSumados) {
        AlertaConDivisor alert = new AlertaConDivisor(context);
        String param = String.valueOf(puntosSumados);

        if (puntosSumados < 4) {
            String message = ACVHelper.messageScoreACVAbcd2Bajo();
            alert.color = Color.parseColor("#FF4AB955");
            alert.setText(message, param);
        } else {
            String message = ACVHelper.messageScoreACVAbcd2Alto();
            alert.color = Color.RED;
            alert.setText(message, param);
        }

        alert.show();
    }

}
