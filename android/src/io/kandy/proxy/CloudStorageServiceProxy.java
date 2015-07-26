package io.kandy.proxy;

import android.app.Activity;
import android.net.Uri;
import android.util.Log;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.chats.IKandyFileItem;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyMessageBuilder;
import com.genband.kandy.api.services.chats.KandyThumbnailSize;
import com.genband.kandy.api.services.common.KandyResponseCancelListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.common.KandyUploadProgressListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.KandyConstant;
import io.kandy.KandyModule;
import io.kandy.utils.FileUtils;
import io.kandy.utils.KandyUtils;
import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollFunction;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.proxy.TiViewProxy;
import org.appcelerator.titanium.view.TiUIView;

import java.util.Calendar;
import java.util.UUID;

@Kroll.proxy(creatableInModule = KandyModule.class)
public class CloudStorageServiceProxy extends TiViewProxy {

	private static final String LCAT = CloudStorageServiceProxy.class.getSimpleName();

	@Override
	public TiUIView createView(Activity activity) {
		return null;
	}

	@Kroll.method
	public void uploadMedia(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String uri = args.getString("uri");

		try {
			IKandyFileItem item = KandyMessageBuilder.createFile("", Uri.parse(uri));
			Kandy.getServices().geCloudStorageService().uploadMedia(item, new KandyUploadProgressListener() {

				@Override
				public void onRequestFailed(int code, String err) {
					Log.d(LCAT, "KandyUploadProgressListener->onRequestFailed() was invoked: " + String.valueOf(code)
							+ " - " + err);
					KandyUtils.sendFailResult(getKrollObject(), error, code, err);
				}

				@Override
				public void onRequestSucceded() {
					Log.d(LCAT, "KandyUploadProgressListener->onRequestSucceded() was invoked.");
					KandyUtils.sendSuccessResult(getKrollObject(), success);
				}

				@Override
				public void onProgressUpdate(IKandyTransferProgress progress) {
					Log.d(LCAT, "KandyUploadProgressListener->onProgressUpdate() was invoked: "
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
	public void downloadMediaFromCloudStorage(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String uuid = args.getString("uuid");
		String fileName = args.getString("filename");

		final long timeStamp = Calendar.getInstance().getTimeInMillis();
		Uri fileUri = Uri.parse(FileUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE).getAbsolutePath() + "//"
				+ timeStamp + fileName);

		try {
			IKandyFileItem item = KandyMessageBuilder.createFile("", fileUri);
			item.setServerUUID(UUID.fromString(uuid));
			Kandy.getServices().geCloudStorageService().downloadMedia(item, new KandyResponseProgressListener() {

				@Override
				public void onRequestFailed(int code, String err) {
					Log.d(LCAT, "KandyResponseProgressListener->onRequestFailed() was invoked: " + String.valueOf(code)
							+ " - " + err);
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
	public void downloadMediaThumbnailFromCloudStorage(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String uuid = args.getString("uuid");
		String fileName = args.getString("filename");

		KandyThumbnailSize thumbnailSize;
		try {
			thumbnailSize = KandyThumbnailSize.valueOf(args.getString("thumbnailSize"));
		} catch (Exception e) {
			thumbnailSize = KandyThumbnailSize.MEDIUM;
		}

		final long timeStamp = Calendar.getInstance().getTimeInMillis();
		Uri fileUri = Uri.parse(FileUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE).getAbsolutePath() + "//"
				+ timeStamp + fileName);

		try {
			IKandyFileItem item = KandyMessageBuilder.createFile("", fileUri);
			item.setServerUUID(UUID.fromString(uuid));
			Kandy.getServices().geCloudStorageService()
					.downloadMediaThumbnail(item, thumbnailSize, new KandyResponseProgressListener() {

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
	public void cancelMediaTransferFromCloudStorage(KrollDict args) {
		final KrollFunction success = (KrollFunction) args.get("success");
		final KrollFunction error = (KrollFunction) args.get("error");

		String uuid = args.getString("uuid");
		String fileName = args.getString("filename");

		final long timeStamp = Calendar.getInstance().getTimeInMillis();
		Uri fileUri = Uri.parse(FileUtils.getFilesDirectory(KandyConstant.LOCAL_STORAGE).getAbsolutePath() + "//"
				+ timeStamp + fileName);

		try {
			IKandyFileItem item = KandyMessageBuilder.createFile("", fileUri);
			item.setServerUUID(UUID.fromString(uuid));
			Kandy.getServices().geCloudStorageService().cancelMediaTransfer(item, new KandyResponseCancelListener() {

				@Override
				public void onRequestFailed(int code, String err) {
					Log.d(LCAT, "KandyResponseCancelListener->onRequestFailed() was invoked: " + String.valueOf(code)
							+ " - " + err);
					KandyUtils.sendFailResult(getKrollObject(), error, code, err);
				}

				@Override
				public void onCancelSucceded() {
					Log.d(LCAT, "KandyResponseCancelListener->onRequestSucceded() was invoked.");
					KandyUtils.sendSuccessResult(getKrollObject(), success);
				}
			});
		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			KandyUtils.sendFailResult(getKrollObject(), error, e.getMessage());
		}
	}

}
