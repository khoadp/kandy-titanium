package io.kandy.proxy.views;

import io.kandy.utils.KandyUtils;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.view.LayoutInflater;
import android.view.View;

import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyView;

public class KandyVideoView extends TiUIView {

	private KandyView kandyView;

	public KandyVideoView(TiViewProxy proxy) {
		super(proxy);

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_video_view"), null);

		kandyView = (KandyView) layoutWraper.findViewById(KandyUtils.getId("kandy_calls_video_view"));
		
		setNativeView(layoutWraper);
	}

	public void setLocalVideoView(IKandyCall call) {
		if (kandyView != null)
			call.setLocalVideoView(kandyView);
	}

	public void setRemoteVideoView(IKandyCall call) {
		if (kandyView != null)
			call.setRemoteVideoView(kandyView);
	}
}