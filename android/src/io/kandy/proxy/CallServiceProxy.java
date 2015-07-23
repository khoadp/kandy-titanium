package io.kandy.proxy;

import android.app.Activity;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyOutgingVoipCallOptions;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyModule;
import io.kandy.proxy.views.CallViewProxy;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

/**
 * Call service
 * 
 * @author kodeplusdev
 * 
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class CallServiceProxy extends TiViewProxy {

	public static final String LCAT = CallServiceProxy.class.getSimpleName();

	private KrollDict callbacks;
	private CallViewProxy viewProxy;

	private KrollDict calls = new KrollDict();
	private KrollDict localVideoViews = new KrollDict();
	private KrollDict remoteVideoViews = new KrollDict();

	public CallServiceProxy() {
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
		viewProxy = new CallViewProxy(this);
		return viewProxy;
	}

	/**
	 * Set call callbacks
	 * 
	 * @param args
	 */
	@Kroll.setProperty
	@Kroll.method
	public void setCallbacks(KrollDict args) {
		callbacks = args;
	}

	@Kroll.method
	public void createVoipCall(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String username = args.getString("username");
		boolean startWithVieo = args.getBoolean("startWithVideo");

		KandyRecord callee;
		try {
			callee = new KandyRecord(username);
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error,
					KandyUtils.getString("kandy_calls_invalid_phone_text_msg"));
			return;
		}

		KandyOutgingVoipCallOptions callOptions = startWithVieo ? KandyOutgingVoipCallOptions.START_CALL_WITH_VIDEO
				: KandyOutgingVoipCallOptions.START_CALL_WITHOUT_VIDEO;
		IKandyCall call = Kandy.getServices().getCallService().createVoipCall(null, callee, callOptions);
		setKandyVideoViewsAndEstablishCall(call);
	}

	private void setKandyVideoViewsAndEstablishCall(IKandyCall call) {

	}
}
