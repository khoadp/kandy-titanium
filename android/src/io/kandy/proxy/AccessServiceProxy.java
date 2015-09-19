package io.kandy.proxy;

import android.app.Activity;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyConnectServiceNotificationListener;
import com.genband.kandy.api.access.KandyConnectionState;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyModule;
import io.kandy.proxy.views.AccessViewProxy;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * Access service proxy.
 * 
 * @author kodeplusdev
 */
@Kroll.proxy(creatableInModule = KandyModule.class, propertyAccessors = { "type" })
public class AccessServiceProxy extends TiViewProxy implements KandyConnectServiceNotificationListener {

	public static final String LCAT = AccessServiceProxy.class.getSimpleName();

	public static final String ACCESS_TYPE_PASSWORD = "password";
	public static final String ACCESS_TYPE_TOKEN = "token";

	private AccessViewProxy viewProxy;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TiUIView createView(Activity activity) {
		viewProxy = new AccessViewProxy(this);
		return viewProxy;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPause(Activity activity) {
		Log.d(LCAT, "onPause() was invoked.");
		super.onPause(activity);
		unregisterNotificationListener();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResume(Activity activity) {
		Log.d(LCAT, "onResume() was invoked.");
		super.onResume(activity);
		registerNotificationListener();
	}

	@Kroll.method
	public void registerNotificationListener() {
		Kandy.getAccess().registerNotificationListener(this);
	}

	@Kroll.method
	public void unregisterNotificationListener() {
		Kandy.getAccess().unregisterNotificationListener(this);
	}

	/**
	 * Register/login the user on the server with credentials received from
	 * admin.
	 * 
	 * @param args
	 *            The argument login.
	 */
	@Kroll.method
	public void login(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");
		final String username = args.getString("username");
		final String password = args.getString("password");
		final String token = args.getString("token");

		if (token != null && token != "") {
			Kandy.getAccess().login(token, new KandyLoginResponseListener() {

				@Override
				public void onRequestFailed(int code, String err) {
					Log.d(LCAT, "KandyLoginResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
							+ " - " + err);
					KandyUtils.sendFailResult(getKrollObject(), error, code, err);
				}

				@Override
				public void onLoginSucceeded() {
					Log.d(LCAT, "KandyLoginResponseListener->onLoginSucceeded() was invoked.");
					KandyUtils.sendSuccessResult(getKrollObject(), success);
				}
			});
		} else {
			KandyRecord kandyUser;

			try {
				kandyUser = new KandyRecord(username);

			} catch (KandyIllegalArgumentException ex) {
				String err = KandyUtils.getString("kandy_login_empty_username_text");
				KandyUtils.sendFailResult(getKrollObject(), error, err);
				Log.d(LCAT, err);
				return;
			}

			if (password == null || password.isEmpty()) {
				String err = KandyUtils.getString("kandy_login_empty_password_text");
				KandyUtils.sendFailResult(getKrollObject(), error, err);
				Log.d(LCAT, err);
				return;
			}

			Kandy.getAccess().login(kandyUser, password, new KandyLoginResponseListener() {

				@Override
				public void onRequestFailed(int code, String err) {
					Log.d(LCAT, "KandyLoginResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
							+ " - " + err);
					KandyUtils.sendFailResult(getKrollObject(), error, code, err);
				}

				@Override
				public void onLoginSucceeded() {
					Log.d(LCAT, "KandyLoginResponseListener->onLoginSucceeded() was invoked.");
					KandyUtils.sendSuccessResult(getKrollObject(), success);
				}
			});
		}
	}

	/**
	 * This method unregisters user from the Kandy server.
	 * 
	 * @param args
	 *            The arguments logout.
	 */
	@Kroll.method
	public void logout(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getAccess().logout(new KandyLogoutResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, "KandyLogoutResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
						+ " - " + err);
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onLogoutSucceeded() {
				Log.d(LCAT, "KandyLogoutResponseListener->onLogoutSucceeded() was invoked.");
				KandyUtils.sendSuccessResult(getKrollObject(), success);
			}
		});
	}

	@Kroll.method
	public boolean isLoggedIn() {
		return (KandyConnectionState.CONNECTED.equals(Kandy.getAccess().getConnectionState()));
	}

	@Override
	public void onConnectionStateChanged(KandyConnectionState state) {
		Log.d(LCAT, "onConnectionStateChanged() was invoked: " + state.name());
		if (hasListeners("onConnectionStateChanged")) {
			KrollDict props = new KrollDict();
			props.put("state", state.name());
			fireEvent("onConnectionStateChanged", props);
		}
		if (viewProxy != null)
			viewProxy.onConnectionStateChanged(state);
	}

	@Override
	public void onInvalidUser(String error) {
		Log.d(LCAT, "onInvalidUser() was invoked: " + error);
		if (hasListeners("onInvalidUser")) {
			KrollDict props = new KrollDict();
			props.put("error", error);
			fireEvent("onInvalidUser", props);
		}
		if (viewProxy != null)
			viewProxy.onInvalidUser(error);
	}

	@Override
	public void onSDKNotSupported(String error) {
		Log.d(LCAT, "onSDKNotSupported() was invoked: " + error);
		if (hasListeners("onSDKNotSupported")) {
			KrollDict props = new KrollDict();
			props.put("error", error);
			fireEvent("onSDKNotSupported", props);
		}
		if (viewProxy != null)
			viewProxy.onSDKNotSupported(error);
	}

	@Override
	public void onSessionExpired(String error) {
		Log.d(LCAT, "onSessionExpired() was invoked: " + error);
		if (hasListeners("onSessionExpired")) {
			KrollDict props = new KrollDict();
			props.put("error", error);
			fireEvent("onSessionExpired", props);
		}
		if (viewProxy != null)
			viewProxy.onSessionExpired(error);
	}

	@Override
	public void onSocketConnected() {
		Log.d(LCAT, "onSocketConnected() was invoked.");
		fireEvent("onSocketConnected", null);
		if (viewProxy != null)
			viewProxy.onSocketConnected();
	}

	@Override
	public void onSocketConnecting() {
		Log.d(LCAT, "onSocketConnecting() was invoked.");
		fireEvent("onSocketConnecting", null);
		if (viewProxy != null)
			viewProxy.onSocketConnecting();
	}

	@Override
	public void onSocketDisconnected() {
		Log.d(LCAT, "onSocketDisconnected() was invoked.");
		fireEvent("onSocketDisconnected", null);
		if (viewProxy != null)
			viewProxy.onSocketDisconnected();
	}

	@Override
	public void onSocketFailedWithError(String error) {
		Log.d(LCAT, "onSocketFailedWithError() was invoked: " + error);
		if (hasListeners("onSocketFailedWithError")) {
			KrollDict props = new KrollDict();
			props.put("error", error);
			fireEvent("onSocketFailedWithError", props);
		}
		if (viewProxy != null)
			viewProxy.onSocketFailedWithError(error);
	}
}
