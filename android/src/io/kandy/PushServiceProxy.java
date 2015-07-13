package io.kandy;

import android.text.TextUtils;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.google.android.gcm.GCMRegistrar;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

/**
 * Push service
 * 
 * @author kodeplusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class PushServiceProxy extends KrollProxy {

	public PushServiceProxy() {
		super();
	}

	/**
	 * Enable push notification
	 * 
	 * @param args
	 */
	@Kroll.method
	public void enablePushNotification(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");
		
		GCMRegistrar.checkDevice(getActivity());
        GCMRegistrar.checkManifest(getActivity());
        String registrationId = GCMRegistrar.getRegistrationId(getActivity());

        if (TextUtils.isEmpty(registrationId)) {
            GCMRegistrar.register(getActivity(), KandyConstant.GCM_PROJECT_ID);
        }
		
		Kandy.getServices().getPushService().enablePushNotification(registrationId, new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						KandyUtils.sendFailResult(getKrollObject(), error, code, err);

					}

					@Override
					public void onRequestSucceded() {
						KandyUtils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

	/**
	 * Disable push notification
	 * 
	 * @param args
	 */
	@Kroll.method
	public void disablePushNotification(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getPushService()
				.disablePushNotification(new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						KandyUtils.sendFailResult(getKrollObject(), error, code, err);

					}

					@Override
					public void onRequestSucceded() {
						KandyUtils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

}
