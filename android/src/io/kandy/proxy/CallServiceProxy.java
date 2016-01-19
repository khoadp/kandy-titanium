package io.kandy.proxy;

import io.kandy.KandyModule;
import io.kandy.utils.KandyUtils;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.IKandyOutgoingCall;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyOutgingVoipCallOptions;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class CallServiceProxy extends KrollProxy implements KandyCallServiceNotificationListener {

	private static final String LCAT = CallServiceProxy.class.getSimpleName();

	private static final HashMap<String, IKandyCall> calls = new HashMap<String, IKandyCall>();

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

	public static IKandyCall getKandyCall(String id) {
		return calls.get(id);
	}

	public static void addKandyCall(IKandyCall callee) {
		if (!calls.containsKey(callee.getCallee().getUri()))
			calls.put(callee.getCallee().getUri(), callee);
	}

	public static void removeKandyCall(String id) {
		if (calls.containsKey(id)) {
			calls.remove(id);
		}
	}

	@Kroll.method
	public void createVoipCall(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String username = args.getString("callee");
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
		setKandyVideoViewsAndEstablishCall(call, success, error);
	}

	@Kroll.method
	public void createPSTNCall(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String number = args.getString("callee");

		number = number.replace("+", "");
		number = number.replace("-", "");

		IKandyCall call = Kandy.getServices().getCallService().createPSTNCall(null, number, null);
		setKandyVideoViewsAndEstablishCall(call, success, error);
	}

	@Kroll.method
	public void createSIPTrunkCall(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String number = args.getString("callee");

		KandyRecord callee = null;

		try {
			number = KandyRecord.normalize(number);
			callee = new KandyRecord(number);
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error,
					KandyUtils.getString("kandy_calls_invalid_phone_text_msg"));
		}

		IKandyCall call = Kandy.getServices().getCallService().createSIPTrunkCall(null, callee);
		setKandyVideoViewsAndEstablishCall(call, success, error);
	}

	private void setKandyVideoViewsAndEstablishCall(IKandyCall call, KrollFunction success, KrollFunction error) {
		setDummyKandyVideoView(call);

		calls.put(call.getCallee().getUri(), call);

		((IKandyOutgoingCall) call).establish(new KandyCallResponseListenerCallBack(success, error));
	}

	private void setDummyKandyVideoView(IKandyCall call) {
		KandyView kView = new KandyView(getActivity(), null);
		call.setLocalVideoView(kView);
		call.setRemoteVideoView(kView);
	}

	private boolean checkActiveCall(String id, KrollFunction error) {
		if (!calls.containsKey(id)) {
			KandyUtils.sendFailResult(getKrollObject(), error,
					KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
			return false;
		}
		return true;
	}

	@Kroll.method
	public void accept(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");
		boolean startWithVideo = args.getBoolean("startWithVideo");

		if (!checkActiveCall(id, error))
			return;

		IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
		setDummyKandyVideoView((IKandyCall) calls);

		call.accept(startWithVideo, new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void ignore(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;
		IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
		call.ignore(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void reject(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;

		IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
		call.reject(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void hangup(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.hangup(new KandyCallResponseListenerCallBack(success, error));
		removeKandyCall(id);
	}

	@Kroll.method
	public void mute(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.mute(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void unmute(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.unmute(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void hold(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.hold(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void unhold(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.unhold(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void switchVideoOn(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.startVideoSharing(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void switchVideoOff(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.stopVideoSharing(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void switchCameraFront(KrollDict args) {
		args.put("camera", KandyCameraInfo.FACING_FRONT);
		switchCamera(args);
	}

	@Kroll.method
	public void switchCameraBack(KrollDict args) {
		args.put("camera", KandyCameraInfo.FACING_BACK);
		switchCamera(args);
	}

	@Kroll.method
	public void switchCamera(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("callee");

		KandyCameraInfo cameraInfo;
		try {
			cameraInfo = KandyCameraInfo.valueOf(args.getString("camera"));
		} catch (Exception e) {
			e.printStackTrace();
			cameraInfo = KandyCameraInfo.FACING_FRONT;
		}

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.switchCamera(cameraInfo);

		KandyUtils.sendSuccessResult(getKrollObject(), success);
	}

	@Kroll.method
	public void switchSpeakerOn(KrollDict args) {
		AudioManager mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setSpeakerphoneOn(true);
	}

	@Kroll.method
	public void switchSpeakerOff(KrollDict args) {
		AudioManager mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.setSpeakerphoneOn(false);
	}

	@Kroll.method
	public boolean isInCall() {
		return Kandy.getServices().getCallService().isInCall();
	}

	@Kroll.method
	public boolean isInGSMCall() {
		return Kandy.getServices().getCallService().isInGSMCall();
	}

	public class KandyCallResponseListenerCallBack implements KandyCallResponseListener {

		private KrollFunction success, error;

		public KandyCallResponseListenerCallBack(KrollFunction success, KrollFunction error) {
			this.success = success;
			this.error = error;
		}

		public void onRequestFailed(IKandyCall call, int code, String err) {
			Log.d(LCAT, "KandyCallResponseListenerCallBack->onRequestFailed() was invoked: "
					+ call.getCallee().getUri() + " - " + String.valueOf(code) + " - " + err);
			removeKandyCall(call.getCallee().getUri());
			KandyUtils.sendFailResult(getKrollObject(), error, code, err);
		}

		public void onRequestSucceeded(IKandyCall call) {
			Log.d(LCAT, "KandyCallResponseListenerCallBack->onRequestSucceeded() was invoked: "
					+ call.getCallee().getUri());
			KandyUtils.sendSuccessResult(getKrollObject(), success, KandyUtils.getKrollDictFromKandyCall(call));
		}

	}

	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		Log.i("KandyCallServiceProxyNotificationListener", "onCallStateChanged was invoked.");
		if (hasListeners("onCallStateChanged"))
			fireEvent("onCallStateChanged", KandyUtils.getKrollDictFromKandyCall(call));
	}

	public void onGSMCallConnected(IKandyCall call, String incomingNumber) {
		if (hasListeners("onGSMCallConnected")){
			KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
			result.put("incomingNumber", incomingNumber);
			fireEvent("onGSMCallConnected", result);
		}
	}

	public void onGSMCallDisconnected(IKandyCall call, String incomingNumber) {
		if (hasListeners("onGSMCallDisconnected")){
			KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
			result.put("incomingNumber", incomingNumber);
			fireEvent("onGSMCallDisconnected", result);
		}
	}

	public void onGSMCallIncoming(IKandyCall call, String incomingNumber) {
		if (hasListeners("onGSMCallIncoming")){
			KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
			result.put("incomingNumber", incomingNumber);
			fireEvent("onGSMCallIncoming", result);
		}
	}

	public void onIncomingCall(IKandyIncomingCall call) {
		if (hasListeners("onIncomingCall"))
			fireEvent("onIncomingCall", KandyUtils.getKrollDictFromKandyCall(call));
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
	}

	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
		if (hasListeners("onVideoStateChanged")) {
			KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
			result.put("isSendingVideo", isSendingVideo);
			result.put("isReceivingVideo", isReceivingVideo);

			fireEvent("onVideoStateChanged", result);
		}
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
	}
}
