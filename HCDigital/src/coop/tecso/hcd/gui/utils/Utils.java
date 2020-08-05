package coop.tecso.hcd.gui.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Base64;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import coop.tecso.hcd.R;
import coop.tecso.hcd.application.HCDigitalApplication;
import coop.tecso.hcd.entities.EntidadBusqueda;
import coop.tecso.hcd.gui.helpers.FilaTablaBusqueda;
import coop.tecso.hcd.gui.helpers.Tratamiento;
import coop.tecso.hcd.gui.helpers.Value;
import coop.tecso.udaa.domain.base.AbstractEntity;

public final class Utils {

	/**
	 * Construye un mapa de Filas con valores de las Entidades de Busqueda
	 */
	public static Map<String,List<FilaTablaBusqueda>> getMapEntidadBusqueda(List<EntidadBusqueda> entidadBusquedaList) {
		Map<String,List<FilaTablaBusqueda>> mapEntidadBusqueda = new HashMap<String, List<FilaTablaBusqueda>>();

		if (entidadBusquedaList != null) {
			for (EntidadBusqueda entidadBusqueda : entidadBusquedaList) {
				if (entidadBusqueda != null) {
					String entidad = entidadBusqueda.getEntidad();
		
					if (entidad != null) {
						List<FilaTablaBusqueda> tablaBusqueda = mapEntidadBusqueda.get(entidad);
						if (null == tablaBusqueda) tablaBusqueda = new ArrayList<FilaTablaBusqueda>();
		
						FilaTablaBusqueda fila = new FilaTablaBusqueda();					
						fila.setId(entidadBusqueda.getId());
						fila.setCodigo(entidadBusqueda.getCodigo());
						fila.setDescripcion(entidadBusqueda.getDescripcion());

						tablaBusqueda.add(fila);
						mapEntidadBusqueda.put(entidad, tablaBusqueda);
					}
				}
			}
		}
		
		return mapEntidadBusqueda;
	}

	/**
	 * Completa un mapa con la lista de valores iniciales para cada campo.
	 */
	public static Map<String, List<Value>> fillInitialValuesMaps(List<Value> listValue){
		Map<String, List<Value>> mapInitialValues = new HashMap<>();
		for (Value value : listValue) {
			String key = "";
			// Se carga el valor en la lista asociada al campo
			key = value.getCampo().getId()+"|0|0";
			List<Value> initialValues = mapInitialValues.get(key);
			if (initialValues == null) {
				initialValues = new ArrayList<>();
			}
			initialValues.add(value);
			mapInitialValues.put(key, initialValues);

			// Se carga el valor en la lista asociada al campoValor
			if(isNotNull(value.getCampoValor())){
				key = value.getCampo().getId()+"|"+value.getCampoValor().getId()+"|0";
				initialValues = mapInitialValues.get(key);
				if(initialValues == null){
					initialValues = new ArrayList<Value>();
				}
				initialValues.add(value);
				mapInitialValues.put(key, initialValues);
			}

			// Se carga el valor en la lista asociada al campoValorOpcion
			if(isNotNull(value.getCampoValorOpcion())){
				key = value.getCampo().getId()+"|"+value.getCampoValor().getId()+"|"+value.getCampoValorOpcion().getId();
				initialValues = mapInitialValues.get(key);
				if(initialValues == null){
					initialValues = new ArrayList<Value>();
				}
				initialValues.add(value);
				mapInitialValues.put(key, initialValues);
			}
		}
		return mapInitialValues;
	}


	/**
	 * Completa un mapa con la lista de valores iniciales para cada campo. 
	 * La clave se genera de esta manera:
	 * idCampo
	 * idCampo-idCampoValor
	 *
	 */
	public static Map<String, String> fillValueForReport(List<Value> listValue, List<EntidadBusqueda> entidadBusquedaLis){
		Map<String, String> mapResult = new HashMap<String, String>();
		Map<String, List<FilaTablaBusqueda>> mapEntBus = getMapEntidadBusqueda(entidadBusquedaLis);
		
		String key;
		String separator = "; ";
		String separatorLine = "\n";
		StringBuilder strValue;

		for (Value value : listValue) {
			// Valores asociados a campo
			key = value.getCampo().getId()+"";
			strValue = new StringBuilder();
			if(null != mapResult.get(key)){
				strValue.append(mapResult.get(key));
			} 
			
			// Tratamiento
			Tratamiento tratamiento = Tratamiento.getByCod(value.getCampo().getCampo().getTratamiento());

			if(tratamiento.equals(Tratamiento.DESCONOCIDO) && value.getCampo().getCampo().getTratamientoDefault() != null){
				tratamiento = Tratamiento.getByCod(value.getCampo().getCampo().getTratamientoDefault());
			}

			switch (tratamiento) { 
			case LO:
				if(strValue.length() > 1) strValue.append(separatorLine);
				String label = value.getCampoValor().getCampoValor().getEtiqueta();
				strValue.append(label);
				
				if(isNotNull(value.getCampoValorOpcion())){
					
					//CampoValorOpcion
					String selection = value.getCampoValorOpcion().getCampoValorOpcion().getEtiqueta();
					if(!selection.equals(value.getValor())){
						strValue.append(": ");
						strValue.append(selection);
						if(!TextUtils.isEmpty(value.getValor())){
							strValue.append(" -> ");
							strValue.append(value.getValor());
						} 
						break;
					}
				}
				//Caso: LO y NA
				if(!label.equals(value.getValor())){
					strValue.append(": ");
					strValue.append(value.getValor());
				}
				break;
			case LOIMPR:
				if(strValue.length() > 1) strValue.append(separatorLine);
				String labelLOIMPR = value.getCampoValor().getCampoValor().getEtiqueta();
				
				String unidad = getBeetween(labelLOIMPR, "[", "]");
				labelLOIMPR = deleteBeetween(labelLOIMPR, "[", "]").trim();
				
				strValue.append(labelLOIMPR);
				
				if(isNotNull(value.getCampoValorOpcion())){
					
					//CampoValorOpcion
					String selection = value.getCampoValorOpcion().getCampoValorOpcion().getEtiqueta();
					if(!selection.equals(value.getValor())){
						strValue.append(": ");
						strValue.append(selection);
						if(!TextUtils.isEmpty(value.getValor())){
							strValue.append(" -> ");
							strValue.append(value.getValor());
						} 
						break;
					}
				}
				//Caso: LO y NA
				if(!labelLOIMPR.equals(value.getValor())){
					strValue.append(": ");
					strValue.append(value.getValor());
					
					if(unidad != null && unidad.length() != 0) {
						strValue.append(" " + unidad);
					}
				}
				break;
			case LB:
				String entBus = value.getCampo().getCampo().getEntidadBusqueda();
				for(FilaTablaBusqueda fila : mapEntBus.get(entBus)){
					if(fila.getCodigo().equals(value.getCodigoEntidadBusqueda())){
						// CampoValor
						if(isNotNull(value.getCampoValor())){
							strValue.append(" -> ");
							strValue.append(value.getCampoValor().getCampoValor().getEtiqueta());
							strValue.append(": ");
							strValue.append(value.getValor());
							break;
						}
						//Separador
						if(strValue.length() > 1) strValue.append(separatorLine);
						strValue.append(fila.getDescripcion());
						break;
					}
				}
				break;
			case LBD:
				String entBusLBD = value.getCampo().getCampo().getEntidadBusqueda();
				for(FilaTablaBusqueda fila : mapEntBus.get(entBusLBD)){
					if(fila.getCodigo().equals(value.getCodigoEntidadBusqueda())){
						// CampoValor
						if(isNotNull(value.getCampoValor())){
							strValue.append(" -> ");
							strValue.append(value.getValor());
							break;
						}
						//Separador
						if(strValue.length() > 1) strValue.append(separatorLine);
						strValue.append(fila.getDescripcion());
						break;
					}
				}
				break;
			case LBDIMPR:
				String entBusLBDIMP = value.getCampo().getCampo().getEntidadBusqueda();
				for(FilaTablaBusqueda fila : mapEntBus.get(entBusLBDIMP)){
					if(fila.getCodigo().equals(value.getCodigoEntidadBusqueda())){
						// CampoValor
						if(isNotNull(value.getCampoValor())){
							strValue.append(" -> ");
							strValue.append(value.getValor());
							break;
						}
						//Separador
						if(strValue.length() > 1) strValue.append(separatorLine);
						strValue.append(deleteBeetween(fila.getDescripcion(), "(", ")"));
						break;
					}
				}
				break;
			case LD:
				if(strValue.length() > 1) strValue.append(separatorLine);
				strValue.append(value.getValor());
				break;	
			case OP:
				label = value.getCampoValor().getCampoValor().getEtiqueta();
				if(!label.equals(value.getValor())){
					strValue.append(label);
					if(!TextUtils.isEmpty(value.getValor())) strValue.append(" -> ");
				}
				strValue.append(value.getValor());
				break;
			default:
				// Default
				strValue.append(value.getValor());
				break;
			}
			//Campo => Valor
			mapResult.put(key, strValue.toString());

			// Valores asociados a campoValor
			if (isNotNull(value.getCampoValor())) {
				key = value.getCampo().getId()+"-"+value.getCampoValor().getId();
				if (mapResult.get(key) == null) {
					strValue = new StringBuilder();
				} else {
					strValue.append("##");
				}
				strValue.append(value.getValor());
				mapResult.put(key, strValue.toString());
			}

			// Valores asociados a campoValorOpcion
			if (isNotNull(value.getCampoValorOpcion())) {
				key = value.getCampo().getId()+"-"+value.getCampoValor().getId()+"-"+value.getCampoValorOpcion().getId();
				//--
				if (mapResult.get(key) == null) {
					strValue = new StringBuilder();
				} else {
					strValue.append("###");
				}
				strValue.append(value.getValor());
				mapResult.put(key, strValue.toString());
			}
		}
		return mapResult;
	}

	/**
     *
	 * Completa una cadena con las etiquetas y los valores de los campos con Id en sKeys
	 *
	 * @param listValue Valores de los componentes del formulario
	 * @param entidadBusquedaLis .
	 * @param sKeys Ids campos relacionados
	 *
	 * @return Una cadena con las etiquetas y los valores de los campos con Ids en sKeys
	 */
	public static String fillValueForEpicrisis(List<Value> listValue,
											   List<EntidadBusqueda> entidadBusquedaLis,
											   String sKeys){

		int titleLength;
		String title;
		String key;
		String separator = "; ";
		String enter = "\n";
		String[] asId = sKeys.split(",");
		StringBuilder strValue;
		Map<String, String> mapResult = new HashMap<String, String>();
		Map<String, List<FilaTablaBusqueda>> mapEntBus = getMapEntidadBusqueda(entidadBusquedaLis);

		for (Value value : listValue) {
			key = String.valueOf(value.getCampo().getId());
			if (Arrays.asList(asId).contains(key)) {
				title = value.getCampo().getCampo().getEtiqueta();
				titleLength = title.length() + 1;
			}
			else
				continue;

			strValue = new StringBuilder();
			if(mapResult.get(key) == null)
				strValue.append(title).append(enter);
			else
				strValue.append(mapResult.get(key));

			Tratamiento tratamiento = Tratamiento.getByCod(value.getCampo().getCampo().getTratamiento());

			switch (tratamiento) {
				case LO:
				case LOIMPR:
					if(strValue.length() > titleLength)
						strValue.append(separator);
					String label = value.getCampoValor().getCampoValor().getEtiqueta();
					strValue.append(label);

					if(isNotNull(value.getCampoValorOpcion())){
						String selection = value.getCampoValorOpcion().getCampoValorOpcion().getEtiqueta();
						if(!selection.equals(value.getValor())){
							strValue.append(": ");
							strValue.append(selection);
							if(!TextUtils.isEmpty(value.getValor())){
								strValue.append(" -> ");
								strValue.append(value.getValor());
							}
							break;
						}
					}
					//Caso: LO y NA
					if(!label.equals(value.getValor())){
						strValue.append(": ");
						strValue.append(value.getValor());
					}
					break;
				case LB:
				case LBD:
				case LBDIMPR:
					String entBus = value.getCampo().getCampo().getEntidadBusqueda();
					for(FilaTablaBusqueda fila : mapEntBus.get(entBus)){
						if(fila.getCodigo().equals(value.getCodigoEntidadBusqueda())){
							if(isNotNull(value.getCampoValor())){
								strValue.append(" -> ");
								strValue.append(value.getCampoValor().getCampoValor().getEtiqueta());
								strValue.append(": ");
								strValue.append(value.getValor());
								break;
							}
							if(strValue.length() > titleLength)
								strValue.append(separator);
							strValue.append(fila.getDescripcion());
							break;
						}
					}
					break;
				case LD:
					if(strValue.length() > titleLength)
						strValue.append(separator);
					strValue.append(value.getValor());
					break;
				case OP:
					label = value.getCampoValor().getCampoValor().getEtiqueta();
					if(!label.equals(value.getValor())){
						strValue.append(label);
						if(!TextUtils.isEmpty(value.getValor())) strValue.append(" -> ");
					}
					strValue.append(value.getValor());
					break;
				default:
					// Default
					strValue.append(value.getValor());
					break;
			}

			if (strValue.length() > titleLength) {
                mapResult.put(key, strValue.toString());
            }

		}

		StringBuilder sbResult = new StringBuilder();
		for (String k: asId) {
            if (mapResult.containsKey(k)) {
                sbResult.append(mapResult.get(k))
                        .append(enter);
            }
        }

		return sbResult.toString();

	}

	public static boolean isNotNull(AbstractEntity entity){
		return null != entity && entity.getId() >= 1;
	}

	@SuppressLint("SimpleDateFormat")
    public static String formatDateToJSON(Date date){
		try {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(date);
		} catch (Exception e) {
			return "";
		}
	}

	public static String encodeToBase64(byte[] ba){
		return Base64.encodeToString(ba, Base64.DEFAULT);
	}

	public static String getFormattedTitle(Context context) {
		HCDigitalApplication app = (HCDigitalApplication) context.getApplicationContext();
		String versionName = "";

		try {
			versionName = "v" + app.getPackageManager().getPackageInfo(app.getPackageName(), 0).versionName;
		} catch (Exception ignore) {}

		return app.getString(R.string.app_header_tittle, versionName);
	}

	public static String getFormattedSubtitled(Context context) {
		HCDigitalApplication app = (HCDigitalApplication) context.getApplicationContext();
		// Logged User
		String userName = "";
		String especialidad = "";
		if (app.getCurrentUser() != null) {
			especialidad = app.getCurrentUser().getEspecialidad();
			userName = app.getCurrentUser().getNombre();
		}
		return especialidad+" "+userName;
	}

	public static boolean canHandleIntent(Context context, Intent intent){
		PackageManager pm = context.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return activities.size() > 0;
	}
	
	public static boolean isAppInstalled(Context context, String pkg){
		try{
			context.getPackageManager().getApplicationInfo(pkg,PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
			return true;
		} catch( PackageManager.NameNotFoundException e ){
			return false;
		}
	}

	/**
	 * Completa la cadena recibida con ceros a la izquierda hasta llegar a la longitud deseada.
	 */
	public static String completarCerosIzq(String valor, int longitud ){
		try {
			String valorRet = valor;
			if (valor.length() < longitud){
				for(int i= valor.length() ; i < longitud ; i++) {
					valorRet = "0" + valorRet;
				}
			}
			return valorRet;
		} catch (Exception e) {
			return valor;
		}
	}

	public static String completarCaracterDer(String valor, int longitud, Character caracter ){
		try {
			String valorRet = valor;
			if (valor.length() < longitud){
				for(int i= valor.length() ; i < longitud ; i++) {
					valorRet = valorRet + caracter;
				}
			}
			return valorRet;
		} catch (Exception e) {
			return valor;
		}
	}

	public static boolean isInteger(String string) {
	    try {
	        Integer.valueOf(string);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
	}
	
	private static String deleteBeetween(String sentence, String firstParam, String secondParam) {
		return sentence.replaceAll("\\" + firstParam + ".*\\" + secondParam, "");		
	}
	
	private static String getBeetween(String sentence, String firstParam, String secondParam) {
		if (sentence.contains(firstParam) && sentence.contains(secondParam)) {
			return sentence.substring(sentence.indexOf(firstParam) + 1, sentence.indexOf(secondParam));		
		}
		
		return "";		
	}

	public static Bitmap getBitmapWitheBackground(Bitmap imageBitmap) {
		Bitmap bitmap = Bitmap.createBitmap(imageBitmap.getWidth(), imageBitmap.getHeight(), imageBitmap.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(imageBitmap, 0, 0, null);
		return bitmap;
	}
	
	public static Bitmap decodeByteArrayMemOpt(InputStream inputStream, int inSampleSize) {
        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;

        try {
            while ((len = inputStream.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = inSampleSize;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

}
