package io.kandy.proxy.views;

import io.kandy.proxy.CallServiceProxy;
import io.kandy.proxy.CallWidgetProxy;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

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

public class CallWidgetViewProxy extends TiUIView implements KandyCallServiceNotificationListener {

	private static final String TAG = CallWidgetViewProxy.class.getSimpleName();

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

	private KandyIncallDialog inCallDialog;
	private AlertDialog mIncomingCallDialog;

	// private CalleeAdapter calleeAdapter;
	// private ArrayList<String> calleeList;

	public CallWidgetViewProxy(TiViewProxy proxy) {
		super(proxy);

		activity = proxy.getActivity();

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_call_widget"), null);

		uiPhoneNumber = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_phone_number_edit"));

		uiVideoCheckbox = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_checkbox"));
		uiVideoCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			
			public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
				isVideoCall = isChecked;
			}
		});

		uiPSTNCheckbox = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_pstn_checkbox"));
		uiPSTNCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			
			public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
				isPSTNCall = isChecked;
			}
		});

		uiSIPCheckbox = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_sip_trunk_checkbox"));
		uiSIPCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			
			public void onCheckedChanged(CompoundButton cb, boolean isChecked) {
				isSIPCall = isChecked;
			}
		});

		callButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_call_button"));
		callButton.setOnClickListener(new OnClickListener() {

			
			public void onClick(View v) {
				doCall();
			}
		});

		setNativeView(layoutWraper);

		inCallDialog = new KandyIncallDialog(activity);
	}

	
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
			if (!hasOutgoingDialog) {
				uiPhoneNumber.setVisibility(View.GONE);
				uiVideoCheckbox.setVisibility(View.GONE);
				uiPSTNCheckbox.setVisibility(View.GONE);
				uiSIPCheckbox.setVisibility(View.GONE);
				callButton.setVisibility(View.GONE);
			}
		}
	}

	private void doCall() {
		// IKandyCall call;
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

		((IKandyOutgoingCall) call).establish(new KandyCallResponseListener() {

			
			public void onRequestSucceeded(IKandyCall call) {
				Log.i(TAG, "KandyCallResponseListener.onRequestSucceeded() was invoked: " + call.getCallee().getUri());
				inCallDialog.show();
			}

			
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
		if (hasIncomingDialog) {
			activity.runOnUiThread(new Runnable() {

				
				public void run() {
					createIncomingCallPopup(call);
				}
			});
		}
	}

	private void createIncomingCallPopup(final IKandyIncomingCall pInCall) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setPositiveButton(KandyUtils.getString("kandy_calls_answer_button_label"),
				new DialogInterface.OnClickListener() {

					
					public void onClick(DialogInterface dialog, int which) {
						call = pInCall;
						accept();
						dialog.dismiss();
					}
				});

		builder.setNeutralButton(KandyUtils.getString("kandy_calls_ignore_incoming_call_button_label"),
				new DialogInterface.OnClickListener() {

					
					public void onClick(DialogInterface dialog, int which) {
						ignoreIncomingCall((IKandyIncomingCall) pInCall);
						dialog.dismiss();
					}
				});

		builder.setNegativeButton(KandyUtils.getString("kandy_calls_reject_incoming_call_button_label"),
				new DialogInterface.OnClickListener() {

					
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
		if (call.canReceiveVideo()) {
			((IKandyIncomingCall) call).accept(isVideoCall, new KandyCallResponseListener() {

				
				public void onRequestSucceeded(IKandyCall call) {
					inCallDialog.show();
				}

				
				public void onRequestFailed(IKandyCall call, int code, String error) {
					UIUtils.showToastWithMessage(activity, error);
				}
			});
		} else {
			((IKandyIncomingCall) call).accept(false, new KandyCallResponseListener() {

				
				public void onRequestSucceeded(IKandyCall call) {
					inCallDialog.show();
				}

				
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

			
			public void onRequestSucceeded(IKandyCall call) {
				CallServiceProxy.removeKandyCall(call.getCallee().getUri());
			}

			
			public void onRequestFailed(IKandyCall call, int code, String error) {
				UIUtils.showToastWithMessage(activity, error);
			}
		});
	}

	public void rejectIncomingCall(IKandyIncomingCall pIncall) {
		if (pIncall == null)
			return;
		pIncall.ignore(new KandyCallResponseListener() {

			
			public void onRequestSucceeded(IKandyCall call) {
				CallServiceProxy.removeKandyCall(call.getCallee().getUri());
			}

			
			public void onRequestFailed(IKandyCall call, int code, String error) {
				UIUtils.showToastWithMessage(activity, error);
			}
		});
	}

	
	public void onCallStateChanged(KandyCallState state, IKandyCall call) {
		Log.i(TAG, "onCallStateChanged() was invoked: " + call.getCallee().getUri() + " " + state.name());
		if (state == KandyCallState.TERMINATED && inCallDialog.isShowing()) {
			CallServiceProxy.removeKandyCall(call.getCallee().getUri());
			inCallDialog.hide();
		}
	}

	
	public void onGSMCallConnected(IKandyCall call) {
		if (inCallDialog != null)
			inCallDialog.switchHold(true);
	}

	
	public void onGSMCallDisconnected(IKandyCall call) {
		if (inCallDialog != null)
			inCallDialog.switchHold(false);
	}

	
	public void onGSMCallIncoming(IKandyCall call) {
	}

	
	public void onIncomingCall(final IKandyIncomingCall call) {
		Log.i(TAG, "onIncomingCall() was invoked: " + call.getCallee().getUri());
		answerIncomingCall(call);
	}

	
	public void onMissedCall(KandyMissedCallMessage message) {
	}

	
	public void onVideoStateChanged(IKandyCall call, boolean isReceivingVideo, boolean isSendingVideo) {
	}

	
	public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage message) {
	}

	public class KandyIncallDialog extends Dialog {

		private ImageView uiUnknownAvatar;
		private View videoCallLayout;
		private KandyView localView, remoteView;

		private ToggleButton holdTbutton, muteTbutton, videoTbutton, cameraTbutton;
		private ImageButton hangupButton;

		// private ListView uiLeftDrawer;

		public KandyIncallDialog(Context context) {
			super(context, android.R.style.Theme_Light_NoTitleBar);

			setContentView(KandyUtils.getLayout("kandy_incall_dialog"));
			setCancelable(false);
			setCanceledOnTouchOutside(false);

			uiUnknownAvatar = (ImageView) findViewById(KandyUtils.getId("kandy_calls_unknown_avatar"));
			videoCallLayout = findViewById(KandyUtils.getId("kandy_calls_video_layout"));

//			calleeList = new ArrayList<String>();
//			calleeAdapter = new CalleeAdapter(context, KandyUtils.getLayout("call_listview_item"), calleeList);
//			uiLeftDrawer = (ListView) findViewById(KandyUtils.getId("kandy_calls_left_drawer"));
//			uiLeftDrawer.setAdapter(calleeAdapter);
//			uiLeftDrawer.setOnItemClickListener(new OnItemClickListener() {
//
//				
//				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
//
//				}
//			});

			// TODO: support multi-call
			DrawerLayout drawerLayout = (DrawerLayout)findViewById(KandyUtils.getId("drawer_layout"));
			drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			
			localView = (KandyView) findViewById(KandyUtils.getId("kandy_calls_local_video_view"));
			localView.setZOrderOnTop(true);
			remoteView = (KandyView) findViewById(KandyUtils.getId("kandy_calls_video_view"));
			remoteView.setZOrderMediaOverlay(true);

			holdTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_hold_tbutton"));
			holdTbutton.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					boolean isChecked = ((ToggleButton) v).isChecked();
					switchHold(isChecked);
				}
			});

			muteTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_mute_tbutton"));
			muteTbutton.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					boolean isChecked = ((ToggleButton) v).isChecked();
					switchMute(isChecked);
				}
			});

			videoTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_video_tbutton"));
			videoTbutton.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					boolean isChecked = ((ToggleButton) v).isChecked();
					switchVideo(isChecked);
				}
			});

			cameraTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_switch_camera_tbutton"));
			cameraTbutton.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					boolean isChecked = ((ToggleButton) v).isChecked();
					switchCamera(isChecked);
				}
			});

			hangupButton = (ImageButton) findViewById(KandyUtils.getId("kandy_calls_hangup_button"));
			hangupButton.setOnClickListener(new View.OnClickListener() {

				
				public void onClick(View v) {
					hangup();
				}
			});
		}

		
		public void show() {
			call.setLocalVideoView(localView);
			call.setRemoteVideoView(remoteView);
			activity.runOnUiThread(new Runnable() {

				
				public void run() {
					switchVideoView(call.isCallStartedWithVideo());
				}
			});
			super.show();
		}

		public void switchVideoView(final boolean isVideoView) {
			activity.runOnUiThread(new Runnable() {

				
				public void run() {
					if (isVideoView) {
						videoTbutton.setChecked(true);
						videoCallLayout.setVisibility(View.VISIBLE);
						localView.setVisibility(View.VISIBLE);
						call.setRemoteVideoView(remoteView);
						uiUnknownAvatar.setVisibility(View.GONE);
					} else {
						videoTbutton.setChecked(false);
						localView.setVisibility(View.GONE);
						videoCallLayout.setVisibility(View.GONE);
						uiUnknownAvatar.setVisibility(View.VISIBLE);
					}
				}
			});
		}

		public void switchHold(boolean isChecked) {
			if (call == null)
				return;
			if (isChecked)
				call.hold(new KandyCallResponseListener() {

					
					public void onRequestSucceeded(IKandyCall call) {
					}

					
					public void onRequestFailed(IKandyCall call, int code, String error) {
						// UIUtils.showToastWithMessage(activity, error);
						activity.runOnUiThread(new Runnable() {

							
							public void run() {
								holdTbutton.setChecked(false);
							}
						});
					}
				});
			else
				call.unhold(new KandyCallResponseListener() {

					
					public void onRequestSucceeded(IKandyCall call) {
					}

					
					public void onRequestFailed(IKandyCall call, int code, String error) {
						// UIUtils.showToastWithMessage(activity, error);
						activity.runOnUiThread(new Runnable() {

							
							public void run() {
								holdTbutton.setChecked(true);
							}
						});
					}
				});
		}

		public void switchMute(boolean isChecked) {
			if (call == null)
				return;
			Log.i("KandyInCallDialog", "switchMute() was invoked: " + String.valueOf(isChecked));
			if (isChecked)
				call.mute(new KandyCallResponseListener() {

					
					public void onRequestSucceeded(IKandyCall call) {
					}

					
					public void onRequestFailed(IKandyCall call, int code, String error) {
						// UIUtils.showToastWithMessage(activity, error);
						activity.runOnUiThread(new Runnable() {

							
							public void run() {
								muteTbutton.setChecked(false);
							}
						});
					}
				});
			else
				call.unmute(new KandyCallResponseListener() {

					
					public void onRequestSucceeded(IKandyCall call) {
					}

					
					public void onRequestFailed(IKandyCall call, int code, String error) {
						// UIUtils.showToastWithMessage(activity, error);
						activity.runOnUiThread(new Runnable() {

							
							public void run() {
								muteTbutton.setChecked(true);
							}
						});
					}
				});
		}

		public void switchVideo(boolean isChecked) {
			if (call == null)
				return;
			if (isChecked)
				call.startVideoSharing(new KandyCallResponseListener() {

					
					public void onRequestSucceeded(IKandyCall call) {
						switchVideoView(true);
					}

					
					public void onRequestFailed(IKandyCall call, int code, String error) {
						// UIUtils.showToastWithMessage(activity, error);
						switchVideoView(false);
					}
				});
			else
				call.stopVideoSharing(new KandyCallResponseListener() {

					
					public void onRequestSucceeded(IKandyCall call) {
						switchVideoView(false);
					}

					
					public void onRequestFailed(IKandyCall call, int code, String error) {
						// UIUtils.showToastWithMessage(activity, error);
						switchVideoView(true);
					}
				});
		}

		public void switchCamera(boolean isChecked) {
			if (call == null)
				return;
			KandyCameraInfo cameraInfo = isChecked ? KandyCameraInfo.FACING_FRONT : KandyCameraInfo.FACING_BACK;
			call.switchCamera(cameraInfo);
		}

		public void hangup() {
			call.hangup(new KandyCallResponseListener() {

				
				public void onRequestSucceeded(IKandyCall callee) {
					// FIXME: this listener is not called
					Log.i("KandyIncallDialog", "onRequestSucceeded()");
					// CallServiceProxy.removeKandyCall(call.getCallee().getUri());
					// inCallDialog.hide();
					// call = null;
				}

				
				public void onRequestFailed(IKandyCall callee, int code, String error) {
					// call = null;
					// CallServiceProxy.removeKandyCall(call.getCallee().getUri());
					// inCallDialog.hide();
					UIUtils.showDialogWithErrorMessage(activity, error);
				}
			});

			CallServiceProxy.removeKandyCall(call.getCallee().getUri());
			inCallDialog.hide();
			call = null;
		}
	}

	public void onGSMCallConnected(IKandyCall arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onGSMCallDisconnected(IKandyCall arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	public void onGSMCallIncoming(IKandyCall arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}
}
