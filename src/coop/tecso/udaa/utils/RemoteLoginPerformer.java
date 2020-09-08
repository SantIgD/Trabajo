package coop.tecso.udaa.utils;


import android.app.AlertDialog;

import coop.tecso.udaa.R;
import coop.tecso.udaa.activities.LoginActivity;
import coop.tecso.udaa.base.UDAAException;
import coop.tecso.udaa.common.WebServiceDAO;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

public class RemoteLoginPerformer {

    private String user;
    private String password;
    private DispositivoMovil dispositivoMovil;
    private LoginActivity loginActivity;
    private boolean doContigencia = false;
    private boolean waitingForResponse = true;
    private ContingenciaData contingenciaData;

    public static String mensaje;
    public static boolean contingencia=false;
    public static String[] tipoMovil;

    public RemoteLoginPerformer(String user, String password, DispositivoMovil dispositivoMovil,LoginActivity loginActivity){
        this.user = user;
        this.password = password;
        this.dispositivoMovil = dispositivoMovil;
        this.loginActivity = loginActivity;
    }

    public UsuarioApm performLogin()throws Exception{
        try {
            int id = 1; // dispositivoMovil.getId();
           return WebServiceDAO.login(user, password, id);
        }
        catch (UDAAException e) {
            if (true){ //isContingenciaError(e)
                String error = e.getError();
                contingenciaData = new ContingenciaData(error);
                performContingencia();
            }
            else{
                throw e;
            }
        }

        return null;
    }

    /**
     * Reconoce si el error que ocurrio corresponde al dominio de contingencia
     */
    private boolean isContingenciaError(UDAAException e){

        return e.getError().contains("Error-116");
    }

    private void performContingencia() throws UDAAException {

        if (contingenciaData.isContingencia()){
            askForContingencia();
        }else{
            throw new UDAAException(contingenciaData.getError());
        }

    }

    private void askForContingencia() throws UDAAException{

        if (true) {
            askForMACMIC();
        } else {
            askForMAD();
        }


    }



    private void askForMACMIC() throws UDAAException{
        askForContingenciaChoice("Desea agregar dispositivo movil a la ambulancia?");

        while (waitingForResponse){
            Helper.sleep(1000);
        }


        if(doContigencia){

            throw  new UDAAException(ErrorConstants.ERROR_115);
        }

    }

    private void askForMAD() throws UDAAException{
        askForContingenciaChoice("Desea intercambiar de dispositivo movil?");


        while (waitingForResponse){

            Helper.sleep(1000);

        }


        if(doContigencia){
            throw  new UDAAException(ErrorConstants.ERROR_114);
        }


    }

    private void askForContingenciaChoice(String mensaje){

        loginActivity.runOnUiThread(() -> {
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(loginActivity);
            alertBuilder.setTitle(contingenciaData.getMensaje());
            alertBuilder.setCancelable(false);
            alertBuilder.setMultiChoiceItems(contingenciaData.getOpciones(), contingenciaData.getChecked(), (dialog, index, isChecked) -> {
                contingenciaData.changeCheckedValue(index,isChecked);
            });
            alertBuilder.setPositiveButton("Aceptar", (dialog, id) -> {
                this.doContigencia = true;
                this.waitingForResponse = false;
            });
            alertBuilder.setNegativeButton("Cancelar", (dialog, id) -> {
                this.doContigencia = false;
                this.waitingForResponse = false;
            });
            alertBuilder.create().show();
        });
    }




}


