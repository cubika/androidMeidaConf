package com.http;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpUtils {
	
	public static String jSessionID;
  
	
	public static String sendPostMessage(String urlPath,Map<String,String> params,String encode) throws IOException{
		StringBuffer buffer = new StringBuffer();		
		try {
			if(params!=null&&!params.isEmpty()){
				for(Map.Entry<String, String> entry:params.entrySet()){
					buffer.append(entry.getKey())
					.append("=")
					.append(URLEncoder.encode(entry.getValue(),encode))
					.append("&");
				}
			}
			if(buffer.length()!=0)
				buffer.deleteCharAt(buffer.length()-1);
			Log.v("SendPost",buffer.toString());
			
			URL url = new URL(urlPath); 
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

			// //设置连接属性
			httpConn.setConnectTimeout(5000);
			httpConn.setDoOutput(true);          // 使用 URL 连接进行输出
			httpConn.setDoInput(true);           // 使用 URL 连接进行输入
			httpConn.setUseCaches(false);        // 忽略缓存
			httpConn.setRequestMethod("POST");   // 设置URL请求方法
			httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			httpConn.setRequestProperty("Charset", "UTF-8");
			httpConn.setRequestProperty("Connection", "Keep-Alive");
			httpConn.setRequestProperty("Cookie", "JSESSIONID="+jSessionID);//session也是根据cookie来实现的呦
			
			byte[] mydata = buffer.toString().getBytes();
			httpConn.setRequestProperty("Content-Length",String.valueOf(mydata.length));
			
			OutputStream outputStream = httpConn.getOutputStream();
			outputStream.write(mydata);
		//	System.out.println("httpConn.getResponseCode()  "+httpConn.getResponseCode());
			if(httpConn.getResponseCode() == 200){
				return changeInputStream(httpConn.getInputStream(),encode);
			}            
	            

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
        	
		return "";
	}

	private static String changeInputStream(InputStream inputStream,
			String encode) {
		// TODO Auto-generated method stub
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] data = new byte[1024];
		int len = 0;
		String result="";
		if(inputStream!=null){
			
			try {
				while((len=inputStream.read(data))!=-1){
					outputStream.write(data, 0, len);
				}
				result = new String(outputStream.toByteArray(),encode);
			//	System.out.println("return result"+result);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
	}
	
	
	public static String getSourceCode(String url) throws IOException{
		System.out.println("get source code from:"+url);
		HttpClient client = new DefaultHttpClient();
		HttpGet request = new HttpGet(url);
		request.setHeader("Cookie", "JSESSIONID="+jSessionID);
		HttpResponse response = client.execute(request);

		String html = "";
		InputStream in = response.getEntity().getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		StringBuilder str = new StringBuilder();
		String line = null;
		while((line = reader.readLine()) != null)
		{
			if(line.contains("Ext.onReady"))
				break;
		    str.append(line);
		}
		in.close();
		html = str.toString();
		return html;
	}
	
	public static String execRequest(String urlPath,List<NameValuePair> params) throws IOException{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		HttpEntity httpEntity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
		HttpPost httpPost = new HttpPost(urlPath);
		httpPost.setEntity(httpEntity);
		HttpResponse httpResponse = httpClient.execute(httpPost);
		String ret=null;
		if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            HttpEntity entity = httpResponse.getEntity();
            ret = EntityUtils.toString(entity);
            CookieStore mCookieStore = httpClient.getCookieStore();
            List<Cookie> cookies = mCookieStore.getCookies();
            for (int i = 0; i < cookies.size(); i++) {
            	System.out.println(cookies.get(i).getName()+":"+cookies.get(i).getValue());
            	if(cookies.get(i).getName().equals("JSESSIONID"))
            		jSessionID=cookies.get(i).getValue();
            }
		}
		return ret;
	}

	
}
