package io.kandy.proxy;

import android.app.Activity;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import io.kandy.KandyConstant;
import io.kandy.KandyModule;
import io.kandy.KandyUtils;
import io.kandy.proxy.view.ProvisioningViewProxy;
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

	public static final String LCAT = ProvisioningServiceProxy.class
			.getSimpleName();

	private KrollDict callbacks = null;

	private String twoLetterISOCountryCode;
	private ProvisioningViewProxy viewProxy;

	public ProvisioningServiceProxy() {
		super();
	}

	/**
	 * Get two letter ISO of the user's country
	 */
	private void getTwoLetterISOCountryCode() {
		try {
			Kandy.getServices().getLocationService()
					.getCountryInfo(new KandyCountryInfoResponseListener() {

						@Override
						public void onRequestFailed(int code, String err) {
							Log.d(LCAT,
									"KandyCountryInfoResponseListener->onRequestFailed() was invoked: "
											+ String.valueOf(code) + " - "
											+ err);
							twoLetterISOCountryCode = KandyConstant.DEFAULT_TWO_LETTER_ISO_COUNTRY_CODE;
						}

						@Override
						public void onRequestSuccess(IKandyAreaCode respond) {
							Log.d(LCAT,
									"KandyCountryInfoResponseListener->onRequestSuccess() was invoked.");
							twoLetterISOCountryCode = respond
									.getCountryNameShort();
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
			twoLetterISOCountryCode = KandyConstant.DEFAULT_TWO_LETTER_ISO_COUNTRY_CODE;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TiUIView createView(Activity arg0) {
		viewProxy = new ProvisioningViewProxy(this);
		this.getTwoLetterISOCountryCode();
		return viewProxy;
	}

	/**
	 * Set request callbacks
	 * 
	 * @param callbacks
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setCallbacks(KrollDict callbacks) {
		this.callbacks = callbacks;
	}

	/**
	 * Set the signed phone number text above deactivate button
	 * 
	 * @param phoneNumber
	 */
	@Kroll.method
	public void setSignedPhoneNumber(String phoneNumber) {
		viewProxy.setSignedPhoneNumber(phoneNumber);
	}

	/**
	 * Request code
	 * 
	 * @param args
	 */
	@Kroll.method
	public void requestCode(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		final String phoneNumber = (String) args.get("phoneNumber");
		String twoLetterISOCountryCode = (String) args.get("countryCode");

		if (twoLetterISOCountryCode == null) {
			Log.i(LCAT, "Using default twoLetterISOCountryCode");
			twoLetterISOCountryCode = this.twoLetterISOCountryCode;
		}

		Kandy.getProvisioning().requestCode(phoneNumber,
				twoLetterISOCountryCode, new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Log.d(LCAT,
								"KandyResponseListener->onRequestFailed() was invoked: "
										+ String.valueOf(code) + " - " + err);
						KandyUtils.sendFailResult(getKrollObject(), error,
								code, err);
					}

					@Override
					public void onRequestSucceded() {
						Log.d(LCAT,
								"KandyResponseListener->onRequestSucceded() was invoked.");
						KandyUtils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

	/**
	 * Request code
	 * 
	 * @param phoneNumber
	 * @param twoLetterISOCountryCode
	 */
	public void requestCode(final KrollFunction success, KrollFunction error,
			final String phoneNumber) {
		KrollDict requestArgs = KandyUtils.getKrollDictFromCallbacks(callbacks,
				"request");

		requestArgs.put("success", KandyUtils.joinKrollFunctions(success,
				(KrollFunction) requestArgs.get("success")));
		requestArgs.put("error", KandyUtils.joinKrollFunctions(error,
				(KrollFunction) requestArgs.get("error")));

		requestArgs.put("phoneNumber", phoneNumber);

		requestCode(requestArgs);
	}

	/**
	 * Validate the OTP code
	 * 
	 * @param args
	 */
	@Kroll.method
	public void validate(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String phoneNumber = (String) args.get("phoneNumber");
		String twoLetterISOCountryCode = (String) args.get("countryCode");
		String otp = (String) args.get("otp");

		if (twoLetterISOCountryCode == null) {
			Log.i(LCAT, "Using default twoLetterISOCountryCode");
			twoLetterISOCountryCode = this.twoLetterISOCountryCode;
		}

		Kandy.getProvisioning().validateAndProvision(phoneNumber, otp,
				twoLetterISOCountryCode, new KandyValidationResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Log.d(LCAT,
								"KandyValidationResponseListener->onRequestFailed() was invoked: "
										+ String.valueOf(code) + " - " + err);
						KandyUtils.sendFailResult(getKrollObject(), error,
								code, err);
					}

					@Override
					public void onRequestSuccess(
							IKandyValidationResponse reqpond) {
						Log.d(LCAT,
								"KandyValidationResponseListener->onRequestSuccess() was invoked.");
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
	public void validate(KrollFunction success, KrollFunction error,
			String phoneNumber, String otp) {
		KrollDict validateArgs = KandyUtils.getKrollDictFromCallbacks(
				callbacks, "validate");

		validateArgs.put("success", KandyUtils.joinKrollFunctions(success,
				(KrollFunction) validateArgs.get("success")));
		validateArgs.put("error", KandyUtils.joinKrollFunctions(error,
				(KrollFunction) validateArgs.get("error")));

		validateArgs.put("phoneNumber", phoneNumber);
		validateArgs.put("otp", otp);

		validate(validateArgs);
	}

	/**
	 * Deactivate phone number
	 * 
	 * @param callbacks
	 */
	@Kroll.method
	public void deactivate(KrollDict callbacks) {
		final KrollFunction success = (KrollFunction) callbacks.get("success");
		final KrollFunction error = (KrollFunction) callbacks.get("error");

		Kandy.getProvisioning().deactivate(new KandyResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT,
						"KandyResponseListener->onRequestFailed() was invoked: "
								+ String.valueOf(code) + " - " + err);
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.d(LCAT,
						"KandyResponseListener->onRequestSucceded() was invoked.");
				KandyUtils.sendSuccessResult(getKrollObject(), success);
			}
		});
	}

	/**
	 * Deactivate phone number
	 */
	public void deactivate(KrollFunction success, KrollFunction error) {
		KrollDict deactivateArgs = KandyUtils.getKrollDictFromCallbacks(
				callbacks, "deactivate");

		deactivateArgs.put("success", KandyUtils.joinKrollFunctions(success,
				(KrollFunction) deactivateArgs.get("success")));
		deactivateArgs.put("error", KandyUtils.joinKrollFunctions(error,
				(KrollFunction) deactivateArgs.get("error")));

		deactivate(deactivateArgs);
	}
}
