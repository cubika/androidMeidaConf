package com.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class HttpUtils {
	
	public HttpUtils() {
		// TODO Auto-generated constructor stub
	}	 
  
	
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

	
}
