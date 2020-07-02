package coop.tecso.hcd.gui.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.gesture.Gesture;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coop.tecso.hcd.R;
import coop.tecso.hcd.activities.SignActivity;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.hcd.gui.utils.Utils;
import coop.tecso.hcd.helpers.GUIHelper;
import coop.tecso.hcd.utils.CollectionUtils;
import coop.tecso.hcd.utils.Constants;
import coop.tecso.hcd.utils.ParamHelper;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;

/**
 * 
 * @author tecso.coop
 *
 */
public final class SignGUI extends CampoGUI {
	
	private final String LOG_TAG = SignGUI.class.getSimpleName();

	private LinearLayout mainLayout;
	private ImageButton  btnSign;
	private ImageView 	 mImageView;
	private float escala;	
	private byte[] data;
	private boolean isValid = true;

	public SignGUI(Context context, boolean enabled) {
		super(context, enabled);
	}

	// Metodos
	@Override
	public View build() {
		String label = this.getEtiqueta() + ": ";

		// Etiqueta
		this.label = new TextView(context);
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(label);
		this.mImageView = new ImageView(context);
		
		// Se define un LinearLayout para ubicar: 'Label / EditText'
		this.mainLayout = new LinearLayout(context);
		this.mainLayout.setOrientation(LinearLayout.VERTICAL);
		this.mImageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		this.mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.mainLayout.setGravity(Gravity.CENTER_VERTICAL);
		
		// Attached image
		List<Value> initialValues = this.getInitialValues();
		if(!CollectionUtils.isEmpty(initialValues)){
			Value initialValue = initialValues.get(0);
			try {
				// Decode image in BASE64
				data = initialValue.getImagen();
				
			    if (data != null && data.length > 0) {
					Options options = new BitmapFactory.Options();
				    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				    options.inDither = false;
					
				    byte[] thumb = initialValue.getThumbnail();
				    if(thumb != null){
				    	Bitmap bitmap = Utils.getBitmapWitheBackground(BitmapFactory.decodeByteArray(thumb, 0, thumb.length, options));
				    	mImageView.setImageBitmap(bitmap);
				    }
			    } else {
					this.mImageView.setImageResource(R.drawable.no_signdig);
			    }
				
				
			} catch (Exception e) {
				// unexpected conversion
				this.mImageView.setImageResource(R.drawable.no_signdig);
			}

		} else {
			this.mImageView.setImageResource(R.drawable.no_signdig);
		}

		this.mImageView.setPadding(5, 0, 5, 0);
		this.btnSign = new ImageButton(context);
		this.btnSign.setBackgroundResource(android.R.drawable.ic_menu_edit);
		this.btnSign.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.btnSign.setOnClickListener(v -> {
            // Marco el campo como selected asi el activity sabe cual adjunto refrescar
            perfilGUI.setCampoGUISelected(SignGUI.this);
            String key = getEntity().getId()+"|"+getPerfilGUI().getBussEntity().getId();

            Intent intent = new Intent(context, SignActivity.class);
            intent.putExtra(Constants.GESTURE_ID, key);
            intent.putExtra(Constants.GESTURE_TITLE, getEtiqueta());
            intent.putExtra(Constants.GESTURE_VALID, isValid);
            ((Activity) context).startActivityForResult(intent, Constants.REQUEST_NEW_GESTURE);
        });
		this.btnSign.setEnabled(enabled);
		this.btnSign.setFocusable(enabled);

		LinearLayout linearBtnSign = new LinearLayout(context);
		linearBtnSign.setGravity(Gravity.END);
		linearBtnSign.setPadding(25, 0, 0, 0);
		linearBtnSign.addView(this.btnSign);
		
		
		LinearLayout component = new LinearLayout(context);
		component.setGravity(Gravity.CENTER);
		component.addView(this.mImageView);
		component.addView(linearBtnSign);

		this.mainLayout.addView(this.label);
		this.mainLayout.addView(component);
		
		this.view = mainLayout;

		return this.view;
	}

	@Override
	public View redraw() {
		this.btnSign.setEnabled(enabled);
		this.btnSign.setFocusable(enabled);
		return this.view;
	}

	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();

		AplPerfilSeccionCampo campo = null;
		AplPerfilSeccionCampoValor campoValor = null;
		AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;
		if(this.entity instanceof AplPerfilSeccionCampo){
			campo = (AplPerfilSeccionCampo) this.entity;
		}else if(this.entity instanceof AplPerfilSeccionCampoValor){
			campoValor = (AplPerfilSeccionCampoValor) this.entity;
			campo = campoValor.getAplPerfilSeccionCampo();
		}else if(this.entity instanceof AplPerfilSeccionCampoValorOpcion){
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) this.entity;
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}
		String nombreCampo = campo!=null?campo.getCampo().getEtiqueta():"No identificado";
		String valor = "";

		Log.d(LOG_TAG,"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
				+" idCampo: "+(campo!=null?campo.getId():"null")
				+", idCampoValor: "+(campoValor!=null?campoValor.getId():"null")
				+", idCampoValorOpcion: "+(campoValorOpcion!=null?campoValorOpcion.getId():"null")
				+", Valor: "+valor);

		Value value = new Value(campo,campoValor,campoValorOpcion,valor, null, data);
		
		mImageView.buildDrawingCache();
		Bitmap thumb = mImageView.getDrawingCache();
		if (thumb == null && data != null && data.length > 0) {

			byte[] thumbData = this.getInitialValues().get(0).getThumbnail();
			if (thumbData != null && thumbData.length > 0) {
				Options options = new  BitmapFactory.Options();
			    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			    options.inDither = false;
				thumb = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, options);
			}
		}
		if(thumb != null){
			thumb = getBitmapWitheBackground(thumb);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			thumb.compress(Bitmap.CompressFormat.JPEG, 85, outStream);		
			value.setThumbnail(outStream.toByteArray());
		}
		
		this.values.add(value);

		return this.values;
	}

	@Override
	public boolean isDirty(){
		boolean dirty;
		byte[] currentValue = data;
		List<Value> initialValues = this.getInitialValues();
		if (!CollectionUtils.isEmpty(initialValues)) {
			byte[] initialValue = initialValues.get(0).getImagen();
			dirty = !Arrays.equals(currentValue, initialValue);
		} else {
			dirty = currentValue != null;
		}

		this.dirty = currentValue != null && enabled;
		if(dirty){
			Log.d(LOG_TAG, String.format("%s isDirty: true", getEtiqueta()));
		}
		return dirty;
	}

	@Override
	public boolean validate() {
		if(isObligatorio() && (data == null || data.length == 0)){
			GUIHelper.showError(context, context.getString(R.string.field_required, getEtiqueta()));
			mImageView.requestFocus();
			
			return false;
		}
		return true;
	} 
	
	private void setImageBitmap(Bitmap imageBitmap) {
		Bitmap bitmap = getBitmapWitheBackground(imageBitmap);
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
		this.data = outStream.toByteArray();

		if (imageBitmap != null) {
			imageBitmap.recycle();
		}

		evalCondicionalSoloLectura();
	}

	private Bitmap getBitmapWitheBackground(Bitmap imageBitmap) {
		Bitmap bitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(imageBitmap, 0, 0, null);
		return bitmap;
	}
	
	public void setImages(Gesture gesture){
		Resources resources = context.getResources();
		//thumbnail
		int inset = (int) resources.getDimension(R.dimen.gesture_thumbnail_inset);
		int size = (int) resources.getDimension(R.dimen.gesture_thumbnail_size);
		mImageView.setImageBitmap(gesture.toBitmap(size, size, inset, Color.BLACK));
		
		if(escala == 0){
			mImageView.setDrawingCacheEnabled(true);       
			mImageView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
			            	MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

			mImageView.buildDrawingCache(true);
			mImageView.buildDrawingCache();
			setImageBitmap(mImageView.getDrawingCache());
			mImageView.setDrawingCacheEnabled(false);
		} else {
			String[] maxResolutions = ParamHelper.getString("signMaxResolution", resources.getDisplayMetrics().widthPixels + "x" + resources.getDisplayMetrics().heightPixels).split("x");
			
			boolean isPortrait = resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
			if(isPortrait && Integer.parseInt(maxResolutions[0]) > Integer.parseInt(maxResolutions[1])){
				//cambio el alto por el ancho
				String aux = maxResolutions[0];
				maxResolutions[0] = maxResolutions[1];
				maxResolutions[1] = aux;
			}
			
			//image
			int w = (int) (Integer.parseInt(maxResolutions[0]) * escala);
			int h = (int) (Integer.parseInt(maxResolutions[1]) * escala);
			
			setImageBitmap(gesture.toBitmap(w, h, inset, Color.BLACK));
		}
		
	}
	
	public void invalidate() {
		isValid = false;
		this.mImageView.setImageResource(R.drawable.no_signdig);
		this.data = new byte[]{};
	}

	@Override
	public void removeAllViewsForMainLayout(){
		this.mainLayout.removeAllViews();
	}

	public void setEscala(float escala) {
		this.escala = escala;
	}

	@Override
	public void clearData() {
		this.mImageView.setImageResource(R.drawable.no_signdig);
	}
	
	@Override
	public void setFocus() {
		this.mainLayout.requestFocusFromTouch();
	}

}