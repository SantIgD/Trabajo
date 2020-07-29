package coop.tecso.hcd.helpers;

import java.util.Date;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.Atencion;
import coop.tecso.hcd.entities.Despachador;
import coop.tecso.hcd.entities.EstadoAtencion;

import android.util.TypedValue;

@SuppressLint("SetTextI18n")
public final class SearchPageAdapter extends ArrayAdapter<Atencion> {
	protected Activity context;
	private List<Atencion> atencionList;

	static class ViewHolder {
		TextView  fechaAtencionTextView;
		TextView  nroAtencionTextView;
		TextView  domicilioTextView;
		TextView  codigoTextView;
		ImageView estadoAtencionImageView;
		TextView  estadoAtencionTextView;
		TextView  syncronizedTextView;
        ImageView dispatcherImageView;
	}
	public SearchPageAdapter(Activity context, List<Atencion> atencionList) {
		super(context, R.layout.fila_im, atencionList);
		this.context = context;
		this.atencionList = atencionList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		HCDigitalApplication appState = (HCDigitalApplication) context.getApplicationContext();

		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(R.layout.fila_im, null);
			ViewHolder viewHolder = new ViewHolder();
			viewHolder.estadoAtencionImageView = rowView.findViewById(R.id.estadoAtencionImage);
			viewHolder.estadoAtencionTextView = rowView.findViewById(R.id.estadoAtencionText);
			viewHolder.fechaAtencionTextView = rowView.findViewById(R.id.fechaAtencionText);
			viewHolder.nroAtencionTextView = rowView.findViewById(R.id.nroAtencionText);
			viewHolder.domicilioTextView = rowView.findViewById(R.id.domicilioText);
			viewHolder.codigoTextView = rowView.findViewById(R.id.codigoText);
			viewHolder.syncronizedTextView = rowView.findViewById(R.id.syncronizedText);
            viewHolder.dispatcherImageView = rowView.findViewById(R.id.dispatcherImageView);
			rowView.setTag(viewHolder);
		}
 
		ViewHolder holder = (ViewHolder) rowView.getTag();
		Atencion atencion = atencionList.get(position);
		
		holder.fechaAtencionTextView.setText((atencion.getFechaAtencion()!=null?formatDate(atencion.getFechaAtencion()):"-"));
		holder.nroAtencionTextView.setText("Número: "+(atencion.getNumeroAtencion()!=null?atencion.getNumeroAtencion():"-"));	
		holder.domicilioTextView.setText((atencion.getDomicilioAtencion()!=null?atencion.getDomicilioAtencion():"-"));   	
		holder.codigoTextView.setText("Código: "+(atencion.getCodigoAtencion()!=null?atencion.getCodigoAtencion():"-"));
		holder.estadoAtencionTextView.setText(atencion.getEstadoAtencion().getDescripcion());
		
		if(atencion.isDeleted()) {
			holder.syncronizedTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
			holder.syncronizedTextView.setTypeface(null, Typeface.NORMAL);
			holder.syncronizedTextView.setTextColor(Color.DKGRAY);
			holder.syncronizedTextView.setText(R.string.synchronized_item_label);
		}
		else {
			holder.syncronizedTextView.setText(R.string.unsynchronized_item_label);
			if (EstadoAtencion.ID_CERRADA_DEFINITIVA == atencion.getEstadoAtencion().getId() ||
					EstadoAtencion.ID_ANULADA == atencion.getEstadoAtencion().getId()) {
				holder.syncronizedTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
				holder.syncronizedTextView.setTypeface(null, Typeface.BOLD);
				holder.syncronizedTextView.setTextColor(Color.RED);
			}
			else {
				holder.syncronizedTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
				holder.syncronizedTextView.setTypeface(null, Typeface.NORMAL);
				holder.syncronizedTextView.setTextColor(Color.DKGRAY);
			}
		}
		
		// Cambiamos el icono segun el estado de la atencion
		if (EstadoAtencion.ID_EN_PREPARACION == atencion.getEstadoAtencion().getId()) {
			holder.estadoAtencionImageView.setImageResource(R.drawable.presence_online);
		} else if (EstadoAtencion.ID_CERRADA_PROVISORIA == atencion.getEstadoAtencion().getId()) {
			holder.estadoAtencionImageView.setImageResource(R.drawable.presence_away);
		} else if (EstadoAtencion.ID_ANULADA == atencion.getEstadoAtencion().getId()) {
			holder.estadoAtencionImageView.setImageResource(R.drawable.presence_busy);
		} else {
			holder.estadoAtencionImageView.setImageResource(R.drawable.ic_lock_lock);
		}

        // Imagen del Despachador
        // Si la atención viene con detalle del despachador, se verá la imagen; de lo contrario
        // se inhabilitará el objeto
        // Reemplazar por, por ejemplo:
        // atencion.getDispatcher() != null
        if (atencion.getDespachador() != null && atencion.getDespachador().getId() != 0) {
        	Despachador desp = appState.getHCDigitalDAO().getDespachadorById(atencion.getDespachador().getId());
            // obtengo la imagen
            byte[] byteArray = desp.getFoto();
            if (byteArray != null) {
                Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                holder.dispatcherImageView.setImageBitmap(bmp);
            }
            else {
                // imagen no disponible
                holder.dispatcherImageView.setImageResource(R.drawable.no_disponible);
            }
            holder.dispatcherImageView.setId(desp.getId());
            holder.dispatcherImageView.setEnabled(true);
            holder.dispatcherImageView.setVisibility( View.VISIBLE);
        }
        else  {
            holder.dispatcherImageView.setEnabled(false);
            holder.dispatcherImageView.setVisibility( View.INVISIBLE);
        }
		
		return rowView;
	}

	/**
	 * Formatea una fecha y hora en funcion del dia. Si la fecha es del dia actual sólo muestra la hora. En caso contrario sólo la fecha.
	 * Si la fecha es null devuelve un string vacio.
	 */
	private String formatDate(Date fecha){
		try {
			return "Fecha: " + DateFormat.format("dd/MM/yyyy", fecha).toString();
		} catch (Exception e) {
			return "Fecha: ";
		}
	}

}
