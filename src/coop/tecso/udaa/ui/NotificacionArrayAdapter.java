package coop.tecso.udaa.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import coop.tecso.udaa.R;
import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.domain.notificaciones.Notificacion;

public final class NotificacionArrayAdapter extends ArrayAdapter<Notificacion> {

	private static final String LOG_TAG = NotificacionArrayAdapter.class.getSimpleName();
		
	private Context context;

	private List<Notificacion> data;

	public NotificacionArrayAdapter(Context context, int textViewResourceId, List<Notificacion> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.data = objects;
	}

	public int getCount() {
		return this.data.size();
	}

	public Notificacion getItem(int index) {
		return this.data.get(index);
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		if (row == null) {
			// ROW INFLATION
			Log.d(LOG_TAG, "Starting XML Row Inflation ... ");
			LayoutInflater inflater = (LayoutInflater) 
				getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.list_item_notificacion, parent, false);
			Log.d(LOG_TAG, "Successfully completed XML Row Inflation!");
		}

		// Get item
		Notificacion notificacion = getItem(position);

		// Get reference to ImageView
		ImageView iconView = row.findViewById(R.id.notificacion_icon);

		try {
			UdaaApplication udaaApplication = (UdaaApplication) context.getApplicationContext();
			String imgFilePath = udaaApplication.getImagesDir() +
                    notificacion.getTipoNotificacion().getUbicacionIcono();
			Bitmap bitmap = BitmapFactory.decodeStream(this.context.getResources().getAssets().open(imgFilePath));
			iconView.setImageBitmap(bitmap);
		} catch (IOException | NullPointerException e) {
			iconView.setImageResource(android.R.drawable.ic_dialog_info);
			e.printStackTrace();
		}

		// Get reference to TextViews
		TextView descripcionReducidaTextView = row.findViewById(R.id.notificacion_descripcionReducida);
		descripcionReducidaTextView.setText(notificacion.getDescripcionReducida());

		TextView descripcionAmpliadaTextView = row.findViewById(R.id.notificacion_descripcionAmpliada);
		descripcionAmpliadaTextView.setText(notificacion.getDescripcionAmpliada());
		return row;
	}
}