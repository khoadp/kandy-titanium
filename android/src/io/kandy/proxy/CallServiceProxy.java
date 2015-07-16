package io.kandy.proxy;
//package io.kandy;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.*;
//import android.widget.CompoundButton.OnCheckedChangeListener;
//import com.genband.kandy.api.Kandy;
//import com.genband.kandy.api.services.calls.*;
//import com.genband.kandy.api.utils.KandyIllegalArgumentException;
//import org.appcelerator.kroll.KrollDict;
//import org.appcelerator.kroll.KrollFunction;
//import org.appcelerator.kroll.annotations.Kroll;
//import org.appcelerator.titanium.proxy.TiViewProxy;
//import org.appcelerator.titanium.view.TiUIView;
//
///**
// * Call service
// * 
// * @author kodeplusdev
// *
// */
//@Kroll.proxy(creatableInModule = KandyModule.class)
//public class CallServiceProxy extends TiViewProxy {
//
//	public static final String LCAT = CallServiceProxy.class.getSimpleName();
//	
//	/**
//	 * Call widget
//	 */
//	private class CallWidget extends TiUIView {
//
//		KandyView localView, remoteView;
//		
//		public CallWidget(TiViewProxy proxy) {
//			super(proxy);
//			
//			View layoutWraper;
//			
//			LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
//			layoutWraper = layoutInflater.inflate(
//					KandyUtils.getLayout("kandy_call_widget"), null);
//			
//			localView = (KandyView)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_local_video_view"));
//			remoteView = (KandyView)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_remote_video_view"));
//			
//			final EditText phoneNumber = (EditText)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_phone_number_edit"));
//			CheckBox startWithVideo = (CheckBox)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_video_checkbox"));
//			
//			Button btnCall = (Button)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_call_button"));
//			Button btnHangup = (Button)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_hangup_button"));
//			ToggleButton tbtnHold = (ToggleButton)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_hold_tbutton"));
//			ToggleButton tbtnMute = (ToggleButton)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_mute_tbutton"));
//			ToggleButton tbtnVideo = (ToggleButton)layoutWraper.findViewById(
//					KandyUtils.getId("kandy_video_tbutton"));
//			
//			startWithVideo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				
//				public void onCheckedChanged(CompoundButton comp, boolean checked) {
//					_startWithVideo = checked;
//				}
//			});
//			
//			btnCall.setOnClickListener(new View.OnClickListener() {
//				
//				public void onClick(View v) {
//					// TODO: some UI tweaks
//					CallServiceProxy.this.createVoipCall(phoneNumber.getText().toString());
//				}
//			});
//			
//			btnHangup.setOnClickListener(new View.OnClickListener() {
//							
//				public void onClick(View v) {
//					// TODO: some UI tweak
//					CallServiceProxy.this.hangup();
//				}
//			});
//			
//			tbtnHold.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				
//				public void onCheckedChanged(CompoundButton btn, boolean checked) {
//					CallServiceProxy.this.hold(checked);
//				}
//			});
//			
//			tbtnMute.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				
//				public void onCheckedChanged(CompoundButton btn, boolean checked) {
//					CallServiceProxy.this.mute(checked);
//				}
//			});
//			
//			tbtnVideo.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				
//				public void onCheckedChanged(CompoundButton btn, boolean checked) {
//					CallServiceProxy.this.videoSharing(checked);
//				}
//			});
//			
//			setNativeView(layoutWraper);
//		}
//		
//		public void setVideoViewForCall(final IKandyCall call){
//			if (call != null){
//				getActivity().runOnUiThread(new Runnable() {
//					
//					public void run() {
//						if (localView != null && remoteView != null){
//							call.setLocalVideoView(localView);
//							call.setRemoteVideoView(remoteView);
//						}
//					}
//				});
//			}
//		}
//	}
//	
//	private KrollDict callbacks;
//	
//	private CallWidget callWidget;
//	private IKandyCall _call;
//	private boolean _startWithVideo = true;
//	private AlertDialog mIncomingCallDialog;
//
//	private KandyCallServiceNotificationListener _kandyCallServiceNotificationListener = null;
//	
//	public CallServiceProxy() {
//		super();
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void handleCreationDict(KrollDict options) {
//		super.handleCreationDict(options);
//
//		if (options.containsKey("startWithVideo")) {
//			_startWithVideo = options.getBoolean("startWithVideo");
//		}
//	}
//	
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public TiUIView createView(Activity activity) {
//		callWidget = new CallWidget(this);
//		return callWidget;
//	}
//	
//	/**
//	 * Set call callbacks
//	 * 
//	 * @param args
//	 */
//	@Kroll.setProperty
//	@Kroll.method
//	public void setCallbacks(KrollDict args){
//		callbacks = args;
//	}
//
//	private void answerCall(final IKandyIncomingCall pCall) {
//		getActivity().runOnUiThread(new Runnable() {
//			
//			public void run() {
//				createIncomingCallPopup(pCall);
//			}
//		});
//	}
//	
//
//	/**
//	 * Creates the popup for incoming call 
//	 * @param pInCall incoming call for which dialog will be created
//	 */
//	private void createIncomingCallPopup(IKandyIncomingCall pInCall) {
//		_call = pInCall;
//		
//		callWidget.setVideoViewForCall(_call);
//
//		/*if(isFinishing()) {
//			//FIXME remove this after fix twice callback call
//			Log.i(TAG, "createIncomingCallPopup is finishing()");
//			return;
//		}*/
//
//		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//		builder.setPositiveButton(KandyUtils.getString("kandy_calls_answer_button_label"),
//				new DialogInterface.OnClickListener() {
//
//					public void onClick(DialogInterface dialog, int which) {
//						accept();
//						dialog.dismiss();
//					}
//		});
//
//		builder.setNeutralButton(KandyUtils.getString("kandy_calls_ignore_incoming_call_button_label"),
//				new DialogInterface.OnClickListener() {
//
//			public void onClick(DialogInterface dialog, int which) {
//				ignoreIncomingCall((IKandyIncomingCall)_call);
//				dialog.dismiss();
//			}
//		});
//
//		builder.setNegativeButton(KandyUtils.getString("kandy_calls_reject_incoming_call_button_label"),
//				new DialogInterface.OnClickListener() {
//
//			public void onClick(DialogInterface dialog, int which) {
//				rejectIncomingCall((IKandyIncomingCall)_call);
//				dialog.dismiss();
//			}
//		});
//
//		builder.setMessage(KandyUtils.getString("kandy_calls_incoming_call_popup_message_label") + _call.getCallee().getUri());
//
//		mIncomingCallDialog = builder.create();
//		mIncomingCallDialog.show();
//	}
//
//	/**
//	 * Ignoring the incoming call -  the caller wont know about ignore, call will continue on his side 
//	 * @param pCall incoming call instance
//	 */
//	public void ignoreIncomingCall(IKandyIncomingCall pCall) {
//		if(pCall == null) {
//			Log.i(LCAT, KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
//			return;
//		}
//
//		pCall.ignore(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				Log.i(LCAT, "mCurrentIncomingCall.ignore succeed" );
//				_call = null;
//			}
//
//			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
//				Log.i(LCAT, "mCurrentIncomingCall.ignore failed");
//			}
//		});
//	}
//
//	/**
//	 * Reject the incoming call
//	 * @param pCall incoming call instance
//	 */
//	public void rejectIncomingCall(IKandyIncomingCall pCall) {
//		if(pCall == null) {
//			Log.i(LCAT, KandyUtils.getString("kandy_calls_invalid_hangup_text_msg"));
//			return;
//		}
//
//		pCall.reject(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				Log.i(LCAT, "mCurrentIncomingCall.reject succeeded" );
//				_call = null;
//			}
//
//			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
//				Log.i(LCAT, "mCurrentIncomingCall.reject. Error: " + err + "\nResponse code: " + responseCode);
//			}
//		});
//	}
//
//	/**
//	 * Accept incoming call
//	 */
//	public void accept() {
//		((IKandyIncomingCall)_call).accept(_startWithVideo, new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				Log.i(LCAT, "mCurrentIncomingCall.accept succeed");
//			}
//
//			public void onRequestFailed(IKandyCall call, int responseCode, String err) {
//				Log.i(LCAT, "mCurrentIncomingCall.accept. Error: " + err + "\nResponse code: " + responseCode);
//			}
//		});	
//	}
//
//	
//	/**
//	 * Create a PSTN call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void createPSTNCall(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		String number = (String) args.get("callee");
//
//		_call = Kandy.getServices().getCallService().createPSTNCall(number);
//	}
//
//	/**
//	 * Create a voip call
//	 * 
//	 * @param callee
//	 */
//	public void createVoipCall(String callee){
//		KrollDict args = KandyUtils.getKrollDictFromCallbacks(callbacks, "create");
//		
//		args.put("callee", callee);
//		
//		createVoipCall(args);
//	}
//	
//	/**
//	 * Create a voip call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void createVoipCall(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//		String callee = (String) args.get("callee");
//		
//		KandyRecord calleeRecord;
//		try {
//			calleeRecord = new KandyRecord(callee);
//		} catch (KandyIllegalArgumentException ex) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_phone_text_msg");
//			return;
//		}
//
//		_call = Kandy.getServices().getCallService()
//				.createVoipCall(calleeRecord, _startWithVideo);
//		
//		callWidget.setVideoViewForCall(_call);
//		
//		((IKandyOutgoingCall)_call).establish(new KandyCallResponseListener() {
//			
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//			}
//			
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//			}
//		});
//	}
//	
//	/**
//	 * Hangup current call
//	 */
//	public void hangup(){
//		KrollDict onHangup = KandyUtils.getKrollDictFromCallbacks(callbacks, "hangup");		
//		this.hangup(onHangup);
//	}
//	
//	/**
//	 * Hangup current call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void hangup(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		_call.hangup(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//				_call = null;
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//
//	/**
//	 * Mute/Unmute the current call
//	 * 
//	 * @param checked
//	 */
//	public void mute(Boolean checked){
//		KrollDict onMute = KandyUtils.getKrollDictFromCallbacks(callbacks, "mute");
//		
//		if (checked){
//			this.mute(onMute);
//		} else {
//			this.unmute(onMute);
//		}
//	}
//	
//	/**
//	 * Mute current call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void mute(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		_call.mute(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//
//	/**
//	 * Unmute current call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void unmute(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		_call.unmute(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//
//	/**
//	 * Hold/Unhold the current call
//	 * 
//	 * @param checked
//	 */
//	public void hold(Boolean checked){
//		KrollDict onHold = KandyUtils.getKrollDictFromCallbacks(callbacks, "mute");
//		
//		if (checked){
//			this.hold(onHold);
//		} else {
//			this.unhold(onHold);
//		}
//	}
//	/**
//	 * Hold current call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void hold(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		_call.hold(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//	
//	
//
//	/**
//	 * Unhold current call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void unhold(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		_call.unhold(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//	
//	public void videoSharing(Boolean checked){
//		KrollDict onVideoSharing = KandyUtils.getKrollDictFromCallbacks(callbacks, "videoSharing");
//		
//		if (checked){
//			this.startVideoSharing(onVideoSharing);
//		} else {
//			this.stopVideoSharing(onVideoSharing);
//		}
//	}
//
//	/**
//	 * Enable video sharing 
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void startVideoSharing(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		_call.startVideoSharing(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//
//	/**
//	 * Disable video sharing
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void stopVideoSharing(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		_call.stopVideoSharing(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//
//	/**
//	 * Accept a coming call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void accept(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		((IKandyIncomingCall) _call).accept(_startWithVideo, // TODO: change to
//																// prefs
//				new KandyCallResponseListener() {
//
//					public void onRequestSucceeded(IKandyCall call) {
//						KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//					}
//
//					public void onRequestFailed(IKandyCall call, int code,
//							String err) {
//						KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//					}
//				});
//	}
//
//	/**
//	 * Reject a coming call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void reject(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		((IKandyIncomingCall) _call).reject(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//
//	/**
//	 * Ignore a coming call
//	 * 
//	 * @param args
//	 */
//	@Kroll.method
//	public void ignore(KrollDict args) {
//		final KrollFunction success = (KrollFunction) args.get("success");
//		final KrollFunction error = (KrollFunction) args.get("error");
//
//		if (_call == null) {
//			KandyUtils.sendFailResult(getKrollObject(), error,
//					"kandy_calls_invalid_hangup_text_msg");
//			return;
//		}
//
//		((IKandyIncomingCall) _call).ignore(new KandyCallResponseListener() {
//
//			public void onRequestSucceeded(IKandyCall call) {
//				KandyUtils.sendSuccessResult(getKrollObject(), success);
//
//			}
//
//			public void onRequestFailed(IKandyCall call, int code, String err) {
//				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
//
//			}
//		});
//	}
//}