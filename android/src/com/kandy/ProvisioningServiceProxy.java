package com.kandy;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.common.KandyResponseListener;

/**
 * Provisioning service
 * 
 * @author kodeplusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class ProvisioningServiceProxy extends KrollProxy {

	public ProvisioningServiceProxy() {
		super();
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

		String id = (String) args.get("id");
		String twoLetterISOCountryCode = (String) args.get("code");

		Kandy.getProvisioning().requestCode(id, twoLetterISOCountryCode,
				new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSucceded() {
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

	/**
	 * Validate
	 * 
	 * @param args
	 */
	@Kroll.method
	public void validate(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String id = (String) args.get("id");
		String otp = (String) args.get("otp");
		String twoLetterISOCountryCode = (String) args.get("code");

		Kandy.getProvisioning().validate(id, otp, twoLetterISOCountryCode,
				new KandyValidationResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSuccess(IKandyValidationResponse arg0) {
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

	/**
	 * Deactive
	 * 
	 * @param args
	 */
	@Kroll.method
	public void deactivate(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getProvisioning().deactivate(new KandyResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onRequestSucceded() {
				Utils.sendSuccessResult(getKrollObject(), success);
			}
		});
	}

}
