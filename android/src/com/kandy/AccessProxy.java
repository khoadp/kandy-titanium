package com.kandy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.common.IKandySession;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class AccessProxy extends KrollProxy {

	public AccessProxy() {
		super();
	}

	@Kroll.method
	public void login(HashMap args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String username = (String) args.get("username");
		String password = (String) args.get("password");
		
		KandyRecord kandyUser;

		try {
			kandyUser = new KandyRecord(username);

		} catch (IllegalArgumentException ex) {
			Utils.sendFailResult(getKrollObject(), error, Utils.getString(
					getActivity(), "kandy_login_empty_username_text"));
			return;
		}

		if (password == null || password.isEmpty()) {
			Utils.sendFailResult(getKrollObject(), error, Utils.getString(
					getActivity(), "kandy_login_empty_password_text"));
			return;
		}

		Kandy.getAccess().login(kandyUser, password,
				new KandyLoginResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onLoginSucceeded() {
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

	@Kroll.method
	public void logout(HashMap args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getAccess().logout(new KandyLogoutResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onLogoutSucceeded() {
				Utils.sendSuccessResult(getKrollObject(), success);
			}
		});
	}

	@Kroll.method
	public void loadSession(HashMap args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		IKandySession session = Kandy.getSession();

		if (session.getKandyUser().getUser() != null) {
			HashMap data = new HashMap();

			data.put("userId", session.getKandyUser().getUserId());
			data.put("user", session.getKandyUser().getUser());
			data.put("domain", session.getKandyDomain().getName());
			data.put("deviceId", session.getKandyUser().getDeviceId());

			Utils.sendSuccessResult(getKrollObject(), success, data);
		} else {
			Utils.sendFailResult(getKrollObject(), error, 404,
					"Session not found.");
		}
	}
}
