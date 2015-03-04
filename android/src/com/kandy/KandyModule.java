package com.kandy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsFilter;

/**
 * Kandy module
 * 
 * @author kodeplusdev
 *
 */
@Kroll.module(name = "Kandy", id = "com.kandy")
public class KandyModule extends KrollModule {

	// Standard Debugging variables
	private static final String LCAT = "KandyModule";
	// private static final boolean DBG = TiConfig.LOGD;

	@Kroll.constant
	public static final int STATUS_ERROR = 0;
	@Kroll.constant
	public static final int STATUS_SUCCESS = 1;

	// KandyDeviceContactsFilter Constants
	@Kroll.constant
	public static int ALL = KandyDeviceContactsFilter.ALL.ordinal();
	@Kroll.constant
	public static int HAS_EMAIL_ADDRESS = KandyDeviceContactsFilter.HAS_EMAIL_ADDRESS.ordinal();
	@Kroll.constant
	public static int HAS_PHONE_NUMBER = KandyDeviceContactsFilter.HAS_PHONE_NUMBER.ordinal();
	@Kroll.constant
	public static int IS_FAVORITE = KandyDeviceContactsFilter.IS_FAVORITE.ordinal();

	public KandyModule() {
		super();
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		Log.d(LCAT, "inside onAppCreate");
		// put module init code that needs to run when the application is
		// created
	}

	/**
	 * Setup Kandy API
	 * 
	 * @param args This is an object with the properties: api_key, api_secret
	 */
	@Kroll.method
	public void setup(HashMap args) {
		String apiKey = (String) args.get("api_key");
		String apiSecret = (String) args.get("api_secret");
		Kandy.initialize(getActivity(), apiKey, apiSecret);
	}
}
