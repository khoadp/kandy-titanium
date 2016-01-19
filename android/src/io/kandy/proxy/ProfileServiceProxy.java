package io.kandy.proxy;

import io.kandy.KandyModule;
import io.kandy.utils.KandyUtils;

import java.util.ArrayList;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.json.JSONArray;

import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.profile.IKandyDeviceProfile;
import com.genband.kandy.api.services.profile.KandyDeviceProfileParams;
import com.genband.kandy.api.services.profile.KandyDeviceProfileResponseListener;

/**
 * Profile service proxy.
 * 
 * @author kodeplusdev
 * 
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class ProfileServiceProxy extends KrollProxy {

	private static final String LCAT = ProfileServiceProxy.class.getSimpleName();

	@Kroll.method
	public void getUserDeviceProfiles(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getProfileService().getUserDeviceProfiles(new KandyDeviceProfileResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT,
						"KandyDeviceProfileResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
								+ " - " + err);
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onRequestSuccess(ArrayList<IKandyDeviceProfile> profiles) {
				JSONArray results = new JSONArray();

				for (IKandyDeviceProfile profile : profiles) {
					KrollDict p = new KrollDict();
					p.put("deviceDisplayName", profile.getDeviceDisplayName());
					p.put("kandyDeviceId", profile.getKandyDeviceId());
					results.put(p);
				}

				KandyUtils.sendSuccessResult(getKrollObject(), success, results);
			}
		});
	}

	@Kroll.method
	public void updateDeviceProfile(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String deviceDisplayName = args.getString("displayName");
		String deviceName = args.getString("name");
		String deviceFamily = args.getString("fanmily");

		KandyDeviceProfileParams profileParams = new KandyDeviceProfileParams(deviceDisplayName);
		profileParams.setDeviceName(deviceName);
		profileParams.setDeviceFamily(deviceFamily);

		Kandy.getServices().getProfileService().updateDeviceProfile(profileParams, new KandyResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, "KandyResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - "
						+ err);
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.d(LCAT, "KandyResponseListener->onRequestSucceded() was invoked.");
				KandyUtils.sendSuccessResult(getKrollObject(), success);
			}
		});
	}
}
