package io.kandy;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * Provisioning service
 * 
 * @author kodeplusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class ProvisioningServiceProxy extends TiViewProxy {
	
	public static final String LCAT = ProvisioningServiceProxy.class.getSimpleName();
	
	/**
	 * Provisioning widget
	 */
	private class ProvisioningWidget extends TiUIView {

		TextView signedPhoneNumber;
		
		public ProvisioningWidget(TiViewProxy proxy) {
			super(proxy);
			
			View layoutWraper;
			
			// Get view
			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
			layoutWraper = layoutInflater.inflate(
					KandyUtils.getLayout("kandy_provisioning_widget"), null);
			
			// Get widgets
			
			final EditText phoneNumber = (EditText)layoutWraper.findViewById(
					KandyUtils.getId("kandy_phone_number_edit"));
			final EditText otpCode = (EditText)layoutWraper.findViewById(
					KandyUtils.getId("kandy_otp_code_edit"));
			signedPhoneNumber = (TextView)layoutWraper.findViewById(
					KandyUtils.getId("kandy_signed_phone_number_tview"));
			
			Button requestBtn = (Button)layoutWraper.findViewById(
					KandyUtils.getId("kandy_request_button"));
			Button validateBtn = (Button)layoutWraper.findViewById(
					KandyUtils.getId("kandy_validate_button"));
			Button deactivateBtn = (Button)layoutWraper.findViewById(
					KandyUtils.getId("kandy_deactivate_button"));
			
			// Set click events
			
			requestBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					ProvisioningServiceProxy.this.requestCode(phoneNumber.getText().toString(),
							twoLetterISOCountryCode);
				}
			});
			
			validateBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					ProvisioningServiceProxy.this.validate(phoneNumber.getText().toString(),
							twoLetterISOCountryCode, otpCode.getText().toString());
				}
			});
			
			deactivateBtn.setOnClickListener(new View.OnClickListener() {
				
				public void onClick(View v) {
					ProvisioningServiceProxy.this.deactivate();
				}
			});
			
			setNativeView(layoutWraper);
		}
		
		public void setSignedPhoneNumber(final String phoneNumber){
			if (phoneNumber != null){
				getActivity().runOnUiThread(new Runnable() {
					
					public void run() {
						signedPhoneNumber.setText(phoneNumber);
					}
				});	
			}
		}
	}

	private KrollDict callbacks = null;
	
	private String twoLetterISOCountryCode;
	private ProvisioningWidget provisioningWidget;
	
	public ProvisioningServiceProxy() {
		super();
	}

	/**
	 * Get two letter ISO of the user's country
	 */
	private void getTwoLetterISOCountryCode(){
		// FIXME invalid token error when user don't login
		try {
			Kandy.getServices().getLocationService().getCountryInfo(new KandyCountryInfoResponseListener() {
				
				@Override
				public void onRequestFailed(int code, String err) {
					Log.d(LCAT, String.format(KandyUtils.getString("kandy_error_message"), code, err));
					twoLetterISOCountryCode = "US"; // FIXME: dummy code
				}
				
				@Override
				public void onRequestSuccess(IKandyAreaCode respond) {
					Log.d(LCAT, "Kandy > getServices > getLocationService > getCountryInfo > onRequestSuccess");
					twoLetterISOCountryCode = respond.getCountryNameShort();
				}
			});	
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TiUIView createView(Activity arg0) {
		provisioningWidget = new ProvisioningWidget(this);
		this.getTwoLetterISOCountryCode();
		return provisioningWidget;
	}
	
	/**
	 * Set request callbacks
	 * 
	 * @param callbacks
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setCallbacks(KrollDict callbacks){
		this.callbacks = callbacks;
	}
	
	/**
	 * Set the signed phone number text above deactivate button
	 * 
	 * @param phoneNumber
	 */
	@Kroll.method
	public void setSignedPhoneNumber(String phoneNumber){
		provisioningWidget.setSignedPhoneNumber(phoneNumber);
	}
	
	/**
	 * Request code
	 * 
	 * @param args
	 */
	@Kroll.method
	public void requestCode(KrollDict args){
		final KrollFunction success = (KrollFunction)args.get("success");
		final KrollFunction error = (KrollFunction)args.get("error");
		final String phoneNumber = (String)args.get("phoneNumber");
		String twoLetterISOCountryCode = (String)args.get("countryCode");

		if (twoLetterISOCountryCode == null){
			Log.i(LCAT, "Get default twoLetterISOCountryCode");
			twoLetterISOCountryCode = this.twoLetterISOCountryCode;
		}
		
		Kandy.getProvisioning().requestCode(phoneNumber, twoLetterISOCountryCode,
				new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Log.d(LCAT, String.format(KandyUtils.getString("kandy_error_message"), code, err));
						KandyUtils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSucceded() {
						Log.d(LCAT, "Kandy > getProvisioning > requestCode > onRequestSucceded");
						KandyUtils.sendSuccessResult(getKrollObject(), success);
						provisioningWidget.setSignedPhoneNumber(phoneNumber);
					}
				});
	}
	
	/**
	 * Request code
	 * 
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 */
	public void requestCode(final String phoneNumber, String twoLetterISOCountryCode) {
		KrollDict requestArgs = KandyUtils.getKrollDictFromCallbacks(callbacks, "request");
		
		requestArgs.put("phoneNumber", phoneNumber);
		requestArgs.put("countryCode", twoLetterISOCountryCode);
		
		requestCode(requestArgs);
	}

	/**
	 * Validate the OTP code
	 * 
	 * @param args
	 */
	@Kroll.method
	public void validate(KrollDict args){
		final KrollFunction success = (KrollFunction)args.get("success");
		final KrollFunction error = (KrollFunction)args.get("error");
		String phoneNumber = (String)args.get("phoneNumber");
		String twoLetterISOCountryCode = (String)args.get("countryCode");
		String otp = (String)args.get("otp");
		
		if (twoLetterISOCountryCode == null){
			Log.i(LCAT, "Get default twoLetterISOCountryCode");
			twoLetterISOCountryCode = this.twoLetterISOCountryCode;
		}
		
		Kandy.getProvisioning().validate(phoneNumber, otp, twoLetterISOCountryCode,
				new KandyValidationResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Log.d(LCAT, String.format(KandyUtils.getString( "kandy_error_message"), code, err));
						KandyUtils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSuccess(IKandyValidationResponse reqpond) {
						Log.d(LCAT, "Kandy > getProvisioning > validate > onRequestSuccess");
						KandyUtils.sendSuccessResult(getKrollObject(), success);
					}
				});		
	}
	
	/**
	 * Validate the otp code
	 * 
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 * @param otp
	 */
	public void validate(String phoneNumber, String otp, String twoLetterISOCountryCode) {
		KrollDict validateArgs = KandyUtils.getKrollDictFromCallbacks(callbacks, "validate");
		
		validateArgs.put("phoneNumber", phoneNumber);
		validateArgs.put("otp", otp);
		validateArgs.put("countryCode", twoLetterISOCountryCode);
		
		validate(validateArgs);
	}
	
	/**
	 * Deactivate phone number
	 * 
	 * @param callbacks
	 */
	@Kroll.method
	public void deactivate(KrollDict callbacks) {
		final KrollFunction success = (KrollFunction)callbacks.get("success");
		final KrollFunction error = (KrollFunction)callbacks.get("error");

		Kandy.getProvisioning().deactivate(new KandyResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, String.format(KandyUtils.getString("kandy_error_message"), code, err));
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.d(LCAT, "Kandy > getProvisioning > deactivate > onRequestSucceded");
				KandyUtils.sendSuccessResult(getKrollObject(), success);
				provisioningWidget.setSignedPhoneNumber("(none)");
			}
		});
	}
	
	/**
	 * Deactivate phone number
	 */
	public void deactivate(){
		KrollDict deactivateArgs = KandyUtils.getKrollDictFromCallbacks(callbacks, "deactivate");
		deactivate(deactivateArgs);
	}
}
