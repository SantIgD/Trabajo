package coop.tecso.hcd.dao;

import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.table.TableInfo;

import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.persistence.DatabaseHelper;
import coop.tecso.udaa.domain.base.AbstractEntity;

public abstract class GenericDAO<T extends AbstractEntity> {

    protected final String LOG_TAG = getDomainClass().getSimpleName();
    protected Context context;
    protected HCDigitalApplication appState;

    private Class<T> domainClass = getDomainClass();
    private OrmLiteSqliteOpenHelper databaseHelper;

    /**
     * Constructor
     */
    public GenericDAO(Context context) {
        this.context = context;
        this.databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        this.appState = HCDigitalApplication.getApplication(context);
    }

    public T findById(int id) {
        return getDAO().queryForId(id);
    }

    public int delete(T entity) {
        return getDAO().delete(entity);
    }

    public int delete(Collection<T> entities) {
        return getDAO().delete(entities);
    }

    public int delete(PreparedDelete<T> preparedDelete) {
        return getDAO().delete(preparedDelete);
    }

    public int refresh(T entity) {
        return getDAO().refresh(entity);
    }

    public boolean idExists(int id) {
        return getDAO().idExists(id);
    }

    public int create(T entity) {
        // Data for audit
        entity.setModificationTimeStamp(new Date());
        entity.setModificationUser(appState.getCurrentUser().getNombre());
        entity.setDeleted(false);
        return getDAO().create(entity);
    }

    public int update(T entity) {
        // Data for audit
        entity.setModificationTimeStamp(new Date());
        entity.setModificationUser(appState.getCurrentUser().getNombre());
        return getDAO().update(entity);
    }

    /**
     * Retrieves all active entities ("deleted" is false).
     */
    public List<T> findAllActive(){
        // Filters
        Map<String,Object> mFilter = new HashMap<>();
        mFilter.put("deleted", false);

        return getDAO().queryForFieldValuesArgs(mFilter);
    }

    /**
     * Retrieves all inactive entities ("deleted" is true).
     */
    List<T> findAllInactive(){
        // Filters
        Map<String,Object> mFilter = new HashMap<>();
        mFilter.put("deleted", true);

        return getDAO().queryForFieldValuesArgs(mFilter);
    }

    protected RuntimeExceptionDao<T, Integer> getDAO(){
        return databaseHelper.getRuntimeExceptionDao(domainClass);
    }

    /**
     * Method to return the class of the domain object
     */
    @SuppressWarnings("unchecked")
    private Class<T> getDomainClass() {
        if (null == domainClass) {
            ParameterizedType thisType = (ParameterizedType) getClass()
                    .getGenericSuperclass();
            domainClass = (Class<T>) thisType.getActualTypeArguments()[0];
        }
        return domainClass;
    }

}