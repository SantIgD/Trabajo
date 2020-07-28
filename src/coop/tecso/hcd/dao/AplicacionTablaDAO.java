package coop.tecso.hcd.dao;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.entities.AplicacionTablaHC;
import coop.tecso.hcd.utils.CollectionUtils;

public class AplicacionTablaDAO extends GenericDAO<AplicacionTablaHC> {

    public AplicacionTablaDAO(Context context) {
        super(context);
    }

    public List<String> getTableNames() {
        // Filters
        Map<String,Object> filter = new HashMap<>();
        filter.put("deleted", false);
        List<AplicacionTablaHC> tables = getDAO().queryForFieldValuesArgs(filter);
        return CollectionUtils.map(tables,AplicacionTablaHC::getTabla);
    }


}