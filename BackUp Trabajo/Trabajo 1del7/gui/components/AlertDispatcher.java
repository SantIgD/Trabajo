package coop.tecso.hcd.gui.components;

import android.content.Context;
import coop.tecso.hcd.gui.utils.EvalEvent;

@SuppressWarnings("unused")
public interface AlertDispatcher {
	/**
	 * Se llama durante la inicializacion del form.
	 */
	void onInit(PerfilGUI perfilGUI, Context context);
	
	void onFinish();
	
	void eval(CampoGUI campoGUI, EvalEvent evalEvent);
	
	void evalAll(PerfilGUI perfilGUI);
	
	String parseNavigationURI(String val);
	
	boolean evalAllCierre(PerfilGUI perfilGUI);	// 02-12-2013 - R. Iván G. Vesco	
}
