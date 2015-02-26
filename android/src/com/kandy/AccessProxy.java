package com.kandy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiC;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.util.TiConvert;
import org.appcelerator.titanium.view.TiCompositeLayout;
import org.appcelerator.titanium.view.TiUIView;
import org.appcelerator.titanium.view.TiCompositeLayout.LayoutArrangement;

import android.app.Activity;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;

@Kroll.proxy(creatableInModule=KandyModule.class)
public class AccessProxy extends KrollProxy {
	
	public AccessProxy(){
		super();
	}
	
	@Kroll.method
	public void login(HashMap args){
		String username = (String)args.get("username");
		String password = (String)args.get("password");
		final KrollFunction success = (KrollFunction)args.get("success");
		final KrollFunction error = (KrollFunction)args.get("error");
		
		KandyRecord kandyUser;
		
		try {
            kandyUser = new KandyRecord(username);

        } catch (IllegalArgumentException ex) {
        	if(error != null){
        		HashMap result = new HashMap();
            	result.put("status", KandyModule.STATUS_ERROR);
                error.call(getKrollObject(), result);	
        	}
            return;
        }

		Kandy.getAccess().login(kandyUser, password, new KandyLoginResponseListener() {

            @Override
            public void onRequestFailed(int responseCode, String err) {
            	if(error != null){
            		HashMap result = new HashMap();
                	result.put("status", KandyModule.STATUS_ERROR);
                	result.put("code", responseCode);
                	result.put("message", err);
                    error.call(getKrollObject(), result);	
            	}
            }
            @Override
            public void onLoginSucceeded() {
            	if(success != null){
            		HashMap result = new HashMap();
                	result.put("status", KandyModule.STATUS_SUCCESS);
                    success.call(getKrollObject(), result);	
            	}
            }
        });
	}
	
	@Kroll.method
	public void logout(HashMap args){
		final KrollFunction success = (KrollFunction)args.get("success");
		final KrollFunction error = (KrollFunction)args.get("error");
		
		Kandy.getAccess().logout(new KandyLogoutResponseListener() {
			
			@Override
			public void onRequestFailed(int responseCode, String err) {
				if(error != null){
					HashMap result = new HashMap();
	            	result.put("status", KandyModule.STATUS_ERROR);
	            	result.put("code", responseCode);
	            	result.put("message", err);
	                error.call(getKrollObject(), result);	
				}				
			}
			
			@Override
			public void onLogoutSucceeded() {
				if(success != null){
					HashMap result = new HashMap();
	            	result.put("status", KandyModule.STATUS_SUCCESS);
	                success.call(getKrollObject(), result);	
				}				
			}
		});
	}
}
