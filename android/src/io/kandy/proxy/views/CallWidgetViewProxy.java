package io.kandy.proxy.views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.*;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.proxy.CallServiceProxy;
import io.kandy.proxy.CallWidgetProxy;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

public class CallWidgetViewProxy extends TiUIView implements KandyCallServiceNotificationListener {

	private static final String TAG = CallWidgetViewProxy.class.getSimpleName();

	private LinearLayout uiCallEditLayout;

	private EditText uiPhoneNumber;
	private Button callButton;
	private CheckBox uiVideoCheckbox, uiPSTNCheckbox, uiSIPCheckbox;

	private boolean isVideoCall = false;
	private boolean isPSTNCall = false;
	private boolean isSIPCall = false;

	private boolean hasOutgoingDialog = true;
	private boolean hasIncomingDialog = true;

	private Activity activity;
	private IKandyCall call;

	private KandyInCallDialog inCallDialog;
	private AlertDialog mIncomingCallDialog;

	public CallWidgetViewProxy(TiViewProxy proxy) {
		super(proxy);

		activity = proxy.getActivity();

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_call_widget"), null);

		uiCallEditLayout = (LinearLayout) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_with_edit"));

		uiPhoneNumber = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_phone_number_edit"));

		uiVideoCheckbox = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_checkbox"));
		uiVideoCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
				isVideoCall = isChecked;
			}
		});

		uiPSTNCheckbox = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_pstn_checkbox"));
		uiPSTNCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
				isPSTNCall = isChecked;
			}
		});

		uiSIPCheckbox = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_sip_trunk_checkbox"));
		uiSIPCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
				isSIPCall = isChecked;
			}
		});

		callButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_call_button"));
		callButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doCall();
			}
		});

		setNativeView(layoutWraper);
	}

	@Override
	public void processProperties(KrollDict properties) {
		super.processProperties(properties);
		if (properties.containsKey(CallWidgetProxy.CALLEE)) {
			uiPhoneNumber.setText(properties.getString(CallWidgetProxy.CALLEE));
			uiPhoneNumber.setVisibility(View.GONE);
		}
		if (properties.containsKey(CallWidgetProxy.CALL_TEXT_BUTTON)) {
			callButton.setText(properties.getString(CallWidgetProxy.CALL_TEXT_BUTTON));
		}
		if (properties.containsKey(CallWidgetProxy.CALL_TYPE)) {
			String type = properties.getString(CallWidgetProxy.CALL_TYPE).toLowerCase();
			if (TextUtils.equals(type, CallWidgetProxy.CALL_VIDEO_TYPE))
				isVideoCall = true;
			else if (TextUtils.equals(type, CallWidgetProxy.CALL_PSTN_TYPE))
				isPSTNCall = true;
			else if (TextUtils.equals(type, CallWidgetProxy.CALL_SIP_TYPE))
				isSIPCall = true;
		} else if (properties.containsKey(CallWidgetProxy.CALL_TYPES)) {
			String[] types = properties.getStringArray(CallWidgetProxy.CALL_TYPES);
			for (String type : types) {
				type = type.toLowerCase();
				if (TextUtils.equals(type, CallWidgetProxy.CALL_VIDEO_TYPE)) {
					uiVideoCheckbox.setVisibility(View.VISIBLE);
				} else if (TextUtils.equals(type, CallWidgetProxy.CALL_PSTN_TYPE)) {
					uiPSTNCheckbox.setVisibility(View.VISIBLE);
				} else if (TextUtils.equals(type, CallWidgetProxy.CALL_SIP_TYPE)) {
					uiSIPCheckbox.setVisibility(View.VISIBLE);
				}
			}
		}

		if (properties.containsKey(CallWidgetProxy.CALL_WIDGETS)) {
			hasOutgoingDialog = hasIncomingDialog = false;
			String[] types = properties.getStringArray(CallWidgetProxy.CALL_WIDGETS);
			for (String type : types) {
				type = type.toLowerCase();
				if (TextUtils.equals(type, CallWidgetProxy.CALL_OUTGOING_WIDGET))
					hasOutgoingDialog = true;
				else if (TextUtils.equals(type, CallWidgetProxy.CALL_INCOMING_WIDGET))
					hasIncomingDialog = true;
			}
		}
	}

	private void doCall() {
		//IKandyCall call;
		String number = uiPhoneNumber.getText().toString();
		if (isPSTNCall) {
			call = Kandy.getServices().getCallService().createPSTNCall(null, number, null);
		} else if (isSIPCall) {
			KandyRecord callee;
			try {
				number = KandyRecord.normalize(number);
				callee = new KandyRecord(number);
			} catch (KandyIllegalArgumentException e) {
				e.printStackTrace();
				Log.d(TAG, "doCall: " + e.getLocalizedMessage(), e);
				UIUtils.showDialogWithErrorMessage(proxy.getActivity(),
						KandyUtils.getString("kandy_chat_phone_number_verification_text"));
				return;
			}
			call = Kandy.getServices().getCallService().createSIPTrunkCall(null, callee);
		} else {
			KandyRecord callee;
			try {
				callee = new KandyRecord(number);
			} catch (KandyIllegalArgumentException e) {
				e.printStackTrace();
				Log.d(TAG, "doCall: " + e.getLocalizedMessage(), e);
				UIUtils.showDialogWithErrorMessage(proxy.getActivity(),
						KandyUtils.getString("kandy_chat_phone_number_verification_text"));
				return;
			}
			KandyOutgingVoipCallOptions callOptions = isVideoCall ? KandyOutgingVoipCallOptions.START_CALL_WITH_VIDEO
					: KandyOutgingVoipCallOptions.START_CALL_WITHOUT_VIDEO;
			call = Kandy.getServices().getCallService().createVoipCall(null, callee, callOptions);
		}

		if (call == null) {
			UIUtils.showDialogWithErrorMessage(proxy.getActivity(), "Invalid number");
			return;
		}

		setDummyKandyVideoView(call);

		CallServiceProxy.addKandyCall(call);

		inCallDialog = new KandyInCallDialog(proxy);
		inCallDialog.setKandyCall(call);

		((IKandyOutgoingCall) call).establish(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "KandyCallResponseListener.onRequestSucceeded() was invoked: " + call.getCallee().getUri());
				inCallDialog.show();
			}

			@Override
			public void onRequestFailed(IKandyCall call, int code, String error) {
				CallServiceProxy.removeKandyCall(call.getCallee().getUri());
				UIUtils.showDialogWithErrorMessage(proxy.getActivity(), error);
			}
		});
	}

	private void setDummyKandyVideoView(IKandyCall call) {
		KandyView kView = new KandyView(proxy.getActivity(), null);
		call.setLocalVideoView(kView);
		call.setRemoteVideoView(kView);
	}

	public void answerIncomingCall(final IKandyIncomingCall call) {
		CallServiceProxy.addKandyCall(call);
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				createIncomingCallPopup(call);
			}
		});
	}

	private void createIncomingCallPopup(final IKandyIncomingCall pInCall) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setPositiveButton(KandyUtils.getString("kandy_calls_answer_button_label"),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						call = pInCall;
						accept();
						dialog.dismiss();
					}
				});

		builder.setNeutralButton(KandyUtils.getString("kandy_calls_ignore_incoming_call_button_label"),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						ignoreIncomingCall((IKandyIncomingCall) pInCall);
						dialog.dismiss();
					}
				});

		builder.setNegativeButton(KandyUtils.getString("kandy_calls_reject_incoming_call_button_label"),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						rejectIncomingCall((IKandyIncomingCall) pInCall);
						dialog.dismiss();
					}
				});

		builder.setMessage(KandyUtils.getString("kandy_calls_incoming_call_popup_message_label")
				+ pInCall.getCallee().getUri());

		mIncomingCallDialog = builder.create();
		mIncomingCallDialog.show();
	}

	public void accept() {
		if (inCallDialog != null)
			inCallDialog.hangup();
		if (call.canReceiveVideo()) {
			((IKandyIncomingCall) call).accept(isVideoCall, new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
					inCallDialog = new KandyInCallDialog(proxy);
					inCallDialog.setKandyCall(call);
					inCallDialog.show();
				}

				@Override
				public void onRequestFailed(IKandyCall call, int code, String error) {
					UIUtils.showToastWithMessage(activity, error);
				}
			});
		} else {
			((IKandyIncomingCall) call).accept(false, new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
					inCallDialog = new KandyInCallDialog(proxy);
					inCallDialog.setKandyCall(call);
					inCallDialog.show();
				}

				@Override
				public void onRequestFailed(IKandyCall call, int code, String error) {
					UIUtils.showToastWithMessage(activity, error);
				}
			});
		}
	}

	public void ignoreIncomingCall(IKandyIncomingCall pIncall) {
		if (pIncall == null)
			return;
		pIncall.reject(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				CallServiceProxy.removeKandyCall(call.getCallee().getUri());
			}

			@Override
			public void onRequestFailed(IKandyCall call, int code, String error) {
				UIUtils.showToastWithMessage(activity, error);
			}
		});
	}

	public void rejectIncomingCall(IKandyIncomingCall pIncall) {
		if (pIncall == null)
			return;
		pIncall.ignore(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				CallServiceProxy.removeKandyCall(call.getCallee().getUri());
			}

			@Override
			public void onRequestFailed(IKandyCall call, int code, String error) {
				UIUtils.showToastWithMessage(activity, error);
			}
		});
	}

	@Override
	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		Log.i(TAG, "onCallStateChanged() was invoked: " + call.getCallee().getUri() + " " + state.name());
		if (state == KandyCallState.TERMINATED && inCallDialog.isShowing()
				&& inCallDialog.isKandyCall(call.getCallee().getUri())) {
			CallServiceProxy.removeKandyCall(call.getCallee().getUri());
			inCallDialog.dismiss();
		}
	}

	@Override
	public void onGSMCallConnected(IKandyCall call) {
		if (inCallDialog != null)
			inCallDialog.switchHold(true);
	}

	@Override
	public void onGSMCallDisconnected(IKandyCall call) {
		if (inCallDialog != null)
			inCallDialog.switchHold(false);
	}

	@Override
	public void onGSMCallIncoming(IKandyCall call) {
	}

	@Override
	public void onIncomingCall(final IKandyIncomingCall call) {
		Log.i(TAG, "onIncomingCall() was invoked: " + call.getCallee().getUri());
		answerIncomingCall(call);
	}

	@Override
	public void onMissedCall(KandyMissedCallMessage message) {
	}

	@Override
	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
	}

	@Override
	public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage message) {
	}
}
