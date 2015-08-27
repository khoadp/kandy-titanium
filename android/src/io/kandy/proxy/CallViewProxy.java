package io.kandy.proxy;

import android.app.Activity;
import com.genband.kandy.api.services.calls.IKandyCall;
import io.kandy.KandyModule;
import io.kandy.proxy.views.KandyVideoView;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class CallViewProxy extends TiViewProxy {

	private IKandyCall callee;
	private KandyVideoView videoView;
	private boolean isLocalVideoView;

	@Override
	public TiUIView createView(Activity activity) {
		videoView = new KandyVideoView(this);
		if (callee != null) {
			if (isLocalVideoView)
				videoView.setLocalVideoView(callee);
			else
				videoView.setRemoteVideoView(callee);
		}
		return videoView;
	}

	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
		if (options.containsKey("local")) {
			isLocalVideoView = true;
			this.callee = CallServiceProxy.getKandyCall(options.getString("local"));
		} else if (options.containsKey("remote")) {
			isLocalVideoView = false;
			this.callee = CallServiceProxy.getKandyCall(options.getString("remote"));
		}
	}

	@Kroll.method
	public void setLocalVideoView(String callee) {
		isLocalVideoView = true;
		this.callee = CallServiceProxy.getKandyCall(callee);
		if (this.callee != null && videoView != null) {
			videoView.setLocalVideoView(this.callee);
		}
	}

	@Kroll.method
	public void setRemoteVideoView(String callee) {
		isLocalVideoView = false;
		this.callee = CallServiceProxy.getKandyCall(callee);
		if (this.callee != null && videoView != null) {
			videoView.setRemoteVideoView(this.callee);
		}
	}
}
