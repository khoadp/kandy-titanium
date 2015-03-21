package com.kandy;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.view.View;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.IKandyOutgoingCall;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyView;

/**
 * Call service
 * 
 * @author kodeplusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class CallServiceProxy extends KrollProxy {

	private IKandyCall _call;
	private KandyView _localVideoView;
	private KandyView _remoteVideoView;
	
	private boolean _startWithVideo = true;

	private KandyCallServiceNotificationListener _kandyCallServiceNotificationListener = null;
	
	public CallServiceProxy() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);

		if (options.containsKey("startWithVideo")) {
			_startWithVideo = options.getBoolean("startWithVideo");
		}
	}
	
	/**
	 * Register notification listeners
	 * 
	 * @param callbacks
	 */
	@Kroll.method
	public void registerNotificationListener( final KrollDict callbacks) {

		if (_kandyCallServiceNotificationListener != null) {
			unregisterNotificationListener();
		}

		_kandyCallServiceNotificationListener = new KandyCallServiceNotificationListener() {

			public void onVideoStateChanged(IKandyCall callee,
					boolean receiving, boolean sending) {
				KrollDict data = new KrollDict();
				// TODO: add callee
				data.put("id", callee.getCallId());
				data.put("receiving", receiving);
				data.put("sending", sending);
				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onVideoStateChanged"), data);
			}

			public void onIncomingCall(IKandyIncomingCall callee) {
				if (_call == null){
					_call = callee; // TODO: fix this
				}
				KrollDict data = new KrollDict();
				data.put("id", callee.getCallId());
				data.put("callee", callee.getCallee().getUri());
				data.put("via", callee.getVia());
				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onIncomingCall"), data);
			}

			public void onGSMCallIncoming(IKandyCall callee) {
				KrollDict data = new KrollDict();
				data.put("id", callee.getCallId());
				data.put("callee", callee.getCallee().getUri());
				data.put("via", callee.getVia());
				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onGSMCallIncoming"), data);
			}

			public void onGSMCallDisconnected(IKandyCall callee) {
				KrollDict data = new KrollDict();
				data.put("id", callee.getCallId());
				data.put("callee", callee.getCallee().getUri());
				data.put("via", callee.getVia());
				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onGSMCallDisconnected"), data);
			}

			public void onGSMCallConnected(IKandyCall callee) {
				KrollDict data = new KrollDict();
				data.put("id", callee.getCallId());
				data.put("callee", callee.getCallee().getUri());
				data.put("via", callee.getVia());
				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onGSMCallConnected"), data);
			}

			public void onCallStateChanged(KandyCallState state,
					IKandyCall callee) {
				KrollDict data = new KrollDict();
				data.put("id", callee.getCallId());
				data.put("state", state.name());
				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onCallStateChanged"), data);
			}

			public void onAudioStateChanged(IKandyCall callee, boolean mute) {
				KrollDict data = new KrollDict();
				data.put("mute", mute);
				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onAudioStateChanged"), data);
			}
		};

		Kandy.getServices()
				.getCallService()
				.registerNotificationListener(
						_kandyCallServiceNotificationListener);
	}

	/**
	 * Unregister notification listeners
	 */
	@Kroll.method
	public void unregisterNotificationListener() {
		if (_kandyCallServiceNotificationListener != null) {
			Kandy.getServices()
					.getCallService()
					.unregisterNotificationListener(
							_kandyCallServiceNotificationListener);
			_kandyCallServiceNotificationListener = null;
		}
	}

	/**
	 * Create a PSTN call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void createPSTNCall(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String number = (String) args.get("number");

		_call = Kandy.getServices().getCallService().createPSTNCall(number);
	}

	/**
	 * Create a voip call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void createVoipCall(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String number = (String) args.get("number");

		KandyRecord callee;
		try {
			callee = new KandyRecord(number);
		} catch (IllegalArgumentException ex) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_phone_text_msg");
			return;
		}

		_call = Kandy.getServices().getCallService()
				.createVoipCall(callee, _startWithVideo);
		
		if (_localVideoView != null && _remoteVideoView != null){
			_call.setLocalVideoView(_localVideoView);
			_call.setRemoteVideoView(_remoteVideoView);
		} else {
			KandyView dummyVideoView = new KandyView(getActivity(), null);
			
			_call.setLocalVideoView(dummyVideoView);
			_call.setRemoteVideoView(dummyVideoView);
		}
		
		((IKandyOutgoingCall)_call).establish(new KandyCallResponseListener() {
			
			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);
			}
			
			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);
			}
		});
	}

	/**
	 * Hangup current call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void hangup(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		_call.hangup(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);
				_call = null;
			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}

	/**
	 * Mute current call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void mute(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		_call.mute(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);

			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}

	/**
	 * Unmute current call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void unmute(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		_call.unmute(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);

			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}

	/**
	 * Hold current call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void hold(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		_call.hold(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);

			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}

	/**
	 * Unhold current call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void unhold(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		_call.unhold(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);

			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}

	/**
	 * Enable video sharing 
	 * 
	 * @param args
	 */
	@Kroll.method
	public void startVideoSharing(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		_call.startVideoSharing(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);

			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}

	/**
	 * Disable video sharing
	 * 
	 * @param args
	 */
	@Kroll.method
	public void stopVideoSharing(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		_call.stopVideoSharing(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);

			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}

	/**
	 * Accept a coming call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void accept(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		((IKandyIncomingCall) _call).accept(_startWithVideo, // TODO: change to
																// prefs
				new KandyCallResponseListener() {

					public void onRequestSucceeded(IKandyCall call) {
						Utils.sendSuccessResult(getKrollObject(), success);

					}

					public void onRequestFailed(IKandyCall call, int code,
							String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);

					}
				});
	}

	/**
	 * Reject a coming call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void reject(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		((IKandyIncomingCall) _call).reject(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);

			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}

	/**
	 * Ignore a coming call
	 * 
	 * @param args
	 */
	@Kroll.method
	public void ignore(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		((IKandyIncomingCall) _call).ignore(new KandyCallResponseListener() {

			public void onRequestSucceeded(IKandyCall call) {
				Utils.sendSuccessResult(getKrollObject(), success);

			}

			public void onRequestFailed(IKandyCall call, int code, String err) {
				Utils.sendFailResult(getKrollObject(), error, code, err);

			}
		});
	}
}
