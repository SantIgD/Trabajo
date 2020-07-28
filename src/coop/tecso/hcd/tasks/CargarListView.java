package coop.tecso.hcd.tasks;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.util.List;

import coop.tecso.hcd.activities.MainHCActivity;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.helpers.SearchPageAdapter;

@SuppressLint("StaticFieldLeak")
public class CargarListView extends AsyncTask<Void, Void, List<Atencion>> {

    private MainHCActivity mainHCActivity;
    private HCDigitalApplication appState;
    private ProgressDialog dialog;

    // MARK: - Init

    public CargarListView(MainHCActivity mainHCActivity) {
        this.mainHCActivity = mainHCActivity;
        this.appState = HCDigitalApplication.getApplication(mainHCActivity);
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        this.dialog = new ProgressDialog(mainHCActivity);
        this.dialog.setMessage("Cargando Lista");
        this.dialog.setCancelable(true);
        this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        this.dialog.show();
    }

    @Override
    protected List<Atencion> doInBackground(Void... arg0) {
        try {
            Thread.sleep(1000);
            return appState.getHCDigitalDAO().getListAtencion();
        } catch (InterruptedException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Atencion> result) {
        super.onPostExecute(result);

        if (result != null) {
            SearchPageAdapter adapter = new SearchPageAdapter(mainHCActivity, result);
            mainHCActivity.setListAdapter(adapter);
        }

        this.dialog.dismiss();
    }

}
