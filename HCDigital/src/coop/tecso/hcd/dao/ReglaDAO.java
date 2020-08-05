package coop.tecso.hcd.dao;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.entities.Regla;

public class ReglaDAO extends GenericDAO<Regla> {

    public ReglaDAO(Context context) {
        super(context);
    }

    public List<Regla> getReglas() {
        // Filters
        Map<String,Object> filter = new HashMap<>();
        filter.put("deleted", false);
        return getDAO().queryForFieldValuesArgs(filter);
    }


}