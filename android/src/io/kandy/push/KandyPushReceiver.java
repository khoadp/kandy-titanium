package io.kandy.push;

import android.content.Context;
import com.google.android.gcm.GCMBroadcastReceiver;

public class KandyPushReceiver extends GCMBroadcastReceiver {

	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		return KandyPushService.class.getName();
	}

}
