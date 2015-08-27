package io.kandy.proxy;

import android.app.Activity;
import io.kandy.KandyModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class CallWidgetProxy extends TiViewProxy {

	@Override
	public TiUIView createView(Activity activity) {
		return null;
	}

}
