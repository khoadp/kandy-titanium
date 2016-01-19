package io.kandy.proxy.views;

import io.kandy.KandyConstant;
import io.kandy.proxy.ProvisioningServiceProxy;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationMethoud;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.common.KandyResponseListener;

/**
 * Provisioning view proxy.
 * 
 * @author kodeplusdev
 * 
 */
public class ProvisioningViewProxy extends TiUIView {

	private static final String LCAT = ProvisioningViewProxy.class.getSimpleName();

	private Activity activity;

	private View requestView, validateView, deactivateView, deactiveLabel;

	private TextView signedPhoneNumber;
	private Button requestBtn, validateBtn, deactivateBtn;
	private EditText phoneNumber, otpCode;

	private String twoLetterISOCountryCode = KandyConstant.DEFAULT_TWO_LETTER_ISO_COUNTRY_CODE;

	private String mUserId;

	public ProvisioningViewProxy(TiViewProxy proxy) {
		super(proxy);
		this.activity = proxy.getActivity();

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_provisioning_widget"), null);

		requestView = layoutWraper.findViewById(KandyUtils.getId("kandy_request_view_container"));
		validateView = layoutWraper.findViewById(KandyUtils.getId("kandy_validate_view_container"));
		deactivateView = layoutWraper.findViewById(KandyUtils.getId("kandy_deactivate_view_container"));
		deactiveLabel = layoutWraper.findViewById(KandyUtils.getId("kandy_deactivate_label"));

		phoneNumber = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_phone_number_edit"));
		otpCode = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_otp_code_edit"));
		signedPhoneNumber = (TextView) layoutWraper.findViewById(KandyUtils.getId("kandy_signed_phone_number_tview"));

		requestBtn = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_request_button"));
		validateBtn = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_validate_button"));
		deactivateBtn = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_deactivate_button"));

		requestBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startSignupProcess();
			}
		});

		validateBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				startValidationProcess();
			}
		});

		deactivateBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				deactivate();
			}
		});

		setNativeView(layoutWraper);
	}

	@Override
	public void processProperties(KrollDict options) {
		super.processProperties(options);
		if (options.containsKey("twoLetterISOCountryCode"))
			setTwoLetterISOCountryCode(options.getString("twoLetterISOCountryCode"));
		if (options.containsKey(ProvisioningServiceProxy.PROVISIONING_WIDGET_TYPE)) {
			String type = options.getString(ProvisioningServiceProxy.PROVISIONING_WIDGET_TYPE).toLowerCase();
			if (TextUtils.equals(type, ProvisioningServiceProxy.PROVISIONING_REQUEST_WIDGET)) {
				validateView.setVisibility(View.GONE);
				deactivateView.setVisibility(View.GONE);
			} else if (TextUtils.equals(type, ProvisioningServiceProxy.PROVISIONING_VALIDATE_WIDGET)) {
				requestView.setVisibility(View.GONE);
				deactivateView.setVisibility(View.GONE);
			} else if (TextUtils.equals(type, ProvisioningServiceProxy.PROVISIONING_DEACTIVATE_WIDGET)) {
				deactiveLabel.setVisibility(View.GONE);
				requestView.setVisibility(View.GONE);
				validateView.setVisibility(View.GONE);
			}
		}
	}

	public void setTwoLetterISOCountryCode(String code) {
		this.twoLetterISOCountryCode = code;
	}

	public void startSignupProcess() {

		mUserId = getPhoneNumber();
		if (mUserId == null || mUserId.isEmpty()) {
			UIUtils.showToastWithMessage(activity, KandyUtils.getString("kandy_signup_enter_phone_label"));
			return;
		}

		UIUtils.showProgressDialogWithMessage(activity, KandyUtils.getString("registration_signinup_msg"));

		requestCode(mUserId);
	}

	public void requestCode(String phoneNumber) {
		Kandy.getProvisioning().requestCode(KandyValidationMethoud.SMS, phoneNumber, twoLetterISOCountryCode, null, new KandyResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(LCAT, "sendSignupRequest: onRequestFailed: " + err + " response code: " + responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.i(LCAT, "sendSignupRequest: onRequestSucceded");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("kandy_signup_signup_succeed"));
			}
		});
	}

	private void startValidationProcess() {
		String otp = getOTPCode();

		if (otp == null || otp.isEmpty()) {
			UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("kandy_signup_enter_otp_label"));
			return;
		}

		UIUtils.showProgressDialogWithMessage(activity, KandyUtils.getString("registration_validation_msg"));
		validate(otp);
	}

	public void validate(String pOtp) {
		Kandy.getProvisioning().validateAndProvision(mUserId, pOtp, twoLetterISOCountryCode,
				new KandyValidationResponseListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						Log.e(LCAT, "sendValidationCode: onRequestFailed: " + err + " response code: " + responseCode);
						UIUtils.handleResultOnUiThread(activity, true, err);
					}

					@Override
					public void onRequestSuccess(IKandyValidationResponse response) {
						mUserId = response.getUserId();
						setSignedUser(mUserId);

						String message = "You are now signup to Kandy!\n\nDomain:%s\nUser:%s\nPassword:%s\n\nPlease Login to start using the SDK";
						message = String.format(message, response.getDomainName(), response.getUser(),
								response.getUserPassword());
						UIUtils.showDialogOnUiThread(activity,
								KandyUtils.getString("kandy_signup_validation_succeed_title"), message);
					}
				});
	}

	public void deactivate() {
		UIUtils.showProgressDialogWithMessage(activity, KandyUtils.getString("registration_signoff_msg"));

		Kandy.getProvisioning().deactivate(new KandyResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.i(LCAT, "send deactivateRequest: onRequestFailed: " + err + " response code: " + responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.i(LCAT, "send deactivateRequest: onRequestSucceded");
				UIUtils.handleResultOnUiThread(activity, false,
						KandyUtils.getString("kandy_signup_signoff_succeed_label"));
				setSignedUser("");
			}
		});
	}

	public void setSignedUser(final String user) {
		activity.runOnUiThread(new Runnable() {

			public void run() {
				signedPhoneNumber.setText(user);
			}
		});
	}

	public String getPhoneNumber() {
		return phoneNumber.getText().toString();
	}

	public String getOTPCode() {
		return otpCode.getText().toString();
	}
}