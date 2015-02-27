package com.kandy;

import java.util.HashMap;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyRecord;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class CallServiceProxy extends KrollProxy {

	private IKandyCall _call;
	private boolean _startWithVideo = true;

	public CallServiceProxy() {
		super();
	}

	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);

		if (options.containsKey("startWithVideo")) {
			_startWithVideo = options.getBoolean("startWithVideo");
		}
	}

	@Kroll.method
	public void createPSTNCall(HashMap args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String number = (String) args.get("number");

		_call = Kandy.getServices().getCallService().createPSTNCall(number);
	}

	@Kroll.method
	public void createVoipCall(HashMap args) {
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
				.createVoipCall(callee, _startWithVideo); // TODO: change to prefs
		// TODO: create a view
	}

	@Kroll.method
	public void hangup(HashMap args) {
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

	@Kroll.method
	public void mute(HashMap args) {
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

	@Kroll.method
	public void unmute(HashMap args) {
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

	@Kroll.method
	public void hold(HashMap args) {
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

	@Kroll.method
	public void unhold(HashMap args) {
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

	@Kroll.method
	public void startVideoSharing(HashMap args) {
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

	@Kroll.method
	public void stopVideoSharing(HashMap args) {
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

	@Kroll.method
	public void accept(HashMap args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		if (_call == null) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_calls_invalid_hangup_text_msg");
			return;
		}

		((IKandyIncomingCall) _call).accept(_startWithVideo, // TODO: change to prefs
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

	@Kroll.method
	public void reject(HashMap args) {
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

	@Kroll.method
	public void ignore(HashMap args) {
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
