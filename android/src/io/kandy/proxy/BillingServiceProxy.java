package io.kandy.proxy;

import android.app.Activity;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.billing.IKandyBillingPackage;
import com.genband.kandy.api.services.billing.IKandyCredit;
import com.genband.kandy.api.services.billing.KandyUserCreditResponseListener;
import io.kandy.KandyModule;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;
import org.json.JSONArray;

import java.util.ArrayList;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class BillingServiceProxy extends TiViewProxy {

	private static final String LCAT = BillingServiceProxy.class.getSimpleName();

	@Override
	public TiUIView createView(Activity activity) {
		return null;
	}

	@Kroll.method
	public void getUserCredit(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getBillingService().getUserCredit(new KandyUserCreditResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, "KandyUserCreditResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
						+ " - " + err);
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onRequestSuccess(IKandyCredit credit) {
				Log.d(LCAT, "KandyUserCreditResponseListener->onRequestSuccess() was invoked.");
				KrollDict result = new KrollDict();
				result.put("credit", credit.getCredit());
				result.put("currency", credit.getCurrency());
				result.put("dids", credit.getDids());

				JSONArray billingPackages = new JSONArray();

				if (credit.getPackages().size() > 0) {
					ArrayList<IKandyBillingPackage> billingPackageArrayList = credit.getPackages();
					for (IKandyBillingPackage billingPackage : billingPackageArrayList)
						billingPackages.put(KandyUtils.getKrollDictFromKandyPackagesCredit(billingPackage));
				}
				result.put("packages", billingPackages);
				KandyUtils.sendSuccessResult(getKrollObject(), success, result);
			}
		});
	}

}
