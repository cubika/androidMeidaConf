package com.http;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.AsyncTask;

public class GetUserInfoTask extends AsyncTask<String,Integer,String>{

	public static String userName;
	public static String userId;
	public static String gender;
	public static String cellphoneNumber;
	public static String email;
	private Map<String,String> userInfo=new HashMap<String,String>();
	
	@Override
	protected String doInBackground(String... params) {
		String url=params[0];
		String result=null;
		try {
			System.out.println("url is "+url);
			result=HttpUtils.getSourceCode(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("The user info result is :"+result);
		return result;
	}
	
	@Override
	protected void onPostExecute(String result){
		if(result==null || result.trim().length()<=0)
			return;
		
//	    var userName='chenlve';
//	    var userId='74';
//	    var gender='male';
//	    var cardType='null';
//	    var cardNumber='';
//	    var email='clcl-1126@163.com';
//	    var phoneNumber='62284401';
//	    var cellphoneNumber='13810693291';
//	    var address='';
//	    var postNumber='';
//	    var informationPrefer='email';
//	    var headUrl='1299587160568.jpg';
//	    var nameUsed=true;
		Pattern pattern=Pattern.compile("var (\\w+)='(.*?)';");
		Matcher matcher=pattern.matcher(result);
		
		while(matcher.find()){
			System.out.println(matcher.group(1)+":"+matcher.group(2));
			userInfo.put(matcher.group(1), matcher.group(2));
		}
		userName=userInfo.get("userName");
		userId=userInfo.get("userId");
		gender=userInfo.get("gender");
		cellphoneNumber=userInfo.get("cellphoneNumber");
		email=userInfo.get("email");
		
	}
	
	@Override
	protected void onPreExecute() {
	}
}