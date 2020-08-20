package coop.tecso.udaa.activities;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import coop.tecso.udaa.R;
import coop.tecso.udaa.common.UDAADao;
import coop.tecso.udaa.domain.notificaciones.Notificacion;

/**
 * Detalle de Notificacion.
 * 
 *  @author Tecso Coop. Ltda.
 */
public final class NotificacionDetailActivity extends Activity {

	public static final String PARAM_NOTIFICACION_ID = "PARAM_NOTIFICACION_ID";
	
	@Override
	public void onCreate(Bundle icicle) {
		Log.d(LOG_TAG, "onCreate init...");		
		super.onCreate(icicle);
		
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout detail = (LinearLayout) inflater.inflate(R.layout.notificacion_detail, null , false);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			Integer notificacionID = extras.getInt(PARAM_NOTIFICACION_ID);
			
			UDAADao dao = new UDAADao(this);
			Notificacion notif = dao.getNotificacionById(notificacionID);
			
			TextView descripcionReducidaLbl = detail.findViewById(R.id.detail_descripcionReducidaLabel);
			descripcionReducidaLbl.setText("Descripción reducida: ");
			
			TextView descripcionReducida = detail.findViewById(R.id.detail_descripcionReducida);
			descripcionReducida.setText(notif.getDescripcionReducida());
			
			TextView descripcionAmpliadaLbl = detail.findViewById(R.id.detail_descripcionAmpliadaLabel);
			descripcionAmpliadaLbl.setText("Descripción ampliada: ");
			
			TextView descripcionAmpliada = detail.findViewById(R.id.detail_descripcionAmpliada);
			descripcionAmpliada.setText(notif.getDescripcionAmpliada());
			
			Button confirmButton = detail.findViewById(R.id.detail_button);
			confirmButton.setOnClickListener(v -> NotificacionDetailActivity.this.finish());
		}
		
		setContentView(detail);
	}
	
	// Implementation helpers

	private static final String LOG_TAG = NotificacionDetailActivity.class.getSimpleName();

}
