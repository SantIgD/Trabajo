package coop.tecso.hcd.dao;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;

import java.sql.SQLException;

import coop.tecso.udaa.domain.base.TablaVersion;

public class TablaVersionDAO extends GenericDAO<TablaVersion> {

    public static final String TAG = TablaVersionDAO.class.getSimpleName();

    public TablaVersionDAO(Context context) {
        super(context);
    }

    public TablaVersion getTablaVersionByTableName(String tableName) {

        QueryBuilder<TablaVersion, Integer> queryBuilder = this.getDAO().queryBuilder();

        try {
            queryBuilder.where().rawComparison("tabla", "COLLATE NOCASE LIKE", tableName)
                                .and()
                                .eq("deleted", false);

            Log.d(TAG, queryBuilder.prepareStatementString());

            // Return the first element
            return  this.getDAO().queryForFirst(queryBuilder.prepare());

        } catch (SQLException e) {
            Log.e(TAG, "getTablaVersionByTableName: ***ERROR***", e);
        }

        return null;
    }

    public TablaVersion getAtencionesTablaVersion() throws SQLException {
        PreparedQuery<TablaVersion> preparedQuery = this.getDAO().queryBuilder().where().eq("tabla", "hcd_atencion").prepare();
        return this.getDAO().queryForFirst(preparedQuery);
    }

    public void resetTablaVersion() throws SQLException {
        UpdateBuilder<TablaVersion, Integer> updateBuilder =  this.getDAO().updateBuilder();
        updateBuilder.updateColumnValue("lastVersion", 0);
        this.getDAO().update(updateBuilder.prepare());
    }

}
