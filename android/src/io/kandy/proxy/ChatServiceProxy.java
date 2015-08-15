package io.kandy.proxy;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyRecordType;
import com.genband.kandy.api.services.chats.*;
import com.genband.kandy.api.services.common.KandyResponseCancelListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.location.KandyCurrentLocationListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyConstant;
import io.kandy.KandyModule;
import io.kandy.proxy.views.ChatViewProxy;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

/**
 * Chat service
 * 
 * @author kodpelusdev
 * 
 */
@Kroll.proxy(creatableInModule = KandyModule.class)
public class ChatServiceProxy extends TiViewProxy implements KandyChatServiceNotificationListener {

	private static final String LCAT = ChatServiceProxy.class.getSimpleName();

	private ChatViewProxy viewProxy;
	private KrollDict callbacks = new KrollDict();

	@Override
	public TiUIView createView(Activity activity) {
		viewProxy = new ChatViewProxy(this);
		return viewProxy;
	}

	@Override
	protected void initActivity(Activity activity) {
		attachActivityLifecycle(activity);
	}

	@Override
	public void onCreate(Activity activity, Bundle savedInstanceState) {
		super.onCreate(activity, savedInstanceState);
		activity.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		if (viewProxy != null) {
			viewProxy.registerNotificationListener();
		} else {
			Kandy.getServices().getChatService().registerNotificationListener(this);
		}
	}

	@Override
	public void onPause(Activity activity) {
		super.onPause(activity);
		if (viewProxy != null) {
			viewProxy.unregisterNotificationListener();
		} else {
			Kandy.getServices().getChatService().unregisterNotificationListener(this);
		}
	}

	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
		if (viewProxy != null) {
			viewProxy.registerNotificationListener();
		} else {
			Kandy.getServices().getChatService().registerNotificationListener(this);
		}
	}

	@Kroll.method
	@Kroll.setProperty
	public void setCallbacks(KrollDict callbacks) {
		if (viewProxy != null) {
			viewProxy.setCallbacks(callbacks);
		} else {
			this.callbacks = callbacks;
		}
	}

	@Kroll.method
	@Kroll.getProperty
	public KrollDict getCallbacks() {
		if (viewProxy != null) {
			return viewProxy.getCallbacks();
		}
		return callbacks;
	}

	@Kroll.method
	public void sendSMS(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String destination = args.getString("destination");
		String title = args.getString("title");
		String text = args.getString("message");

		if (text == null || text.equals("")) {
			KandyUtils
					.sendFailResult(getKrollObject(), error, KandyUtils.getString("kandy_chat_message_empty_message"));
			return;
		}

		final KandySMSMessage message;

		try {
			message = new KandySMSMessage(destination, title, text);
		} catch (KandyIllegalArgumentException e) {
			KandyUtils
					.sendFailResult(getKrollObject(), error, KandyUtils.getString("kandy_chat_message_invalid_phone"));
			Log.e(LCAT, "sendSMS: " + " " + e.getLocalizedMessage(), e);
			return;
		}

		Kandy.getServices().getChatService().sendSMS(message, new KandyResponseListenerCallback(success, error));
	}

	private void sendChatMessage(KrollFunction success, KrollFunction error, String destination, IKandyMediaItem data,
			String type) {
		KandyRecord recipient;
		try {
			KandyRecordType recordType;
			try {
				recordType = KandyRecordType.valueOf(type);
			} catch (Exception ex) {
				recordType = KandyRecordType.CONTACT;
			}

			recipient = new KandyRecord(destination, recordType);
		} catch (KandyIllegalArgumentException e) {
			KandyUtils
					.sendFailResult(getKrollObject(), error, KandyUtils.getString("kandy_chat_message_invalid_phone"));
			Log.e(LCAT, "sendChatMessage: " + " " + e.getLocalizedMessage(), e);
			return;
		}

		KandyChatMessage message = new KandyChatMessage(recipient, data);
		Kandy.getServices().getChatService().sendChat(message, new KandyResponseListenerCallback(success, error));
	}

	@Kroll.method
	public void sendChat(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String destination = args.getString("destination");
		String text = args.getString("message");
		String type = args.getString("type");

		IKandyTextItem kandyText = KandyMessageBuilder.createText(text);
		sendChatMessage(success, error, destination, kandyText, type);
	}

	@Kroll.method
	public void pickAudio() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("audio/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		getActivity().startActivityForResult(intent, KandyConstant.AUDIO_PICKER_RESULT);
	}

	@Kroll.method
	public void sendAudio(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String destination = args.getString("destination");
		String caption = args.getString("caption");
		String uri = args.getString("uri");
		String type = args.getString("type");

		IKandyAudioItem kandyAudio = null;
		try {
			kandyAudio = KandyMessageBuilder.createAudio(caption, Uri.parse(uri));
		} catch (KandyIllegalArgumentException e) {
			Log.d(LCAT, "sendAudio: " + e.getLocalizedMessage(), e);
		}

		sendChatMessage(success, error, destination, kandyAudio, type);
	}

	@Kroll.method
	public void pickVideo() {
		Intent intent = new Intent();
		intent.setType("video/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		getActivity().startActivityForResult(intent, KandyConstant.VIDEO_PICKER_RESULT);
	}

	@Kroll.method
	public void sendVideo(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String destination = args.getString("destination");
		String caption = args.getString("caption");
		String uri = args.getString("uri");
		String type = args.getString("type");

		IKandyVideoItem kandyVideo = null;
		try {
			kandyVideo = KandyMessageBuilder.createVideo(caption, Uri.parse(uri));
		} catch (KandyIllegalArgumentException e) {
			Log.d(LCAT, "sendVideo: " + e.getLocalizedMessage(), e);
		}

		sendChatMessage(success, error, destination, kandyVideo, type);
	}

	@Kroll.method
	public void pickImage() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		getActivity().startActivityForResult(intent, KandyConstant.IMAGE_PICKER_RESULT);
	}

	@Kroll.method
	public void sendImage(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String destination = args.getString("destination");
		String caption = args.getString("caption");
		String uri = args.getString("uri");
		String type = args.getString("type");

		IKandyImageItem kandyImage = null;
		try {
			kandyImage = KandyMessageBuilder.createImage(caption, Uri.parse(uri));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
		}

		sendChatMessage(success, error, destination, kandyImage, type);
	}

	@Kroll.method
	public void pickContact() {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
		getActivity().startActivityForResult(contactPickerIntent, KandyConstant.CONTACT_PICKER_RESULT);
	}

	@Kroll.method
	public void sendContact(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String destination = args.getString("destination");
		String caption = args.getString("caption");
		String uri = args.getString("uri");
		String type = args.getString("type");

		IKandyContactItem kandyContact = null;
		try {
			kandyContact = KandyMessageBuilder.createContact(caption, Uri.parse(uri));
		} catch (KandyIllegalArgumentException e) {
			Log.d(LCAT, "sendContact: " + e.getLocalizedMessage(), e);
		}

		sendChatMessage(success, error, destination, kandyContact, type);
	}

	@Kroll.method
	public void pickFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			getActivity().startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"),
					KandyConstant.FILE_PICKER_RESULT);
		} catch (android.content.ActivityNotFoundException ex) {
			// Potentially direct the user to the Market with a Dialog
			// Toast.makeText(this, "Please install a File Manager.",
			// Toast.LENGTH_SHORT).show(); }
			Toast.makeText(getActivity(), "Please install a File Manager", Toast.LENGTH_SHORT);
		}
	}

	@Kroll.method
	public void sendFile(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String destination = args.getString("destination");
		String caption = args.getString("caption");
		String uri = args.getString("uri");
		String type = args.getString("type");

		IKandyFileItem kandyFile = null;
		try {
			kandyFile = KandyMessageBuilder.createFile(caption, Uri.parse(uri));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
		}

		sendChatMessage(success, error, destination, kandyFile, type);
	}

	@Kroll.method
	public void sendCurrentLocation(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		final String caption = args.getString("caption");
		final String destination = args.getString("destination");
		final String type = args.getString("type");

		try {
			Kandy.getServices().getLocationService().getCurrentLocation(new KandyCurrentLocationListener() {

				@Override
				public void onCurrentLocationReceived(Location location) {
					Log.d(LCAT,
							"KandyCurrentLocationListener->onCurrentLocationReceived() was invoked: "
									+ location.toString());
					IKandyLocationItem kandyLocation = KandyMessageBuilder.createLocation(caption, location);
					sendChatMessage(success, error, destination, kandyLocation, type);
				}

				@Override
				public void onCurrentLocationFailed(int code, String err) {
					Log.d(LCAT, "KandyCurrentLocationListener->onRequestFailed() was invoked: " + String.valueOf(code)
							+ " - " + error);
					KandyUtils.sendFailResult(getKrollObject(), error, code, err);
				}
			});
		} catch (KandyIllegalArgumentException e) {
			Log.e(LCAT, "sendCurrentLocation(); " + e.getLocalizedMessage(), e);
		}
	}

	@Kroll.method
	public void sendLocation(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String caption = args.getString("caption");
		String destination = args.getString("destination");
		KrollDict location = args.getKrollDict("location");
		String type = args.getString("type");

		IKandyLocationItem kandyLocation = KandyMessageBuilder.createLocation(caption,
				KandyUtils.getLocationFromJson(new JSONObject(location)));
		sendChatMessage(success, error, destination, kandyLocation, type);
	}

	@Kroll.method
	public void cancelMediaTransfer(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String uuid = args.getString("uuid");

		try {
			KandyChatMessage message = getMessageFromUUID(uuid);
			Kandy.getServices().getChatService()
					.cancelMediaTransfer(message, new KandyResponseCancelListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void downloadMedia(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String uuid = args.getString("uuid");

		try {
			KandyChatMessage message = getMessageFromUUID(uuid);
			Kandy.getServices().getChatService()
					.downloadMedia(message, new KandyResponseProgressListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void downloadMediaThumbnail(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String uuid = args.getString("uuid");
		KandyThumbnailSize thumbnailSize;
		try {
			thumbnailSize = KandyThumbnailSize.valueOf(args.getString("thumbnailSize"));
		} catch (Exception e) {
			thumbnailSize = KandyThumbnailSize.MEDIUM;
		}

		try {
			KandyChatMessage message = getMessageFromUUID(uuid);
			Kandy.getServices()
					.getChatService()
					.downloadMediaThumbnail(message, thumbnailSize,
							new KandyResponseProgressListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	@Kroll.method
	public void markAsReceived(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String uuid = args.getString("uuid");

		try {
			getMessageFromUUID(uuid).markAsReceived(new KandyResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
			e.printStackTrace();
		}
	}

	private KandyChatMessage getMessageFromUUID(String uuid) throws KandyIllegalArgumentException {
		KandyChatMessage message = new KandyChatMessage(new KandyRecord("dummy@dummy.com"), "dummy");
		message.setUUID(UUID.fromString(uuid));
		return message;
	}

	@Kroll.method
	public void pullEvents(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getChatService().pullEvents(new KandyResponseListenerCallback(success, error));
	}

	private class KandyResponseCancelListenerCallback extends KandyResponseCancelListener {

		private KrollFunction success, error;

		public KandyResponseCancelListenerCallback(KrollFunction success, KrollFunction error) {
			this.success = success;
			this.error = error;
		}

		@Override
		public void onCancelSucceded() {
			Log.d(LCAT, "KandyResponseCancelListenerCallback->onRequestSucceded() was invoked.");
			KandyUtils.sendSuccessResult(getKrollObject(), success);
		}

		@Override
		public void onRequestFailed(int code, String err) {
			Log.d(LCAT, "KandyResponseCancelListenerCallback->onRequestFailed() was invoked: " + String.valueOf(code)
					+ " - " + err);
			KandyUtils.sendFailResult(getKrollObject(), error, code, err);
		}

	}

	private class KandyResponseProgressListenerCallback extends KandyResponseProgressListener {

		private KrollFunction success, error;

		public KandyResponseProgressListenerCallback(KrollFunction success, KrollFunction error) {
			this.success = success;
			this.error = error;
		}

		@Override
		public void onProgressUpdate(IKandyTransferProgress progress) {
			Log.d(LCAT, "KandyResponseProgressListener->onRequestSucceded() was invoked: "
					+ progress.getState().toString() + " " + progress.getProgress());

			KrollDict result = new KrollDict();

			result.put("process", progress.getProgress());
			result.put("state", progress.getState().toString());
			result.put("byteTransfer", progress.getByteTransfer());
			result.put("byteExpected", progress.getByteExpected());

			KandyUtils.sendSuccessResult(getKrollObject(), success, result);
		}

		@Override
		public void onRequestSucceded(Uri uri) {
			Log.d(LCAT, "KandyResponseProgressListener->onRequestSucceded() was invoked: " + uri);
			KandyUtils.sendSuccessResult(getKrollObject(), success, uri.toString());
		}

		@Override
		public void onRequestFailed(int code, String err) {
			Log.d(LCAT, "KandyResponseProgressListenerCallback->onRequestFailed() was invoked: " + String.valueOf(code)
					+ " - " + err);
			KandyUtils.sendFailResult(getKrollObject(), error, code, err);
		}
	}

	private class KandyResponseListenerCallback extends KandyResponseListener {

		private KrollFunction success, error;

		public KandyResponseListenerCallback(KrollFunction success, KrollFunction error) {
			this.success = success;
			this.error = error;
		}

		@Override
		public void onRequestSucceded() {
			Log.d(LCAT, "KandyResponseListenerCallback->onRequestSucceded() was invoked.");
			KandyUtils.sendSuccessResult(getKrollObject(), success);
		}

		@Override
		public void onRequestFailed(int code, String err) {
			Log.d(LCAT, "KandyResponseListenerCallback->onRequestFailed() was invoked: " + String.valueOf(code) + " - "
					+ err);
			KandyUtils.sendFailResult(getKrollObject(), error, code, err);
		}

	}

	@Override
	public void onChatDelivered(KandyDeliveryAck ack) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatDelivered() was invoked: " + ack.getUUID());
		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onChatDelivered"), ack.toJson());
	}

	@Override
	public void onChatMediaAutoDownloadFailed(IKandyMessage message, int code, String error) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadFailed() was invoked.");

		KrollDict result = new KrollDict();
		result.put("error", error);
		result.put("code", code);
		try {
			result.put("message", message.toJson().getJSONObject("message"));
		} catch (JSONException e) {
			e.printStackTrace();
			result.put("message", "Error parse message json!");
		}

		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onChatMediaAutoDownloadFailed"),
				result);
	}

	@Override
	public void onChatMediaAutoDownloadProgress(IKandyMessage message, IKandyTransferProgress progress) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadProgress() was invoked: " + progress);

		KrollDict result = new KrollDict();
		result.put("process", progress.getProgress());
		result.put("state", progress.getState());
		result.put("byteTransfer", progress.getByteTransfer());
		result.put("byteExpected", progress.getByteExpected());
		try {
			result.put("message", message.toJson().get("message"));
		} catch (JSONException e) {
			e.printStackTrace();
			result.put("message", "Error parse message json!");
		}

		KandyUtils.sendSuccessResult(getKrollObject(),
				(KrollFunction) callbacks.get("onChatMediaAutoDownloadProgress"), result);
	}

	@Override
	public void onChatMediaAutoDownloadSucceded(IKandyMessage message, Uri uri) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadSucceded() was invoked: " + uri);

		KrollDict result = new KrollDict();
		result.put("uri", uri);
		try {
			result.put("message", message.toJson().get("message"));
		} catch (JSONException e) {
			e.printStackTrace();
			result.put("message", "Error parse message json!");
		}

		KandyUtils.sendSuccessResult(getKrollObject(),
				(KrollFunction) callbacks.get("onChatMediaAutoDownloadSucceded"), result);
	}

	@Override
	public void onChatReceived(IKandyMessage message, KandyRecordType type) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatReceived() was invoked: " + type.name());

		KrollDict result = new KrollDict();
		result.put("type", type.name());
		try {
			result.put("message", message.toJson().get("message"));
		} catch (JSONException e) {
			e.printStackTrace();
			result.put("message", "Error parse message json!");
		}

		KandyUtils.sendSuccessResult(getKrollObject(), (KrollFunction) callbacks.get("onChatReceived"), result);
	}
}
