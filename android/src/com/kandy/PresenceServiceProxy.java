package com.kandy;

import java.util.ArrayList;
import java.util.Arrays;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.presence.IKandyPresence;
import com.genband.kandy.api.services.presence.KandyPresenceServiceNotificationListener;

/**
 * Presence service
 * 
 * @author kodeplusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class PresenceServiceProxy extends KrollProxy {

	private KandyPresenceServiceNotificationListener _KandyPresenceServiceNotificationListener = null;

	public PresenceServiceProxy() {
		super();
	}

	/**
	 * register notification listener
	 * 
	 * @param callbacks
	 */
	@Kroll.method
	public void registerNotificationListener(final KrollDict callbacks) {
		if (_KandyPresenceServiceNotificationListener != null) {
			unregisterNotificationListener();
		}

		_KandyPresenceServiceNotificationListener = new KandyPresenceServiceNotificationListener() {

			public void onPresenceStateChanged(IKandyPresence presence) {
				KrollDict data = new KrollDict();
				data.put("user", presence.getContactName());
				data.put("state", presence.getState().name());
				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onPresenceStateChanged"), data);
			}
		};

		Kandy.getServices()
				.getPresenceService()
				.registerNotificationListener(
						_KandyPresenceServiceNotificationListener);
	}

	/**
	 * unregister notification listener
	 */
	@Kroll.method
	public void unregisterNotificationListener() {
		if (_KandyPresenceServiceNotificationListener != null) {
			Kandy.getServices()
					.getPresenceService()
					.unregisterNotificationListener(
							_KandyPresenceServiceNotificationListener);
			_KandyPresenceServiceNotificationListener = null;
		}
	}

	/**
	 * Start watch users
	 * 
	 * @param args
	 */
	@Kroll.method
	public void startWatch(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String[] list = (String[]) args.get("list");
		ArrayList<String> arrayList = new ArrayList<String>(Arrays.asList(list));

		Kandy.getServices().getPresenceService()
				.startWatch(arrayList, new KandyResponseListener() {

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
