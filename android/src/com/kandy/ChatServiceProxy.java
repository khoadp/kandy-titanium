package com.kandy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.chats.IKandyEvent;
import com.genband.kandy.api.services.chats.KandyMessage;
import com.genband.kandy.api.services.common.KandyResponseListener;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class ChatServiceProxy extends KrollProxy {

	public ChatServiceProxy() {
		super();
	}

	@Kroll.method
	public void send(HashMap args) {
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

	@Kroll.method
	public void markAsReceived(HashMap args) {
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

	@Kroll.method
	public void pullEvents() {
		Kandy.getServices().getChatService().pullEvents();
	}

	@Kroll.method
	public void pullEvents(HashMap args) {
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
