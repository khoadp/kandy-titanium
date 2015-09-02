package io.kandy.proxy;

import android.app.Activity;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;
import io.kandy.KandyModule;
import io.kandy.proxy.views.CallWidgetViewProxy;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class CallWidgetProxy extends TiViewProxy implements KandyCallServiceNotificationListener {

	@Kroll.constant
	public static final String CALLEE = "callee";
	@Kroll.constant
	public static final String CALL_TEXT_BUTTON = "callText";
	@Kroll.constant
	public static final String CALL_TYPE = "callType";
	@Kroll.constant
	public static final String CALL_TYPES = "callTypes";
	@Kroll.constant
	public static final String CALL_WIDGETS = "widgets";
	@Kroll.constant
	public static final String CALL_VOICE_TYPE = "voice";
	@Kroll.constant
	public static final String CALL_VIDEO_TYPE = "video";
	@Kroll.constant
	public static final String CALL_PSTN_TYPE = "pstn";
	@Kroll.constant
	public static final String CALL_SIP_TYPE = "sip";
	@Kroll.constant
	public static final String CALL_OUTGOING_WIDGET = "outgoing";
	@Kroll.constant
	public static final String CALL_INCOMING_WIDGET = "incoming";

	private CallWidgetViewProxy viewProxy;

	@Override
	public TiUIView createView(Activity activity) {
		viewProxy = new CallWidgetViewProxy(this);
		return viewProxy;
	}

	@Override
	public void onPause(Activity activity) {
		super.onPause(activity);
		unregisterNotificationListener();
	}

	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
		registerNotificationListener();
	}

	@Kroll.method
	public void registerNotificationListener() {
		Kandy.getServices().getCallService().registerNotificationListener(this);
	}

	@Kroll.method
	public void unregisterNotificationListener() {
		Kandy.getServices().getCallService().unregisterNotificationListener(this);
	}

	@Override
	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		Log.i("KandyCallServiceProxyNotificationListener", "onCallStateChanged was invoked.");
		fireEvent("onCallStateChanged", KandyUtils.getKrollDictFromKandyCall(call));
		if (viewProxy != null)
			viewProxy.onCallStateChanged(state, call);
	}

	@Override
	public void onGSMCallConnected(IKandyCall call) {
		fireEvent("onGSMCallConnected", KandyUtils.getKrollDictFromKandyCall(call));
		if (viewProxy != null)
			viewProxy.onGSMCallConnected(call);
	}

	@Override
	public void onGSMCallDisconnected(IKandyCall call) {
		fireEvent("onGSMCallDisconnected", KandyUtils.getKrollDictFromKandyCall(call));
		if (viewProxy != null)
			viewProxy.onGSMCallDisconnected(call);
	}

	@Override
	public void onGSMCallIncoming(IKandyCall call) {
		fireEvent("onGSMCallIncoming", KandyUtils.getKrollDictFromKandyCall(call));
		if (viewProxy != null)
			viewProxy.onGSMCallIncoming(call);
	}

	@Override
	public void onIncomingCall(IKandyIncomingCall call) {
		fireEvent("onIncomingCall", KandyUtils.getKrollDictFromKandyCall(call));
		if (viewProxy != null)
			viewProxy.onIncomingCall(call);
	}

	@Override
	public void onMissedCall(KandyMissedCallMessage call) {
		KrollDict result = new KrollDict();

		result.put("uuid", call.getUUID().toString());
		result.put("timestamp", call.getTimestamp());
		result.put("via", call.getVia());
		result.put("source", KandyUtils.getKrollDictFromKandyRecord(call.getSource()));
		result.put("eventType", call.getEventType().name());

		fireEvent("onMissedCall", result);
		if (viewProxy != null)
			viewProxy.onMissedCall(call);
	}

	@Override
	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
		KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
		result.put("isSendingVideo", isSendingVideo);
		result.put("isReceivingVideo", isReceivingVideo);

		fireEvent("onVideoStateChanged", result);

		if (viewProxy != null)
			viewProxy.onVideoStateChanged(call, isReceivingVideo, isSendingVideo);
	}

	@Override
	public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage call) {
		KrollDict result = new KrollDict();

		result.put("uuid", call.getUUID().toString());
		result.put("totalMessages", call.getTotalMessages());
		result.put("numOfUnreadMessages", call.getNumOfUnreadMessages());
		result.put("timestamp", call.getTimestamp());
		result.put("eventType", call.getEventType().name());

		fireEvent("onWaitingVoiceMailCall", result);

		if (viewProxy != null)
			viewProxy.onWaitingVoiceMailCall(call);
	}
}
