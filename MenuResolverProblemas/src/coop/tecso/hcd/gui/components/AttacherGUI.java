package coop.tecso.hcd.gui.components;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import coop.tecso.hcd.R;
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
@SuppressWarnings({"ResultOfMethodCallIgnored", "SameParameterValue"})
public final class AttacherGUI extends CampoGUI {

	private final String LOG_TAG = AttacherGUI.class.getSimpleName();

	private LinearLayout mainLayout;
	private ImageButton  btnAttach;
	private ImageView 	 mImageView;
	private EditText 	 textBox;

	private byte[] data;
	private int maxChar = 250;
	private float escala;

	private String newPhotoPath;

	// MARK: - Constructor

	public AttacherGUI(Context context, boolean enabled) {
		super(context, enabled);
	}

	public AttacherGUI(Context context, boolean enabled, int maxLength) {
		super(context, enabled);
		if (maxLength > 0){
			maxChar = maxLength;
		}
	}

	// MARK: - Metodos

	@SuppressLint("ResourceType")
	@Override
	public View build() {
	    String label = this.getEtiqueta() + ": ";

		// Etiqueta
		this.label = new TextView(context);
		this.label.setTextColor(context.getResources().getColor(R.color.label_text_color));
		this.label.setText(label);
		this.label.setPadding(0, 10, 0, 15);

		this.mImageView = new ImageView(context);
		this.mImageView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        // Se define un LinearLayout para ubicar: 'Label / EditText'
        this.mainLayout = new LinearLayout(context);
        this.mainLayout.setOrientation(LinearLayout.VERTICAL);
		this.mainLayout.setBackgroundColor(context.getResources().getColor(R.color.default_background_color));
		this.mainLayout.setGravity(Gravity.CENTER_VERTICAL);

		// Attached image
		this.textBox = new EditText(context);
		this.textBox.setEnabled(enabled);
		this.textBox.setFocusable(enabled);

		List<Value> initialValues = this.getInitialValues();
		if (!CollectionUtils.isEmpty(initialValues)) {
		    Value initialValue = initialValues.get(0);
			this.textBox.setText(initialValue.getValor());
			this.data = initialValue.getImagen();

			if (data == null) {
				this.mImageView.setImageResource(R.drawable.no_image);
			} else {
				Options options = new BitmapFactory.Options();
			    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
			    options.inDither = false;
				
			    byte[] thumb = initialValue.getThumbnail();
			    if (thumb != null) {
			    	Bitmap bitmap = Utils.getBitmapWitheBackground(BitmapFactory.decodeByteArray(thumb, 0, thumb.length, options));
			    	mImageView.setImageBitmap(bitmap);
			    }
			}
		} else {
			this.textBox.setText(this.getValorDefault());
			this.mImageView.setImageResource(R.drawable.no_image);
		}

		//   Se crea y aplica un filtro para definir la cantidad maxima de caracteres de la caja de texto
		InputFilter maxLengthFilter = new InputFilter.LengthFilter(maxChar);
		this.textBox.setFilters(new InputFilter[]{ maxLengthFilter });

		this.btnAttach = new ImageButton(context);
		this.btnAttach.setEnabled(enabled);
		this.btnAttach.setFocusable(enabled);
		this.btnAttach.setBackgroundResource(android.R.drawable.ic_menu_camera);
		this.btnAttach.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		this.btnAttach.setOnClickListener(v -> {
			// Marco el campo como selected asi el activity sabe cual adjunto refrescar
			perfilGUI.setCampoGUISelected(AttacherGUI.this);

			Log.i(LOG_TAG, "After Photo Capture - System.gc()");
			System.gc();

			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			File file = createEmptyImageFile();
			if (file == null) {
				return;
			}

			Uri uri = FileProvider.getUriForFile(context, "com.fantommers.hc.fileprovider", file);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);

			((Activity) context).startActivityForResult(intent, Constants.REQUEST_NEW_PHOTO);
        });

		//Genero layout para poder manipular las imagenes.
		LinearLayout btnLayout = new LinearLayout(context);
		btnLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT) );
		btnLayout.setGravity(Gravity.END);
		btnLayout.setPadding(20, 10, 0, 10);
		btnLayout.addView(btnAttach);

		//Genero layout para poder manipular las imagenes.
		LinearLayout photoLayout = new LinearLayout(context);
		photoLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT) );
		photoLayout.setPadding(20, 15, 0, 25); //solo si la foto fue sacada.
		photoLayout.setGravity(Gravity.START);
		photoLayout.addView(mImageView);

		//Linea separadora de imagens.
		View gap = new View(context);
		gap.setBackgroundColor(context.getResources().getColor(R.color.label_text_color));
		
		RelativeLayout comp = new RelativeLayout(context);

		photoLayout.setId(1);
		btnLayout.setId(2);

		RelativeLayout.LayoutParams lpImage = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lpImage.addRule(RelativeLayout.ALIGN_LEFT, btnLayout.getId());
		comp.addView(photoLayout, lpImage);

		RelativeLayout.LayoutParams lpBtn = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		lpBtn.addRule(RelativeLayout.ALIGN_RIGHT, -1) ;
		comp.addView(btnLayout, lpBtn);

		this.mainLayout.addView(this.label);
		this.mainLayout.addView(comp);
		this.mainLayout.addView(textBox);
		this.textBox.requestFocus();
		this.mainLayout.addView(gap, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));

		this.view = mainLayout;

		return this.view;
	}

	@Override
	public View redraw() {
		this.btnAttach.setEnabled(enabled);
		this.btnAttach.setFocusable(enabled);
		this.textBox.setEnabled(enabled);
		this.textBox.setFocusable(enabled);
		this.textBox.setFocusableInTouchMode(enabled);
		return this.view;
	}

	@Override
	public List<Value> values() {
		this.values = new ArrayList<>();

		AplPerfilSeccionCampo campo = null;
		AplPerfilSeccionCampoValor campoValor = null;
		AplPerfilSeccionCampoValorOpcion campoValorOpcion = null;
		if (this.entity instanceof AplPerfilSeccionCampo) {
			campo = (AplPerfilSeccionCampo) this.entity;
		} else if(this.entity instanceof AplPerfilSeccionCampoValor) {
			campoValor = (AplPerfilSeccionCampoValor) this.entity;
			campo = campoValor.getAplPerfilSeccionCampo();
		} else if(this.entity instanceof AplPerfilSeccionCampoValorOpcion) {
			campoValorOpcion = (AplPerfilSeccionCampoValorOpcion) this.entity;
			campoValor = campoValorOpcion.getAplPerfilSeccionCampoValor();
			campo = campoValor.getAplPerfilSeccionCampo();
		}
		String nombreCampo = campo!=null?campo.getCampo().getEtiqueta():"No identificado";
		String valor = this.textBox.getText().toString();

		Log.d(AttacherGUI.class.getSimpleName(),"save() : "+this.getTratamiento()+" :Campo: "+nombreCampo
				+" idCampo: "+(campo!=null?campo.getId():"null")
				+", idCampoValor: "+(campoValor!=null?campoValor.getId():"null")
				+", idCampoValorOpcion: "+(campoValorOpcion!=null?campoValorOpcion.getId():"null")
				+", Valor: "+valor);

		Value vData = new Value(campo,campoValor,campoValorOpcion,valor, null, data);
		
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

		if (thumb != null) {
			thumb = Utils.getBitmapWitheBackground(thumb);
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			thumb.compress(Bitmap.CompressFormat.JPEG, 85, outStream);		
			vData.setThumbnail(outStream.toByteArray());
		}
		
		this.values.add(vData);
		return this.values;
	}

	@Override
	public boolean isDirty() {
		if (super.isDirty()) {
			return true;
		}

		String valorActual = this.textBox.getText().toString();
		if (!CollectionUtils.isEmpty(this.getInitialValues())) {
			byte[] image = this.getInitialValues().get(0).getImagen();
			dirty = !Arrays.equals(data, image) || !valorActual.equals(this.getInitialValues().get(0).getValor());
		} else if (data != null) {
			dirty = (data.length > 0) || !TextUtils.isEmpty(valorActual);
		}

		return super.isDirty();
	}

	@Override
	public boolean validate() {
		String text = this.textBox.getText().toString();
		if (isObligatorio() && data.length == 0 && TextUtils.isEmpty(text)) {
			GUIHelper.showError(context, context.getString(R.string.field_required, getEtiqueta()));
			mImageView.requestFocus();

			return false;
		}
		return true;
	}

	// URGMNT-211: Optimizaciones de memoria agresivas mediante compresión JPEG previa a las operaciones
	public void loadCapturedPhoto() {
		Bitmap imageBitmap = resizeBitmap(125, 125, newPhotoPath);

        this.mImageView.setImageBitmap(imageBitmap);

        if (escala == 0) {
			imageBitmap = loadCapturedImage();
        } else {
        	try {
				Options options = new BitmapFactory.Options();
				options.inSampleSize = 1;
				imageBitmap = loadCapturedImage();

				String[] maxResolutions = ParamHelper.getString("signMaxResolution", imageBitmap.getWidth() + "x" + imageBitmap.getHeight()).split("x");
				boolean isPortrait = imageBitmap.getHeight() > imageBitmap.getWidth();

				if (isPortrait && Integer.parseInt(maxResolutions[0]) > Integer.parseInt(maxResolutions[1])) {
					//cambio el alto por el ancho
					String aux = maxResolutions[0];
					maxResolutions[0] = maxResolutions[1];
					maxResolutions[1] = aux;
				}

				//image
				int w = (int) (Integer.parseInt(maxResolutions[0]) * escala);
				int h = (int) (Integer.parseInt(maxResolutions[1]) * escala);

				imageBitmap = Bitmap.createScaledBitmap(imageBitmap, w, h, true);
				System.gc();
				Log.d(LOG_TAG, String.format("Se transformo la imagen a %sx%s", w, h));
			} catch (Exception e) {
        		e.printStackTrace();
			}
        }

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream);
        this.data = stream.toByteArray();

        this.deleteCapturedImage();

        evalCondicionalSoloLectura();
	}

	@Override
	public void removeAllViewsForMainLayout() {
		this.mainLayout.removeAllViews();
	}

	public void setEscala(float escala) {
		this.escala = escala;
	}

	@Override
	public void clearData() {
		this.textBox.setText(this.getValorDefault());
		this.mImageView.setImageResource(R.drawable.no_image);
		this.data = null;
	}
	
	@Override
	public void setFocus() {
		this.mainLayout.requestFocusFromTouch();
	}

	// MARK: - Internal

	private File createEmptyImageFile() {
		String imageFileName = "" + System.currentTimeMillis();
		File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		File image;

		try {
			image = File.createTempFile(imageFileName, ".jpg", storageDir);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// Save a file: path for use with ACTION_VIEW intents
		newPhotoPath = image.getAbsolutePath();
		return image;
	}

	/**
	 * Devuelve el BitMap redimenzionado, desde un path específico.
	 */
	private static Bitmap resizeBitmap(int targetWidth, int targetHeight, String path) {
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

		int scaleFactor = 1;
		if ((targetWidth > 0) || (targetHeight > 0)) {
			scaleFactor = Math.min(photoW/targetWidth, photoH/targetHeight);
		}

		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		return BitmapFactory.decodeFile(path, bmOptions);
	}

	private void deleteCapturedImage() {
		if (newPhotoPath == null) {
			return;
		}

		File file = new File(newPhotoPath);
		if (file.exists()) {
			try {
				file.delete();
			} catch (Exception ignore) {}
		}

		this.newPhotoPath = null;
	}

	private Bitmap loadCapturedImage() {
		File imageFile = new File(newPhotoPath);

		Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;

		try {
			FileInputStream fileInputStream = new FileInputStream(imageFile);
			return Utils.decodeByteArrayMemOpt(fileInputStream, options.inSampleSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}