package io.kandy.proxy;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.*;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyModule;
import io.kandy.proxy.views.CallViewProxy;
import io.kandy.proxy.views.KandyVideoView;
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

	@Kroll.constant
	public static final String CALL_START_WITH_VIDEO = "startWithVideo";

	private boolean videoEnabled = true;

	private CallViewProxy viewProxy;

	private KrollDict calls = new KrollDict();
	private KrollDict localVideoViews = new KrollDict();
	private KrollDict remoteVideoViews = new KrollDict();

	public CallServiceProxy() {
		super();
	}

	@Override
	public void onPause(Activity activity) {
		if (viewProxy != null)
			viewProxy.unregisterCallListener();
		super.onPause(activity);
	}

	@Override
	public void onResume(Activity activity) {
		if (viewProxy != null)
			viewProxy.registerCallListener();
		super.onResume(activity);
	}

	@Override
	public void onDestroy(Activity activity) {
		if (viewProxy != null)
			viewProxy.destroyCurrentCall();
		super.onDestroy(activity);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCreationDict(KrollDict options) {
		Log.d(LCAT, "handleCreationDict() invoked.");
		if (options.containsKey(CALL_START_WITH_VIDEO))
			videoEnabled = options.getBoolean(CALL_START_WITH_VIDEO);
		super.handleCreationDict(options);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TiUIView createView(Activity activity) {
		viewProxy = new CallViewProxy(this);
		Log.d(LCAT, "createView() invoked.");
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
		if (viewProxy != null)
			viewProxy.setCallbacks(args);
	}

	@Kroll.getProperty
	@Kroll.method
	public KrollDict getCallbacks() {
		if (viewProxy != null)
			return viewProxy.getCallbacks();
		return new KrollDict();
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
		setKandyVideoViewsAndEstablishCall(call, success, error);
	}

	@Kroll.method
	public void createPSTNCall(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String number = args.getString("numberPhone");

		number = number.replace("+", "");
		number = number.replace("-", "");

		IKandyCall call = Kandy.getServices().getCallService().createPSTNCall(null, number, null);
		setKandyVideoViewsAndEstablishCall(call, success, error);
	}

	@Kroll.method
	public void createSIPTrunkCall(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String number = args.getString("numberPhone");

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
		KandyVideoView localVideo = new KandyVideoView(getActivity());
		KandyVideoView remoteVideo = new KandyVideoView(getActivity());

		localVideo.setLocalVideoView(call);
		remoteVideo.setRemoteVideoView(call);

		calls.put(call.getCallee().getUri(), call);
		localVideoViews.put(call.getCallee().getUri(), localVideo);
		remoteVideoViews.put(call.getCallee().getUri(), remoteVideo);

		((IKandyOutgoingCall) call).establish(new KandyCallResponseListenerCallBack(success, error));
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
		if (localVideoViews.containsKey(id)) {
			((KandyVideoView) localVideoViews.get(id)).dismiss();
			localVideoViews.remove(id);
		}
		if (remoteVideoViews.containsKey(id)) {
			((KandyVideoView) remoteVideoViews.get(id)).dismiss();
			remoteVideoViews.remove(id);
		}
	}

	@Kroll.method
	public void showLocalVideo(KrollDict args) {
		String id = args.getString("id");
		int top = args.getInt("top");
		int left = args.getInt("left");
		int width = args.getInt("width");
		int height = args.getInt("height");
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		if (!checkActiveCall(id, error))
			return;

		KandyVideoView localVideo = (KandyVideoView) localVideoViews.get(id);
		localVideo.layout(left, top, width, height);
		localVideo.show();

		KandyUtils.sendSuccessResult(getKrollObject(), success);
	}

	@Kroll.method
	public void showRemoteVideo(KrollDict args) {
		String id = args.getString("id");
		int top = args.getInt("top");
		int left = args.getInt("left");
		int width = args.getInt("width");
		int height = args.getInt("height");
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		if (!checkActiveCall(id, error))
			return;

		KandyVideoView remoteVideo = (KandyVideoView) remoteVideoViews.get(id);
		remoteVideo.layout(left, top, width, height);
		remoteVideo.show();

		KandyUtils.sendSuccessResult(getKrollObject(), success);
	}

	@Kroll.method
	public void hideLocalVideo(KrollDict args) {
		String id = args.getString("id");
		int top = args.getInt("top");
		int left = args.getInt("left");
		int width = args.getInt("width");
		int height = args.getInt("height");
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		if (!checkActiveCall(id, error))
			return;

		KandyVideoView localVideo = (KandyVideoView) localVideoViews.get(id);
		localVideo.layout(left, top, width, height);
		localVideo.hide();

		KandyUtils.sendSuccessResult(getKrollObject(), success);
	}

	@Kroll.method
	public void hideRemoteVideo(KrollDict args) {
		String id = args.getString("id");
		int top = args.getInt("top");
		int left = args.getInt("left");
		int width = args.getInt("width");
		int height = args.getInt("height");
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		if (!checkActiveCall(id, error))
			return;

		KandyVideoView remoteVideo = (KandyVideoView) remoteVideoViews.get(id);
		remoteVideo.layout(left, top, width, height);
		remoteVideo.hide();

		KandyUtils.sendSuccessResult(getKrollObject(), success);
	}

	@Kroll.method
	public void accept(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyIncomingCall call = (IKandyIncomingCall) calls.get(id);
		KandyVideoView localVideo = new KandyVideoView(getActivity());
		KandyVideoView remoteVideo = new KandyVideoView(getActivity());

		localVideo.setLocalVideoView(call);
		remoteVideo.setRemoteVideoView(call);

		localVideoViews.put(call.getCallee().getUri(), localVideo);
		remoteVideoViews.put(call.getCallee().getUri(), remoteVideo);

		call.accept(videoEnabled, new KandyCallResponseListenerCallBack(success, error));
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
	public void enableVideo(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.startVideoSharing(new KandyCallResponseListenerCallBack(success, error));
	}

	@Kroll.method
	public void disableVideo(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");
		String id = args.getString("id");

		if (!checkActiveCall(id, error))
			return;

		IKandyCall call = (IKandyCall) calls.get(id);
		call.stopVideoSharing(new KandyCallResponseListenerCallBack(success, error));
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
}
