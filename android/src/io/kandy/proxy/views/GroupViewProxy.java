package io.kandy.proxy.views;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import io.kandy.proxy.fragments.DetailGroupFragment;
import io.kandy.utils.KandyUtils;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

public class GroupViewProxy extends TiUIView {

	private static final String LCAT = GroupViewProxy.class.getSimpleName();

	private Activity activity;

	public GroupViewProxy(TiViewProxy proxy) {
		super(proxy);
		activity = proxy.getActivity();

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_group_widget"), null);

		FragmentManager fragmentManager = activity.getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		fragmentTransaction.add(KandyUtils.getId("kandy_group_fragment_container"), new DetailGroupFragment())
				.addToBackStack(null);
		fragmentTransaction.commit();

		setNativeView(layoutWraper);
	}
}
