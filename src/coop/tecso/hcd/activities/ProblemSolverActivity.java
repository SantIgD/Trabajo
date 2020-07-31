package coop.tecso.hcd.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;

import coop.tecso.hcd.R;
import coop.tecso.hcd.dao.TablaVersionDAO;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.persistence.DatabaseConfigUtil;
import coop.tecso.hcd.persistence.DatabaseHelper;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.udaa.domain.base.AbstractEntity;
import coop.tecso.udaa.domain.base.TablaVersion;

public class ProblemSolverActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_problem_solver);

        Button wipeAllDataButton = findViewById(R.id.delete_all_buttons);
        wipeAllDataButton.setOnClickListener(view -> confirmAndWipeAllData());

        Button deleteAtenciones = findViewById(R.id.delete_im_buttons);
        deleteAtenciones.setOnClickListener(view -> confirmAndDeleteAtenciones());


        this.setResult(Activity.RESULT_CANCELED);

    }

    public static void go(Activity sourceActivity ){
        Intent intent = new Intent(sourceActivity, ProblemSolverActivity.class);
        sourceActivity.startActivityForResult(intent,Constants.PROBLEM_SOLVER_REQUEST_CODE);
    }

    private void confirmAndWipeAllData() {

        int title = R.string.confirm_title;
        int mensaje = R.string.delete_all_confirm_msg;

        AlertDialog alertDialog = GUIHelper.createAlertDialog(this,title,mensaje, this::wipeAllDataAndShutDown,null);
        alertDialog.show();

    }


    private void wipeAllDataAndShutDown(){
        try{
            wipeAllData();
            setResult(RESULT_OK);

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        this.finish();
    }

    private void wipeAllData() throws SQLException {

        for (Class<?> clazz: DatabaseConfigUtil.PERSISTENT_CLASSES) {
            if(!clazz.equals(TablaVersion.class)){
                DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
                RuntimeExceptionDao<AbstractEntity, Integer> entidadBusquedaDAO = databaseHelper.getRuntimeExceptionDao((Class<AbstractEntity>) clazz);

                DeleteBuilder<AbstractEntity, Integer> deleteBuilder = entidadBusquedaDAO.deleteBuilder();
                entidadBusquedaDAO.delete(deleteBuilder.prepare());
            }else{
                TablaVersionDAO tablaVersionDAO = new TablaVersionDAO(this);
                tablaVersionDAO.resetTablaVersion();
            }

        }

    }


    private void confirmAndDeleteAtenciones(){
        int title = R.string.confirm_title;
        int mensaje = R.string.delete_atenciones_msg;

        AlertDialog alertDialog = GUIHelper.createAlertDialog(this,title,mensaje, this::deleteAtenciones,null);
        alertDialog.show();

    }

    private void deleteAtenciones(){
        try{
            for (Class<?> clazz: DatabaseConfigUtil.PERSISTENT_CLASSES) {
                if(!clazz.equals(Atencion.class)) {
                    continue;
                }
                DatabaseHelper databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
                RuntimeExceptionDao<AbstractEntity, Integer> entidadBusquedaDAO = databaseHelper.getRuntimeExceptionDao((Class<AbstractEntity>) clazz);

                DeleteBuilder<AbstractEntity, Integer> deleteBuilder = entidadBusquedaDAO.deleteBuilder();
                entidadBusquedaDAO.delete(deleteBuilder.prepare());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }



}
