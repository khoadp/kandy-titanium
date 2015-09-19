package io.kandy.proxy.fragments;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyRecordType;
import com.genband.kandy.api.services.chats.*;
import com.genband.kandy.api.services.common.KandyResponseCancelListener;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyResponseProgressListener;
import com.genband.kandy.api.services.common.KandyUploadProgressListener;
import com.genband.kandy.api.services.location.KandyCurrentLocationListener;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import io.kandy.proxy.ChatServiceProxy;
import io.kandy.proxy.adapters.MessagesAdapter;
import io.kandy.proxy.views.ChatViewProxy;
import io.kandy.utils.KandyUtils;
import io.kandy.utils.UIUtils;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.TiLifecycle.OnActivityResultEvent;
import org.appcelerator.titanium.view.TiUIView;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ChatFragment extends Fragment implements OnItemClickListener, OnActivityResultEvent,
		KandyChatServiceNotificationListener {

	private static final String TAG = ChatViewProxy.class.getSimpleName();

	private MessagesAdapter mAdapter;
	private Queue<IKandyMessage> mMessagesUUIDQueue;
	private InputMethodManager mInputMethodManager;

	private Button uiIncomingMsgsPullerButton;

	private EditText uiPhoneNumberEdit;
	private EditText uiMessageEdit;

	private Message mSelectedMessage = null;
	private boolean isGroupChatMode;
	private String id;

	private TiUIView viewProxy;
	private Activity activity;

	private Fragment backFragment;

	public ChatFragment(TiUIView viewProxy, String id, boolean isGroupChatMode, Fragment back) {
		this(viewProxy, id, isGroupChatMode);
		backFragment = back;
	}

	public ChatFragment(TiUIView viewProxy, String id, boolean isGroupChatMode) {
		this.viewProxy = viewProxy;
		this.isGroupChatMode = isGroupChatMode;
		this.id = id;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	@Override
	public void onResume() {
		super.onResume();
		Kandy.getServices().getChatService().registerNotificationListener(this);
	}

	@Override
	public void onPause() {
		super.onPause();
		Kandy.getServices().getChatService().unregisterNotificationListener(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View layoutWraper = inflater.inflate(KandyUtils.getLayout("kandy_chat_fragment"), container, false);

		mInputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

		ListView uiMessagesListView = (ListView) layoutWraper
				.findViewById(KandyUtils.getId("kandy_chat_messages_list"));
		uiMessagesListView.setOnItemClickListener(this);

		mMessagesUUIDQueue = new LinkedBlockingQueue<IKandyMessage>();
		mAdapter = new MessagesAdapter(activity, KandyUtils.getLayout("message_listview_item"),
				new ArrayList<Message>());
		uiMessagesListView.setAdapter(mAdapter);

		ImageButton uiSendButton = (ImageButton) layoutWraper.findViewById(KandyUtils
				.getId("kandy_chat_send_msg_button"));
		uiSendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendText();
			}
		});

		uiIncomingMsgsPullerButton = (Button) layoutWraper.findViewById(KandyUtils
				.getId("kandy_chat_get_incoming_msgs_button"));
		uiIncomingMsgsPullerButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pullPendingEvents();
			}
		});

		Button uiSendImgButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_img_button"));
		uiSendImgButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickImage();
			}
		});

		uiPhoneNumberEdit = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_phone_number_edit"));
		uiMessageEdit = (EditText) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_message_edit"));

		if (isGroupChatMode && id != null && id != "") {
			uiPhoneNumberEdit.setText(id);
			uiPhoneNumberEdit.setEnabled(false);
		}

		Button uiSendContactButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_contact_button"));
		uiSendContactButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickContact();
			}
		});

		Button uiSendAudioButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_audio_button"));
		uiSendAudioButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickAudio();
			}
		});

		Button uiSendVideoButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_video_button"));
		uiSendVideoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickVideo();
			}
		});

		Button uiSendFileButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_file_button"));
		uiSendFileButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickFile();
			}
		});

		Button uiSendLocationButton = (Button) layoutWraper
				.findViewById(KandyUtils.getId("kandy_chat_location_button"));
		uiSendLocationButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendCurrentLocation();
			}
		});

		Button uiSendSMSButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_send_sms_button"));
		uiSendSMSButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendSMS();
			}
		});

		if (backFragment != null) {
			Button uiBackButton = (Button) layoutWraper.findViewById(KandyUtils.getId("kandy_chat_back_button"));
			uiBackButton.setVisibility(View.VISIBLE);
			uiBackButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					FragmentManager fragmentManager = getFragmentManager();
					FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
					fragmentTransaction.remove(ChatFragment.this);
					fragmentTransaction.show(backFragment);
					fragmentTransaction.commit();
				}
			});
		}

		((TiBaseActivity) activity).addOnActivityResultListener(this);

		return layoutWraper;
	}

	public void registerNotificationListener() {
		Log.d(TAG, "registerNotifications");
		Kandy.getServices().getChatService().registerNotificationListener(this);
	}

	public void unregisterNotificationListener() {
		Log.d(TAG, "unRegisterNotifications");
		Kandy.getServices().getChatService().unregisterNotificationListener(this);
	}

	private void sendText() {
		String destination = getDestination();
		String text = getMessageText();

		// Set the recipient
		KandyRecord recipient = getRecipient(destination);

		if (recipient == null)
			return;

		// creating message to be sent
		final KandyChatMessage message = new KandyChatMessage(recipient, text);
		// Sending message
		Kandy.getServices().getChatService().sendChat(message, new KandyResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "Kandy.getChatService().sendMessage:onRequestFailed - Error: " + err + "\nResponse code: "
						+ responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "Kandy.getChatService().sendMessage:onRequestSucceded - Message sent");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("kandy_chat_message_sent_label"));
				addMessageOnUI(message);
			}
		});
	}

	private void sendSMS() {
		String destination = getDestination();
		String text = getMessageText();

		if (text.equals("") || text == null) {
			UIUtils.showDialogOnUiThread(activity, KandyUtils.getString("kandy_chat_message_invalid_title"),
					KandyUtils.getString("kandy_chat_message_empty_message"));
			return;
		}

		final KandySMSMessage message;

		try {
			message = new KandySMSMessage(destination, "Kandy SMS", text);
		} catch (KandyIllegalArgumentException e) {
			UIUtils.handleResultOnUiThread(activity, true, KandyUtils.getString("kandy_chat_message_invalid_phone"));
			Log.e(TAG, "sendSMS: " + " " + e.getLocalizedMessage(), e);
			return;
		}

		// Sending message
		Kandy.getServices().getChatService().sendSMS(message, new KandyResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "Kandy.getChatService().sendSMS:onRequestFailed - Error: " + err + "\nResponse code: "
						+ responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "Kandy.getChatService().sendSMS:onRequestSucceded - Message sent");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("kandy_chat_message_sent_label"));
				addMessageOnUI(message);
			}
		});
	}

	private void sendCurrentLocation() {

		try {

			Kandy.getServices().getLocationService().getCurrentLocation(new KandyCurrentLocationListener() {

				@Override
				public void onCurrentLocationReceived(Location location) {
					Log.d(TAG,
							"onCurrentLocationReceived: lat: " + location.getLatitude() + " lon: "
									+ location.getLongitude());
					sendLocation(location);
				}

				@Override
				public void onCurrentLocationFailed(int errorCode, String error) {
					Log.d(TAG, "onCurrentLocationFailed: errorCode: " + errorCode + " error: " + error);
					UIUtils.handleResultOnUiThread(activity, true, error);
				}
			});

		} catch (KandyIllegalArgumentException e) {
			e.printStackTrace();
			Log.e(TAG, "initViews:sendCurrentLocation(); " + e.getLocalizedMessage(), e);
		}
	}

	private void sendLocation(Location location) {
		String destination = getDestination();
		String text = getMessageText();

		IKandyLocationItem kandyLocation = KandyMessageBuilder.createLocation(text, location);
		KandyRecord recipient = getRecipient(destination);

		if (recipient == null)
			return;

		final KandyChatMessage message = new KandyChatMessage(recipient, kandyLocation);

		Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "uploadKandyLocationFile():onRequestFailed " + err + " error code: " + responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onProgressUpdate(IKandyTransferProgress progress) {
				Log.i(TAG, "uploadKandyLocationFile():onProgressUpdate(): " + progress + "%");
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "uploadKandyLocationFile():onRequestSucceded()");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("msg_upload_succeed"));
				addMessageOnUI(message);
			}
		});
	}

	private void addMessageOnUI(final IKandyMessage message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				uiMessageEdit.setText("");
				closeKeyboard();
				mAdapter.add(new Message(message));
				mAdapter.notifyDataSetChanged();
			}
		});
	}

	private void pullPendingEvents() {

		UIUtils.showProgressDialogOnUiThread(activity, KandyUtils.getString("msg_pull_events"));
		Kandy.getServices().getChatService().pullEvents(new KandyResponseListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				UIUtils.handleResultOnUiThread(activity, true, err);
				Log.e(TAG, "Kandy.getServices().getChatService().pullEvents:onRequestFailed: " + err
						+ " response code: " + responseCode);
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "Kandy.getServices().getChatService().pullEvents:onRequestSucceded");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("msg_pull_events_succeed"));
			}
		});
	}

	private void pickImage() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("image/*");
		activity.startActivityForResult(intent, ChatServiceProxy.IMAGE_PICKER_RESULT);
	}

	public void pickContact() {
		Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
		activity.startActivityForResult(contactPickerIntent, ChatServiceProxy.CONTACT_PICKER_RESULT);
	}

	private void pickAudio() {
		Intent intent = new Intent(Intent.ACTION_PICK);
		intent.setType("audio/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		activity.startActivityForResult(intent, ChatServiceProxy.AUDIO_PICKER_RESULT);
	}

	private void pickVideo() {
		Intent intent = new Intent();
		intent.setType("video/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		activity.startActivityForResult(intent, ChatServiceProxy.VIDEO_PICKER_RESULT);
	}

	private void pickFile() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			activity.startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"),
					ChatServiceProxy.FILE_PICKER_RESULT);
		} catch (android.content.ActivityNotFoundException ex) {
			// Potentially direct the user to the Market with a Dialog
			// Toast.makeText(this, "Please install a File Manager.",
			// Toast.LENGTH_SHORT).show(); }
			Toast.makeText(activity, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
		}
	}

	private KandyRecord getRecipient(String destination) {
		KandyRecord recipient = null;

		try {
			if (isGroupChatMode) {
				recipient = new KandyRecord(destination, KandyRecordType.GROUP);
			} else {
				recipient = new KandyRecord(destination);
			}
		} catch (KandyIllegalArgumentException ex) {
			UIUtils.showDialogWithErrorMessage(activity,
					KandyUtils.getString("kandy_chat_phone_number_verification_text"));
			return null;
		}

		return recipient;
	}

	private String getMessageText() {
		return uiMessageEdit.getText().toString();
	}

	private String getDestination() {
		return uiPhoneNumberEdit.getText().toString();
	}

	private void closeKeyboard() {
		mInputMethodManager.hideSoftInputFromWindow(uiMessageEdit.getWindowToken(), 0);
	}

	private void showCancelableUploadProgressDialog(final IKandyMessage message, String textMEssage) {

		UIUtils.showCancelableProgressDialogOnUiThreadWithProgress(activity, textMEssage, new OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				Kandy.getServices().getChatService().cancelMediaTransfer(message, new KandyResponseCancelListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						Log.d(TAG, "onRequestFailed: " + " responseCode: " + responseCode + " error: " + err);
					}

					@Override
					public void onCancelSucceded() {
						Log.d(TAG, "onCancelSucceded: " + "upload canceled");
					}
				});

				dialog.dismiss();
			}
		});
	}

	@Override
	public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {

			switch (requestCode) {
			case ChatServiceProxy.CONTACT_PICKER_RESULT:
				sendContact(data.getData());
				break;

			case ChatServiceProxy.IMAGE_PICKER_RESULT:
				sendImage(data.getData());
				break;

			case ChatServiceProxy.VIDEO_PICKER_RESULT:
				sendVideo(data.getData());
				break;

			case ChatServiceProxy.AUDIO_PICKER_RESULT:
				sendAudio(data.getData());
				break;

			case ChatServiceProxy.FILE_PICKER_RESULT:
				sendFile(data.getData());
				break;

			}
		}
	}

	private void sendContact(Uri contactUri) {

		String destination = getDestination();
		String text = getMessageText();

		IKandyContactItem kandyContact = null;

		try {
			kandyContact = KandyMessageBuilder.createContact(text, contactUri);
		} catch (KandyIllegalArgumentException e) {
			Log.d(TAG, "sendContact: " + e.getLocalizedMessage(), e);
		}
		KandyRecord recipient = getRecipient(destination);

		if (recipient == null)
			return;

		final KandyChatMessage message = new KandyChatMessage(recipient, kandyContact);

		showCancelableUploadProgressDialog(message, KandyUtils.getString("msg_uploading_contact"));

		Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "uploadKandyContactFile():onRequestFailed " + err + " error code: " + responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onProgressUpdate(IKandyTransferProgress progress) {
				Log.i(TAG, "uploadKandyContactFile():onProgressUpdate(): " + progress + "%");
				UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "uploadKandyContactFile():onRequestSucceded()");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("msg_upload_succeed"));
				addMessageOnUI(message);
			}
		});
	}

	private void sendImage(Uri imgUri) {

		String destination = getDestination();
		String text = getMessageText();

		KandyRecord recipient = getRecipient(destination);

		if (recipient == null)
			return;

		IKandyImageItem kandyImage = null;
		try {
			kandyImage = KandyMessageBuilder.createImage(text, imgUri);
		} catch (KandyIllegalArgumentException e) {
			Log.d(TAG, "sendImage: " + e.getLocalizedMessage(), e);
		}
		final KandyChatMessage message = new KandyChatMessage(recipient, kandyImage);

		showCancelableUploadProgressDialog(message, KandyUtils.getString("msg_uploading_image"));

		Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "uploadKandyFile():onRequestFailed " + err + " error code: " + responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onProgressUpdate(IKandyTransferProgress progress) {
				Log.i(TAG, "uploadKandyFile():onProgressUpdate(): " + progress + "%");
				UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "uploadKandyFile():onRequestSucceded()");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("msg_upload_succeed"));
				addMessageOnUI(message);
			}
		});
	}

	private void sendAudio(Uri audioUri) {
		String destination = getDestination();
		String text = getMessageText();

		IKandyAudioItem kandyAudio = null;
		try {
			kandyAudio = KandyMessageBuilder.createAudio(text, audioUri);
		} catch (KandyIllegalArgumentException e) {
			Log.d(TAG, "sendAudio: " + e.getLocalizedMessage(), e);
		}
		KandyRecord recipient = getRecipient(destination);

		if (recipient == null)
			return;

		final KandyChatMessage message = new KandyChatMessage(recipient, kandyAudio);

		showCancelableUploadProgressDialog(message, KandyUtils.getString("msg_uploading_audio"));

		Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "uploadKandyAudioFile():onRequestFailed " + err + " error code: " + responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onProgressUpdate(IKandyTransferProgress progress) {
				Log.i(TAG, "uploadKandyAudioFile():onProgressUpdate(): " + progress + "%");
				UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "uploadKandyAudioFile():onRequestSucceded()");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("msg_upload_succeed"));
				addMessageOnUI(message);
			}
		});
	}

	private void sendVideo(Uri videoUri) {

		String destination = getDestination();
		String text = getMessageText();

		IKandyVideoItem kandyAudio = null;
		try {
			kandyAudio = KandyMessageBuilder.createVideo(text, videoUri);
		} catch (KandyIllegalArgumentException e) {
			Log.d(TAG, "sendVideo: " + e.getLocalizedMessage(), e);
		}
		KandyRecord recipient = getRecipient(destination);

		if (recipient == null)
			return;

		final KandyChatMessage message = new KandyChatMessage(recipient, kandyAudio);

		showCancelableUploadProgressDialog(message, KandyUtils.getString("msg_uploading_video"));

		Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "uploadKandyVideoFile():onRequestFailed " + err + " error code: " + responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onProgressUpdate(IKandyTransferProgress progress) {
				Log.d(TAG, "uploadKandyVideoFile():onProgressUpdate(): " + progress + "%");
				UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
			}

			@Override
			public void onRequestSucceded() {
				Log.d(TAG, "uploadKandyVideoFile():onRequestSucceded()");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("msg_upload_succeed"));
				addMessageOnUI(message);
			}
		});
	}

	private void sendFile(Uri fileUri) {

		String destination = getDestination();
		String text = getMessageText();

		KandyRecord recipient = getRecipient(destination);

		if (recipient == null)
			return;

		IKandyFileItem kandyFile = null;
		try {
			kandyFile = KandyMessageBuilder.createFile(text, fileUri);
		} catch (KandyIllegalArgumentException e) {
			Log.d(TAG, "sendFile: " + e.getLocalizedMessage(), e);
		}
		final KandyChatMessage message = new KandyChatMessage(recipient, kandyFile);

		showCancelableUploadProgressDialog(message, KandyUtils.getString("msg_uploading_image"));

		Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {

			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "uploadKandyFile():onRequestFailed " + err + " error code: " + responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onProgressUpdate(IKandyTransferProgress progress) {
				Log.i(TAG, "uploadKandyFile():onProgressUpdate(): " + progress + "%");
				UIUtils.showProgressInDialogOnUiThread(progress.getProgress());
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "uploadKandyFile():onRequestSucceded()");
				UIUtils.handleResultOnUiThread(activity, false, KandyUtils.getString("msg_upload_succeed"));
				addMessageOnUI(message);
			}
		});
	}

	private void markMsgAsReceived(final IKandyMessage message) {

		message.markAsReceived(new KandyResponseListener() {
			@Override
			public void onRequestFailed(int responseCode, String err) {
				Log.e(TAG, "Kandy.getEventsService().ackEvent:onRequestFailed error: " + err + ".Response code: "
						+ responseCode);
				UIUtils.handleResultOnUiThread(activity, true, err);
			}

			@Override
			public void onRequestSucceded() {
				Log.i(TAG, "Kandy.getEventsService().ackEvent:onRequestSucceded");
				UIUtils.handleResultOnUiThread(activity, false, "Ack has been sent");
				markAsReceivedOnUI(message);
			}
		});
	}

	private void markAsDeliveredOnUI(final KandyDeliveryAck message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAdapter.markAsDelivered(message);
			}
		});
	}

	private void markAsReceivedOnUI(final IKandyMessage message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mAdapter.markAsReceived(message)) {
					mAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	private void addUniqueMessageOnUI(final IKandyMessage message) {
		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAdapter.addUniqe(new Message(message));
			}
		});
	}

	@Override
	public void onChatDelivered(KandyDeliveryAck message) {
		Log.d(TAG, "ChatsActivity:onChatDelivered " + message.getEventType().name() + " uuid: " + message.getUUID());
		((KandyChatServiceNotificationListener) viewProxy.getProxy()).onChatDelivered(message);

		markAsDeliveredOnUI(message);
	}

	@Override
	public void onChatMediaAutoDownloadFailed(IKandyMessage message, int errorCode, String err) {
		Log.d(TAG, "onChatMediaDownloadFailed: messageUUID: " + message.getUUID() + " error: " + err + " error code: "
				+ errorCode);

		((KandyChatServiceNotificationListener) viewProxy.getProxy()).onChatMediaAutoDownloadFailed(message, errorCode,
				err);

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
			}
		});
	}

	@Override
	public void onChatMediaAutoDownloadProgress(IKandyMessage message, IKandyTransferProgress progress) {
		Log.d(TAG, "onChatMediaDownloadProgress: messageUUID: " + message.getUUID().toString() + " progress: "
				+ progress);

		((KandyChatServiceNotificationListener) viewProxy.getProxy())
				.onChatMediaAutoDownloadProgress(message, progress);

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.setProgressBarIndeterminateVisibility(Boolean.TRUE);
			}
		});
	}

	@Override
	public void onChatMediaAutoDownloadSucceded(IKandyMessage message, Uri path) {
		Log.d(TAG, "onChatMediaDownloadSucceded: messageUUID: " + message.getUUID() + " uri path: " + path.getPath());

		((KandyChatServiceNotificationListener) viewProxy.getProxy()).onChatMediaAutoDownloadSucceded(message, path);

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				activity.setProgressBarIndeterminateVisibility(Boolean.FALSE);
			}
		});
	}

	@Override
	public void onChatReceived(IKandyMessage message, KandyRecordType type) {
		Log.d(TAG, "onChatReceived: message: " + message + " type: " + type);

		((KandyChatServiceNotificationListener) viewProxy.getProxy()).onChatReceived(message, type);

		addUniqueMessageOnUI((KandyChatMessage) message);
		mMessagesUUIDQueue.add((KandyChatMessage) message);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		mSelectedMessage = mAdapter.getItem(position);
		showMessageActionMenu();
	}

	private void showMessageActionMenu() {

		final CharSequence[] actions;

		// for incoming and not acked messages will be shown the message ack
		// action
		if (mSelectedMessage.kandyMessage.isIncoming() && !mSelectedMessage.isMsgAcked) {

			if (isDownloading(mSelectedMessage.kandyMessage)) {
				actions = new CharSequence[] { KandyUtils.getString("kandy_chat_message_click_menu_cancel_download"),
						KandyUtils.getString("kandy_chat_message_click_menu_ack"),
						KandyUtils.getString("kandy_chat_message_click_menu_cancel") };
			} else {
				actions = new CharSequence[] { KandyUtils.getString("kandy_chat_message_click_menu_view"),
						KandyUtils.getString("kandy_chat_message_click_menu_ack"),
						KandyUtils.getString("kandy_chat_message_click_menu_cancel") };
			}
		} else {
			if (isDownloading(mSelectedMessage.kandyMessage)) {
				actions = new CharSequence[] { KandyUtils.getString("kandy_chat_message_click_menu_cancel_download"),
						KandyUtils.getString("kandy_chat_message_click_menu_cancel") };
			} else {
				actions = new CharSequence[] { KandyUtils.getString("kandy_chat_message_click_menu_view"),
						KandyUtils.getString("kandy_chat_message_click_menu_cancel") };
			}
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(KandyUtils.getString("kandy_chat_message_click_menu_title")).setItems(actions,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {

						if (actions[which].equals(KandyUtils.getString("kandy_chat_message_click_menu_view"))) {
							viewMessage();
						} else if (actions[which].equals(KandyUtils.getString("kandy_chat_message_click_menu_ack"))) {
							markMsgAsReceived(mSelectedMessage.kandyMessage);
							mSelectedMessage = null;
						} else if (actions[which].equals(KandyUtils
								.getString("kandy_chat_message_click_menu_cancel_download"))) {
							cancelDownloadProcess();
						} else {
							dialog.dismiss();
							mSelectedMessage = null;
						}
					}
				});

		builder.create().show();
	}

	private boolean isDownloading(IKandyMessage message) {
		if ((KandyMessageMediaItemType.AUDIO.equals(message.getMediaItem().getMediaItemType()))
				|| (KandyMessageMediaItemType.VIDEO.equals(message.getMediaItem().getMediaItemType()))
				|| (KandyMessageMediaItemType.FILE.equals(message.getMediaItem().getMediaItemType()))
				|| (KandyMessageMediaItemType.IMAGE.equals(message.getMediaItem().getMediaItemType()))
				|| (KandyMessageMediaItemType.CONTACT.equals(message.getMediaItem().getMediaItemType()))) {

			IKandyFileItem item = (IKandyFileItem) message.getMediaItem();

			KandyTransferState state = item.getTransferProgress().getState();
			if (KandyTransferState.IN_PROGRESS == state) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	private void cancelDownloadProcess() {
		Kandy.getServices().getChatService()
				.cancelMediaTransfer(mSelectedMessage.kandyMessage, new KandyResponseCancelListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						Log.d(TAG, "cancelDownloadProcess(): onRequestFailed: code: " + responseCode + " error: " + err);
					}

					@Override
					public void onCancelSucceded() {
						Log.d(TAG, "cancelDownloadProcess():onCancelSucceded()");
					}
				});
	}

	private void viewMessage() {
		IKandyMessage kandyMessage = (IKandyMessage) mSelectedMessage.kandyMessage;
		IKandyMediaItem kandyMediaItem = kandyMessage.getMediaItem();

		KandyMessageMediaItemType mediaType = mSelectedMessage.kandyMessage.getMediaItem().getMediaItemType();
		switch (mediaType) {
		case AUDIO:
		case FILE:
		case IMAGE:
		case CONTACT:
		case VIDEO:
			downloadAndOpenKandyFile((KandyChatMessage) kandyMessage);
			break;

		case LOCATION:
			Location location = ((IKandyLocationItem) kandyMediaItem).getLocation();
			openLocation(location);
			break;

		case TEXT:
			break;

		default:
			break;
		}
	}

	private void openLocation(Location location) {
		String locUri = String.format(Locale.ENGLISH, "geo:%f,%f", location.getLatitude(), location.getLongitude());
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(locUri));
		activity.startActivity(intent);
	}

	private void downloadAndOpenKandyFile(final KandyChatMessage message) {

		final Uri uri = ((IKandyFileItem) message.getMediaItem()).getLocalDataUri();

		if (uri != null) {

			final File f = new File(uri.getPath());

			if (!f.exists()) {
				// download the media if not exists
				Kandy.getServices().getChatService().downloadMedia(message, new KandyResponseProgressListener() {

					@Override
					public void onRequestFailed(int responseCode, String err) {
						Log.e(TAG, "onRequestFailed: " + err + " response code: " + responseCode);
					}

					@Override
					public void onRequestSucceded(Uri fileUri) {
						Intent intent = new Intent(Intent.ACTION_VIEW);
						intent.setDataAndType(uri, message.getMediaItem().getMimeType());
						activity.startActivity(intent);
					}

					@Override
					public void onProgressUpdate(IKandyTransferProgress progress) {
						Log.d(TAG, "onProgressUpdate: " + " progress: " + progress);
					}
				});
			} else {
				// launch an application to display the media to the user
				Log.d(TAG, "downloadAndOpenKandyFile: " + "message with UUID: " + message.getUUID().toString()
						+ " with uri: " + uri.toString());

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri, message.getMediaItem().getMimeType());

				activity.startActivity(intent);
			}
		} else {

			Kandy.getServices().getChatService().downloadMedia(message, new KandyResponseProgressListener() {

				@Override
				public void onRequestFailed(int responseCode, String err) {
					Log.e(TAG, "onRequestFailed: " + err + " response code: " + responseCode);
				}

				@Override
				public void onRequestSucceded(Uri fileUri) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(fileUri, message.getMediaItem().getMimeType());
					activity.startActivity(intent);
				}

				@Override
				public void onProgressUpdate(IKandyTransferProgress progress) {
					Log.d(TAG, "onProgressUpdate: " + " progress: " + progress);
				}
			});
		}
	}

	public class Message {
		public IKandyMessage kandyMessage;
		public boolean isMsgDelivered;
		public boolean isMsgAcked;
		public Uri thumbnail;

		public Message(IKandyMessage message) {
			kandyMessage = message;
			isMsgAcked = false;
			isMsgDelivered = false;
		}
	}
}
