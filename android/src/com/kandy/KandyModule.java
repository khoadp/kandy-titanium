package com.kandy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.kroll.common.TiConfig;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;


@Kroll.module(name="Kandy", id="com.kandy")
public class KandyModule extends KrollModule
{
	// Standard Debugging variables
	private static final String LCAT = "KandyModule";
	//private static final boolean DBG = TiConfig.LOGD;
	
	@Kroll.constant public static final int STATUS_ERROR = 0;
	@Kroll.constant public static final int STATUS_SUCCESS = 1;
	
	public KandyModule()
	{
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app)
	{
		Log.d(LCAT, "inside onAppCreate");
		// put module init code that needs to run when the application is created
	}
	
	@Kroll.method
	public void setup(HashMap args){
		String apiKey = (String)args.get("api_key");
		String apiSecret = (String) args.get("api_secret");
		Kandy.initialize(getActivity(), apiKey, apiSecret);
	}
}

