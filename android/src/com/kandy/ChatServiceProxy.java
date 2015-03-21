package com.kandy;

import java.util.ArrayList;
import java.util.UUID;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.json.JSONException;

import android.net.Uri;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.chats.IKandyEvent;
import com.genband.kandy.api.services.chats.IKandyMessage;
import com.genband.kandy.api.services.chats.KandyChatServiceNotificationListener;
import com.genband.kandy.api.services.chats.KandyMessage;
import com.genband.kandy.api.services.common.KandyResponseListener;

/**
 * Chat service
 * 
 * @author kodpelusdev
 *
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class ChatServiceProxy extends KrollProxy {

	private KandyChatServiceNotificationListener _kandyChatServiceNotificationListener = null;

	public ChatServiceProxy() {
		super();
	}

	/**
	 * Register notification listeners
	 * 
	 * @param callbacks
	 */
	@Kroll.method
	public void registerNotificationListener(final KrollDict callbacks) {

		if (_kandyChatServiceNotificationListener != null) {
			unregisterNotificationListener();
		}

		_kandyChatServiceNotificationListener = new KandyChatServiceNotificationListener() {

			public void onChatReceived(IKandyMessage message) {
				KrollDict data = null;

				try {
					data = Utils.JSONObjectToKrollDict(message.toJson());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onChatReceived"), data);
			}

			public void onChatMediaDownloadSucceded(IKandyMessage message,
					Uri uri) {
				KrollDict data = new KrollDict();
				try {
					data = Utils.JSONObjectToKrollDict(message.toJson());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				data.put("uri", uri);

				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onChatMediaDownloadSucceded"), data);
			}

			public void onChatMediaDownloadProgress(IKandyMessage message,
					int process) {
				KrollDict data = new KrollDict();
				try {
					data = Utils.JSONObjectToKrollDict(message.toJson());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				data.put("process", process);

				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onChatMediaDownloadProgress"), data);
			}

			public void onChatMediaDownloadFailed(IKandyMessage message,
					String error) {
				KrollDict data = new KrollDict();
				try {
					data = Utils.JSONObjectToKrollDict(message.toJson());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				data.put("error", error);

				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onChatMediaDownloadFailed"), data);
			}

			public void onChatDelivered(IKandyMessage message) {
				KrollDict data = null;
				try {
					data = Utils.JSONObjectToKrollDict(message.toJson());
				} catch (JSONException e) {
					e.printStackTrace();
				}

				Utils.checkAndSendResult(getKrollObject(),
						(KrollFunction)callbacks.get("onChatDelivered"), data);

			}
		};

		Kandy.getServices()
				.getChatService()
				.registerNotificationListener(
						_kandyChatServiceNotificationListener);
	}

	/**
	 * Unregister notification listeners
	 */
	@Kroll.method
	public void unregisterNotificationListener() {
		if (_kandyChatServiceNotificationListener != null) {
			Kandy.getServices()
					.getChatService()
					.unregisterNotificationListener(
							_kandyChatServiceNotificationListener);
			_kandyChatServiceNotificationListener = null;
		}
	}

	/**
	 * Send message
	 * 
	 * @param args
	 */
	@Kroll.method
	public void send(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String user = (String) args.get("user");
		String text = (String) args.get("message");

		KandyRecord recipient;
		try {
			recipient = new KandyRecord(user);
		} catch (IllegalArgumentException ex) {
			Utils.sendFailResult(getKrollObject(), error,
					"kandy_chat_phone_number_verification_text");
			return;
		}

		final KandyMessage message = new KandyMessage(recipient, text);

		Kandy.getServices().getChatService()
				.send(message, new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSucceded() {
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});

	}

	/**
	 * Send message received signal
	 * 
	 * @param args
	 */
	@Kroll.method
	public void markAsReceived(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String[] list = (String[]) args.get("list");
		ArrayList<IKandyEvent> events = new ArrayList<IKandyEvent>();
		for (String i : list) {
			KandyMessage event = new KandyMessage(new KandyRecord(
					"dummy@domain.com"), "dummy");
			event.setUUID(UUID.fromString(i));
			events.add(event);
		}

		Kandy.getServices().getChatService()
				.markAsReceived(events, new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSucceded() {
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}

	/**
	 * Pull pending envents
	 */
	@Kroll.method
	public void pullEvents() {
		Kandy.getServices().getChatService().pullEvents();
	}

	/**
	 * Pull pending events with callback
	 * 
	 * @param args
	 */
	@Kroll.method
	public void pullEvents(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getChatService()
				.pullEvents(new KandyResponseListener() {

					@Override
					public void onRequestFailed(int code, String err) {
						Utils.sendFailResult(getKrollObject(), error, code, err);
					}

					@Override
					public void onRequestSucceded() {
						Utils.sendSuccessResult(getKrollObject(), success);
					}
				});
	}
}
