package coop.tecso.udaa.utils;

import android.text.TextUtils;

import coop.tecso.udaa.domain.seguridad.DispositivoMovil;

public class Helper {

    public static boolean isAmbulancia(DispositivoMovil dispositivoMovil) {
        String valorTipoMovil = dispositivoMovil.getAsignadoA();
        return TextUtils.equals(valorTipoMovil, "MOVIL");
    }

    public static boolean isContingencia(DispositivoMovil dispositivoMovil) {
        //return dispositivoMovil.getContingencia();
        return false;
    }

    public static void sleep(int milis){

            try {
                Thread.sleep(milis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

    }
}
