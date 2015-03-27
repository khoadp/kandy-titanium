package com.kandy;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.common.IKandySession;

/**
 * Access service
 * 
 * @author kodeplusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class AccessProxy extends TiViewProxy {
	
	public static final String LCAT = AccessProxy.class.getSimpleName();
	
	/**
	 * Access widget
	 */
	private class AccessWidget extends TiUIView {
		
		View loginView, logoutView;
		TextView userInfo;
		
		public AccessWidget(TiViewProxy proxy) {
			super(proxy);
		
			View layoutWraper;
			
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
			layoutWraper = layoutInflater.inflate(
					Utils.getLayout(getActivity(), "kandy_access_widget"), null);
			
			// Get view containers
			loginView = layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_login_view_container"));
			logoutView = layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_logout_view_container"));
			
			// Get widgets
			final EditText username = (EditText)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_username_edit"));
			final EditText password = (EditText)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_password_edit"));
			Button loginBtn = (Button)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_login_button"));
			
			userInfo = (TextView)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kand_user_info_edit"));
			Button logoutBtn = (Button)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_logout_button"));
			
			// Set click events
			loginBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					AccessProxy.this.login(username.getText().toString(), password.getText().toString());
				}
			});
			
			logoutBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					AccessProxy.this.logout();
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
		 * Set the user information at the logout view
		 * 
		 * @param info
		 */
		public void setUserInformation(final String info){
			if (userInfo != null){
				getActivity().runOnUiThread(new Runnable() {
					
					public void run() {
						userInfo.setText(info);	
					}
				});
			}
		}
		
		/**
		 * Display login view
		 */
		public void displayLoginView(){
			getActivity().runOnUiThread(new Runnable() {
				
				public void run() {
					loginView.setVisibility(View.VISIBLE);
					logoutView.setVisibility(View.GONE);
				}
			});
		}
		
		/**
		 * Display logout view
		 */
		public void displayLogoutView(){
			getActivity().runOnUiThread(new Runnable() {
				
				public void run() {
					loginView.setVisibility(View.GONE);
					logoutView.setVisibility(View.VISIBLE);	
				}
			});
		}
		
	}
	
	// Callbacks Be used by widget
	private KrollDict callbacks = null;
	
	private AccessWidget accessWidget;
	
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
		accessWidget  = new AccessWidget(this);
		return accessWidget;
	}
	
	/**
	 * Set callbacks for login action
	 * 
	 * @param callbacks
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setCallbacks(KrollDict callbacks){
		this.callbacks = callbacks;
	}
	
	/**
	 * Display login view
	 */
	@Kroll.method
	public void displayLoginVIew(){
		accessWidget.displayLoginView();
	}
	
	/**
	 * Display logout view
	 */
	@Kroll.method
	public void displayLogoutVIew(){
		accessWidget.displayLogoutView();
	}
	
	/**
	 * Set user information at the logout view
	 * 
	 * @param username
	 */
	@Kroll.method
	public void setUserInformation(String username){
		accessWidget.setUserInformation(username);
	}
	
	/**
	 * Register/login the user on the server with credentials received from
	 * admin.
	 * 
	 * @param args
	 */
	@Kroll.method
	public void login(KrollDict args){
		final KrollFunction success = (KrollFunction)args.get("success");
		final KrollFunction error = (KrollFunction)args.get("success");
		final String username = (String)args.get("username");
		final String password = (String)args.get("password");
		
		KandyRecord kandyUser;

		try {
			kandyUser = new KandyRecord(username);

		} catch (IllegalArgumentException ex) {
			String err = Utils.getString(getActivity(), "kandy_login_empty_username_text");
			Utils.sendFailResult(getKrollObject(), error, err);
			Log.d(LCAT, err);
			return;
		}

		if (password == null || password.isEmpty()) {
			String err = Utils.getString(getActivity(), "kandy_login_empty_password_text");
			Utils.sendFailResult(getKrollObject(), error, err);
			Log.d(LCAT, err);
			return;
		}
		
		Kandy.getAccess().login(kandyUser, password,
				new KandyLoginResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Log.d(LCAT, String.format(Utils.getString(getActivity(), "kandy_error_message"), code, err));
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onLoginSucceeded() {
						accessWidget.setUserInformation(username);
						accessWidget.displayLogoutView();
						Log.d(LCAT, "Kandy > getAccess > login > onLoginSucceeded");
						Utils.sendSuccessResult(getKrollObject(), success);
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
	public void login(final String username, final String password) {
		
		KrollDict loginArgs = Utils.getKrollDictFromCallbacks(callbacks, "login");
		
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
		final KrollFunction success = (KrollFunction)callbacks.get("success");
		final KrollFunction error = (KrollFunction)callbacks.get("error");
		
		Kandy.getAccess().logout(new KandyLogoutResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, String.format(Utils.getString(getActivity(), "kandy_error_message"), code, err));
				Utils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onLogoutSucceeded() {
				accessWidget.displayLoginView();
				Log.d(LCAT, "Kandy > getAccess > logout > onLogoutSucceeded");
				Utils.sendSuccessResult(getKrollObject(), success);
			}
		});
	}
	
	/**
	 * This method unregisters user from the Kandy server.
	 */
	public void logout(){
		KrollDict logoutArgs = Utils.getKrollDictFromCallbacks(callbacks, "logout");
		
		logout(logoutArgs);
	}

	/**
	 * Load previous session.
	 * 
	 * @param args
	 */
	@Kroll.method
	public void loadSession(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		IKandySession session = Kandy.getSession();

		if (session.getKandyUser().getUser() != null) {
			KrollDict data = new KrollDict();

			data.put("userId", session.getKandyUser().getUserId());
			data.put("user", session.getKandyUser().getUser());
			data.put("domain", session.getKandyDomain().getName());
			data.put("deviceId", session.getKandyUser().getDeviceId());

			Utils.sendSuccessResult(getKrollObject(), success, data);
		} else {
			Log.d(LCAT, "Session not found.");
			Utils.sendFailResult(getKrollObject(), error, 404, "Session not found.");
		}
	}
}
