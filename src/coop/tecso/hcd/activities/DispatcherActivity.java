package coop.tecso.hcd.activities;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Despachador;
import coop.tecso.hcd.utils.Constants;

/**
 *
 * @author tecso.coop
 *
 */
public final class DispatcherActivity extends Activity {
	
	protected final String LOG_TAG = getClass().getSimpleName();

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		HCDigitalApplication appState = (HCDigitalApplication) getApplicationContext();

		setContentView(R.layout.dispatcher_detail);

		ImageView imageView = findViewById(R.id.imageView);
		TextView textView = findViewById(R.id.textView);

		Bundle bundle = getIntent().getExtras();
		int idDespachador = bundle.getInt(Constants.ENTITY_ID);

		Despachador despachador = appState.getHCDigitalDAO().getDespachadorById(idDespachador);

		if (despachador != null) {
			// obtengo la imagen
			byte[] byteArray = despachador.getFoto();
			if (byteArray != null) {
				Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
				imageView.setImageBitmap(bmp);
			}

			textView.setText(despachador.getInformacionPersonal());
		}
    }
}