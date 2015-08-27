package io.kandy.proxy;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.*;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyModule;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import java.util.HashMap;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class CallServiceProxy extends KrollProxy implements KandyCallServiceNotificationListener {

	private static final String LCAT = CallServiceProxy.class.getSimpleName();

	private static final HashMap<String, IKandyCall> calls = new HashMap<String, IKandyCall>();
	private KrollDict callbacks = new KrollDict();

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

	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
		if (options.containsKey("callbacks"))
			setCallbacks(options.getKrollDict("callbacks"));
	}

	@Kroll.setProperty
	@Kroll.method
	public void setCallbacks(KrollDict callbacks) {
		this.callbacks = callbacks;
	}

	@Kroll.getProperty
	@Kroll.method
	public KrollDict getCallbacks() {
		return callbacks;
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

	private void removeCall(String id) {
		if (calls.containsKey(id))
			calls.remove(id);
	}

	@Kroll.method
	public void accept(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");
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
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;
		IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
		call.ignore(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void reject(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
		call.reject(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void hangup(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.hangup(new KandyCallResponseListenerCallBack(success, error));
		removeCall(id);
	}

	@Kroll.method
	public void mute(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.mute(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void unmute(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.unmute(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void hold(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.hold(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void unhold(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.unhold(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void switchVideoOn(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.startVideoSharing(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void switchVideoOff(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

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
		String id = args.getString("id");

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

		@Override
		public void onRequestFailed(IKandyCall call, int code, String err) {
			Log.d(LCAT, "KandyCallResponseListenerCallBack->onRequestFailed() was invoked: "
					+ call.getCallee().getUri() + " - " + String.valueOf(code) + " - " + err);
			KandyUtils.sendFailResult(getKrollObject(), error, code, err);
		}

		@Override
		public void onRequestSucceeded(IKandyCall call) {
			Log.d(LCAT, "KandyCallResponseListenerCallBack->onRequestSucceeded() was invoked: "
					+ call.getCallee().getUri());
			KandyUtils.sendSuccessResult(getKrollObject(), success, KandyUtils.getKrollDictFromKandyCall(call));
		}

	}

	@Override
	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onCallStateChanged"),
				KandyUtils.getKrollDictFromKandyCall(call));
	}

	@Override
	public void onGSMCallConnected(IKandyCall call) {
		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onGSMCallConnected"),
				KandyUtils.getKrollDictFromKandyCall(call));
	}

	@Override
	public void onGSMCallDisconnected(IKandyCall call) {
		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onGSMCallDisconnected"),
				KandyUtils.getKrollDictFromKandyCall(call));
	}

	@Override
	public void onGSMCallIncoming(IKandyCall call) {
		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onGSMCallIncoming"),
				KandyUtils.getKrollDictFromKandyCall(call));
	}

	@Override
	public void onIncomingCall(IKandyIncomingCall call) {
		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onIncomingCall"),
				KandyUtils.getKrollDictFromKandyCall(call));
	}

	@Override
	public void onMissedCall(KandyMissedCallMessage call) {
		KrollDict result = new KrollDict();

		result.put("uuid", call.getUUID().toString());
		result.put("timestamp", call.getTimestamp());
		result.put("via", call.getVia());
		result.put("source", KandyUtils.getKrollDictFromKandyRecord(call.getSource()));
		result.put("eventType", call.getEventType().name());

		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onMissedCall"), result);
	}

	@Override
	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
		KrollDict result = KandyUtils.getKrollDictFromKandyCall(call);
		result.put("isSendingVideo", isSendingVideo);
		result.put("isReceivingVideo", isReceivingVideo);

		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onVideoStateChanged"), result);
	}

	@Override
	public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage call) {
		KrollDict result = new KrollDict();

		result.put("uuid", call.getUUID().toString());
		result.put("totalMessages", call.getTotalMessages());
		result.put("numOfUnreadMessages", call.getNumOfUnreadMessages());
		result.put("timestamp", call.getTimestamp());
		result.put("eventType", call.getEventType().name());

		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onWaitingVoiceMailCall"), result);
	}
}
