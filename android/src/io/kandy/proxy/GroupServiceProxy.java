package io.kandy.proxy;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.groups.KandyGroup;
import com.genband.kandy.api.services.groups.KandyGroupParams;
import com.genband.kandy.api.services.groups.KandyGroupResponseListener;
import com.genband.kandy.api.services.groups.KandyGroupsResponseListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyModule;
import io.kandy.proxy.views.GroupViewProxy;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class GroupServiceProxy extends TiViewProxy {

	private static final String LCAT = GroupServiceProxy.class.getSimpleName();

	private GroupViewProxy viewProxy;

	@Override
	public TiUIView createView(Activity activity) {
		viewProxy = new GroupViewProxy(this);
		return viewProxy;
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
}