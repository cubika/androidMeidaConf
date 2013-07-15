package com.http;

import com.rongdian.SipTerm;

public class RegisterThread extends Thread  
{  
	long term;  
    public RegisterThread(long term) {  
        this.term = term;       
    }
    @Override  
    public void run() {    	    	
    	
    	while(true){
			try {
				boolean flag = SipTerm.doRegister(term, 120);
				if(flag){			
				}
				else{	
				//	System.out.println("Register called bad!");
				}
				Thread.sleep(60000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}	
    }
}