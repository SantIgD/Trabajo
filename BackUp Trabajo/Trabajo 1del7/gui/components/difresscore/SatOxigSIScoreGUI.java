package coop.tecso.hcd.gui.components.difresscore;

import android.content.Context;
import android.database.Cursor;

import coop.tecso.hcd.gui.components.GenericScoreGUI;
import coop.tecso.hcd.helpers.SQLHelper;
import coop.tecso.hcd.utils.Helper;

public class SatOxigSIScoreGUI extends GenericScoreGUI {

    // MARK: - Init

    public SatOxigSIScoreGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    @Override
    protected Cursor getCamposCursor(SQLHelper sqlHelper) {
        Integer scoreType = DifRespHelper.scoreDifResSatOxSiTipo();
        String selection = "tipo = " + scoreType;
        String[] columnas = {"idSucursal","grupo","titulo","ordenTitulo","opcion","ordenOpcion", "peso"};
        return sqlHelper.query("hcd_score", columnas , selection, null,null,null, "ordenTitulo, ordenOpcion");
    }

    @Override
    protected void onScoreFilled(int puntosSumados) {
        DifRespHelper.getDifResScore(perfilGUI).showAlertIfNeeded();
    }

    @Override
    protected void globalLayoutDidChange() {
        Helper.setSectionVisibilityByCampo(this, shouldDisplaySection());
    }

    @Override
    public boolean isObligatorio() {
        return shouldDisplaySection();
    }

    @Override
    public boolean validate() {
        return super.validate() && isAllGroupsSelected();
    }

    // MARK: - Internal

    public boolean shouldDisplaySection() {
        Boolean insufRespSelectedOption = DifRespHelper.getInsufRespiratoriaSelectedOption(perfilGUI);
        return Helper.compareBoolean(insufRespSelectedOption, true);
    }

}