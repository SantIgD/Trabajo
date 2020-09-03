package coop.tecso.udaa.utils;

import android.content.Context;
import android.text.TextUtils;

import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;

public class Helper {

    public static boolean isAmbulancia(DispositivoMovil dispositivoMovil) {
        String valorTipoMovil = dispositivoMovil.getAsignadoA();
        return TextUtils.equals(valorTipoMovil, "MOVIL");
    }

    public static boolean isContingencia(DispositivoMovil dispositivoMovil) {
        //return dispositivoMovil.getContingencia();
        return true;
    }
}
