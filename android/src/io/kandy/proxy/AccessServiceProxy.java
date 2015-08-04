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
 * @version 1.2.0
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class AccessServiceProxy extends TiViewProxy implements KandyConnectServiceNotificationListener {

	public static final String LCAT = AccessServiceProxy.class.getSimpleName();

	private KrollDict callbacks = null;

	private AccessViewProxy viewProxy;

	public AccessServiceProxy() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
		if (options.containsKey("callbacks"))
			callbacks = options.getKrollDict("callbacks");
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
	 * {@inheritDoc}
	 */
	@Override
	public void onPause(Activity activity) {
		super.onPause(activity);
		if (viewProxy != null)
			viewProxy.unregisterNotificationListener();
		Kandy.getAccess().unregisterNotificationListener(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
		if (viewProxy != null)
			viewProxy.registerNotificationListener();
		Kandy.getAccess().registerNotificationListener(this);
	}

	/**
	 * Set callbacks for access service.
	 * 
	 * @param callbacks
	 *            The access callbacks.
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
	 *            The argument login.
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
		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onConnectionStateChanged"),
				state.name());
	}

	@Override
	public void onInvalidUser(String error) {
		Log.d(LCAT, "onInvalidUser() was invoked: " + error);
		KandyUtils.sendFailResult(getKrollObject(), (KrollFunction) callbacks.get("onInvalidUser"), error);
	}

	@Override
	public void onSDKNotSupported(String error) {
		Log.d(LCAT, "onSDKNotSupported() was invoked: " + error);
		KandyUtils.sendFailResult(getKrollObject(), (KrollFunction) callbacks.get("onSDKNotSupported"), error);
	}

	@Override
	public void onSessionExpired(String error) {
		Log.d(LCAT, "onSessionExpired() was invoked: " + error);
		KandyUtils.sendFailResult(getKrollObject(), (KrollFunction) callbacks.get("onSessionExpired"), error);
	}

	@Override
	public void onSocketConnected() {
		Log.d(LCAT, "onSocketConnected() was invoked.");
		KandyUtils.sendFailResult(getKrollObject(), (KrollFunction) callbacks.get("onSocketConnected"));
	}

	@Override
	public void onSocketConnecting() {
		Log.d(LCAT, "onSocketConnecting() was invoked.");
		KandyUtils.sendFailResult(getKrollObject(), (KrollFunction) callbacks.get("onSocketConnecting"));
	}

	@Override
	public void onSocketDisconnected() {
		Log.d(LCAT, "onSocketDisconnected() was invoked.");
		KandyUtils.sendFailResult(getKrollObject(), (KrollFunction) callbacks.get("onSocketDisconnected"));
	}

	@Override
	public void onSocketFailedWithError(String error) {
		Log.d(LCAT, "onSocketFailedWithError() was invoked: " + error);
		KandyUtils.sendFailResult(getKrollObject(), (KrollFunction) callbacks.get("onSocketFailedWithError"), error);
	}
}
