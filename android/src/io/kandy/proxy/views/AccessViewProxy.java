package io.kandy.proxy.views;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyConnectServiceNotificationListener;
import com.genband.kandy.api.access.KandyConnectionState;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.proxy.AccessServiceProxy;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * The view for access proxy.
 * 
 * @author kodeplusdev
 */
public class AccessViewProxy extends TiUIView implements KandyConnectServiceNotificationListener {

	private static final String LCAT = AccessViewProxy.class.getSimpleName();

	private Activity activity;

	private View loginView, logoutView;
	private TextView userInfo;
	private EditText username, password, accessToken;
	private Button loginBtn, logoutBtn;

	public AccessViewProxy(TiViewProxy proxy) {
		super(proxy);

		activity = proxy.getActivity();

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_access_widget"), null);

		// Get view containers
		loginView = layoutWraper.findViewById(KandyUtils.getId("kandy_login_view_container"));
		logoutView = layoutWraper.findViewById(KandyUtils.getId("kandy_logout_view_container"));

		// Get widgets
		username = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_username_edit"));
		password = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_password_edit"));
		accessToken = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_access_token_edit"));
		loginBtn = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_login_button"));

		userInfo = (TextView) layoutWraper.findViewById(KandyUtils.getId("kandy_user_info_edit"));
		logoutBtn = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_logout_button"));

		String type = (String) proxy.getProperty("type");

		if (type != null && type.equals(AccessServiceProxy.ACCESS_TYPE_TOKEN)) {
			username.setVisibility(View.GONE);
			password.setVisibility(View.GONE);
		} else {
			accessToken.setVisibility(View.GONE);
		}

		// Set click events
		loginBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startLoginProcess();
			}
		});

		logoutBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startLogoutProcess();
			}
		});

		setNativeView(layoutWraper);
	}

	/**
	 * Display login view
	 */
	public void displayLoginView() {
		activity.runOnUiThread(new Runnable() {

			public void run() {
				loginView.setVisibility(View.VISIBLE);
				logoutView.setVisibility(View.GONE);
			}
		});
	}

	/**
	 * Display logout view
	 */
	public void displayLogoutView(final String info) {
		activity.runOnUiThread(new Runnable() {

			public void run() {
				userInfo.setText(info);
				loginView.setVisibility(View.GONE);
				logoutView.setVisibility(View.VISIBLE);
			}
		});
	}

	private void loginEnabled(final boolean enabled) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				username.setEnabled(enabled);
				password.setEnabled(enabled);
				accessToken.setEnabled(enabled);
				loginBtn.setClickable(enabled);
			}
		});
	}

	private void logoutEnabled(final boolean enabled) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				logoutBtn.setClickable(enabled);
			}
		});
	}

	private String getUsername() {
		return username.getText().toString();
	}

	private String getPassword() {
		return password.getText().toString();
	}

	private void startLoginProcess() {
		UIUtils.showProgressDialogWithMessage(activity, KandyUtils.getString("kandy_login_login_process"));
		loginEnabled(false);
		// if user access token is define we login without user and password
		String token = accessToken.getText().toString();
		if (!TextUtils.isEmpty(token)) {
			login(token);
		} else {
			login(getUsername(), getPassword());
		}
	}

	private void login(final String username, String password) {
		KandyRecord kandyUser;

		try {
			kandyUser = new KandyRecord(username);

		} catch (KandyIllegalArgumentException ex) {
			UIUtils.showDialogWithErrorMessage(activity, KandyUtils.getString("kandy_login_empty_username_text"));
			loginEnabled(true);
			return;
		}

		if (password == null || password.isEmpty()) {
			UIUtils.showDialogWithErrorMessage(activity, KandyUtils.getString("kandy_login_empty_password_text"));
			loginEnabled(true);
			return;
		}

		Kandy.getAccess().login(kandyUser, password, new KandyLoginResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(LCAT, "Kandy.getAccess().login:onRequestFailed error: " + err + ". Response code: "
						+ responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
				loginEnabled(true);
			}

			@Override
			public void onLoginSucceeded() {
				Log.i(LCAT, "Kandy.getAccess().login:onLoginSucceeded");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("kandy_login_login_success"));
				logoutEnabled(true);
				displayLogoutView(username);
			}
		});
	}

	private void login(String userAccessToken) {
		Kandy.getAccess().login(userAccessToken, new KandyLoginResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(LCAT, "Kandy.getAccess().login:onRequestFailed error: " + err + ". Response code: "
						+ responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
				loginEnabled(true);
			}

			@Override
			public void onLoginSucceeded() {
				Log.i(LCAT, "Kandy.getAccess().login:onLoginSucceeded");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("kandy_login_login_success"));
				logoutEnabled(true);
				displayLogoutView(Kandy.getSession().getKandyUser().getUser());
			}
		});
	}

	private void startLogoutProcess() {
		UIUtils.showProgressDialogWithMessage(getProxy().getActivity(),
				KandyUtils.getString("kandy_login_logout_process"));
		logoutEnabled(false);
		logout();
	}

	private void logout() {
		Kandy.getAccess().logout(new KandyLogoutResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(LCAT, "Kandy.getAccess().logout:onRequestFailed error: " + err + ". Response code: "
						+ responseCode);
				UIUtils.handleResultOnUiThread(getProxy().getActivity(), true, err);
				logoutEnabled(true);
			}

			@Override
			public void onLogoutSucceeded() {
				UIUtils.handleResultOnUiThread(getProxy().getActivity(), false,
						KandyUtils.getString("kandy_login_logout_success"));
				loginEnabled(true);
				displayLoginView();
			}
		});
	}

	public void registerNotificationListener() {
		Log.d(LCAT, "viewProxy->registerNotificationListener() was invoked.");
		Kandy.getAccess().registerNotificationListener(this);
	}

	public void unregisterNotificationListener() {
		Log.d(LCAT, "viewProxy->unregisterNotificationListener() was invoked.");
		Kandy.getAccess().unregisterNotificationListener(this);
	}

	@Override
	public void onSocketFailedWithError(String error) {
		((KandyConnectServiceNotificationListener) proxy).onSocketFailedWithError(error);
	}

	@Override
	public void onSocketDisconnected() {
		((KandyConnectServiceNotificationListener) proxy).onSocketDisconnected();
	}

	@Override
	public void onSocketConnecting() {
		((KandyConnectServiceNotificationListener) proxy).onSocketConnecting();
	}

	@Override
	public void onSocketConnected() {
		((KandyConnectServiceNotificationListener) proxy).onSocketConnected();
	}

	@Override
	public void onConnectionStateChanged(KandyConnectionState state) {
		((KandyConnectServiceNotificationListener) proxy).onConnectionStateChanged(state);
	}

	@Override
	public void onInvalidUser(String error) {
		((KandyConnectServiceNotificationListener) proxy).onInvalidUser(error);
	}

	@Override
	public void onSessionExpired(String error) {
		((KandyConnectServiceNotificationListener) proxy).onSessionExpired(error);
	}

	@Override
	public void onSDKNotSupported(String error) {
		((KandyConnectServiceNotificationListener) proxy).onSDKNotSupported(error);
	}
}
