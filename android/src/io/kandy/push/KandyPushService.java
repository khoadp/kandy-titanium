package io.kandy.push;

import io.kandy.KandyConstant;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.google.android.gcm.GCMBaseIntentService;

public class KandyPushService extends GCMBaseIntentService {
	private static final String TAG = KandyPushService.class.getSimpleName();

	public KandyPushService() {
		super(KandyConstant.GCM_PROJECT_ID);
		Log.d(TAG, "KandyPushService: ");
	}

	protected KandyPushService(String senderId) {
		super(senderId);
		Log.d(TAG, "KandyPushService: senderId: " + senderId);
	}

	@Override
	protected void onError(Context context, String errorId) {
		Log.d(TAG, "onError: context: " + context + " errorId: " + errorId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Kandy.getServices().getPushService().handlePushNotification(context, intent, new KandyResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {

				Log.d(TAG, "onMessage: handlePushNotification: not KANDY related push message, ignored, responseCode: "
						+ responseCode + " error: " + err);
			}

			@Override
			public void onRequestSucceded() {
				Log.d(TAG, "onMessage: handlePushNotification: Handled by KANDY");
			}
		});
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.d(TAG, "onRegistered: context: " + context + " registrationId: " + registrationId);

	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.d(TAG, "onUnregistered: context: " + context + " registrationId: " + registrationId);

	}

}
