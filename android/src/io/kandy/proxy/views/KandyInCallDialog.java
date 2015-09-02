package io.kandy.proxy.views;

import android.app.Activity;
import android.app.Dialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import io.kandy.proxy.CallServiceProxy;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;
import org.appcelerator.kroll.KrollProxy;

public class KandyInCallDialog extends Dialog {

	private ImageView uiUnknownAvatar;
	private View videoCallLayout;
	private KandyView localView, remoteView;

	private ToggleButton holdTbutton, muteTbutton, videoTbutton, cameraTbutton;
	private ImageButton hangupButton;

	private Activity activity;

	private IKandyCall callee;

	public KandyInCallDialog(KrollProxy proxy) {
		super(proxy.getActivity(), android.R.style.Theme_Light_NoTitleBar);
		activity = proxy.getActivity();

		setContentView(KandyUtils.getLayout("kandy_incall_dialog"));
		setCancelable(false);
		setCanceledOnTouchOutside(false);

		uiUnknownAvatar = (ImageView) findViewById(KandyUtils.getId("kandy_calls_unknown_avatar"));
		videoCallLayout = findViewById(KandyUtils.getId("kandy_calls_video_layout"));

		localView = (KandyView) findViewById(KandyUtils.getId("kandy_calls_local_video_view"));
		localView.setZOrderOnTop(true);
		remoteView = (KandyView) findViewById(KandyUtils.getId("kandy_calls_video_view"));
		remoteView.setZOrderMediaOverlay(true);

		holdTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_hold_tbutton"));
		holdTbutton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isChecked = ((ToggleButton) v).isChecked();
				switchHold(isChecked);
			}
		});

		muteTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_mute_tbutton"));
		muteTbutton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isChecked = ((ToggleButton) v).isChecked();
				switchMute(isChecked);
			}
		});

		videoTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_video_tbutton"));
		videoTbutton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isChecked = ((ToggleButton) v).isChecked();
				switchVideo(isChecked);
			}
		});

		cameraTbutton = (ToggleButton) findViewById(KandyUtils.getId("kandy_calls_switch_camera_tbutton"));
		cameraTbutton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				boolean isChecked = ((ToggleButton) v).isChecked();
				switchCamera(isChecked);
			}
		});

		hangupButton = (ImageButton) findViewById(KandyUtils.getId("kandy_calls_hangup_button"));
		hangupButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				hangup();
			}
		});
	}

	public void setKandyCall(String id) {
		IKandyCall call = CallServiceProxy.getKandyCall(id);
		if (call != null)
			setKandyCall(call);
	}

	public void setKandyCall(final IKandyCall call) {
		this.callee = call;
		this.callee.setLocalVideoView(localView);
		this.callee.setRemoteVideoView(remoteView);
		switchVideoView(call.isCallStartedWithVideo());
	}

	public boolean isKandyCall(String id) {
		if (callee == null)
			return false;
		else
			return TextUtils.equals(id, callee.getCallee().getUri());
	}

	public void switchVideoView(final boolean isVideoView) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (isVideoView) {
					videoTbutton.setChecked(true);
					videoCallLayout.setVisibility(View.VISIBLE);
					localView.setVisibility(View.VISIBLE);
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
		if (callee == null)
			return;
		if (isChecked)
			callee.hold(new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
				}

				@Override
				public void onRequestFailed(IKandyCall call, int code, String error) {
					// UIUtils.showToastWithMessage(activity, error);
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							holdTbutton.setChecked(false);
						}
					});
				}
			});
		else
			callee.unhold(new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
				}

				@Override
				public void onRequestFailed(IKandyCall call, int code, String error) {
					// UIUtils.showToastWithMessage(activity, error);
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							holdTbutton.setChecked(true);
						}
					});
				}
			});
	}

	public void switchMute(boolean isChecked) {
		if (callee == null)
			return;
		Log.i("KandyInCallDialog", "switchMute() was invoked: " + String.valueOf(isChecked));
		if (isChecked)
			callee.mute(new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
				}

				@Override
				public void onRequestFailed(IKandyCall call, int code, String error) {
					// UIUtils.showToastWithMessage(activity, error);
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							muteTbutton.setChecked(false);
						}
					});
				}
			});
		else
			callee.unmute(new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
				}

				@Override
				public void onRequestFailed(IKandyCall call, int code, String error) {
					// UIUtils.showToastWithMessage(activity, error);
					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							muteTbutton.setChecked(true);
						}
					});
				}
			});
	}

	public void switchVideo(boolean isChecked) {
		if (callee == null)
			return;
		if (isChecked)
			callee.startVideoSharing(new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
					switchVideoView(true);
				}

				@Override
				public void onRequestFailed(IKandyCall call, int code, String error) {
					// UIUtils.showToastWithMessage(activity, error);
					switchVideoView(false);
				}
			});
		else
			callee.stopVideoSharing(new KandyCallResponseListener() {

				@Override
				public void onRequestSucceeded(IKandyCall call) {
					switchVideoView(false);
				}

				@Override
				public void onRequestFailed(IKandyCall call, int code, String error) {
					// UIUtils.showToastWithMessage(activity, error);
					switchVideoView(true);
				}
			});
	}

	public void switchCamera(boolean isChecked) {
		if (callee == null)
			return;
		KandyCameraInfo cameraInfo = isChecked ? KandyCameraInfo.FACING_FRONT : KandyCameraInfo.FACING_BACK;
		callee.switchCamera(cameraInfo);
	}

	public void hangup() {
		callee.hangup(new KandyCallResponseListener() {

			@Override
			public void onRequestSucceeded(IKandyCall call) {
				// FIXME: this listener is not called
				Log.i("KandyIncallDialog", "onRequestSucceeded()");
				CallServiceProxy.removeKandyCall(call.getCallee().getUri());
				KandyInCallDialog.this.dismiss();
			}

			@Override
			public void onRequestFailed(IKandyCall call, int code, String error) {
				UIUtils.showDialogWithErrorMessage(getContext(), error);
			}
		});

		// CallServiceProxy.removeKandyCall(callee.getCallee().getUri());
		// hide();
	}
}
