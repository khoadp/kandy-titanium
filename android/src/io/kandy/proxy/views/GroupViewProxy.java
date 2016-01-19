package io.kandy.proxy.views;

import io.kandy.proxy.fragments.GroupsFragment;
import io.kandy.utils.KandyUtils;

import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;

public class GroupViewProxy extends TiUIView {

	private Activity activity;
	private FragmentManager fragmentManager;

	public GroupViewProxy(TiViewProxy proxy) {
		super(proxy);
		activity = proxy.getActivity();

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_group_widget"), null);

		fragmentManager = activity.getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

		fragmentTransaction.add(KandyUtils.getId("kandy_group_fragment_container"), new GroupsFragment(this))
				.addToBackStack(null);
		fragmentTransaction.commit();

		setNativeView(layoutWraper);
	}
}
