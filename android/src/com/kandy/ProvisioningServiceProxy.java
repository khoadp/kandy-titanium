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
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;

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
					Utils.getLayout(getActivity(), "kandy_provisioning_widget"), null);
			
			// Get widgets
			
			final EditText phoneNumber = (EditText)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_phone_number_txt"));
			final EditText otpCode = (EditText)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_otp_code_txt"));
			signedPhoneNumber = (TextView)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_signed_phone_number_txt"));
			
			Button requestBtn = (Button)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_request_btn"));
			Button validateBtn = (Button)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_validate_btn"));
			Button deactivateBtn = (Button)layoutWraper.findViewById(
					Utils.getId(getActivity(), "kandy_deactivate_btn"));
			
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

	private KrollDict requestCallbacks;
	private KrollDict validateCallbacks;
	private KrollDict deactivateCallbacks;
	private String twoLetterISOCountryCode;
	private ProvisioningWidget provisioningWidget;
	
	public ProvisioningServiceProxy() {
		super();
		this.getTwoLetterISOCountryCode();
	}

	/**
	 * Get two letter ISO of the user's country
	 */
	private void getTwoLetterISOCountryCode(){
		Kandy.getServices().getLocationService().getCountryInfo(new KandyCountryInfoResponseListener() {
			
			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, String.format(Utils.getString(getActivity(), "kandy_error_message"), code, err));
				twoLetterISOCountryCode = "US"; // TODO: dummy code
			}
			
			@Override
			public void onRequestSuccess(IKandyAreaCode respond) {
				Log.d(LCAT, "Kandy > getServices > getLocationService > getCountryInfo > onRequestSuccess");
				twoLetterISOCountryCode = respond.getCountryNameShort();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TiUIView createView(Activity arg0) {
		provisioningWidget = new ProvisioningWidget(this);
		return provisioningWidget;
	}
	
	/**
	 * Set request callbacks
	 * 
	 * @param callbacks
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setRequestCallbacks(KrollDict callbacks){
		requestCallbacks = callbacks;
	}
	
	/**
	 * Set validate callbacks
	 * 
	 * @param callbacks
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setValidateCallbacks(KrollDict callbacks){
		validateCallbacks = callbacks;
	}
	
	/**
	 * Set deactivate callbacks
	 * 
	 * @param callbacks
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setDeactivateCallbacks(KrollDict callbacks){
		deactivateCallbacks = callbacks;
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
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 * @param success
	 * @param error
	 */
	@Kroll.method
	public void requestCode(String phoneNumber, String twoLetterISOCountryCode,
			KrollFunction success, KrollFunction error){
		this.requestCallbacks = new KrollDict();
		this.requestCallbacks.put("success", success);
		this.requestCallbacks.put("error", error);
		this.requestCode(phoneNumber, twoLetterISOCountryCode);
	} 
	
	/**
	 * Request code
	 * 
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 * @param callbacks
	 */
	@Kroll.method
	public void requestCode(String phoneNumber, String twoLetterISOCountryCode, KrollDict callbacks){
		this.requestCallbacks = callbacks;
		this.requestCode(phoneNumber, twoLetterISOCountryCode);
	}
	/**
	 * Request code
	 * 
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 */
	@Kroll.method
	public void requestCode(final String phoneNumber, String twoLetterISOCountryCode) {
		final KrollFunction success, error;
		
		if (requestCallbacks != null){
			success = (KrollFunction) requestCallbacks.get("success");
			error = (KrollFunction) requestCallbacks.get("error");
		} else {
			success = error = null;
		}

		if (twoLetterISOCountryCode == null){
			twoLetterISOCountryCode = this.twoLetterISOCountryCode;
		}
		
		Kandy.getProvisioning().requestCode(phoneNumber, twoLetterISOCountryCode,
				new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Log.d(LCAT, String.format(Utils.getString(getActivity(), "kandy_error_message"), code, err));
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSucceded() {
						Log.d(LCAT, "Kandy > getProvisioning > requestCode > onRequestSucceded");
						Utils.sendSuccessResult(getKrollObject(), success);
						provisioningWidget.setSignedPhoneNumber(phoneNumber);
					}
				});
	}

	/**
	 * Validate the otp code
	 * 
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 * @param otp
	 * @param success
	 * @param error
	 */
	@Kroll.method
	public void validate(String phoneNumber, String twoLetterISOCountryCode, String otp,
			KrollFunction success, KrollFunction error){
		this.validateCallbacks = new KrollDict();
		this.validateCallbacks.put("success", success);
		this.validateCallbacks.put("error", error);
		this.validate(phoneNumber, twoLetterISOCountryCode, otp);
	} 
	
	/**
	 * Validate the otp code
	 * 
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 * @param otp
	 * @param callbacks
	 */
	@Kroll.method
	public void validate(String phoneNumber, String twoLetterISOCountryCode, String otp, KrollDict callbacks){
		this.validateCallbacks = callbacks;
		this.validate(phoneNumber, twoLetterISOCountryCode, otp);
	}
	
	/**
	 * Validate the otp code
	 * 
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 * @param otp
	 */
	@Kroll.method
	public void validate(String phoneNumber, String twoLetterISOCountryCode, String otp) {
		final KrollFunction success, error;
		
		if (validateCallbacks != null){
			success = (KrollFunction) validateCallbacks.get("success");
			error = (KrollFunction) validateCallbacks.get("error");
		} else {
			success = error = null;
		}

		if (twoLetterISOCountryCode == null){
			twoLetterISOCountryCode = this.twoLetterISOCountryCode;
		}
		
		Kandy.getProvisioning().validate(phoneNumber, otp, twoLetterISOCountryCode,
				new KandyValidationResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Log.d(LCAT, String.format(Utils.getString(getActivity(), "kandy_error_message"), code, err));
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSuccess(IKandyValidationResponse reqpond) {
						Log.d(LCAT, "Kandy > getProvisioning > validate > onRequestSuccess");
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}
	
	/**
	 * Deactivate phone number
	 * 
	 * @param callbacks
	 */
	@Kroll.method
	public void deactivate(KrollDict callbacks) {
		this.deactivateCallbacks = callbacks;
		this.deactivate();
	}
	
	/**
	 * Deactivate phone number
	 */
	@Kroll.method
	public void deactivate(){
		final KrollFunction success, error;
		
		if (deactivateCallbacks != null){
			success = (KrollFunction) deactivateCallbacks.get("success");
			error = (KrollFunction) deactivateCallbacks.get("error");
		} else {
			success = error = null;
		}
		
		Kandy.getProvisioning().deactivate(new KandyResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, String.format(Utils.getString(getActivity(), "kandy_error_message"), code, err));
				Utils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.d(LCAT, "Kandy > getProvisioning > deactivate > onRequestSucceded");
				Utils.sendSuccessResult(getKrollObject(), success);
				provisioningWidget.setSignedPhoneNumber("(none)");
			}
		});
	}
}
