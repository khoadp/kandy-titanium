package io.kandy.proxy.views;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyView;
import io.kandy.utils.KandyUtils;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

public class CallViewProxy extends TiUIView {

	KandyView localView, remoteView;
	Button callBtn, HangupBtn;
	ToggleButton holdBtn, muteHold, videoBtn;

	public CallViewProxy(TiViewProxy proxy) {
		super(proxy);

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_call_widget"), null);

		localView = (KandyView) layoutWraper.findViewById(KandyUtils.getId("kandy_local_video_view"));
		remoteView = (KandyView) layoutWraper.findViewById(KandyUtils.getId("kandy_remote_video_view"));

		final EditText phoneNumber = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_phone_number_edit"));
		CheckBox startWithVideo = (CheckBox) layoutWraper.findViewById(KandyUtils.getId("kandy_video_checkbox"));

		Button btnCall = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_call_button"));
		Button btnHangup = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_hangup_button"));
		ToggleButton tbtnHold = (ToggleButton) layoutWraper.findViewById(KandyUtils.getId("kandy_hold_tbutton"));
		ToggleButton tbtnMute = (ToggleButton) layoutWraper.findViewById(KandyUtils.getId("kandy_mute_tbutton"));
		ToggleButton tbtnVideo = (ToggleButton) layoutWraper.findViewById(KandyUtils.getId("kandy_video_tbutton"));

		startWithVideo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton comp, boolean checked) {
				// _startWithVideo = checked;
			}
		});

		btnCall.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO: some UI tweaks
				// ((CallServiceProxy)getProxy()).createVoipCall(phoneNumber.getText().toString());
			}
		});

		btnHangup.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				// TODO: some UI tweak
				// ((CallServiceProxy)getProxy()).hangup();
			}
		});

		tbtnHold.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton btn, boolean checked) {
				// ((CallServiceProxy)getProxy()).hold(checked);
			}
		});

		tbtnMute.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton btn, boolean checked) {
				// ((CallServiceProxy)getProxy()).mute(checked);
			}
		});

		tbtnVideo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton btn, boolean checked) {
				// ((CallServiceProxy)getProxy()).videoSharing(checked);
			}
		});

		setNativeView(layoutWraper);
	}

	public void setVideoViewForCall(final IKandyCall call) {
		if (call != null) {
			proxy.getActivity().runOnUiThread(new Runnable() {

				public void run() {
					if (localView != null && remoteView != null) {
						call.setLocalVideoView(localView);
						call.setRemoteVideoView(remoteView);
					}
				}
			});
		}
	}
}