package coop.tecso.udaa.utils;


import android.app.AlertDialog;

import coop.tecso.udaa.R;
import coop.tecso.udaa.activities.LoginActivity;
import coop.tecso.udaa.base.UDAAException;
import coop.tecso.udaa.common.GUIHelper;
import coop.tecso.udaa.common.WebServiceDAO;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;
import coop.tecso.udaa.tasks.InitializeAplTask;

public class RemoteLoginPerformer {

    private String user;
    private String password;
    private DispositivoMovil dispositivoMovil;

    public RemoteLoginPerformer(String user, String password, DispositivoMovil dispositivoMovil){
        this.user = user;
        this.password = password;
        this.dispositivoMovil = dispositivoMovil;
    }

    public UsuarioApm performLogin()throws Exception{
        try {
            int id = dispositivoMovil.getId();
           return WebServiceDAO.login(user, password, 1);
        }
        catch (UDAAException e) {
            if (isDispositivoMovilIDError(e)){
                performContingencia();
            }
            else{
                throw e;
            }
        }

        return null;
    }

    private boolean isDispositivoMovilIDError(UDAAException e){

        //verificar el tipo de error que obtendremos.

        return true;
    }

    private void performContingencia() throws Exception {

        if (Helper.isContingencia(dispositivoMovil)){
            // webservice
            // Preguntar si quiere chuculum. Contingencia o tabla
            showDispositivoMovilChangeDialog();

        }

    }

    private void showDispositivoMovilChangeDialog() throws UDAAException{


        if (Helper.isAmbulancia(dispositivoMovil)) {
            throw  new UDAAException(ErrorConstants.ERROR_115);
        } else {
            throw  new UDAAException(ErrorConstants.ERROR_114);
        }


    }



}


