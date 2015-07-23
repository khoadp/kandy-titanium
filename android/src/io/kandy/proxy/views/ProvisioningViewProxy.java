package io.kandy.proxy.views;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import io.kandy.proxy.ProvisioningServiceProxy;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollObject;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import java.util.HashMap;

public class ProvisioningViewProxy extends TiUIView {

	private TextView signedPhoneNumber;
	private Button requestBtn, validateBtn, deactivateBtn;
	private EditText phoneNumber, otpCode;

	public ProvisioningViewProxy(TiViewProxy proxy) {
		super(proxy);

		View layoutWraper;

		// Get view
		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_provisioning_widget"), null);

		// Get widgets

		phoneNumber = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_phone_number_edit"));
		otpCode = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_otp_code_edit"));
		signedPhoneNumber = (TextView) layoutWraper.findViewById(KandyUtils.getId("kandy_signed_phone_number_tview"));

		requestBtn = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_request_button"));
		validateBtn = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_validate_button"));
		deactivateBtn = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_deactivate_button"));

		// Set click events

		requestBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				((ProvisioningServiceProxy) getProxy()).requestCode(requestSuccessCallback, requestErrorCallback,
						phoneNumber.getText().toString());
			}
		});

		validateBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				((ProvisioningServiceProxy) getProxy()).validate(validateSuccessCallback, validateErrorCallback,
						phoneNumber.getText().toString(), otpCode.getText().toString());
			}
		});

		deactivateBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				((ProvisioningServiceProxy) getProxy()).deactivate(deactivateSuccessCallback, deactivateErrorCallback);
			}
		});

		setNativeView(layoutWraper);
	}

	public void setSignedPhoneNumber(final String phoneNumber) {
		if (phoneNumber != null) {
			getProxy().getActivity().runOnUiThread(new Runnable() {

				public void run() {
					signedPhoneNumber.setText(phoneNumber);
				}
			});
		}
	}

	private KrollFunction requestSuccessCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	private KrollFunction requestErrorCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	private KrollFunction validateSuccessCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			signedPhoneNumber.setText(phoneNumber.getText().toString());
		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			signedPhoneNumber.setText(phoneNumber.getText().toString());
		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			signedPhoneNumber.setText(phoneNumber.getText().toString());
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			signedPhoneNumber.setText(phoneNumber.getText().toString());
			return null;
		}
	};

	private KrollFunction validateErrorCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			// TODO Auto-generated method stub
			return null;
		}
	};

	private KrollFunction deactivateSuccessCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			signedPhoneNumber.setText("(none)");
		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			signedPhoneNumber.setText("(none)");
		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			signedPhoneNumber.setText("(none)");
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			signedPhoneNumber.setText("(none)");
			return null;
		}
	};

	private KrollFunction deactivateErrorCallback = new KrollFunction() {

		@Override
		public void callAsync(KrollObject arg0, Object[] arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void callAsync(KrollObject arg0, HashMap arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Object call(KrollObject arg0, Object[] arg1) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object call(KrollObject arg0, HashMap arg1) {
			// TODO Auto-generated method stub
			return null;
		}
	};
}