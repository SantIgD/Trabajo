package coop.tecso.udaa.base;

import android.content.Context;
import android.util.Log;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.common.WebServiceDAO;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

public final class UDAAManager {

	private Context mContext;
	private static String LOG_TAG = UDAAManager.class.getSimpleName();
	
	public UDAAManager(Context context) {
		this.mContext = context;
	}

	public UsuarioApm getUsuarioApm(String username, String password) {
		UDAADao udaaDAO = new UDAADao(mContext);
		
		UsuarioApm usuarioApm = udaaDAO.getUsuarioApmByUserName(username);
		try{
			// Se verifica si existe el usuario en la db local
			if(null == usuarioApm){
				UdaaApplication appState = (UdaaApplication) mContext.getApplicationContext();
				// Si no existe se consulta a traves del WS
				usuarioApm = WebServiceDAO.login(username, password, appState.getDispositivoMovil().getId());	 
				// Guardo el usuario localmente
				udaaDAO.createOrUpdateUsuarioApm(usuarioApm);
			}else{
				// Si existe se verifica el password 
				if(!password.equals(usuarioApm.getPassword())){
					// Si la validacion de password es incorrecta se devuelve null
					usuarioApm = null;
				}
			}
		} catch (Exception e) { 
			Log.e(LOG_TAG, "getUsuarioApm: **ERROR**", e);
		}	
		
		return usuarioApm;
	}

    public UsuarioApm getUsuarioApm(int id) {
        UDAADao udaaDAO = new UDAADao( mContext );
		return udaaDAO.getUsuarioApmById( id );
    }

}