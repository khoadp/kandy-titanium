package com.kandy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.common.KandyResponseListener;

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
	public void enablePushNotification(HashMap args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getPushService()
				.enablePushNotification(new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);

					}

					@Override
					public void onRequestSucceded() {
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

	/**
	 * Disable push notification
	 * 
	 * @param args
	 */
	@Kroll.method
	public void disablePushNotification(HashMap args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getPushService()
				.disablePushNotification(new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);

					}

					@Override
					public void onRequestSucceded() {
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

}
