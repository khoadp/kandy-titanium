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
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * The view for access proxy.
 * 
 * @author kodeplusdev
 * @version 1.2.0
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
	 * {@inheritDoc}
	 */
	@Override
	public void processProperties(KrollDict options) {
		super.processProperties(options);
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

	private void loginEnabled(boolean enabled) {
		username.setEnabled(enabled);
		password.setEnabled(enabled);
		loginBtn.setClickable(enabled);
	}

	private void logoutEnabled(boolean enabled) {
		logoutBtn.setClickable(enabled);
	}

	private String getUsername() {
		return username.getText().toString();
	}

	private String getPassword() {
		return password.getText().toString();
	}

	private void startLoginProcess() {
		UIUtils.showProgressDialogWithMessage(activity, KandyUtils.getString("kandy_login_login_process"));

		// if user access token is define we login without user and password
		String token = accessToken.getText().toString();
		if (!TextUtils.isEmpty(token)) {
			login(token);
		} else {
			login(getUsername(), getPassword());
		}
	}

	private void login(String username, String password) {
		KandyRecord kandyUser;

		try {
			kandyUser = new KandyRecord(username);

		} catch (KandyIllegalArgumentException ex) {
			UIUtils.showDialogWithErrorMessage(activity, KandyUtils.getString("kandy_login_empty_username_text"));
			return;
		}

		if (password == null || password.isEmpty()) {
			UIUtils.showDialogWithErrorMessage(activity, KandyUtils.getString("kandy_login_empty_password_text"));
			return;
		}

		Kandy.getAccess().login(kandyUser, password, new KandyLoginResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(LCAT, "Kandy.getAccess().login:onRequestFailed error: " + err + ". Response code: "
						+ responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onLoginSucceeded() {
				Log.i(LCAT, "Kandy.getAccess().login:onLoginSucceeded");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("kandy_login_login_success"));
				activity.finish();
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
			}

			@Override
			public void onLoginSucceeded() {
				Log.i(LCAT, "Kandy.getAccess().login:onLoginSucceeded");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("kandy_login_login_success"));
				activity.finish();
			}
		});
	}

	private void startLogoutProcess() {
		UIUtils.showProgressDialogWithMessage(getProxy().getActivity(),
				KandyUtils.getString("kandy_login_logout_process"));
		logout();
	}

	private void logout() {
		logoutEnabled(false);
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
		Log.d(LCAT, "registerNotificationListener() was invoked.");
		Kandy.getAccess().registerNotificationListener(this);
	}

	public void unregisterNotificationListener() {
		Log.d(LCAT, "unregisterNotificationListener() was invoked.");
		Kandy.getAccess().unregisterNotificationListener(this);
	}

	@Override
	public void onSocketFailedWithError(String error) {
		Log.w(LCAT, "onSocketFailedWithError(): " + error);
	}

	@Override
	public void onSocketDisconnected() {
		Log.i(LCAT, "onSocketDisconnected() fired");
	}

	@Override
	public void onSocketConnecting() {
		Log.i(LCAT, "onSocketConnecting() fired");
	}

	@Override
	public void onSocketConnected() {
		Log.i(LCAT, "onSocketConnected() fired");
	}

	@Override
	public void onConnectionStateChanged(KandyConnectionState state) {
		Log.i(LCAT, "onRegistrationStateChanged() fired with state: " + state.name());
	}

	@Override
	public void onInvalidUser(String error) {
		Log.i(LCAT, "onInvalidUser() fired with error: " + error);
		UIUtils.handleResultOnUiThread(getProxy().getActivity(), true, error);
	}

	@Override
	public void onSessionExpired(String error) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSDKNotSupported(String error) {
		// TODO Auto-generated method stub

	}
}
