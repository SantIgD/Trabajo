package coop.tecso.udaa.activities;

import java.text.SimpleDateFormat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import coop.tecso.udaa.R;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion.App;
import coop.tecso.udaa.domain.aplicaciones.AplicacionSync;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.util.DeviceContext;
import coop.tecso.udaa.ui.MenuSectionAdapter;
import coop.tecso.udaa.ui.SectionedListAdapter;
import coop.tecso.udaa.utils.Constants;

/**
 * Settings activity.
 */
public final class SettingsActivity extends Activity implements OnItemClickListener {

    private static final int DIALOG_EDIT_URL = 1;
    private static final int DIALOG_EDIT_TT = 2; // Transmittion Type
    private static final int DIALOG_EDIT_PREF_GPS = 3;

    // Items para el AlertDialog de selección de transmisión
    final CharSequence[] csTransType = {"Completa", "Parcial"};
    final CharSequence[] csActivateGPS = {"NO", "SI"};

    private ListView mOptionsListView;
    private SectionedListAdapter optionsAdapter;
    private boolean isEdit = true;
    private boolean isReadOnly = false;
    private SharedPreferences myPrefs;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.settings);

        // create our list and custom adapter
        optionsAdapter = new SectionedListAdapter(this);
        UDAADao udaaDao = new UDAADao(SettingsActivity.this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            isEdit = bundle.getBoolean("ISEDIT");
            isReadOnly = bundle.getBoolean("ISREADONLY");
        }

        myPrefs = getSharedPreferences("settings", MODE_PRIVATE);

        MenuSectionAdapter section1Adapter = new MenuSectionAdapter(this);

        if (isReadOnly) {
            section1Adapter.addTextView(myPrefs.getString("URL", "190.210.56.178:8443/Capacitacion"), "Dirección del servidor Web");
        } else {
            section1Adapter.addTextView("Servidor Web: " + myPrefs.getString("URL", ""), "Configure la dirección del servidor web de la aplicación.");
        }

        // Si no es editable significa que el usuario ya se logueó
        if (!isEdit) {
            boolean bTransTypePartial = myPrefs.getBoolean("TTP", false);
            section1Adapter.addTextView("Tipo de Transmisión", bTransTypePartial ? "Parcial" : "Completa");

            boolean bActivateGPS = myPrefs.getBoolean(Constants.GPS_ACTIVATED_PREF, false);
            section1Adapter.addTextView("Recordar activación GPS", bActivateGPS ? "SI" : "NO");
        }

        String defaultGmailAccount = DeviceContext.getEmail(getApplicationContext());

        section1Adapter.addTextView(getApplicationContext().getString(R.string.settings_correo, defaultGmailAccount), getString(R.string.esta_direcci_n_corresponde_a_la_cuenta_de_correo_asociada_al_equipo));

        TelephonyManager tlpmngr = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        String sIMEI = tlpmngr.getDeviceId();
        section1Adapter.addTextView("IMEI: " + sIMEI , getString(R.string.imei_del_equipo_en_uso));

        String sSIM = tlpmngr.getSimSerialNumber();
        section1Adapter.addTextView("SIM: " + sSIM , getString(R.string.sim_device));

        DispositivoMovil dispositivoMovil = udaaDao.getDispositivoMovil(defaultGmailAccount);
        String sGroup = "";
        if(dispositivoMovil != null){
            sGroup = dispositivoMovil.getGrupoDispositivoNombre();
        }
        section1Adapter.addTextView("Grupo: " + sGroup , getString(R.string.group_device));

        String sOS = android.os.Build.VERSION.RELEASE;
        section1Adapter.addTextView("OS: " + sOS, getString(R.string.os_version));

        String sModel = Build.MODEL;
        section1Adapter.addTextView("MOD: " + sModel, getString(R.string.device_model));

        AplicacionSync aplicacionSync = udaaDao.getApplicationSync();

        if (aplicacionSync.getSyncUDDATimeStamp() != null) {
            String dateString = "";
            try {
                dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(aplicacionSync.getSyncUDDATimeStamp());
            } catch (Exception ignored) {}
            section1Adapter.addTextView("Sincronización "+App.UDAA.toString()+": " + dateString, getString(R.string.ult_sincronizacion)+ " " + App.UDAA.toString());
        }
        if (aplicacionSync.getSyncHCTimeStamp() != null) {
            String dateString = "";
            try {
                dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(aplicacionSync.getSyncHCTimeStamp());
            } catch (Exception ignored) {}
            section1Adapter.addTextView("Sincronización "+App.HCDigital.toString()+": " + dateString, getString(R.string.ult_sincronizacion)+ " " + App.HCDigital.toString());
        }
        if (aplicacionSync.getSyncSATimeStamp() != null) {
            String dateString = "";
            try {
                dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(aplicacionSync.getSyncSATimeStamp());
            } catch (Exception ignored) {}

            section1Adapter.addTextView("Sincronización "+App.SADigital.toString()+": " + dateString, getString(R.string.ult_sincronizacion)+ " " + App.SADigital.toString());
        }
        if (aplicacionSync.getSyncCTOTimeStamp() != null) {
            String dateString = "";
            try {
                dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(aplicacionSync.getSyncCTOTimeStamp());
            } catch (Exception ignored) {}

            section1Adapter.addTextView("Sincronización "+App.CTODigital.toString()+": " + dateString, getString(R.string.ult_sincronizacion)+ " " + App.CTODigital.toString());
        }

        optionsAdapter.addSection("Conexión", section1Adapter);

        mOptionsListView = findViewById(R.id.optionsListView);
        mOptionsListView.setAdapter(optionsAdapter);
        mOptionsListView.setOnItemClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long duration) {
        if (position == DIALOG_EDIT_URL && isEdit) {
            this.showEditUrlDialog();
        }

        // Si no es editable significa que aparece el item seleccionar tipo de transmisión
        else if (position == DIALOG_EDIT_TT && !isEdit) {
            this.showTransmisionTypeDialog();
        }

        // Si no es editable significa que aparece el item seleccionar tipo de transmisión
        else if (position == DIALOG_EDIT_PREF_GPS && !isEdit) {
            this.showGPSDialog();
        }
    }

    // MARK: - Internal

    private void showEditUrlDialog() {
        // Inflate views
        LayoutInflater factory = LayoutInflater.from(this);
        View textEntryView = factory.inflate(R.layout.dialog_edit_text, null);

        final EditText editText = textEntryView.findViewById(R.id.dialogedittext);

        String url = myPrefs.getString("URL", "190.210.56.178:8443/Capacitacion");
        url = url.replace("https://", "");
        editText.setText(url);

        Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Servidor Web");
        builder.setView(textEntryView);
        builder.setPositiveButton("Guardar", (dialog, id) -> {
            SharedPreferences.Editor e = myPrefs.edit();
            e.putString("URL", "https://" + editText.getText().toString());
            e.apply();

            recreateActivity();
        });

        Dialog dialog = builder.setView(textEntryView).create();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        dialog.show();
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    private void showTransmisionTypeDialog() {
        boolean bTransTypePartial = myPrefs.getBoolean( "TTP", false );

        AlertDialog.Builder builder = new AlertDialog.Builder( SettingsActivity.this);
        builder.setTitle("Tipo de Transmisión");
        builder.setSingleChoiceItems(csTransType, bTransTypePartial ? 1 : 0, (dialog, which) -> {
            SharedPreferences.Editor e = myPrefs.edit();
            e.putBoolean( "TTP", which == 1 );
            e.apply();
            dialog.dismiss();

            recreateActivity();
        });

        builder.create().show();
    }

    private void showGPSDialog() {
        boolean bActivateGPS = myPrefs.getBoolean( Constants.GPS_ACTIVATED_PREF, false );

        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Recordar activación GPS");
        builder.setSingleChoiceItems(csActivateGPS, bActivateGPS ? 1 : 0, (dialog, which) -> {
            SharedPreferences.Editor e = myPrefs.edit();
            e.putBoolean( Constants.GPS_ACTIVATED_PREF, which == 1 );
            e.apply();
            dialog.dismiss();

            recreateActivity();
        });
        builder.create().show();
    }

    private void recreateActivity() {
        Intent intent = getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

}
