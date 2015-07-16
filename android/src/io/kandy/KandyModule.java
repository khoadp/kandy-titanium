package io.kandy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.genband.kandy.api.IKandyGlobalSettings;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.addressbook.KandyDeviceContactsFilter;
import com.genband.kandy.api.services.addressbook.KandyDomainContactFilter;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;

/**
 * Kandy module
 * 
 * @author kodeplusdev
 * 
 */
@Kroll.module(name = "Kandy", id = "io.kandy")
public class KandyModule extends KrollModule {

	// Standard Debugging variables
	private static final String LCAT = KandyModule.class.getSimpleName();
	// private static final boolean DBG = TiConfig.LOGD;

	@Kroll.constant
	public static final int STATUS_ERROR = 0;
	@Kroll.constant
	public static final int STATUS_SUCCESS = 1;

	// KandyDeviceContactsFilter Constants
	@Kroll.constant
	public static int ALL = KandyDeviceContactsFilter.ALL.ordinal();
	@Kroll.constant
	public static int HAS_EMAIL_ADDRESS = KandyDeviceContactsFilter.HAS_EMAIL_ADDRESS
			.ordinal();
	@Kroll.constant
	public static int HAS_PHONE_NUMBER = KandyDeviceContactsFilter.HAS_PHONE_NUMBER
			.ordinal();
	@Kroll.constant
	public static int IS_FAVORITE = KandyDeviceContactsFilter.IS_FAVORITE
			.ordinal();

	// KandyDomainContactFilter Constants
	@Kroll.constant
	public static int FIRST_AND_LAST_NAME = KandyDomainContactFilter.FIRST_AND_LAST_NAME
			.ordinal();

	@Kroll.constant
	public static int USER_ID = KandyDomainContactFilter.USER_ID.ordinal();

	@Kroll.constant
	public static int PHONE = KandyDomainContactFilter.PHONE.ordinal();

	private SharedPreferences prefs;

	public KandyModule() {
		super();
	}

	@Override
	public void onCreate(Activity activity, Bundle savedInstanceState) {
		super.onCreate(activity, savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(activity);

		// Initialize Kandy SDK
		Kandy.initialize(
				activity,
				prefs.getString(KandyConstant.API_KEY_PREFS_KEY,
						KandyUtils.getString("kandy_api_key")),
				prefs.getString(KandyConstant.API_SECRET_PREFS_KEY,
						KandyUtils.getString("kandy_api_secret")));

		IKandyGlobalSettings settings = Kandy.getGlobalSettings();
		settings.setKandyHostURL(prefs.getString(
				KandyConstant.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));
	}

	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		Log.d(LCAT, "inside onAppCreate");
		// put module initialize code that needs to run when the application is
		// created
		KandyUtils.initialize(app);
	}

	/**
	 * Setup Kandy API
	 * 
	 * @param args
	 */
	@Kroll.method
	public void setKey(String apiKey, String apiSecret) {
		Log.d(LCAT, "Initializing...");
		Kandy.initialize(getActivity(), apiKey, apiSecret);
	}

	@Kroll.setProperty
	@Kroll.method
	public void setHostUrl(String url) {
		Log.d(LCAT, "Setting host url: " + url);

		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(KandyConstant.KANDY_HOST_PREFS_KEY, url).apply();

		IKandyGlobalSettings settings = Kandy.getGlobalSettings();
		settings.setKandyHostURL(prefs.getString(
				KandyConstant.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));
	}

	@Kroll.getProperty
	@Kroll.method
	public String getHostUrl() {
		return Kandy.getGlobalSettings().getKandyHostURL();
	}

	@Kroll.method
	public String getReport() {
		return Kandy.getGlobalSettings().getReport();
	}

	@Kroll.method
	public KrollDict getSession() {
		KrollDict obj = new KrollDict();

		KrollDict domain = new KrollDict();
		domain.put("apiKey", Kandy.getSession().getKandyDomain().getApiKey());
		domain.put("apiSecret", Kandy.getSession().getKandyDomain()
				.getApiSecret());
		domain.put("name", Kandy.getSession().getKandyDomain().getName());

		KrollDict user = new KrollDict();
		user.put("id", Kandy.getSession().getKandyUser().getUserId());
		user.put("name", Kandy.getSession().getKandyUser().getUser());
		user.put("deviceId", Kandy.getSession().getKandyUser()
				.getKandyDeviceId());
		user.put("password", Kandy.getSession().getKandyUser().getPassword()); // FIXME:
																				// security?

		obj.put("domain", domain);
		obj.put("user", user);

		return obj;
	}
}