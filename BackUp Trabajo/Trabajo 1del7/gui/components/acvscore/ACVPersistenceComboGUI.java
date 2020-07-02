package coop.tecso.hcd.gui.components.acvscore;

import android.content.Context;

import coop.tecso.hcd.R;
import coop.tecso.hcd.gui.components.ComboGUI;
import coop.tecso.hcd.utils.Helper;

import static coop.tecso.hcd.gui.components.acvscore.ACVHelper.persistenceSelectedOption;

public class ACVPersistenceComboGUI extends ComboGUI {

    // MARK: - Init

    public ACVPersistenceComboGUI(Context context, boolean enabled) {
        super(context, enabled);
    }

    @Override
    protected void globalLayoutDidChange() {
        super.globalLayoutDidChange();

        Helper.setSectionVisibilityByCampo(this, shouldBeVisible());
    }

    @Override
    public boolean isObligatorio() {
        return shouldBeVisible();
    }

    @Override
    public boolean validate() {
        if (!shouldBeVisible()) {
            return true;
        }
        if (persistenceSelectedOption(perfilGUI) == null){
            label.setError(context.getString(R.string.field_required, getEtiqueta()));
            return false;
        }
        return true;
    }

    // MARK: - Internal

    private boolean shouldBeVisible() {
        Boolean suspicionSelectedOption = ACVHelper.suspicionSelectedOption(perfilGUI);
        return Helper.compareBoolean(suspicionSelectedOption, true);
    }

}
