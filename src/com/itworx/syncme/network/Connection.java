package com.itworx.syncme.network;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.DetailedState;

import com.itworx.syncme.model.Property;

public class Connection {
	private static final String URL = "https://ec2-107-21-241-72.compute-1.amazonaws.com/api/";
	public final static int SUCCESSED = 111;
	public final static int FAILED = 110;
	public final static String FAILED_FORMATE = "00000000-0000-0000-0000-000000000000";

	/**
	 * 
	 * @param requestName
	 * @param httpMethod
	 *            HTTP.POST or HTTP.GET
	 * @param property
	 *            as key and value to be concatenated with url
	 * @param ParsingClass
	 *            the response class type which the data be reflected
	 * @param body
	 *            the body which be setted on the request
	 * @return responseEntity
	 */

	protected Object sendRequest(String requestName, HttpMethod httpMethod,
			ArrayList<Property> propertyList, Class<?> ParsingClass, Object body) {

		RestTemplate restTemplate = new RestTemplate();

		// The HttpComponentsClientHttpRequestFactory uses the
		// org.apache.http package to make network requests
		restTemplate
				.setRequestFactory(new HttpComponentsClientHttpRequestFactory(
						getNewHttpClient()));

		String url = URL + requestName;

		if (null != propertyList) {
			url += "?";
			for (int i = 0; i < propertyList.size(); i++) {
				if (i != (propertyList.size() - 1)) {
					url += propertyList.get(i).getKey() + "="
							+ propertyList.get(i).getValue() + "&";
				} else {
					url += propertyList.get(i).getKey() + "="
							+ propertyList.get(i).getValue();
				}
			}

		}
		System.out.println("the url is " + url);

		// Make the HTTP GET request, marshaling the response to a JSON
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.setContentType(new MediaType("application", "json"));

		// Add the json message converter
		restTemplate.getMessageConverters().add(new GsonHttpMessageConverter());

		// THE user is body
		HttpEntity<Object> requestEntity;
		if (null == body)
			requestEntity = new HttpEntity<Object>(requestHeaders);
		else
			requestEntity = new HttpEntity<Object>(body, requestHeaders);
		ResponseEntity<?> responseEntity = restTemplate.exchange(url,
				httpMethod, requestEntity, ParsingClass);

		return responseEntity;
	}

	/*
	 * Copy from
	 * http://www.ipragmatech.com/secure-rest-web-service-mobile-application
	 * -android.html
	 */
	public HttpClient getNewHttpClient() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new EasySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ThreadSafeClientConnManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			return new DefaultHttpClient();
		}
	}

	public int checkConnection(Context context) {
		final ConnectivityManager connMgr = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		final android.net.NetworkInfo wifi = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		final android.net.NetworkInfo mobile = connMgr
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifi.isAvailable()
				&& wifi.getDetailedState() == DetailedState.CONNECTED) {
			return 1;
		} else if (mobile.isAvailable()
				&& mobile.getDetailedState() == DetailedState.CONNECTED) {
			return 2;
		}
		return 0;

	}

	/*
	 * Copy from
	 * http://www.ipragmatech.com/secure-rest-web-service-mobile-application
	 * -android.html
	 */

	public class EasySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public EasySSLSocketFactory(KeyStore truststore)
				throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port,
				boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host,
					port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}

	}
}
