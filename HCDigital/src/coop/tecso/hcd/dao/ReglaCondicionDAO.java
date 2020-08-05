package coop.tecso.hcd.dao;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.entities.Regla;
import coop.tecso.hcd.entities.ReglaCondicion;
import coop.tecso.hcd.utils.CollectionUtils;

public class ReglaCondicionDAO extends GenericDAO<ReglaCondicion> {

    public ReglaCondicionDAO(Context context) {
        super(context);
    }

    public List<ReglaCondicion> getReglaCondiciones(Regla regla, int aplicacionPerfil) {
        List<ReglaCondicion> reglaCondiciones = this.getReglaCondiciones(regla);
        return CollectionUtils.filter(reglaCondiciones, reglaCondicion -> shouldApplyReglaCondicion(reglaCondicion, aplicacionPerfil));
    }

    // MARK: - Internal

    private List<ReglaCondicion> getReglaCondiciones(Regla regla) {
        Map<String,Object> filter = new HashMap<>();
        filter.put("deleted", false);
        filter.put("idRegla", regla.getId());

        return getDAO().queryForFieldValuesArgs(filter);
    }

    private boolean shouldApplyReglaCondicion(ReglaCondicion reglaCondicion, int aplicacionPerfil) {
        return reglaCondicion.aplicacionPerfil == 0 || reglaCondicion.aplicacionPerfil == aplicacionPerfil;
    }

}
