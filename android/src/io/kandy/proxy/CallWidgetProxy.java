package io.kandy.proxy;

import io.kandy.KandyModule;
import io.kandy.proxy.views.CallWidgetViewProxy;
import io.kandy.utils.KandyUtils;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;

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

	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		Log.i("KandyCallServiceProxyNotificationListener", "onCallStateChanged was invoked.");
		if (hasListeners("onCallStateChanged"))
			fireEvent("onCallStateChanged", KandyUtils.getKrollDictFromKandyCall(call));
		if (viewProxy != null)
			viewProxy.onCallStateChanged(state, call);
	}

	public void onGSMCallConnected(IKandyCall call, String incomingNumber) {
		if (hasListeners("onGSMCallConnected"))
			fireEvent("onGSMCallConnected", KandyUtils.getKrollDictFromKandyCall(call));
		if (viewProxy != null)
			viewProxy.onGSMCallConnected(call);
	}

	public void onGSMCallDisconnected(IKandyCall call, String incomingNumber) {
		if (hasListeners("onGSMCallDisconnected")){
			KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
			result.put("incomingNumber", incomingNumber);
			fireEvent("onGSMCallDisconnected", result);
		}
		if (viewProxy != null)
			viewProxy.onGSMCallDisconnected(call);
	}

	public void onGSMCallIncoming(IKandyCall call, String incomingNumber) {
		if (hasListeners("onGSMCallIncoming")){
			KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
			result.put("incomingNumber", incomingNumber);
			fireEvent("onGSMCallIncoming", result);
		}
		if (viewProxy != null)
			viewProxy.onGSMCallIncoming(call);
	}

	public void onIncomingCall(IKandyIncomingCall call) {
		if (hasListeners("onIncomingCall"))
			fireEvent("onIncomingCall", KandyUtils.getKrollDictFromKandyCall(call));
		if (viewProxy != null)
			viewProxy.onIncomingCall(call);
	}

	public void onMissedCall(KandyMissedCallMessage call) {
		if (hasListeners("onMissedCall")) {
			KrollDict result = new KrollDict();

			result.put("uuid", call.getUUID().toString());
			result.put("timestamp", call.getTimestamp());
			result.put("via", call.getVia());
			result.put("source", KandyUtils.getKrollDictFromKandyRecord(call.getSource()));
			result.put("eventType", call.getEventType().name());

			fireEvent("onMissedCall", result);
		}
		if (viewProxy != null)
			viewProxy.onMissedCall(call);
	}

	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
		if (hasListeners("onVideoStateChanged")) {
			KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
			result.put("isSendingVideo", isSendingVideo);
			result.put("isReceivingVideo", isReceivingVideo);

			fireEvent("onVideoStateChanged", result);
		}

		if (viewProxy != null)
			viewProxy.onVideoStateChanged(call, isReceivingVideo, isSendingVideo);
	}

	public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage call) {
		if (hasListeners("onWaitingVoiceMailCall")) {
			KrollDict result = new KrollDict();

			result.put("uuid", call.getUUID().toString());
			result.put("totalMessages", call.getTotalMessages());
			result.put("numOfUnreadMessages", call.getNumOfUnreadMessages());
			result.put("timestamp", call.getTimestamp());
			result.put("eventType", call.getEventType().name());

			fireEvent("onWaitingVoiceMailCall", result);
		}

		if (viewProxy != null)
			viewProxy.onWaitingVoiceMailCall(call);
	}
}
