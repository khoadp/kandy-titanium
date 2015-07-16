package io.kandy.proxy;

import android.app.Activity;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyModule;
import io.kandy.KandyUtils;
import io.kandy.proxy.view.AccessViewProxy;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * Access service
 * 
 * @author kodeplusdev
 * 
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class AccessProxy extends TiViewProxy {

	public static final String LCAT = AccessProxy.class.getSimpleName();

	// Callbacks Be used by widget
	private KrollDict callbacks = null;

	private AccessViewProxy viewProxy;

	public AccessProxy() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TiUIView createView(Activity activity) {
		viewProxy = new AccessViewProxy(this);
		return viewProxy;
	}

	/**
	 * Set callbacks for login action
	 * 
	 * @param callbacks
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setCallbacks(KrollDict callbacks) {
		this.callbacks = callbacks;
	}

	/**
	 * Register/login the user on the server with credentials received from
	 * admin.
	 * 
	 * @param args
	 */
	@Kroll.method
	public void login(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");
		final String username = args.getString("username");
		final String password = args.getString("username");

		KandyRecord kandyUser;

		try {
			kandyUser = new KandyRecord(username);

		} catch (KandyIllegalArgumentException ex) {
			String err = KandyUtils
					.getString("kandy_login_empty_username_text");
			KandyUtils.sendFailResult(getKrollObject(), error, err);
			Log.d(LCAT, err);
			return;
		}

		if (password == null || password.isEmpty()) {
			String err = KandyUtils
					.getString("kandy_login_empty_password_text");
			KandyUtils.sendFailResult(getKrollObject(), error, err);
			Log.d(LCAT, err);
			return;
		}

		Kandy.getAccess().login(kandyUser, password,
				new KandyLoginResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Log.d(LCAT,
								"KandyLoginResponseListener->onRequestFailed() was invoked: "
										+ String.valueOf(code) + " - " + err);
						KandyUtils.sendFailResult(getKrollObject(), error,
								code, err);
					}

					@Override
					public void onLoginSucceeded() {
						Log.d(LCAT,
								"KandyLoginResponseListener->onLoginSucceeded() was invoked.");
						KandyUtils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

	/**
	 * Register/login the user on the server with credentials received from
	 * admin.
	 * 
	 * @param username
	 * @param password
	 */
	public void login(KrollFunction success, KrollFunction error,
			String username, String password) {
		KrollDict loginArgs = KandyUtils.getKrollDictFromCallbacks(callbacks,
				"login");

		loginArgs.put("success", KandyUtils.joinKrollFunctions(success,
				(KrollFunction) loginArgs.get("success")));
		loginArgs.put("error", KandyUtils.joinKrollFunctions(success,
				(KrollFunction) loginArgs.get("error")));

		loginArgs.put("username", username);
		loginArgs.put("password", password);

		this.login(loginArgs);
	}

	/**
	 * This method unregisters user from the Kandy server.
	 * 
	 * @param callbacks
	 */
	@Kroll.method
	public void logout(KrollDict callbacks) {
		final KrollFunction success = (KrollFunction) callbacks.get("success");
		final KrollFunction error = (KrollFunction) callbacks.get("error");

		Kandy.getAccess().logout(new KandyLogoutResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT,
						"KandyLogoutResponseListener->onRequestFailed() was invoked: "
								+ String.valueOf(code) + " - " + err);
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onLogoutSucceeded() {
				Log.d(LCAT,
						"KandyLogoutResponseListener->onLogoutSucceeded() was invoked.");
				KandyUtils.sendSuccessResult(getKrollObject(), success);
			}
		});
	}

	/**
	 * This method unregisters user from the Kandy server.
	 */
	public void logout(KrollFunction success, KrollFunction error) {
		KrollDict logoutArgs = KandyUtils.getKrollDictFromCallbacks(callbacks,
				"logout");

		logoutArgs.put("success", KandyUtils.joinKrollFunctions(success,
				(KrollFunction) logoutArgs.get("success")));
		logoutArgs.put("error", KandyUtils.joinKrollFunctions(success,
				(KrollFunction) logoutArgs.get("error")));

		logout(logoutArgs);
	}
}
