package coop.tecso.udaa.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.cert.X509Certificate;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.google.gson.graph.GraphAdapterBuilder;
import com.google.gson.reflect.TypeToken;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import coop.tecso.IUDAAService;
import coop.tecso.udaa.R;
import coop.tecso.udaa.base.UDAAManager;
import coop.tecso.udaa.base.UdaaApplication;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion.App;
import coop.tecso.udaa.domain.aplicaciones.Aplicacion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionBinarioVersion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfil;
import coop.tecso.udaa.domain.aplicaciones.AplicacionPerfilSeccion;
import coop.tecso.udaa.domain.aplicaciones.AplicacionSync;
import coop.tecso.udaa.domain.base.HelperEntity;
import coop.tecso.udaa.domain.notificaciones.Notificacion;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampo;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValor;
import coop.tecso.udaa.domain.perfiles.AplPerfilSeccionCampoValorOpcion;
import coop.tecso.udaa.domain.perfiles.Campo;
import coop.tecso.udaa.domain.perfiles.CampoValor;
import coop.tecso.udaa.domain.perfiles.CampoValorOpcion;
import coop.tecso.udaa.domain.seguridad.DispositivoMovil;
import coop.tecso.udaa.domain.seguridad.UsuarioApm;

@SuppressWarnings({"ResultOfMethodCallIgnored", "TrustAllX509TrustManager", "SimpleDateFormat"})
public final class UDAAService extends Service {

	private static final String LOG_TAG = UDAAService.class.getSimpleName();

	private final IUDAAService.Stub binder = new IUDAAService.Stub() {

		@Override
		public String getCurrentUser() throws RemoteException {
			UdaaApplication appContext = (UdaaApplication) getApplication();
			if (appContext.getCurrentUser() == null) {
				return null;
			}
			UDAADao udaaDao = new UDAADao(UDAAService.this);
			//
			UsuarioApm usuarioApm = udaaDao.getUsuarioApmById(appContext
					.getCurrentUser().getId());

			GsonBuilder builder = new GsonBuilder();
			// Register an adapter to manage the date types as long values
			builder.registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> context.serialize(src.getTime()));
			return builder.create().toJson(usuarioApm);
		}

		@Override
		public String getServerURL() throws RemoteException {
			SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
			return sharedPreferences.getString("URL", "");
		}

		@Override
		public boolean isTransTypePartial() throws RemoteException {
			SharedPreferences sharedPreferences = getSharedPreferences("settings", Context.MODE_PRIVATE);
			return sharedPreferences.getBoolean("TTP", false);
		}

		@Override
		public String getAplicacionPerfilById(int aplicacionPerfilId) throws RemoteException {
			try {
				UDAADao dao = new UDAADao(UDAAService.this);
				GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();

				// Register an adapter to manage the date types as long values
				gsonBuilder.registerTypeAdapter(Date.class,
						(JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()));

				new GraphAdapterBuilder().addType(AplicacionPerfil.class)
						.addType(AplicacionPerfilSeccion.class)
						.addType(AplPerfilSeccionCampo.class)
						.addType(AplPerfilSeccionCampoValor.class)
						.addType(AplPerfilSeccionCampoValorOpcion.class)
						.addType(Campo.class).addType(CampoValor.class)
						.addType(CampoValorOpcion.class)
						.registerOn(gsonBuilder);
				Gson gson = gsonBuilder.create();
				AplicacionPerfil ap = dao
						.getAplicacionPerfilById(aplicacionPerfilId);
				String res = gson.toJson(ap);
				// ??
				gson = null;

				// Create file
				String path = Environment.getExternalStorageDirectory()
						.getAbsolutePath();
				File file = new File(path + File.separator + "apl.json");
				BufferedWriter out = new BufferedWriter(new FileWriter(file),
						8192);
				out.write(res);
				// Close the output stream
				out.close();

				Log.d(LOG_TAG, "UDDA - PATH: " + file.getPath());
				return file.getPath();
			} catch (Exception e) {
				Log.e(LOG_TAG, "BOOM", e);
				return null;
			}
		}

		@Override
		public String getCampoBy(int campoId, int aplicacionPerfilId) throws RemoteException {
			try {
				UDAADao dao = new UDAADao(UDAAService.this);
				GsonBuilder gsonBuilder = new GsonBuilder();

				// Register an adapter to manage the date types as long values
				gsonBuilder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive()
						.getAsLong()));

				new GraphAdapterBuilder().addType(Campo.class)
						.addType(CampoValor.class)
						.addType(CampoValorOpcion.class)
						.registerOn(gsonBuilder);

				Gson gson = gsonBuilder.create();
				Campo campo = dao.getCampoBy(campoId, aplicacionPerfilId);
				return gson.toJson(campo);
			} catch (Exception e) {
				Log.e(LOG_TAG, "getCampoBy: BOOM", e);
				return null;
			}
		}

		@Override
		public String sync(String clazz, String table, int version) throws RemoteException {
			return null;
		}

		@Override
		public String fetchUser( int userID ) throws RemoteException {
			UsuarioApm usuarioApm = new UDAAManager(UDAAService.this).getUsuarioApm(userID);
			if(usuarioApm == null) {
				return null;
			}
			GsonBuilder builder = new GsonBuilder();
			// Register an adapter to manage the date types as long values
			builder.registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> context.serialize(src.getTime()));
			return builder.create().toJson(usuarioApm);
		}

		@Override
		public String login(String username, String password) throws RemoteException {
			UsuarioApm usuarioApm = new UDAAManager(UDAAService.this)
					.getUsuarioApm(username, password);

			if (usuarioApm == null) {
				return null;
			}
			GsonBuilder builder = new GsonBuilder();
			// Register an adapter to manage the date types as long values
			builder.registerTypeAdapter(Date.class,  (JsonSerializer<Date>) (src, typeOfSrc, context) -> context.serialize(src.getTime()));
			return builder.create().toJson(usuarioApm);
		}

		@Override
		public String getDispositivoMovil() throws RemoteException {
			UdaaApplication udaaApplication = (UdaaApplication) getApplicationContext();

			DispositivoMovil dispositivoMovil = udaaApplication.getDispositivoMovil();

			if (dispositivoMovil == null) {
				return null;
			}
			GsonBuilder builder = new GsonBuilder();
			// Register an adapter to manage the date types as long values
			builder.registerTypeAdapter(Date.class, (JsonSerializer<Date>) (src, typeOfSrc, context) -> context.serialize(src.getTime()));
			return builder.create().toJson(dispositivoMovil);
		}

		@Override
		public String getNotificacionById(int notificacionID) throws RemoteException {
			try {
				UDAADao dao = new UDAADao(UDAAService.this);
				GsonBuilder builder = new GsonBuilder();

				// Register an adapter to manage the date types as long values
				builder.registerTypeAdapter(Date.class,  (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive()
						.getAsLong()));

				Notificacion notificacion = dao
						.getNotificacionById(notificacionID);
				String res = builder.create().toJson(notificacion);

				Log.d(LOG_TAG, "UDDA - Notificacion JSON: " + res);
				return res;
			} catch (Exception e) {
				Log.d(LOG_TAG, "BOOM");
				return null;
			}
		}

		@Override
		public void changeSession(String username, String password) throws RemoteException {
			UdaaApplication appContext = (UdaaApplication) getApplication();
			appContext.changeSession(username);
		}

		@Override
		public boolean hasAccess(int usuarioID, String codAplicacion) throws RemoteException {
			UDAADao dao = new UDAADao(UDAAService.this);
			return dao.hasAccess(usuarioID, codAplicacion);
		}

		@Override
		public String getIdAplicacionPerfilDefaultBy(String codAplicacion) throws RemoteException {
			UDAADao dao = new UDAADao(UDAAService.this);
			Integer idAplicacionPerfil = dao.getIdAplicacionPerfilDefaultBy(codAplicacion);
			return String.valueOf(idAplicacionPerfil);
		}

		@Override
		public String getAplPerfilSeccionCampoById(int aplPerfilSeccionCampoId) throws RemoteException {
			try {
				UDAADao dao = new UDAADao(UDAAService.this);
				GsonBuilder gsonBuilder = new GsonBuilder();

				// Register an adapter to manage the date types as long values
				gsonBuilder.registerTypeAdapter(Date.class,  (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive()
						.getAsLong()));
				// Create a builder to manage recursive entities
				new GraphAdapterBuilder()
						.addType(AplicacionPerfilSeccion.class)
						.addType(AplPerfilSeccionCampoValor.class)
						.registerOn(gsonBuilder);

				Gson gson = gsonBuilder.create();

				AplPerfilSeccionCampo aplPerfilSeccionCampo;
				aplPerfilSeccionCampo = dao
						.getAplPerfilSeccionCampoById(aplPerfilSeccionCampoId);
				return gson.toJson(aplPerfilSeccionCampo);
			} catch (Exception e) {
				Log.e(LOG_TAG, "getAplPerfilSeccionCampoById: BOOM", e);
				return null;
			}
		}

		@Override
		public void sendError(int tipoReg) {
			try {
				UdaaApplication appState = (UdaaApplication) getApplicationContext();
				appState.reportGPSLocation(false);
			} catch (Exception e) {
				Log.d(LOG_TAG, " Error : ", e);
			}
		}

		@Override
		public String generateReport(String jsonData, String jsonSection, String templateName) throws RemoteException {
			Log.i(LOG_TAG, "generateReport: enter");

			// Deserialize json to Map
			Map<String, String> mData = new Gson().fromJson(jsonData,
					new TypeToken<Map<String, String>>() {
					}.getType());
			// Deserialize json to Map
			Map<String, Boolean> mSection = new Gson().fromJson(jsonSection,
					new TypeToken<Map<String, Boolean>>() {
					}.getType());

			// Extract codigoAtencion
			String codigoAtencion = null;
			if (mData.containsKey("cierre.codigoAtencion")) {
				codigoAtencion = mData.get("cierre.codigoAtencion");
			}

			Log.i(LOG_TAG, "codigoAtencion = " + codigoAtencion );

			// Application cache directory
			File outputDir = getCacheDir();
			// Application template directory
			File templateDir = getDir("tpl", Context.MODE_PRIVATE);

			// PDF template
			PdfReader template = fetchOnlinePDFTemplate(mData);

			try {
				if (template == null) {
					template = new PdfReader(templateDir + File.separator+ templateName);
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "error reading PDF template", e);
				return getString(R.string.error_reading_template, templateName);
			}

			File path = new File(Environment.getExternalStorageDirectory() + "/UDAA/");
			path.mkdirs();
			for (File file : path.listFiles()) {
				file.delete();
			}

			try {
				// Temp PDF file
				String outputFilename = System.currentTimeMillis() + "_report";
				File outputFile = File.createTempFile(outputFilename, ".pdf", outputDir);
				FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

				PdfStamper stamper = new PdfStamper(template, fileOutputStream);

				AcroFields fields = stamper.getAcroFields();

				for (String key : mData.keySet()) {
					String data = mData.get(key);
					if (key.startsWith("img:")) {
						// Stamp an image object into a field
						float[] photograph = fields.getFieldPositions(key);
						if (photograph == null)
							continue;
						Rectangle rect = new Rectangle(photograph[1], photograph[2], photograph[3], photograph[4]);
						// Decode image from JSON
						Image img = Image.getInstance(Base64.decode(data, Base64.DEFAULT));
						img.scaleToFit(rect.getWidth(), rect.getHeight());
						img.setAbsolutePosition(
								photograph[1]
										+ (rect.getWidth() - img
										.getScaledWidth()) / 2,
								photograph[2]
										+ (rect.getHeight() - img
										.getScaledHeight()) / 2);
						// Put image over the control
						stamper.getOverContent(1).addImage(img);
					}
					else {
						// Stamp form data into a field
						fields.setField(key, mData.get(key));
					}
				}
				stamper.setFormFlattening(true);
				stamper.close();
				template.close();

				// Numbering pages
				StringBuilder pages = new StringBuilder();
				pages.append("1");
				for (String key : mSection.keySet()) {
					float[] pos = fields.getFieldPositions(key);
					if (pos != null && pos.length > 0) {
						pages.append(",");
						pages.append(pos[0]);
					}
				}

				PdfReader reader = new PdfReader(outputFile.getAbsolutePath());
				reader.selectPages(pages.toString());

				outputFile = File.createTempFile(System.currentTimeMillis()
						+ "_report", ".pdf", path);
				stamper = new PdfStamper(reader, new FileOutputStream(
						outputFile));

				stamper.close();

				// si codigoAtención existe, entonces renombro el archivo como tal.
				if (codigoAtencion != null) {
					File finalFile = new File( path + File.separator + codigoAtencion + ".pdf" );
					outputFile.renameTo(finalFile);

					Log.i(LOG_TAG, "generateReport: exit");
					return finalFile.getAbsolutePath();
				}

				Log.i(LOG_TAG, "generateReport: exit");
				// Report path
				return outputFile.getAbsolutePath();
			} catch (Exception e) {
				Log.e(LOG_TAG, "error generating PDF report", e);
				return getString(R.string.error_generating_report);
			}
		}

		private PdfReader fetchOnlinePDFTemplate(Map<String, String> mData) {
			try {
				if (!mData.containsKey("hcdigital.perfilID")) {
					return null;
				}
				String perfilID = mData.get("hcdigital.perfilID");
				Map<String, Boolean> secciones = new HashMap<String, Boolean>();
				secciones.put("seccion-informe-ecg", mData.get("hcdigital.showECG").equals("1"));
				secciones.put("seccion-seguimiento-evolutivo", mData.get("hcdigital.showSeguimiento").equals("1"));

				String dynamicTemplateName = String.format("IMD_DYN_%s.pdf", perfilID);

				String path = Environment.getExternalStorageDirectory() + "/download/";
				File file = new File(path);
				file.mkdirs();
				File outputFile = new File(path, dynamicTemplateName);
				if(outputFile.exists()) {
					outputFile.delete();
				}

				UdaaApplication appState = (UdaaApplication) getApplicationContext();
				String dynamicTemplateURL = WebServiceDAO.getInstance(appState).getPDFTemplate(Integer.parseInt(perfilID), appState.getDispositivoMovil().getId(), mData, secciones);

				String fileurl = dynamicTemplateURL.replace("\"", "") + "?username="
						+ URLEncoder.encode(appState.getCurrentUser().getUsername(),"UTF-8")
						+ "&appID="+URLEncoder.encode(Aplicacion.App.HCDigital.toString(),"UTF-8");

				// Create a trust manager that does not validate certificate chains
				TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(X509Certificate[] certs, String authType) {}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {}
				}
				};

				// Install the all-trusting trust manager
				SSLContext sc = SSLContext.getInstance("TLS");
				sc.init(null, trustAllCerts, new java.security.SecureRandom());
				HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

				// Create all-trusting host name verifier
				HostnameVerifier allHostsValid = (hostname, session) -> true;
				// Install the all-trusting host verifier
				HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

				URL url = new URL(fileurl);

				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();

				FileOutputStream fos = new FileOutputStream(outputFile);
				InputStream is = connection.getInputStream();

				byte[] buffer = new byte[1024];
				int len1;
				while ((len1 = is.read(buffer)) != -1) {
					fos.write(buffer, 0, len1);
				}
				fos.close();
				is.close();

				return new PdfReader(path + dynamicTemplateName);
			}
			catch(Exception ex) {
				Log.e(LOG_TAG, "error reading PDF template", ex);
				return null;
			}
		}

		@Override
		public String getListBinarioPathBy(String codAplicacion, String tipoBinario) throws RemoteException {
			Log.i(LOG_TAG, "getListBinarioPathBy: enter");

			List<String> pathList = new UDAADao(UDAAService.this)
					.getListBinarioPathBy(codAplicacion, tipoBinario);
			String result = new Gson().toJson(pathList);

			Log.i(LOG_TAG, "result: " + result);

			Log.i(LOG_TAG, "getListBinarioPathBy: exit");
			return result;
		}

		@Override
		public String exportDataToFile(String jSonData) throws RemoteException {
			String pathAfi = Environment.getExternalStorageDirectory()
					+ "/SADigital/";
			SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

			UdaaApplication appContext = (UdaaApplication) getApplication();

			Date presentDate = new Date();
			String formatDate = format.format(presentDate);

			String filename = appContext.getCurrentUser().getUsername() + "_"
					+ "Afiliaciones" + "_" + formatDate + "_"
					+ System.currentTimeMillis() + ".txt";

			File dirs = new File(pathAfi);
			Log.d("MAKE DIR", dirs.mkdirs() + "");
			dirs.mkdirs();

			File root = android.os.Environment.getExternalStorageDirectory();
			try {
				File file = new File(pathAfi + filename);

				if (root.canWrite()) {
					FileWriter fwriter = new FileWriter(file, true);
					BufferedWriter writer = new BufferedWriter(fwriter);
					writer.write(jSonData);
					writer.flush();
					writer.close();
				}
			} catch (Exception e) {
				Log.d("ERROR MAKE DIR", e.getMessage().toString());

			}
			return pathAfi + filename;
		}

		@Override
		public String getLastAplicacionBinarioVersionByCodigoAplicacion(String appCode) throws RemoteException {
			UDAADao udaaDao = new UDAADao(UDAAService.this);
			try {
				AplicacionBinarioVersion binary = udaaDao.getLastAplicacionBinarioVersionByCodigoAplicacion(appCode);
				GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
				// Register an adapter to manage the date types as long values
				gsonBuilder.registerTypeAdapter(Date.class,  (JsonSerializer<Date>) (src, typeOfSrc, context) -> context.serialize(src.getTime()));
				return gsonBuilder.create().toJson(binary);
			} catch (SQLException e) {
				return null;
			}
		}

		@Override
		public String confirmForceUpdate(String appCode, boolean lastAppToUpdate) throws RemoteException {
			UdaaApplication appState = (UdaaApplication) getApplicationContext();
			DispositivoMovil movil = appState.getDispositivoMovil();

			App.valueOf(appCode).getId();
			String result = null;
			if(lastAppToUpdate){
				if (App.UDAA.equals(App.valueOf(appCode))) {
					movil.setForzarActualizacion(false);
				}
				if (App.CTODigital.equals(App.valueOf(appCode))) {
					movil.setForzarCTO(false);
				}
				if (App.HCDigital.equals(App.valueOf(appCode))) {
					movil.setForzarHC(false);
				}
				if (App.SADigital.equals(App.valueOf(appCode))) {
					movil.setForzarSA(false);
				}

				WebServiceDAO.getInstance(appState).confirmForceUpdate(App.valueOf(appCode).getId());

				GsonBuilder gsonBuilder = new GsonBuilder().setPrettyPrinting();
				// Register an adapter to manage the date types as long values
				gsonBuilder.registerTypeAdapter(Date.class,  (JsonSerializer<Date>) (src, typeOfSrc, context) -> context.serialize(src.getTime()));
				result = gsonBuilder.create().toJson(movil);
			}

			return result;
		}

		@Override
		public void updateApplicationSync(String appCode) throws RemoteException {
			Log.i(LOG_TAG, "updateApplicationSync: enter");
			UDAADao udaaDao = new UDAADao(UDAAService.this);
			AplicacionSync aplicacionSync = new UDAADao(UDAAService.this).getApplicationSync();

			if (App.UDAA.equals(App.valueOf(appCode))) {
				aplicacionSync.setSyncUDDATimeStamp(new Date());
			}
			if (App.CTODigital.equals(App.valueOf(appCode))) {
				aplicacionSync.setSyncCTOTimeStamp(new Date());
			}
			if (App.HCDigital.equals(App.valueOf(appCode))) {
				aplicacionSync.setSyncHCTimeStamp(new Date());
			}
			if (App.SADigital.equals(App.valueOf(appCode))) {
				aplicacionSync.setSyncSATimeStamp(new Date());
			}

			udaaDao.updateApplicationSync(aplicacionSync);

			Log.i(LOG_TAG, "updateApplicationSync: exit");
		}

		@Override
		public String rawQueryList(String sql, String selectionArgs){
			SQLHelper db = new SQLHelper(UDAAService.this);
			List<String> listResult = new ArrayList<>();

			String[] selectArgs = null;
			if(selectionArgs != null && !TextUtils.isEmpty(selectionArgs)) {
				selectArgs = selectionArgs.split(",");
			}

			try {
				db.openDatabase();

				//Execute query
				Cursor cursor = db.rawQuery(sql, selectArgs);

				if(cursor.moveToFirst()) {
					do {
						listResult.add(cursor.getString(0));
					} while (cursor.moveToNext());
					cursor.close();
				}
			} catch (Exception e) {
				Log.e(LOG_TAG, "SearchPadronTask **ERROR**", e);
			}finally{
				//Close connection
				db.closeDatabase();
			}

			return new Gson().toJson(listResult);
		}

		@Override
		public String rawQueryCiudades(String sql, String selectionArgs){
			SQLHelper db = new SQLHelper(UDAAService.this);
			String result = "";

			List<HelperEntity> results = new ArrayList<HelperEntity>();
			try {
				db.openDatabase();

				// Execute query
				Cursor cursor = db.rawQuery(sql, selectionArgs);
				if (cursor.moveToFirst()) {
					HelperEntity helperEntity = new HelperEntity();
					helperEntity.setId(cursor.getInt(0));
					helperEntity.setForeingId(cursor.getInt(1));
					results.add(helperEntity);
				}
				cursor.close();

				return new GsonBuilder().create().toJson(results);

			} catch (Exception e) {
				Log.e(LOG_TAG, "SearchPadronTask **ERROR**", e);
			}finally{
				//Close connection
				db.closeDatabase();
			}

			return result;
		}

		@Override
		public String query(String table, String columns, String selection, String selectionArgs, String groupBy, String having, String orderBy) {
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			Log.e(LOG_TAG, "**query** START: " + dateformat.format(new Date()));

			String[] queryColumns = null;
			if(columns != null && !TextUtils.isEmpty(columns)) {
				queryColumns = columns.split(",");
				Log.e(LOG_TAG, "**query** queryColumns: " + queryColumns.length);
			}
			else {
				Log.e(LOG_TAG, "**query** columns: null");
			}
			String[] querySelectionArgs = null;
			if(selectionArgs != null && !TextUtils.isEmpty(selectionArgs)){
				querySelectionArgs = selectionArgs.split(",");
				Log.e(LOG_TAG, "**query** querySelectionArgs: " + querySelectionArgs.length);
			}
			else {
				Log.e(LOG_TAG, "**query** querySelectionArgs: null");
			}

			SQLHelper db = new SQLHelper(UDAAService.this);
			String result = "";

			List<HelperEntity> results = new ArrayList<HelperEntity>();
			try {
				db.openDatabase();

				// Execute query
				Log.e(LOG_TAG, "**query** Before query: " + dateformat.format(new Date()));
				Cursor cursor = db.query(table, queryColumns, selection.toString(), querySelectionArgs, groupBy, having, orderBy);
				Log.e(LOG_TAG, "**query** After query: " + dateformat.format(new Date()));

				while (cursor.moveToNext()) {
					HelperEntity helperEntity = new HelperEntity();
					helperEntity.setId(cursor.getInt(0));
					helperEntity.setData(cursor.getString(1));
					// Has extra String data?
					if (cursor.getColumnCount() > 2){
						helperEntity.setExtraData(cursor.getString(2));
					}
					results.add(helperEntity);
				}

				cursor.close();
				Log.e(LOG_TAG, "**query** Before json: " + dateformat.format(new Date()));
				String json = new GsonBuilder().create().toJson(results);
				Log.e(LOG_TAG, "**query** After json: " + dateformat.format(new Date()));
				return json;

			} catch (Exception e) {
				Log.e(LOG_TAG, "SearchPadronTask **ERROR**", e);
			} finally{
				//Close connection
				db.closeDatabase();
			}

			return result;
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

}