package io.kandy.proxy.views;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import io.kandy.proxy.fragments.ChatFragment;
import io.kandy.utils.KandyUtils;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

public class ChatViewProxy extends TiUIView {

	private Activity activity;
	private FragmentManager fragmentManager;

	public ChatViewProxy(TiViewProxy proxy) {
		super(proxy);
		activity = proxy.getActivity();

		View layoutWraper;

		LayoutInflater layoutInflater = LayoutInflater.from(proxy.getActivity());
		layoutWraper = layoutInflater.inflate(KandyUtils.getLayout("kandy_chat_widget"), null);

		fragmentManager = activity.getFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.add(KandyUtils.getId("kandy_chat_fragment_container"), new ChatFragment(this, "", false))
				.addToBackStack(null);
		fragmentTransaction.commit();

		setNativeView(layoutWraper);
	}
}
