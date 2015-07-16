package io.kandy.proxy.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import io.kandy.KandyUtils;
import io.kandy.proxy.AccessProxy;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import java.util.HashMap;

public class AccessViewProxy extends TiUIView {

	private AccessProxy proxy;

	private View loginView, logoutView;
	private TextView userInfo;
	private EditText username, password;
	private Button loginBtn, logoutBtn;

	public AccessViewProxy(TiViewProxy proxy) {
		super(proxy);

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater
				.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(
				KandyUtils.getLayout("kandy_access_widget"), null);

		// Get view containers
		loginView = layoutWraper.findViewById(KandyUtils
				.getId("kandy_login_view_container"));
		logoutView = layoutWraper.findViewById(KandyUtils
				.getId("kandy_logout_view_container"));

		// Get widgets
		username = (EditText) layoutWraper.findViewById(KandyUtils
				.getId("kandy_username_edit"));
		password = (EditText) layoutWraper.findViewById(KandyUtils
				.getId("kandy_password_edit"));
		loginBtn = (Button) layoutWraper.findViewById(KandyUtils
				.getId("kandy_login_button"));

		userInfo = (TextView) layoutWraper.findViewById(KandyUtils
				.getId("kandy_user_info_edit"));
		logoutBtn = (Button) layoutWraper.findViewById(KandyUtils
				.getId("kandy_logout_button"));

		// Set click events
		loginBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				loginEnabled(false);
				((AccessProxy) getProxy()).login(loginSuccessCallback,
						loginErrorCallback, username.getText().toString(),
						password.getText().toString());
			}
		});

		logoutBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				logoutEnabled(false);
				((AccessProxy) getProxy()).logout(logoutSuccessCallback,
						logoutErrorCallback);
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
		getProxy().getActivity().runOnUiThread(new Runnable() {

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
		getProxy().getActivity().runOnUiThread(new Runnable() {

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

	private KrollFunction loginSuccessCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			displayLogoutView(userInfo.getText().toString());
			logoutEnabled(true);
		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			displayLogoutView(userInfo.getText().toString());
			logoutEnabled(true);
		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			displayLogoutView(userInfo.getText().toString());
			logoutEnabled(true);
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			displayLogoutView(userInfo.getText().toString());
			logoutEnabled(true);
			return null;
		}
	};

	private KrollFunction loginErrorCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			loginEnabled(true);
		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			loginEnabled(true);
		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			loginEnabled(true);
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			loginEnabled(true);
			return null;
		}
	};

	private KrollFunction logoutSuccessCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			loginEnabled(true);
			displayLoginView();
		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			loginEnabled(true);
			displayLoginView();
		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			loginEnabled(true);
			displayLoginView();
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			loginEnabled(true);
			displayLoginView();
			return null;
		}
	};

	private KrollFunction logoutErrorCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			logoutEnabled(true);
		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			logoutEnabled(true);
		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			logoutEnabled(true);
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			logoutEnabled(true);
			return null;
		}
	};

}
