package io.kandy;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.location.IKandyAreaCode;
import com.genband.kandy.api.services.location.KandyCountryInfoResponseListener;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

/**
 * Location service
 * 
 * @author kodeplusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class LocationServiceProxy extends KrollProxy {

	public LocationServiceProxy() {
		super();
	}

	/**
	 * Get country info
	 * 
	 * @param args
	 */
	@Kroll.method
	public void getCountryInfo(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getLocationService()
				.getCountryInfo(new KandyCountryInfoResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSuccess(IKandyAreaCode response) {
						KrollDict data = new KrollDict();
						data.put("code", response.getCountryCode());
						data.put("nameLong", response.getCountryNameLong());
						data.put("nameShort", response.getCountryNameShort());
						Utils.sendSuccessResult(getKrollObject(), success, data);
					}
				});
	}

}
