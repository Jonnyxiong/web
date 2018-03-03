package com.ucpaas.sms.util;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucpaas.sms.model.SMSResponse;
import com.ucpaas.sms.util.web.SSLHttpClient;


@SuppressWarnings("deprecation")
public class HttpUtils {

	private static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	public static final int M2_SIZE = 10 * 1024 * 1024;
	
	/**
	 * Http 请求方法
	 * @param url
	 * @param content
	 * @return
	 */
	public static String httpPost(String url, String content, boolean needSSL) {
		// 创建HttpPost
		String result = null;
		HttpClient httpClient = getHttpClient(needSSL, StringUtil.getHostFromURL(url));
		try {
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED + ";charset=utf-8");
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(2000).setConnectTimeout(2000).build();
			httpPost.setConfig(requestConfig);
			BasicHttpEntity requestBody = new BasicHttpEntity();
			requestBody.setContent(new ByteArrayInputStream(content.getBytes("utf-8")));
			requestBody.setContentLength(content.getBytes("utf-8").length);
			httpPost.setEntity(requestBody);
			// 执行客户端请求
			HttpEntity entity = httpClient.execute(httpPost).getEntity();
			
			if (entity != null) {
				result = EntityUtils.toString(entity, "utf-8");
				EntityUtils.consume(entity);
			}

		} catch (Throwable e) {
			logger.error("【HTTP请求失败】: url={}, content={}", url, content );
		}
		
		return result;
	}
	
	
	public static DefaultHttpClient getHttpClient(boolean sslClient, String host){
		DefaultHttpClient httpclient=null;
		if (sslClient) {
			try {
				SSLHttpClient chc = new SSLHttpClient();
				InetAddress address = null;
				String ip;
				try {
				   address = InetAddress.getByName(host);
				   ip = address.getHostAddress().toString();
				   httpclient = chc.registerSSL(ip,"TLS",443,"https");
				} catch (UnknownHostException e) {
				   logger.error("获取请求服务器地址失败：host = {} " + host);
				   e.getStackTrace().toString();
				}
				HttpParams hParams=new BasicHttpParams();
				hParams.setParameter("https.protocols", "SSLv3,SSLv2Hello");
				httpclient.setParams(hParams);
			} catch (KeyManagementException e) {
				logger.error(e.getStackTrace().toString());
			}catch (NoSuchAlgorithmException e) {
				logger.error(e.getStackTrace().toString());
			}
		}else {
			httpclient=new DefaultHttpClient();
		}
		return httpclient;
	}
	
	public static String fakeSMSPHttpPost() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("a", "a");
		map.put("b", "b");
		map.put("c", "c");
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		list.add(map);
		int i = 1;
		SMSResponse resp = new SMSResponse(i, list);
		String json = JsonUtils.toJson(resp);
		
		return json;
	}
	
	
	/**
	 * 根据url下载文件，保存到filepath中
	 * 
	 * @param url
	 * @param filepath
	 * @return
	 */
	public static String download(String url, String filepath) {
		try {
			@SuppressWarnings("resource")
			HttpClient client = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = client.execute(httpget);

			HttpEntity entity = response.getEntity();
			InputStream is = entity.getContent();
			File file = new File(filepath);
			file.getParentFile().mkdirs();
			FileOutputStream fileout = new FileOutputStream(file);
			/**
			 * 根据实际运行效果 设置缓冲区大小
			 */
			byte[] buffer = new byte[M2_SIZE];
			int ch = 0;
			while ((ch = is.read(buffer)) != -1) {
				fileout.write(buffer, 0, ch);
			}
			is.close();
			fileout.flush();
			fileout.close();
			return filepath;

		} catch (Exception e) {
			logger.error("下载文档失败，url="+url, e);
		}
		return null;
	}


	public static void main(String[] args) throws UnknownHostException {
		
		System.out.println(InetAddress.getByName("api.ucpaas.com").getHostAddress().toString());
	}
}
