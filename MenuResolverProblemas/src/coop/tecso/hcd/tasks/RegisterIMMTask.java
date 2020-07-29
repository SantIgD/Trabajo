package coop.tecso.hcd.tasks;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TabHost;

import java.util.ArrayList;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.RegisterActivity;
import coop.tecso.hcd.activities.TabHostActivity;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.dao.HCDigitalDAO;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.EstadoAtencion;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.integration.UDAACoreServiceImpl;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ParamHelper;

/**
 * Registrar Informe de Atencion Medica Multiple
 */
public class RegisterIMMTask extends AsyncTask<Void, CharSequence, Atencion> {

    private static final String TAG = RegisterIMMTask.class.getSimpleName();

    private static final int TIPO_REG_ERR_DES = 2;

    // MARK: - Data

    @SuppressLint("StaticFieldLeak")
    private TabHostActivity tabHostActivity;

    private ProgressDialog dialog;

    private UDAACoreServiceImpl localService;

    private HCDigitalApplication appState;

    // MARK: - Init

    public RegisterIMMTask(TabHostActivity tabHostActivity) {
        this.tabHostActivity = tabHostActivity;
    }

    // MARK: - Life Cycle

    @Override
    protected void onPreExecute() {
        String message = tabHostActivity.getString(R.string.loading_msg);
        this.dialog = ProgressDialog.show(tabHostActivity, "", message, true);
        this.localService = new UDAACoreServiceImpl(tabHostActivity);
        this.appState = (HCDigitalApplication) tabHostActivity.getApplicationContext();
    }

    @Override
    protected Atencion doInBackground(Void... params) {
        try {
            Thread.sleep(1000);
            // Obtiene nuevas atenciones asignadas desde el servidor
            // (sincroniza atencion y su lista de atencionValor)
            Integer dispositivoMovilID = appState.getDispositivoMovil().getId();
            localService.synchronizeAtencion(dispositivoMovilID);

            // Se toman los id cargados en pestañas para ignorarlos al
            // obtener un nuevo IM para abrir
            List<Integer> skipAtencionIdList = new ArrayList<>(tabHostActivity.listIdAtencionInTabHost);

            // Si se obtuvo una nueva atencion se obtiene para preparar el
            // IM
            HCDigitalDAO hcDigitalDAO = appState.getHCDigitalDAO();
            return hcDigitalDAO.getAtencionEnPreparacion(skipAtencionIdList);
        } catch (Exception e) {
            try {
                // Informar a la Udaa Error al descargar IMD
                localService.sendError(TIPO_REG_ERR_DES);
            } catch (Exception err) {
                Log.d(TAG, "Error in sendError service : ", err);
            }
            Log.d(TAG, "Error: ", e);
            return null;
        }
    }

    @Override
    protected void onProgressUpdate(CharSequence... values) {
        dialog.setMessage(values[0]);
    }

    @Override
    protected void onPostExecute(Atencion atencion) {
        if (tabHostActivity.isFinishing()) {
            return;
        }

        GUIHelper.dismissDialog(dialog);

        if (atencion != null) {
            this.openExistingIMMAtention(atencion);
        } else {
            this.openDefaultIMMAttention();
        }
    }

    // MARK: - Internal

    private void openExistingIMMAtention(Atencion atencion) {
        TabHost tabHost = tabHostActivity.getTabHost();
        int newIndex = tabHost.getTabWidget().getChildCount();
        String tabTag = String.valueOf(atencion.getId());

        tabHostActivity.listIdAtencionInTabHost.add(atencion.getId());

        Bundle bundle = new Bundle();
        bundle.putString(Constants.ACTION, Constants.ACTION_CREATE_IMM);
        bundle.putInt(Constants.ESTADO_IM, EstadoAtencion.ID_EN_PREPARACION);
        bundle.putInt(Constants.ENTITY_ID, atencion.getId());

        Intent intent = new Intent(tabHost.getContext(), RegisterActivity.class);
        intent.putExtras(bundle);

        String indicator = "Nro: "+ String.valueOf(atencion.getNumeroAtencion());
        Drawable icon = tabHostActivity.getResources().getDrawable(R.drawable.ic_menu_compose);

        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tabTag);
        tabSpec.setIndicator(indicator, icon).setContent(intent);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTabByTag(tabTag);

        Log.i(TAG, "buildTabHost: addingTab , tabTag=" + tabTag
                + " , indicator=" + indicator);
        tabHostActivity.mTabTag.put(newIndex, tabTag);
    }

    private void openDefaultIMMAttention() {
        AlertDialog.Builder builder = new AlertDialog.Builder(tabHostActivity);
        builder.setTitle(R.string.register_IM_default_title);
        builder.setMessage(R.string.register_IM_default_warning);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.yes, (dialog, id) -> createNewIMMAttention());
        builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.cancel());

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void createNewIMMAttention() {
        TabHost tabHost = tabHostActivity.getTabHost();
        int newIndex = tabHost.getTabWidget().getChildCount();
        String tabTag = Constants.TAB_TAG + newIndex;

        String indicator = "Nuevo IM";
        Drawable icon = tabHostActivity.getResources().getDrawable(R.drawable.ic_menu_compose);

        Intent intent = new Intent(tabHost.getContext(), RegisterActivity.class);
        TabHost.TabSpec tabSpec = tabHost.newTabSpec(tabTag);
        tabSpec.setIndicator(indicator, icon);
        tabSpec.setContent(intent);

        tabHost.addTab(tabSpec);

        // JIRA HCDDM-156: HCDigital (app.) -
        // Modificación a IMD múltiples
        Bundle bundle = new Bundle();
        bundle.putString(Constants.ACTION, Constants.ACTION_CREATE_IMM);
        bundle.putInt(Constants.ESTADO_IM, EstadoAtencion.ID_EN_PREPARACION);
        bundle.putInt(Constants.ENTITY_ID, 0);

        RegisterActivity mainActivity = (RegisterActivity) tabHostActivity.getLocalActivityManager().getActivity(tabHostActivity.MAIN_TAB_TAG);
        List<Value> listValue = mainActivity.getForm().values();

        String paramDomicilios = ParamHelper.getString(ParamHelper.ATENCION_CAMPOS_DOMICILIO, null);
        List<Integer> listaIdDomicilio = new ArrayList<>();

        if (paramDomicilios != null) {
            String[] paramDomiciliosArray = paramDomicilios.split("&");
            for (String paramDomicilio : paramDomiciliosArray) {
                try {
                    int paramDomicilioCampo = Integer.parseInt(paramDomicilio);
                    listaIdDomicilio.add(paramDomicilioCampo);
                } catch (Exception ignore) {}
            }
        }

        for (Value value : listValue) {
            for (Integer domicilioId : listaIdDomicilio) {
                if (value.getCampo().getId() == domicilioId) {
                    bundle.putString(Constants.DOMICILIO_IM, value.getValor());
                    break;
                }
            }
        }

        intent.putExtras(bundle);
        tabHost.setCurrentTabByTag(tabTag);

        Log.i(TAG,"buildTabHost: addingTab , tabTag="+ tabTag+ " , indicator="+ indicator);
        tabHostActivity.mTabTag.put(newIndex, tabTag);
    }

}