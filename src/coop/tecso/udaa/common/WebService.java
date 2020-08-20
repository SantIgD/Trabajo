package coop.tecso.udaa.common;

import static coop.tecso.udaa.utils.ParamHelper.CONNECTION_TIMEOUT;
import static coop.tecso.udaa.utils.ParamHelper.SOCKET_TIMEOUT;
import static coop.tecso.udaa.utils.ParamHelper.getInteger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Models a a Web Service call.
 */
public final class WebService {

	/**
	 * The webServiceUrl should be the name of the service you are going to be
	 * using.
	 */
	WebService(String webServiceUrl) {
		this.httpClient = createHttpClient();
		this.localContext = new BasicHttpContext();
		this.webServiceUrl = webServiceUrl;
	}

	/**
	 * Makes a HttpPost\WebInvoke on a Web Service.
	 */
	String webInvoke(String methodName, Map<String, Object> params) {
		JSONObject jsonObject = Object(params);
		return webInvoke(methodName, jsonObject.toString(), "application/json");
	}

	/**
	 * Makes a HttpGet/WebGet on a Web service.
	 */
	public String webGet(String methodName, Map<String, String> params) {
		String getUrl = webServiceUrl + methodName;
		int i = 0;
		for (Map.Entry<String, String> param : params.entrySet()) {
			if (i == 0) {
				getUrl += "?";
			} else {
				getUrl += "&";
			}
			try {
				getUrl += param.getKey() + "="
						+ URLEncoder.encode(param.getValue(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			i++;
		}

		HttpGet httpGet = new HttpGet(getUrl);
		Log.e("WebGetURL: ", getUrl);

		try {
			response = httpClient.execute(httpGet);
		} catch (Exception e) {
			Log.e("WebService:", e.getMessage());
		}

		try {
			strResponse = EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			Log.e("WebService:", e.getMessage());
		}

		return strResponse;
	}

	private static JSONObject Object(Object o) {
		try {
			return new JSONObject(new Gson().toJson(o));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public InputStream getHttpStream(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;

		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))
			throw new IOException("Not an HTTP connection");

		try {
			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.connect();

			response = httpConn.getResponseCode();

			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
			}
		} catch (Exception e) {
			throw new IOException("Connection error");
		}
		return in;
	}

	void abort() {
		try {
			if (httpClient != null) {
				Log.i(LOG_TAG, "Aborting web service call");
				httpPost.abort();
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Failed to abort connection: ", e);
		}
	}

	public void clearCookies() {
		httpClient.getCookieStore().clear();
	}

	// Implementation helpers

	private static final String LOG_TAG = WebService.class.getName();

	private String webServiceUrl;
	private String strResponse;

	private DefaultHttpClient httpClient;
	private HttpContext localContext;
	private HttpResponse response;
	private HttpPost httpPost;

	// Methods
	String webInvoke(String methodName, String data, String contentType) {
		Log.d(LOG_TAG, "Sending ..." + data);

		// reset fields
		strResponse = null;

		httpClient.getParams().setParameter(ClientPNames.COOKIE_POLICY,
				CookiePolicy.RFC_2109);

		httpPost = new HttpPost(webServiceUrl + methodName);
		response = null;

		StringEntity tmp = null;
		httpPost.setHeader(
				"Accept",
				"text/html,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");

		// Set the content type
		if (contentType != null) {
			httpPost.setHeader("Content-Type", contentType);
		} else {
			httpPost.setHeader("Content-Type",
					"application/x-www-form-urlencoded");
		}

		// Encode data
		try {
			tmp = new StringEntity(data, "UTF-8");
			Log.d(LOG_TAG, webServiceUrl + methodName + "?" + data);
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, "UnsupportedEncodingException: " + e);
		}

		httpPost.setEntity(tmp);

		// Execute the call
		try {
			response = httpClient.execute(httpPost, localContext);
			if (response != null) {
				strResponse = EntityUtils.toString(response.getEntity());
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Unknown exception: " + e);
		}

		return strResponse;
	}

	private class UDAASSLSocketFactory extends SSLSocketFactory {
		private SSLContext sslContext = SSLContext.getInstance("TLS");

		UDAASSLSocketFactory(KeyStore truststore) throws Exception {
			super(truststore);

			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}
				public void checkClientTrusted(X509Certificate[] certs,	String authType) {}
				public void checkServerTrusted(X509Certificate[] certs,	String authType) {}
			} };
			sslContext.init(null, trustAllCerts, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,	port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

	private DefaultHttpClient createHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new UDAASSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			// Set up the http version and charset.
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			// Set up the timeouts for the connection.
			int connectionTimeout = getInteger(CONNECTION_TIMEOUT, 40000);
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
			//
			int socketTimeout = getInteger(SOCKET_TIMEOUT, connectionTimeout);
			HttpConnectionParams.setSoTimeout(params, socketTimeout);

            HttpConnectionParams.setSocketBufferSize(params, 8192); // URGMNT-163

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			Log.e(LOG_TAG, "**ERROR**", e);
			return new DefaultHttpClient();
		}
	}
}