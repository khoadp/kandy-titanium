package io.kandy;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.genband.kandy.api.IKandyGlobalSettings;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.chats.KandyChatSettings;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.ConnectionType;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.utils.FileUtils;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiContext;

import java.io.File;

/**
 * Kandy module
 * 
 * @author kodeplusdev
 */
@Kroll.module(name = "Kandy", id = "io.kandy")
public class KandyModule extends KrollModule {

	// Standard Debugging variables
	private static final String LCAT = KandyModule.class.getSimpleName();
	// private static final boolean DBG = TiConfig.LOGD;

	// Status Constants
	@Kroll.constant
	public static final int STATUS_ERROR = 0;
	@Kroll.constant
	public static final int STATUS_SUCCESS = 1;

	// KandyDeviceContactsFilter Constants
	@Kroll.constant
	public static final String DEVICE_CONTACT_FILTER_ALL = "ALL";
	@Kroll.constant
	public static final String DEVICE_CONTACT_FILTER_HAS_EMAIL_ADDRESS = "HAS_EMAIL_ADDRESS";
	@Kroll.constant
	public static final String DEVICE_CONTACT_FILTER_HAS_PHONE_NUMBER = "HAS_PHONE_NUMBER";
	@Kroll.constant
	public static final String DEVICE_CONTACT_FILTER_IS_FAVORITE = "IS_FAVORITE";

	// KandyDomainContactFilter Constants
	@Kroll.constant
	public static final String DOMAIN_CONTACT_FILTER_ALL = "ALL";
	@Kroll.constant
	public static final String DOMAIN_CONTACT_FILTER_FIRST_AND_LAST_NAME = "FIRST_AND_LAST_NAME";
	@Kroll.constant
	public static final String DOMAIN_CONTACT_FILTER_USER_ID = "USER_ID";
	@Kroll.constant
	public static final String DOMAIN_CONTACT_FILTER_PHONE = "PHONE";

	// KandyRecordType Constants
	@Kroll.constant
	public static final String RECORD_TYPE_CONTACT = "CONTACT";
	@Kroll.constant
	public static final String RECORD_TYPE_GROUP = "GROUP";

	// KandyThumbnailSize Constants
	@Kroll.constant
	public static final String THUMBNAIL_SIZE_SMALL = "SMALL";
	@Kroll.constant
	public static final String THUMBNAIL_SIZE_MEDIUM = "MEDIUM";
	@Kroll.constant
	public static final String THUMBNAIL_SIZE_LARGE = "LARGE";

	// KandyCameraInfo Constants
	@Kroll.constant
	public static final String CAMERA_INFO_FACING_BACK = "FACING_BACK";
	@Kroll.constant
	public static final String CAMERA_INFO_FACING_FRONT = "FACING_FRONT";
	@Kroll.constant
	public static final String CAMERA_INFO_UNKNOWN = "UNKNOWN";

	// KandyConnectionState Constants
	@Kroll.constant
	public static final String CONNECTION_STATE_UNKNOWN = "UNKNOWN";
	@Kroll.constant
	public static final String CONNECTION_STATE_DISCONNECTED = "DISCONNECTED";
	@Kroll.constant
	public static final String CONNECTION_STATE_CONNECTED = "CONNECTED";
	@Kroll.constant
	public static final String CONNECTION_STATE_DISCONNECTING = "DISCONNECTING";
	@Kroll.constant
	public static final String CONNECTION_STATE_CONNECTING = "CONNECTING";
	@Kroll.constant
	public static final String CONNECTION_STATE_FAILED = "FAILED";

	private SharedPreferences prefs;

	public KandyModule(TiContext context){
		prefs = PreferenceManager.getDefaultSharedPreferences(context.getActivity());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onCreate(Activity activity, Bundle savedInstanceState) {
		Log.i(LCAT, "onCreate() was invoked.");
		super.onCreate(activity, savedInstanceState);

		// Initialize Kandy SDK
		Kandy.initialize(activity,
				prefs.getString(KandyConstant.API_KEY_PREFS_KEY, KandyUtils.getString("kandy_api_key")),
				prefs.getString(KandyConstant.API_SECRET_PREFS_KEY, KandyUtils.getString("kandy_api_secret")));

		applyKandyChatSettings();

		IKandyGlobalSettings settings = Kandy.getGlobalSettings();
		settings.setKandyHostURL(prefs.getString(KandyConstant.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));

		prepareLocalStorage();
	}

	@Override
	public void onResume(Activity activity) {
		Log.i(LCAT, "onResume() was invoked.");
		super.onResume(activity);
	}
	
	@Override
	public void onPause(Activity activity) {
		Log.i(LCAT, "onPause() was invoked.");
		super.onPause(activity);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Kroll.onAppCreate
	public static void onAppCreate(TiApplication app) {
		Log.d(LCAT, "inside onAppCreate");
		// put module initialize code that needs to run when the application is
		// created
		KandyUtils.initialize(app);
	}

	private void prepareLocalStorage() {
		File localStorageDirectory = FileUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE);
		FileUtils.clearDirectory(localStorageDirectory);
		FileUtils.copyAssets(getActivity().getApplicationContext(), localStorageDirectory);
	}

	public void applyKandyChatSettings() {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		KandyChatSettings settings = Kandy.getServices().getChatService().getSettings();

		String value = sp.getString(KandyConstant.PREF_KEY_POLICY, null);
		if (value != null) { // otherwise will be used default setting from SDK
			ConnectionType downloadPolicy = ConnectionType.valueOf(value);
			settings.setAutoDownloadMediaConnectionType(downloadPolicy);
		}

		int uploadSize = sp.getInt(KandyConstant.PREF_KEY_MAX_SIZE, -1);

		if (uploadSize != -1) { // otherwise will be used default setting from
								// SDK
			try {
				settings.setMediaMaxSize(uploadSize);
			} catch (KandyIllegalArgumentException e) {
				Log.d(LCAT, "applyKandyChatSettings: " + e.getMessage());
			}
		}

		value = sp.getString(KandyConstant.PREF_KEY_PATH, null);
		if (value != null) { // otherwise will be used default setting from SDK
			File downloadPath = new File(value);
			settings.setDownloadMediaPath(downloadPath);
		}

		value = sp.getString(KandyConstant.PREF_KEY_THUMB_SIZE, null);
		if (value != null) { // otherwise will be used default setting from SDK
			KandyThumbnailSize thumbnailSize = KandyThumbnailSize.valueOf(value);
			settings.setAutoDownloadThumbnailSize(thumbnailSize);
		}
	}

	@Kroll.method
	public void setKandyChatSettings(KrollDict options) {
		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(KandyConstant.PREF_KEY_POLICY, options.getString(KandyConstant.PREF_KEY_POLICY)).apply();
		edit.putString(KandyConstant.PREF_KEY_MAX_SIZE, options.getString(KandyConstant.PREF_KEY_MAX_SIZE)).apply();
		edit.putString(KandyConstant.PREF_KEY_PATH, options.getString(KandyConstant.PREF_KEY_PATH)).apply();
		edit.putString(KandyConstant.PREF_KEY_THUMB_SIZE, options.getString(KandyConstant.PREF_KEY_THUMB_SIZE)).apply();
	}

	/**
	 * Set Kandy api keys.
	 * 
	 * @param apiKey
	 *            The api key.
	 * @param apiSecret
	 *            The secret key.
	 */
	@Kroll.method
	public void setKey(String apiKey, String apiSecret) {
		Log.d(LCAT, "Initializing...");

		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(KandyConstant.API_KEY_PREFS_KEY, apiKey).apply();
		edit.putString(KandyConstant.API_SECRET_PREFS_KEY, apiSecret).apply();

		Kandy.initialize(getActivity(), apiKey, apiSecret);
	}

	/**
	 * Set Kandy host url.
	 * 
	 * @param url
	 *            The host url.
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setHostUrl(String url) {
		Log.d(LCAT, "Setting host url: " + url);

		SharedPreferences.Editor edit = prefs.edit();
		edit.putString(KandyConstant.KANDY_HOST_PREFS_KEY, url).apply();

		IKandyGlobalSettings settings = Kandy.getGlobalSettings();
		settings.setKandyHostURL(prefs.getString(KandyConstant.KANDY_HOST_PREFS_KEY, settings.getKandyHostURL()));
	}

	/**
	 * Get Kandy host url.
	 * 
	 * @return The host url.
	 */
	@Kroll.getProperty
	@Kroll.method
	public String getHostUrl() {
		return Kandy.getGlobalSettings().getKandyHostURL();
	}

	/**
	 * Get Kandy report.
	 * 
	 * @return The Kandy report.
	 */
	@Kroll.method
	public String getReport() {
		return Kandy.getGlobalSettings().getReport();
	}

	/**
	 * Get current session.
	 * 
	 * @return The session information.
	 */
	@Kroll.method
	public KrollDict getSession() {
		KrollDict obj = new KrollDict();

		KrollDict domain = new KrollDict();
		domain.put("apiKey", Kandy.getSession().getKandyDomain().getApiKey());
		domain.put("apiSecret", Kandy.getSession().getKandyDomain().getApiSecret());
		domain.put("name", Kandy.getSession().getKandyDomain().getName());

		KrollDict user = new KrollDict();
		user.put("id", Kandy.getSession().getKandyUser().getUserId());
		user.put("name", Kandy.getSession().getKandyUser().getUser());
		user.put("deviceId", Kandy.getSession().getKandyUser().getKandyDeviceId());
		user.put("password", Kandy.getSession().getKandyUser().getPassword());

		obj.put("domain", domain);
		obj.put("user", user);

		return obj;
	}
}
