package coop.tecso.hcd.tasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.MainHCActivity;
import coop.tecso.hcd.activities.ProblemSolverActivity;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.SearchPageAdapter;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;



public class WipeSyncDataTask extends AsyncTask<Void,Void,Void> {

    private ProgressDialog dialog;
    private MainHCActivity mainHCActivity;
    private HCDigitalApplication appState;
    private UDAACoreServiceImpl localService;

    public WipeSyncDataTask(MainHCActivity mainHCActivity){
        this.mainHCActivity = mainHCActivity;
        this.appState = HCDigitalApplication.getApplication(mainHCActivity);
        this.localService = new UDAACoreServiceImpl(mainHCActivity);

    }


    @Override
    protected void onPreExecute() {
        this.dialog = ProgressDialog.show(mainHCActivity, "", mainHCActivity.getString(R.string.loading_msg), true);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        ProblemSolverActivity.go(mainHCActivity);
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (IllegalArgumentException ignore) {}
        return null;
    }

    protected void onPostExecute() {
        BuildSearchPageTask task = new BuildSearchPageTask(mainHCActivity);
        task.execute();
    }

}