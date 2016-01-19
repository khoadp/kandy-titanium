package io.kandy.proxy;

import io.kandy.KandyModule;
import io.kandy.proxy.views.GroupViewProxy;
import io.kandy.utils.KandyUtils;

import java.util.ArrayList;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;
import org.json.JSONArray;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyRecordType;
import com.genband.kandy.api.services.chats.IKandyMessage;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyChatServiceNotificationListener;
import com.genband.kandy.api.services.chats.KandyDeliveryAck;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.groups.IKandyGroupDestroyed;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantJoined;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantKicked;
import com.genband.kandy.api.services.groups.IKandyGroupParticipantLeft;
import com.genband.kandy.api.services.groups.IKandyGroupUpdated;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParams;
import com.genband.kandy.api.services.groups.KandyGroupResponseListener;
import com.genband.kandy.api.services.groups.KandyGroupServiceNotificationListener;
import com.genband.kandy.api.services.groups.KandyGroupsResponseListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class GroupServiceProxy extends TiViewProxy implements KandyGroupServiceNotificationListener,
		KandyChatServiceNotificationListener {

	private static final String LCAT = GroupServiceProxy.class.getSimpleName();

	private GroupViewProxy viewProxy;

	@Override
	public TiUIView createView(Activity activity) {
		viewProxy = new GroupViewProxy(this);
		return viewProxy;
	}

	@Override
	public void onCreate(Activity activity, Bundle savedInstanceState) {
		super.onCreate(activity, savedInstanceState);
		registerNotificationListener();
	}

	@Override
	public void onResume(Activity activity) {
		super.onResume(activity);
		registerNotificationListener();
	}

	@Override
	public void onPause(Activity activity) {
		super.onPause(activity);
		unregisterNotificationListener();
	}

	@Kroll.method
	public void registerNotificationListener() {
		if (viewProxy == null)
			Kandy.getServices().getGroupService().registerNotificationListener(this);
	}

	@Kroll.method
	public void unregisterNotificationListener() {
		if (viewProxy == null)
			Kandy.getServices().getGroupService().unregisterNotificationListener(this);
	}

	@Kroll.method
	public void createGroup(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupName = args.getString("name");

		KandyGroupParams params = new KandyGroupParams();
		params.setGroupName(groupName);

		Kandy.getServices().getGroupService()
				.createGroup(params, new KandyGroupResponseListenerCallback(success, error));
	}

	@Kroll.method
	public void getMyGroups(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		Kandy.getServices().getGroupService().getMyGroups(new KandyGroupsResponseListener() {

			@Override
			public void onRequestFailed(int code, String err) {
				Log.d(LCAT, "KandyGroupResponseListener->onRequestFailed() was invoked: " + String.valueOf(code)
						+ " - " + error);
				KandyUtils.sendFailResult(getKrollObject(), error, code, err);
			}

			@Override
			public void onRequestSucceded(List<KandyGroup> groups) {
				Log.d(LCAT, "KandyGroupsResponseListener->onRequestSucceeded() was invoked: " + groups.size());

				JSONArray result = new JSONArray();

				for (KandyGroup group : groups)
					result.put(KandyUtils.getKrollDictFromKandyGroup(group));

				KandyUtils.sendSuccessResult(getKrollObject(), success, result);
			}
		});
	}

	@Kroll.method
	public void getGroupById(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String id = args.getString("id");

		try {
			Kandy.getServices().getGroupService()
					.getGroupById(new KandyRecord(id), new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void updateGroupName(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");
		String newName = args.getString("name");

		try {
			Kandy.getServices()
					.getGroupService()
					.updateGroupName(new KandyRecord(groupId), newName,
							new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void updateGroupImage(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");
		String uri = args.getString("uri");

		try {
			Kandy.getServices()
					.getGroupService()
					.updateGroupImage(new KandyRecord(groupId), Uri.parse(uri),
							new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void removeGroupImage(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");

		try {
			Kandy.getServices().getGroupService()
					.removeGroupImage(new KandyRecord(groupId), new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void downloadGroupImage(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");

		try {
			Kandy.getServices().getGroupService()
					.downloadGroupImage(new KandyRecord(groupId), new KandyResponseProgressListener() {

						@Override
						public void onRequestFailed(int code, String err) {
							Log.d(LCAT,
									"KandyResponseProgressListener->onRequestFailed() was invoked: "
											+ String.valueOf(code) + " - " + err);
							KandyUtils.sendFailResult(getKrollObject(), error, code, err);
						}

						@Override
						public void onRequestSucceded(Uri uri) {
							Log.d(LCAT, "KandyResponseProgressListener->onRequestSucceded() was invoked.");
							KandyUtils.sendSuccessResult(getKrollObject(), success, uri.toString());
						}

						@Override
						public void onProgressUpdate(IKandyTransferProgress progress) {
							Log.d(LCAT, "KandyResponseProgressListener->onProgressUpdate() was invoked: "
									+ progress.getState().toString() + " " + progress.getProgress());

							KrollDict result = new KrollDict();
							result.put("process", progress.getProgress());
							result.put("state", progress.getState().toString());
							result.put("byteTransfer", progress.getByteTransfer());
							result.put("byteExpected", progress.getByteExpected());

							KandyUtils.sendSuccessResult(getKrollObject(), success, result);
						}
					});
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void downloadGroupImageThumbnail(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");

		KandyThumbnailSize thumbnailSize;

		try {
			thumbnailSize = KandyThumbnailSize.valueOf(args.getString("thumbnailSize"));
		} catch (Exception e) {
			e.printStackTrace();
			thumbnailSize = KandyThumbnailSize.MEDIUM;
		}
		try {
			Kandy.getServices()
					.getGroupService()
					.downloadGroupImageThumbnail(new KandyRecord(groupId), thumbnailSize,
							new KandyResponseProgressListener() {

								@Override
								public void onRequestFailed(int code, String err) {
									Log.d(LCAT, "KandyResponseProgressListener->onRequestFailed() was invoked: "
											+ String.valueOf(code) + " - " + err);
									KandyUtils.sendFailResult(getKrollObject(), error, code, err);
								}

								@Override
								public void onRequestSucceded(Uri uri) {
									Log.d(LCAT, "KandyResponseProgressListener->onRequestSucceded() was invoked.");
									KandyUtils.sendSuccessResult(getKrollObject(), success, uri.toString());
								}

								@Override
								public void onProgressUpdate(IKandyTransferProgress progress) {
									Log.d(LCAT, "KandyResponseProgressListener->onProgressUpdate() was invoked: "
											+ progress.getState().toString() + " " + progress.getProgress());

									KrollDict result = new KrollDict();
									result.put("process", progress.getProgress());
									result.put("state", progress.getState().toString());
									result.put("byteTransfer", progress.getByteTransfer());
									result.put("byteExpected", progress.getByteExpected());

									KandyUtils.sendSuccessResult(getKrollObject(), success, result);
								}
							});
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void muteGroup(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");

		try {
			Kandy.getServices().getGroupService()
					.muteGroup(new KandyRecord(groupId), new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void unmuteGroup(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");

		try {
			Kandy.getServices().getGroupService()
					.unmuteGroup(new KandyRecord(groupId), new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void destroyGroup(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");

		try {
			Kandy.getServices().getGroupService()
					.destroyGroup(new KandyRecord(groupId), new KandyResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void leaveGroup(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");

		try {
			Kandy.getServices().getGroupService()
					.leaveGroup(new KandyRecord(groupId), new KandyResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void removeParticipants(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");
		String[] participants = args.getStringArray("participants");

		List<KandyRecord> records = new ArrayList<KandyRecord>();
		for (int i = 0; i < participants.length; ++i) {
			try {
				records.add(new KandyRecord(participants[i]));
			} catch (KandyIllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		try {
			Kandy.getServices()
					.getGroupService()
					.removeParticipants(new KandyRecord(groupId), records,
							new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void muteParticipants(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");
		String[] participants = args.getStringArray("participants");

		List<KandyRecord> records = new ArrayList<KandyRecord>();
		for (int i = 0; i < participants.length; ++i) {
			try {
				records.add(new KandyRecord(participants[i]));
			} catch (KandyIllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		try {
			Kandy.getServices()
					.getGroupService()
					.muteParticipants(new KandyRecord(groupId), records,
							new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void unmuteParticipants(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");
		String[] participants = args.getStringArray("participants");

		List<KandyRecord> records = new ArrayList<KandyRecord>();
		for (int i = 0; i < participants.length; ++i) {
			try {
				records.add(new KandyRecord(participants[i]));
			} catch (KandyIllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		try {
			Kandy.getServices()
					.getGroupService()
					.unmuteParticipants(new KandyRecord(groupId), records,
							new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

	@Kroll.method
	public void addParticipants(KrollDict args) {
		KrollFunction success = (KrollFunction) args.get("success");
		KrollFunction error = (KrollFunction) args.get("error");

		String groupId = args.getString("id");
		String[] participants = args.getStringArray("participants");

		List<KandyRecord> records = new ArrayList<KandyRecord>();
		for (int i = 0; i < participants.length; ++i) {
			try {
				records.add(new KandyRecord(participants[i]));
			} catch (KandyIllegalArgumentException e) {
				e.printStackTrace();
			}
		}

		try {
			Kandy.getServices()
					.getGroupService()
					.addParticipants(new KandyRecord(groupId), records,
							new KandyGroupResponseListenerCallback(success, error));
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
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

	private class KandyGroupResponseListenerCallback extends KandyGroupResponseListener {

		KrollFunction success, error;

		public KandyGroupResponseListenerCallback(KrollFunction success, KrollFunction error) {
			this.success = success;
			this.error = error;
		}

		@Override
		public void onRequestSucceded(KandyGroup group) {
			Log.d(LCAT, "KandyGroupResponseListener->onRequestSucceeded() was invoked: " + group.getGroupId().getUri());
			KandyUtils.sendSuccessResult(getKrollObject(), success, KandyUtils.getKrollDictFromKandyGroup(group));
		}

		@Override
		public void onRequestFailed(int code, String err) {
			Log.d(LCAT, "KandyGroupResponseListener->onRequestFailed() was invoked: " + String.valueOf(code) + " - "
					+ error);
			KandyUtils.sendFailResult(getKrollObject(), error, code, err);
		}

	}

	public void onGroupDestroyed(IKandyGroupDestroyed group) {
		if (hasListeners("onGroupDestroyed")) {
			KrollDict result = new KrollDict();

			result.put("uuid", group.getUUID().toString());
			result.put("eraser", KandyUtils.getKrollDictFromKandyRecord(group.getEraser()));
			result.put("groupId", KandyUtils.getKrollDictFromKandyRecord(group.getGroupId()));
			result.put("eventType", group.getEventType().name());
			result.put("timestamp", group.getTimestamp());

			fireEvent("onGroupDestroyed", result);
		}
	}

	public void onGroupUpdated(IKandyGroupUpdated group) {
		if (hasListeners("onGroupUpdated")) {
			KrollDict result = new KrollDict();

			result.put("uuid", group.getUUID().toString());
			result.put("updater", KandyUtils.getKrollDictFromKandyRecord(group.getUpdater()));
			result.put("groupId", KandyUtils.getKrollDictFromKandyRecord(group.getGroupId()));
			result.put("eventType", group.getEventType().name());
			result.put("timestamp", group.getTimestamp());

			fireEvent("onGroupUpdated", result);
		}
	}

	public void onParticipantJoined(IKandyGroupParticipantJoined participant) {
		if (hasListeners("onParticipantJoined")) {
			KrollDict result = new KrollDict();

			result.put("uuid", participant.getUUID().toString());
			result.put("groupId", KandyUtils.getKrollDictFromKandyRecord(participant.getGroupId()));
			result.put("inviter", KandyUtils.getKrollDictFromKandyRecord(participant.getInviter()));

			List<KandyRecord> invitess = participant.getInvitees();
			ArrayList<KrollDict> listInvitess = new ArrayList<KrollDict>();
			if (invitess != null && invitess.size() > 0) {
				for (KandyRecord i : invitess)
					listInvitess.add(KandyUtils.getKrollDictFromKandyRecord(i));
			}
			result.put("invitess", listInvitess.toArray());
			result.put("timestamp", participant.getTimestamp());
			result.put("eventType", participant.getEventType().name());

			fireEvent("onParticipantJoined", result);
		}
	}

	public void onParticipantKicked(IKandyGroupParticipantKicked participant) {
		if (hasListeners("onParticipantKicked")) {
			KrollDict result = new KrollDict();

			result.put("uuid", participant.getUUID().toString());
			result.put("groupId", KandyUtils.getKrollDictFromKandyRecord(participant.getGroupId()));
			result.put("booter", KandyUtils.getKrollDictFromKandyRecord(participant.getBooter()));

			List<KandyRecord> booted = participant.getBooted();
			ArrayList<KrollDict> listBooted = new ArrayList<KrollDict>();
			if (booted != null && booted.size() > 0) {
				for (KandyRecord i : booted)
					listBooted.add(KandyUtils.getKrollDictFromKandyRecord(i));
			}
			result.put("booted", listBooted.toArray());
			result.put("timestamp", participant.getTimestamp());
			result.put("eventType", participant.getEventType().name());

			fireEvent("onParticipantKicked", result);
		}
	}

	public void onParticipantLeft(IKandyGroupParticipantLeft participant) {
		if (hasListeners("onParticipantKicked")) {
			KrollDict result = new KrollDict();

			result.put("uuid", participant.getUUID().toString());
			result.put("groupId", KandyUtils.getKrollDictFromKandyRecord(participant.getGroupId()));
			result.put("leaver", KandyUtils.getKrollDictFromKandyRecord(participant.getLeaver()));

			List<KandyRecord> admins = participant.getAdmins();
			ArrayList<KrollDict> listAdmins = new ArrayList<KrollDict>();
			if (admins != null && admins.size() > 0) {
				for (KandyRecord i : admins)
					listAdmins.add(KandyUtils.getKrollDictFromKandyRecord(i));
			}
			result.put("admins", listAdmins.toArray());
			result.put("timestamp", participant.getTimestamp());
			result.put("eventType", participant.getEventType().name());

			fireEvent("onParticipantKicked", result);
		}
	}

	public void onChatDelivered(KandyDeliveryAck ack) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatDelivered() was invoked: " + ack.getUUID());
		if (hasListeners("onChatDelivered"))
			fireEvent("onChatDelivered", KandyUtils.getKrollDictFromKandyDeliveryAck(ack));
	}

	public void onChatMediaAutoDownloadFailed(IKandyMessage message, int code, String error) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadFailed() was invoked.");

		if (hasListeners("onChatMediaAutoDownloadFailed")) {
			KrollDict result = new KrollDict();
			result.put("error", error);
			result.put("code", code);
			result.put("message", KandyUtils.getKrollDictFromKandyMessage(message));
			fireEvent("onChatMediaAutoDownloadFailed", result);
		}
	}

	public void onChatMediaAutoDownloadProgress(IKandyMessage message, IKandyTransferProgress progress) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadProgress() was invoked: " + progress);

		if (hasListeners("onChatMediaAutoDownloadProgress")) {
			KrollDict result = new KrollDict();
			result.put("process", progress.getProgress());
			result.put("state", progress.getState());
			result.put("byteTransfer", progress.getByteTransfer());
			result.put("byteExpected", progress.getByteExpected());
			result.put("message", KandyUtils.getKrollDictFromKandyMessage(message));
			fireEvent("onChatMediaAutoDownloadProgress", result);
		}
	}

	public void onChatMediaAutoDownloadSucceded(IKandyMessage message, Uri uri) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatMediaAutoDownloadSucceded() was invoked: " + uri);

		if (hasListeners("onChatMediaAutoDownloadSucceded")) {
			KrollDict result = new KrollDict();
			result.put("uri", uri);
			result.put("message", KandyUtils.getKrollDictFromKandyMessage(message));
			fireEvent("onChatMediaAutoDownloadSucceded", result);
		}
	}

	public void onChatReceived(IKandyMessage message, KandyRecordType type) {
		Log.d(LCAT, "KandyChatServiceNotificationListener->onChatReceived() was invoked: " + type.name());
		if (hasListeners("onChatReceived")) {
			KrollDict result = new KrollDict();
			result.put("type", type.name());
			result.put("message", KandyUtils.getKrollDictFromKandyMessage(message));
			fireEvent("onChatReceived", result);
		}
	}
}