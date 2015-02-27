package com.kandy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.common.KandyResponseListener;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class PresenceServiceProxy extends KrollProxy {

	public PresenceServiceProxy() {
		super();
	}

	@Kroll.method
	public void startWatch(HashMap args) {
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
