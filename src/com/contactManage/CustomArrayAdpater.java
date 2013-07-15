package com.contactManage;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

public class CustomArrayAdpater extends ArrayAdapter<String> {
	
	private AutoCompleteTextView actv;
	private Context context;
	
	public CustomArrayAdpater(Context context, int textViewResourceId,
			String[] objects,AutoCompleteTextView actv) {
		super(context, textViewResourceId, objects);
		this.actv=actv;
		this.context=context;
	}


    @Override
    public View getView(int position, View convertView, ViewGroup parent){
    	   View v = super.getView(position, convertView, parent);
    	    v.setOnTouchListener(new View.OnTouchListener() {

    	        @Override
    	        public boolean onTouch(View v, MotionEvent event) {

    	            if (event.getAction() == MotionEvent.ACTION_DOWN) {
    	                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    	                imm.hideSoftInputFromWindow(actv.getWindowToken(), 0);
    	            }

    	            return false;
    	        }
    	    });

    	    return v;
    }
}