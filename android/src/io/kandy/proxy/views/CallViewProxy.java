package io.kandy.proxy.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.*;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

public class CallViewProxy extends TiUIView implements KandyCallServiceNotificationListener {

	private static final String TAG = CallViewProxy.class.getSimpleName();

	private Activity activity;

	private KrollDict callbacks;
	
	private EditText uiPhoneNumberEdit;

	private ImageView uiCallButton;
	private ImageView uiHangupButton;

	private ToggleButton uiHoldTButton;
	private ToggleButton uiMuteTButton;
	private ToggleButton uiVideoTButton;
	private ToggleButton uiSwitchCameraTButton;

	private CheckBox uiVideoCheckBox;
	private CheckBox uiPSTNCheckBox;
	private CheckBox uiSipTrunkCheckBox;

	private KandyView uiRemoteVideoView;
	private KandyView uiLocalVideoView;

	private TextView uiAudioStateTextView;
	private TextView uiVideoStateTextView;
	private TextView uiCallsStateTextView;

	private AlertDialog mIncomingCallDialog;

	private boolean mIsCreateVideoCall = true;
	private boolean mIsPSTNCall = false;
	private boolean mIsSipTrunkCall = false;
	private boolean mIsHold = false;
	private boolean mIsMute = false;
	private boolean mIsVideo = true;

	private IKandyCall mCurrentCall;

	private OnClickListener onCallButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			startCallProcess();
		}
	};

	private OnClickListener onHangupButtonClicked = new OnClickListener() {

		@Override
		public void onClick(View v) {
			doHangup(mCurrentCall);
		}
	};

	private OnCheckedChangeListener onHoldTButtonClicked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (mCurrentCall != null) {
				switchHold(mCurrentCall, isChecked);
			}
		}
	};

	private OnCheckedChangeListener onMuteTButtonClicked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (mCurrentCall != null) {
				switchMute(mCurrentCall, isChecked);
			}
		}
	};

	private OnCheckedChangeListener onVideoTButtonClicked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (mCurrentCall != null) {
				switchVideo(mCurrentCall, isChecked);
			}
		}
	};
	private OnCheckedChangeListener onSwitchCameraTButtonClicked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (mCurrentCall != null) {
				switchCamera(mCurrentCall, isChecked);
			}
		}
	};

	private OnCheckedChangeListener onVideoCheckBoxChecked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mIsCreateVideoCall = isChecked;
		}
	};

	private OnCheckedChangeListener onPSTNCheckBoxChecked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mIsPSTNCall = isChecked;
		}
	};

	private OnCheckedChangeListener onSipTrunkCheckBoxChecked = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			mIsSipTrunkCall = isChecked;
		}
	};

	public CallViewProxy(TiViewProxy proxy) {
		super(proxy);

		activity = proxy.getActivity();

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_call_widget"), null);

		uiPhoneNumberEdit = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_phone_number_edit"));

		uiCallButton = (ImageView) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_call_button"));
		uiCallButton.setOnClickListener(onCallButtonClicked);

		uiHangupButton = (ImageView) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_hangup_button"));
		uiHangupButton.setOnClickListener(onHangupButtonClicked);

		uiHoldTButton = (ToggleButton) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_hold_tbutton"));
		uiHoldTButton.setOnCheckedChangeListener(onHoldTButtonClicked);

		uiMuteTButton = (ToggleButton) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_mute_tbutton"));
		uiMuteTButton.setOnCheckedChangeListener(onMuteTButtonClicked);

		uiVideoTButton = (ToggleButton) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_tbutton"));
		uiVideoTButton.setOnCheckedChangeListener(onVideoTButtonClicked);

		uiSwitchCameraTButton = (ToggleButton) layoutWraper.findViewById(KandyUtils
				.getId("kandy_calls_switch_camera_tbutton"));
		uiSwitchCameraTButton.setOnCheckedChangeListener(onSwitchCameraTButtonClicked);

		uiVideoCheckBox = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_checkbox"));
		uiVideoCheckBox.setOnCheckedChangeListener(onVideoCheckBoxChecked);
		uiVideoCheckBox.setChecked(true);

		uiPSTNCheckBox = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_pstn_checkbox"));
		uiPSTNCheckBox.setOnCheckedChangeListener(onPSTNCheckBoxChecked);

		uiSipTrunkCheckBox = (CheckBox) layoutWraper.findViewById(KandyUtils
				.getId("kandy_calls_video_sip_trunk_checkbox"));
		uiSipTrunkCheckBox.setOnCheckedChangeListener(onSipTrunkCheckBoxChecked);

		uiAudioStateTextView = (TextView) layoutWraper
				.findViewById(KandyUtils.getId("kandy_calls_state_audio_text"));
		setAudioState(mIsMute);

		uiVideoStateTextView = (TextView) layoutWraper
				.findViewById(KandyUtils.getId("kandy_calls_state_video_text"));
		setVideoState(false, false);

		uiCallsStateTextView = (TextView) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_state_call_text"));
		setCallState(KandyCallState.INITIAL.name());

		uiRemoteVideoView = (KandyView) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_view"));
		uiLocalVideoView = (KandyView) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_local_video_view"));

		KandyCallState callState = mCurrentCall != null ? mCurrentCall.getCallState() : KandyCallState.INITIAL;
		setCallSettingsOnUIThread(callState);

		setNativeView(layoutWraper);
	}

	public void setCallbacks(KrollDict callbacks){
		this.callbacks = callbacks;
	}
	
	public KrollDict getCallbacks(){
		return callbacks;
	}
	
	protected void switchCamera(IKandyCall currentCall, boolean isChecked) {
		currentCall.getCameraForVideo();
		KandyCameraInfo cameraInfo = isChecked ? KandyCameraInfo.FACING_FRONT : KandyCameraInfo.FACING_BACK;
		currentCall.switchCamera(cameraInfo);
	}

	/*------------- Functionality logic --------------*/

	private void setAudioState(boolean pState) {

		final String state = String.format("%s %s", KandyUtils.getString("kandy_calls_audio_state"),
				String.valueOf(pState));
		setStateForTextViewOnUIThread(uiAudioStateTextView, state);
	}

	private void setVideoState(boolean isReceiving, boolean isSending) {

		final String state = String.format("%s %s, %s %s",
				KandyUtils.getString("kandy_calls_receiving_video_state"), String.valueOf(isReceiving),
				KandyUtils.getString("kandy_calls_sending_video_state"), String.valueOf(isSending));
		setStateForTextViewOnUIThread(uiVideoStateTextView, state);
	}

	private void setCallState(final String pState) {
		setStateForTextViewOnUIThread(uiCallsStateTextView, pState);
	}

	private void setStateForTextViewOnUIThread(final TextView pTextView, final String pState) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				pTextView.setText(pState);
			}
		});
	}

	private String getPhoneNumber() {
		return uiPhoneNumberEdit.getText().toString();
	}

	private void createIncomingCallPopup(IKandyIncomingCall pInCall) {
		mCurrentCall = pInCall;

		if (activity.isFinishing()) {
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setPositiveButton(KandyUtils.getString("kandy_calls_answer_button_label"),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						accept();
						dialog.dismiss();
					}
				});

		builder.setNeutralButton(KandyUtils.getString("kandy_calls_ignore_incoming_call_button_label"),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						ignoreIncomingCall((IKandyIncomingCall) mCurrentCall);
						dialog.dismiss();
					}
				});

		builder.setNegativeButton(KandyUtils.getString("kandy_calls_reject_incoming_call_button_label"),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						rejectIncomingCall((IKandyIncomingCall) mCurrentCall);
						dialog.dismiss();
					}
				});

		builder.setMessage(KandyUtils.getString("kandy_calls_incoming_call_popup_message_label")
				+ mCurrentCall.getCallee().getUri());

		mIncomingCallDialog = builder.create();
		mIncomingCallDialog.show();
	}

	/**
	 * Ignoring the incoming call - the caller wont know about ignore, call will
	 * continue on his side
	 * 
	 * @param pCall
	 *            incoming call instance
	 */
	public void ignoreIncomingCall(IKandyIncomingCall pCall) {
		if (pCall == null) {
			UIUtils.showToastWithMessage(activity, KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
			return;
		}

		pCall.ignore(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "mCurrentIncomingCall.ignore succeed");
				setCallSettingsOnUIThread(KandyCallState.TERMINATED);
				mCurrentCall = null;
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "mCurrentIncomingCall.ignore failed");
			}
		});
	}

	/**
	 * Reject the incoming call
	 * 
	 * @param pCall
	 *            incoming call instance
	 */
	public void rejectIncomingCall(IKandyIncomingCall pCall) {
		if (pCall == null) {
			UIUtils.showToastWithMessage(activity, KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
			return;
		}

		pCall.reject(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "mCurrentIncomingCall.reject succeeded");
				setCallSettingsOnUIThread(KandyCallState.TERMINATED);
				mCurrentCall = null;
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "mCurrentIncomingCall.reject. Error: " + err + "\nResponse code: " + responseCode);
			}
		});
	}

	/**
	 * Accept incoming call
	 */
	public void accept() {
		if (mCurrentCall.canReceiveVideo()) {
			// Call has video m line
			// so this call can be answered with
			// acceptCall(true) if one wants to answer with video
			// or
			// acceptCall(false) if one wants to answer with audio only

			((IKandyIncomingCall) mCurrentCall).accept(mIsCreateVideoCall, new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
					Log.i(TAG, "mCurrentIncomingCall.accept succeed");
				}

				@Override
				public void onRequestFailed(IKandyCall call, int responseCode, String err) {
					Log.i(TAG, "mCurrentIncomingCall.accept. Error: " + err + "\nResponse code: " + responseCode);
				}
			});
		} else {
			// Call has only one m line, so this call will be answered
			// with only audio
			// acceptCall(false)

			((IKandyIncomingCall) mCurrentCall).accept(false, new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
					Log.i(TAG, "mCurrentIncomingCall.accept succeed");
				}

				@Override
				public void onRequestFailed(IKandyCall call, int responseCode, String err) {
					Log.i(TAG, "mCurrentIncomingCall.accept. Error: " + err + "\nResponse code: " + responseCode);
				}
			});
		}
	}

	/**
	 * Register a listener to receive calls related events like incoming call,
	 * call state change, etc...
	 */
	public void registerCallListener() {
		Log.d(TAG, "registerCallListener()");
		Kandy.getServices().getCallService().registerNotificationListener(this);
	}

	public void unregisterCallListener() {
		Log.d(TAG, "unregisterCallListener()");
		Kandy.getServices().getCallService().unregisterNotificationListener(this);
	}

	private void answerCall(final IKandyIncomingCall pCall) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				createIncomingCallPopup(pCall);
			}
		});
	}

	public void destroyCurrentCall(){
		if(mCurrentCall != null) {
			doHangup(mCurrentCall);
		}
	}
	
	/**
	 * Start calling process
	 */
	private void startCallProcess() {
		doCall(getPhoneNumber());
	}

	/**
	 * Do a call to entered phone number
	 * 
	 * @param pCallee
	 *            user's phone with domain (username@domain) see
	 *            {@link KandyRecord}
	 */
	private void doCall(String number) {

		if (mIsPSTNCall) {
			mCurrentCall = Kandy.getServices().getCallService().createPSTNCall(null, number, null);
		} else if (mIsSipTrunkCall) {
			KandyRecord callee;
			try {
				number = KandyRecord.normalize(number);
				callee = new KandyRecord(number);
			} catch (KandyIllegalArgumentException e) {
				e.printStackTrace();
				Log.d(TAG, "doCall: " + e.getLocalizedMessage(), e);
				UIUtils.showDialogWithErrorMessage(activity,
						KandyUtils.getString("kandy_chat_phone_number_verification_text"));
				return;
			}
			mCurrentCall = Kandy.getServices().getCallService().createSIPTrunkCall(null, callee);
		} else {
			KandyRecord callee;
			try {
				callee = new KandyRecord(number);
			} catch (KandyIllegalArgumentException e) {
				e.printStackTrace();
				Log.d(TAG, "doCall: " + e.getLocalizedMessage(), e);
				UIUtils.showDialogWithErrorMessage(activity,
						KandyUtils.getString("kandy_chat_phone_number_verification_text"));
				return;
			}
			KandyOutgingVoipCallOptions callOptions = mIsCreateVideoCall ? KandyOutgingVoipCallOptions.START_CALL_WITH_VIDEO
					: KandyOutgingVoipCallOptions.START_CALL_WITHOUT_VIDEO;
			mCurrentCall = Kandy.getServices().getCallService().createVoipCall(null, callee, callOptions);
		}

		if (mCurrentCall == null) {
			UIUtils.showDialogWithErrorMessage(activity, "Invalid number");
			return;
		}

		mCurrentCall.setLocalVideoView(uiLocalVideoView);
		mCurrentCall.setRemoteVideoView(uiRemoteVideoView);
		((IKandyOutgoingCall) mCurrentCall).establish(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "doCall:onRequestSucceeded: true");
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "onRequestFailed: " + err + " Response code: " + responseCode);
				UIUtils.showDialogWithErrorMessage(activity, err);
			}
		});
	}

	/**
	 * Hanging up current call, and notify in case of fail.
	 * 
	 * @param pCall
	 *            current call for which hanguo will be applied
	 */
	private void doHangup(IKandyCall pCall) {

		if (pCall == null) {
			UIUtils.showToastWithMessage(activity, KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
			return;
		}

		hangup();
	}

	/**
	 * Raw call hangup
	 */
	public void hangup() {
		mCurrentCall.hangup(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "doHangup:onRequestSucceeded: true");
				mCurrentCall = null;
				setCallSettingsOnUIThread(KandyCallState.TERMINATED);
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "doHangup:onRequestFailed: " + err + " Response code: " + responseCode);
				UIUtils.showDialogWithErrorMessage(activity, err);
				mCurrentCall = null;
				setCallSettingsOnUIThread(KandyCallState.TERMINATED);
			}
		});

	}

	/**
	 * Handle switching between mute/unmute mode of current call (if is active)
	 * 
	 * @param isMute
	 */
	private void switchMute(IKandyCall pCall, boolean isMute) {

		if (pCall == null) {
			uiMuteTButton.setChecked(false);
			UIUtils.showToastWithMessage(activity, KandyUtils.getString("kandy_calls_invalid_mute_call_text_msg"));
			return;
		}

		mIsMute = isMute;

		if (mIsMute)
			doMute(pCall);
		else
			doUnMute(pCall);
	}

	/**
	 * Mute current active call
	 */
	private void doMute(IKandyCall pCall) {

		pCall.mute(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "doMute:onRequestSucceeded: true");
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "doMute:onRequestFailed: " + err + " Response code: " + responseCode);
			}
		});
	}

	/**
	 * Unmute current active call
	 */
	private void doUnMute(IKandyCall pCall) {
		pCall.unmute(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "doUnMute:onRequestSucceeded: true");
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "doUnMute:onRequestFailed: " + err + " Response code: " + responseCode);
			}
		});
	}

	/**
	 * Handle switching between hold/unhold mode of current call (if is active)
	 * 
	 * @param isHold
	 */
	private void switchHold(IKandyCall pCall, boolean isHold) {

		if (pCall == null) {
			uiHoldTButton.setChecked(false);
			UIUtils.showToastWithMessage(activity, KandyUtils.getString("kandy_calls_invalid_hold_text_msg"));
			return;
		}

		mIsHold = isHold;

		if (mIsHold)
			doHold(pCall);
		else
			doUnHold(pCall);
	}

	/**
	 * Hold current active call
	 */
	private void doHold(IKandyCall pCall) {
		pCall.hold(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "doHold:onRequestSucceeded: true");
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "doHold:onRequestFailed: " + err + " Response code: " + responseCode);
			}
		});
	}

	/**
	 * Unhold current active call
	 */
	private void doUnHold(IKandyCall pCall) {
		pCall.unhold(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "doUnHold:onRequestSucceeded: true");
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "doUnHold:onRequestFailed: " + err + " Response code: " + responseCode);
			}
		});
	}

	/**
	 * Handle switching between start/stop video call in current call (if is
	 * active)
	 * 
	 * @param isVideoOn
	 */
	private void switchVideo(IKandyCall pCall, boolean isVideoOn) {
		if (pCall == null) {
			uiVideoTButton.setChecked(false);
			UIUtils.showToastWithMessage(activity, KandyUtils.getString("kandy_calls_invalid_video_call_text_msg"));
			return;
		}

		if (isVideoOn)
			enableVideo(pCall);
		else
			disableVideo(pCall);
	}

	/**
	 * Turn on video during video call
	 */
	private void enableVideo(IKandyCall pCall) {
		pCall.startVideoSharing(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "enableVideo:onRequestSucceeded: true");
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "enableVideo:onRequestFailed: " + err + " Response code: " + responseCode);
			}
		});
	}

	/**
	 * Turn off video during video call
	 */
	private void disableVideo(IKandyCall pCall) {
		pCall.stopVideoSharing(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "disableVideo:onRequestSucceeded: true");
			}

			@Override
			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
				Log.i(TAG, "disableVideo:onRequestFailed: " + err + " Response code: " + responseCode);
			}
		});
	}

	@Override
	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
		Log.i(TAG, "onVideoStateChanged: Receiving: " + isReceivingVideo + " Sending: " + isSendingVideo);
		setVideoState(isReceivingVideo, isSendingVideo);
		setAudioState(call.isMute());
	}

	@Override
	public void onGSMCallIncoming(IKandyCall call) {
		// Here you can implement the GSMCallIncoming behavior/actions
		Log.i(TAG, "onGSMCallIncoming");
	}

	@Override
	public void onGSMCallConnected(IKandyCall call) {
		// Here you can implement the GSMCallConnected behavior/actions
		Log.i(TAG, "onGSMCallConnected");
		doHold(mCurrentCall);
	}

	@Override
	public void onGSMCallDisconnected(IKandyCall call) {
		// Here you can implement the GSMCallDisconnection behavior/actions
		Log.i(TAG, "onGSMCallDisconnected");
		doUnHold(mCurrentCall);
	}

	@Override
	public void onIncomingCall(IKandyIncomingCall call) {
		// Here while state is KandyCallState.RINGING you can play a ringtone
		Log.i(TAG, "onIncomingCall: " + call.getCallId());
		answerCall(call);
	}

	@Override
	public void onMissedCall(KandyMissedCallMessage call) {
		Log.d(TAG, "onMissedCall: call: " + call);
	}

	@Override
	public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage event) {
		Log.d(TAG, "onWaitingVoiceMailCall: event: " + event);

	}

	@Override
	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		// Here while state is KandyCallState.DIALING you can play a ringback
		Log.i(TAG, "onCallStatusChanged: " + state.name());

		setCallSettingsOnUIThread(state);

		String reason = "";
		if (state == KandyCallState.TALKING) {
			mCurrentCall.setLocalVideoView(uiLocalVideoView);
			mCurrentCall.setRemoteVideoView(uiRemoteVideoView);
		} else if (state == KandyCallState.TERMINATED) {
			KandyCallTerminationReason callTerminationReason = call.getTerminationReason();
			Log.d(TAG, "registerCallListener: " + "call TERMINATED reason: " + callTerminationReason);
			reason = "(" + callTerminationReason.getReason() + ")";
			mCurrentCall = null;
		}
		setCallState(state.name() + reason);
	}

	/**
	 * Set the states of video, hold and mute on ToggleButtons
	 * 
	 * @param state
	 */
	private void setCallSettingsOnUIThread(KandyCallState state) {

		final boolean isMute;
		final boolean isVideoEnabled;
		final boolean isHold;
		final boolean isStartWithVideoEnabled;
		final boolean isSwitchCameraEnabled;
		final boolean isMuteEnabled;
		final boolean isHoldEnabled;
		final boolean isVideoButtonEnabled;

		switch (state) {
		case TERMINATED:
			isMute = false;
			isHold = false;
			isVideoEnabled = true;
			isStartWithVideoEnabled = true;
			isSwitchCameraEnabled = false;
			isMuteEnabled = false;
			isHoldEnabled = false;
			isVideoButtonEnabled = false;
			break;
		case TALKING:
			isMute = mIsMute;
			isHold = mIsHold;
			isVideoEnabled = mIsVideo;
			isStartWithVideoEnabled = false;
			isSwitchCameraEnabled = mCurrentCall.isSendingVideo();
			isMuteEnabled = true;
			isHoldEnabled = true;
			isVideoButtonEnabled = true;
			break;

		case INITIAL:
			isMute = false;
			isHold = false;
			isVideoEnabled = true;
			isStartWithVideoEnabled = true;
			isSwitchCameraEnabled = false;
			isMuteEnabled = false;
			isHoldEnabled = false;
			isVideoButtonEnabled = false;
			break;
		case ON_HOLD:
		case ON_DOUBLE_HOLD:
			isHold = true;
			isMute = false;
			isVideoEnabled = true;
			isStartWithVideoEnabled = true;
			isSwitchCameraEnabled = false;
			isMuteEnabled = false;
			isHoldEnabled = true;
			isVideoButtonEnabled = false;
			break;
		default:
			isMute = mIsMute;
			isHold = mIsHold;
			isVideoEnabled = mIsVideo;
			isStartWithVideoEnabled = false;
			isSwitchCameraEnabled = false;
			isMuteEnabled = false;
			isHoldEnabled = false;
			isVideoButtonEnabled = false;
			break;
		}

		mIsHold = isHold;
		mIsVideo = isVideoEnabled;
		mIsMute = isMute;

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				uiHoldTButton.setChecked(isHold);
				uiMuteTButton.setChecked(isMute);
				uiVideoTButton.setChecked(isVideoEnabled);
				uiVideoCheckBox.setEnabled(isStartWithVideoEnabled);
				uiSwitchCameraTButton.setEnabled(isSwitchCameraEnabled);
				uiHoldTButton.setEnabled(isHoldEnabled);
				uiMuteTButton.setEnabled(isMuteEnabled);
				uiVideoTButton.setEnabled(isVideoButtonEnabled);
			}
		});
	}
}